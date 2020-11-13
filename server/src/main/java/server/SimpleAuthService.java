package server;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SimpleAuthService implements AuthService {

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

    @Override
    public String getNickByLoginAndPassword(String login, String password) {
        for (UserData o : users) {
            if (o.login.equals(login) && o.password.equals(password)) {
                return o.nickname;
            }
        }
        return null;
    }

    @Override
    public void changeNickname(String nickname, String newNickname) throws SQLException {

    }

}
