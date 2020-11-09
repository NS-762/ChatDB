package server;

import com.sun.javafx.binding.StringFormatter;
import org.sqlite.JDBC;

import java.sql.*;
import java.util.List;

public class SimpleAuthService implements AuthService {

    private static Connection connection;
    private static Statement stmt;
    private static PreparedStatement psInsert;


    private class UserData {
        String login;
        String password;
        String nickname;

        public UserData(String login, String password, String nickname) {
            this.login = login;
            this.password = password;
            this.nickname = nickname;
        }
    }

    private List<UserData> users;

    /*public SimpleAuthService() { //массив логинов, паролей, никнеймов
        users = new ArrayList<>();
        for (int i = 1; i <= 10; i++) {
            users.add(new UserData("" + i, "" + i, "Nickname" + i));
        }
    }*/

    public static void fillingTheDatabase() throws SQLException {
        try {
            psInsert = connection.prepareStatement("INSERT INTO users (nickname, login, password) VALUES (?,?,?)");
            for (int i = 1; i <= 10; i++) {

                psInsert.setString(1, "Nickname" + i);
                psInsert.setString(2, "" + i);
                psInsert.setString(3, "" + i);
                psInsert.addBatch();
            }
            connection.setAutoCommit(true);

        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }


    public static void changeNickname (String nickname, String newNickname) throws SQLException {
        stmt.executeUpdate(String.format("UPDATE users SET nickname = '%s' WHERE nickname = '%s'", newNickname, nickname));
    }


    @Override
    public String getNickByLoginAndPassword(String login, String password) {
        for (UserData o : users) {
            if (o.login.equals(login) && o.password.equals(password)) {
                return o.nickname;
            }
        }
        return null;
    }


    public static void connect() throws ClassNotFoundException, SQLException { //для подключения к БД
        /*Class.forName("org.sqlite.JDBC");*/

        DriverManager.registerDriver(new JDBC());
        connection = DriverManager.getConnection("jdbc:sqlite:C:\\Users\\Natas\\IdeaProjects\\ChatDB\\server\\src\\main\\java\\server\\chatUsers.db");
        stmt = connection.createStatement();
    }

    public static void disconnect() {
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


}
