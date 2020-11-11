package client;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;
import java.util.Scanner;

public class Client {

    private static Socket socket = null;
    private static DataInputStream in;
    private static DataOutputStream out;
    private static Scanner scan;
    private static boolean authenticated;
    private static String nickname;

    public static void main(String[] args) {

        authenticated = false; //изначально пользователь не аутентифицирован
        try {
            socket = new Socket("localHost", 8191);
            scan = new Scanner(System.in);

            in = new DataInputStream(socket.getInputStream());
            out = new DataOutputStream(socket.getOutputStream());

            System.out.println("Введите логин и пароль через пробел:");


            new Thread(() -> { //для приема сообщений
                try {
                    String str;

                    while (true) { //цикл аутентификации
                        str = in.readUTF();
                        if (str.startsWith("/authok")) { //если пришло одобрение, то выходим из этого цикла: аутентиф. успешна
                            nickname = str.split(" ")[1]; //взять то, что после /authok
                            authenticated = true;
                            break;
                        } else if (str.equals("/timeOut")) { //время аутентификации вышло
                            System.out.println("\nВремя авторизации вышло");
                            break;
                        } else {
                            System.out.println("Неверный логин или пароль. Повторите попытку:");
                        }
                    }

                    if (!str.equals("/timeOut")) { //сюда заходит, только если время аутентификации не вышло
                        while (true) { //цикл работы

                            try {
                                str = in.readUTF();
                            } catch (SocketException e) {
                                throw new IOException();
                            }

                            if (str.equals("/end")) {
                                System.out.println("Отключение от сервера");
                                out.writeUTF(str); //чтобы вырубился и на клиентхендлере
                                break;
                            } if (str.startsWith("/newNickname")) { //для смены ника
                                nickname = str.split(" ")[1];
                            } else {
                                System.out.println(str);
                            }
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    try {
                        in.close();
                        out.close();
                        socket.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }).start();

            new Thread(() -> { //отправка сообщений в клиентхендлер
                try {

                    while (!authenticated) { //цикл аутентификации
                        String str = scan.nextLine();
                        if (!authenticated) {
                            out.writeUTF("/auth " + str); //прекинуть в клиентхендлер логин и пароль
                        } else {
                            out.writeUTF(str);
                            break;
                        }
                    }

                    while (true) { //цикл работы
                        String str = scan.nextLine();
                        if (!str.equals("")) {
                            out.writeUTF(str); //прекинуть в клиентхендлер
                        }
                    }
                } catch (IOException e) {
                    System.out.println("Ошибка отправки");
                } finally {
                    try {
                        in.close();
                        out.close();
                        socket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                }
            }).start();
        } catch (Exception e) {
            e.printStackTrace();

        }
    }
}