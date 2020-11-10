package server;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SimpleAuthService implements AuthService {

    private Connection connection;
    private Statement stmt;
    private PreparedStatement psInsert;

    public SimpleAuthService(Connection connection, Statement stmt, PreparedStatement psInsert) {
        this.connection = connection;
        this.stmt = stmt;
        this.psInsert = psInsert;
    }

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

    public SimpleAuthService() { //массив логинов, паролей, никнеймов
        users = new ArrayList<>();
        for (int i = 1; i <= 10; i++) {
            users.add(new UserData("" + i, "" + i, "Nickname" + i));
        }
    }

    public void fillingTheDatabase() throws SQLException {
        try {
            connection.setAutoCommit(false);
            psInsert = connection.prepareStatement("INSERT INTO users (nickname, login, password) VALUES (?,?,?)");
            for (int i = 1; i <= 10; i++) {

                psInsert.setString(1, "Nickname" + i);
                psInsert.setString(2, "" + i);
                psInsert.setString(3, "" + i);
                psInsert.executeUpdate();
            }
            connection.setAutoCommit(true);
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

        /*public void fillingTheDatabase() throws SQLException {
        try {
            for (int i = 1; i <= 10; i++) {
                stmt.executeUpdate(String.format("INSERT INTO users (nickname, login, password) VALUES ('%s', '%s', '%s')",
                        "Nickname" + i, i, i));
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }*/

    public void changeNickname(String nickname, String newNickname) throws SQLException {
        stmt.executeUpdate(String.format("UPDATE users SET nickname = '%s' WHERE nickname = '%s'", newNickname, nickname));
    }

    @Override
    public String getNickByLoginAndPassword(String login, String password) {
        String nick = null;
        ResultSet rs; //ищет никнейм
        System.out.println("Получены логин и пароль: " + login + "" + password);
        try {
            rs = stmt.executeQuery(String.format("SELECT nickname, login FROM users " +
                    "WHERE login = '%s' AND password = '%s'", login, password));

            while (rs.next()) {
                nick = rs.getString("nickname");
            }

        } catch (SQLException throwables) {
            throwables.printStackTrace();
        } finally {
            return nick;
        }
    }


/*    @Override
    public String getNickByLoginAndPassword(String login, String password) {
        for (UserData o : users) {
            if (o.login.equals(login) && o.password.equals(password)) {
                return o.nickname;
            }
        }
        return null;
    }*/

}
