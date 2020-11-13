package server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.sql.SQLException;
import java.util.Scanner;

public class ClientHandler {

    private Server server = null;
    private Socket socket = null;
    private DataInputStream in;
    private DataOutputStream out;
    private Scanner scan;
    private String login;
    public String nickname;
    private boolean subscribe = false;


    public ClientHandler(Server server, Socket socket) {
        this.server = server;
        this.socket = socket;

        scan = new Scanner(System.in);

        try {
            in = new DataInputStream(socket.getInputStream());
            out = new DataOutputStream(socket.getOutputStream());


            socket.setSoTimeout(120000);

            new Thread(() -> {
                try {
                    while (true) { //цикл аутентификации
                        String str = in.readUTF();
                        if (str.startsWith("/auth")) {
                            String[] token = str.split(" ");
                            if (token.length == 3) {

                                String nick = server.getAuthService().getNickByLoginAndPassword(token[1], token[2]);

                                if (nick != null) {
                                    sendMessageClient("/authok " + nick);
                                    nickname = nick;
                                    login = token[1];
                                    server.subscribe(this); //добавить клиента в список
                                    subscribe = true;
                                    server.acceptAndSendMessage(nickname + " подключился к чату"); // перекинуть сообщение на сервер
                                    break;
                                } else {
                                    sendMessageClient("/error");
                                }
                            } else {
                                sendMessageClient("/error");
                            }
                        }
                    }

                    socket.setSoTimeout(0);

                    while (!socket.isClosed()) {
                        String str = in.readUTF();
                        if (str.equals("/end")) {
                            sendMessageClient(str); //отправить клиенту сообщение о закрытии
                            break;
                        }

                        if (str.startsWith("/w")) { //для личных сообщений
                            String[] privateMessage = str.split(" ", 3); //делим на 3 части
                            server.sendPrivateMessage(privateMessage[1], nickname + "(лс): " + privateMessage[2]);
                        } else if (str.startsWith("/changeNickname")) { //запрос на смену ника

                            String[] nicknameChangeRequest = str.split(" ");
                            if (nicknameChangeRequest.length == 2) {
                                try {
                                    server.getAuthService().changeNickname(nickname, nicknameChangeRequest[1]);
                                    sendMessageClient("Вы удачно сменили никнейм, ваш новый никнейм: " + nicknameChangeRequest[1]);
                                    sendMessageClient("/newNickname " + nicknameChangeRequest[1]);
                                    nickname = nicknameChangeRequest[1];
                                } catch (SQLException throwables) {
                                    sendMessageClient("Невозможно сменить никнейм");
                                }
                            } else {
                                sendMessageClient("Ошибка ввода");
                            }

                        } else { //для отправки всем пользователям
                            server.acceptAndSendMessage(nickname + ": " + str); // перекинуть сообщение на сервер
                        }

                    }
                } catch (SocketTimeoutException e) {
                    sendMessageClient("/timeOut"); //отправить сообщение, что время ожидания вышло
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    try {
                        if (subscribe) { //если клиент был подписан на сервер, то удалить из подписок
                            server.unsubscribe(this, nickname);
                        }
                        in.close();
                        out.close();
                        socket.close();

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }).start();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendMessageClient(String str) { //отправка сообщения клиенту от сервера
        try {
            out.writeUTF(str);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}