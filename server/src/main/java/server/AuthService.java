package server;

public interface AuthService {
    public String getNickByLoginAndPassword(String login, String password);
}
