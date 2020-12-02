package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.Vector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {

    private ServerSocket server = null;
    private Socket socket = null;
    private Scanner scan;
    private String str;
    private Vector<ClientHandler> clients;
    private Database database;

    private ExecutorService executorService;
    private ArrayList<String> lastMessages;

    private AuthService authService;

    public AuthService getAuthService() {
        return authService;
    }

    public Server(Database database) {

        try {
            this.database = database;
            clients = new Vector<>();
            scan = new Scanner(System.in);
            server = new ServerSocket(8191); //создание сервера
            lastMessages = new ArrayList<>();

            try {
                database.connectDatabase(); //подключение сервера к БД
                System.out.println("Подключились к БД");
                database.fillingTheDatabase(); //заполнение БД

                authService = new DBAuthService(database);
            } catch (Exception e) {
                e.printStackTrace();
            }
            System.out.println("Сервер запустился");


            executorService = Executors.newSingleThreadExecutor();
            executorService.execute(() -> {
                while (true) {
                    str = scan.nextLine();

                    if (str.equals("/info")) {
                        System.out.println(lastMessages);
                        continue;
                    } else if (!str.equals("/end")) {
                        if (lastMessages.size() == 100) {
                            lastMessages.remove(0);
                        }
                        lastMessages.add("Сервер пишет: " + str); //будет добавлять сообщение в историю последних ста сообщений
                    }

                    for (ClientHandler c : clients) {
                        if (!str.equals("/end")) { //чтоб правильно считалась команда на клиентхендлере
                            c.sendMessageClient("Сервер пишет: " + str);

                        } else {
                            c.sendMessageClient(str);
                        }
                    }
                }
            });

            /*new Thread(() -> { //поток для отправки сообщений на клиентхендлеры
                while (true) {
                    str = scan.nextLine();

                    if (str.equals("/info")) {
                        System.out.println(lastMessages);
                        continue;
                    } else if (!str.equals("/end")) {
                        if (lastMessages.size() == 100) {
                            lastMessages.remove(0);
                        }
                        lastMessages.add("Сервер пишет: " + str); //будет добавлять сообщение в историю последних ста сообщений
                    }

                    for (ClientHandler c : clients) {
                        if (!str.equals("/end")) { //чтоб правильно считалась команда на клиентхендлере
                            c.sendMessageClient("Сервер пишет: " + str);

                        } else {
                            c.sendMessageClient(str);
                        }
                    }
                }
            }).start();*/

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
                database.disconnectDatabase();
                executorService.shutdown();
                server.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void acceptAndSendMessage(String message) { //принять сообщение, напечатать и отправить всем клиентам
        System.out.println(message); //кроме того, который его отправил

        if (lastMessages.size() == 100) {
            lastMessages.remove(0);
        }
        lastMessages.add(message); //будет добавлять сообщение в историю последних ста сообщений

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

    public ArrayList<String> getLastMessages() {
        return lastMessages;
    }


}