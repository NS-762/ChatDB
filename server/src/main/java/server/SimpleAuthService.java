package server;

import java.sql.*;

public class SimpleAuthService implements AuthService {

    private Database database;

    public SimpleAuthService(Database database) {
        this.database = database;
    }

/*    private class UserData {
        String login;
        String password;
        String nickname;

        public UserData(String login, String password, String nickname) {
            this.login = login;
            this.password = password;
            this.nickname = nickname;
        }
    }*/

    public void changeNickname(String nickname, String newNickname) throws SQLException {
        database.getStmt().executeUpdate(String.format("UPDATE users SET nickname = '%s' WHERE nickname = '%s'",
                newNickname, nickname));
    }

    @Override
    public String getNickByLoginAndPassword(String login, String password) {
        String nick = null;
        ResultSet rs; //ищет никнейм
        try {
            rs = database.getStmt().executeQuery(String.format("SELECT nickname, login FROM users " +
                    "WHERE login = '%s' AND password = '%s'", login, password));
            while (rs.next()) {
                nick = rs.getString("nickname");
            }

        } catch (SQLException throwables) {
            throwables.printStackTrace();
        } finally {
            System.out.println(nick);
            return nick;
        }
    }

}
