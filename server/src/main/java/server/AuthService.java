package server;

import java.sql.SQLException;

public interface AuthService {
    String getNickByLoginAndPassword(String login, String password);
    void changeNickname(String nickname, String newNickname) throws SQLException;
}
