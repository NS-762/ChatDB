package server;

import org.sqlite.JDBC;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.*;
import java.util.Scanner;
import java.util.Vector;

public class Server {

    private AuthService authService;

    public AuthService getAuthService() { //????
        return authService;
    }

    private ServerSocket server = null;
    private Socket socket = null;
    private Scanner scan;
    private String str;
    private Vector<ClientHandler> clients;
    private SimpleAuthService simpleAuthService;




    private static Connection connection;
    private static Statement stmt;
    private static PreparedStatement psInsert;

    public SimpleAuthService getSimpleAuthService() {
        return simpleAuthService;
    }




    public Server() {

        try {
             //для аутентификации

            clients = new Vector<>();
            scan = new Scanner(System.in);
            server = new ServerSocket(8191); //создание сервера

            try {
                connectDatabase(); //подключение сервера к БД
                System.out.println("Подключились к БД");

                simpleAuthService = new SimpleAuthService(connection, stmt, psInsert);
                simpleAuthService.fillingTheDatabase(); //заполнение БД

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

    public static void connectDatabase() throws ClassNotFoundException, SQLException { //для подключения к БД
        /*Class.forName("org.sqlite.JDBC");*/

        DriverManager.registerDriver(new JDBC());
        connection = DriverManager.getConnection("jdbc:sqlite:C:\\Users\\Natas\\IdeaProjects\\ChatDB\\server\\src\\main\\java\\server\\chatUsers.db");
        stmt = connection.createStatement();
    }

    public static void disconnectDatabase() {
        try {
            stmt.close();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        try {
            connection.close();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

    public void changeNickname(String nickname, String newNickname) throws SQLException {
        simpleAuthService.changeNickname(nickname, newNickname);
    }

/*    public String getNickByLoginAndPassword(String login, String password) {
        return simpleAuthService.getNickByLoginAndPassword(login, password);
    }*/


}