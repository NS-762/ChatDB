package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;
import java.util.Vector;

public class Server {

    private ServerSocket server = null;
    private Socket socket = null;
    private Scanner scan;
    private String str;
    private Vector<ClientHandler> clients;
    private AuthService authService;

    public AuthService getAuthService() {
        return authService;
    }

    public Server() {

        try {
            authService = new SimpleAuthService(); //для аутентификации
            clients = new Vector<>();
            scan = new Scanner(System.in);
            server = new ServerSocket(8191); //создание сервера

            try {
                SimpleAuthService.connect(); //подключение сервера к БД
                System.out.println("Подключились к БД");
                SimpleAuthService.fillingTheDatabase();
            } catch (Exception e) {
                e.printStackTrace();
            }




            System.out.println("Сервер запустился");

            new Thread(() -> { //поток для отправки сообщений на клиентхендлеры
                while (true) {
                    str = scan.nextLine();

                    for (ClientHandler c : clients) {
                        if (!str.equals("/end")) { //чтоб правильно считалась команда на клиентхендлере
                            c.sendMessageClient("Сервер пишет: " + str);
                        } else {
                            c.sendMessageClient( str);
                        }
                    }
                }
            }).start();

            while (true) { //для добавления клиентов
                try {
                    socket = server.accept(); //в сокет записываем подключившегося пользователя
                } catch (IOException e) {
                    e.printStackTrace();
                }
                new ClientHandler(this, socket);
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                server.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void acceptAndSendMessage(String message) { //принять сообщение, напечатать и отправить всем клиентам
        System.out.println(message);
        for (ClientHandler c : clients) {
            c.sendMessageClient(message);
        }
    }

    public void sendPrivateMessage(String nickname, String message) {
        for (ClientHandler c : clients) {
            if (c.nickname.equals(nickname)) {
                c.sendMessageClient(message);
            }
        }
    }

    public void subscribe(ClientHandler clientHandler) {
        clients.add(clientHandler);
    }

    public void unsubscribe(ClientHandler clientHandler, String nickname) {
        clients.remove(clientHandler);
        System.out.printf("Клиент %s отключился\n", nickname);
    }

    public void print(String str) {
        System.out.println(str);
    }

    public void changeNickname() {

    }

}