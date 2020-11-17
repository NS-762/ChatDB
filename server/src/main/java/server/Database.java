package server;

import org.sqlite.JDBC;

import java.sql.*;

public class Database {

    private static Connection connection;
    private static Statement stmt;
    private static PreparedStatement psInsert;

    public void connectDatabase() throws SQLException { //для подключения к БД
        DriverManager.registerDriver(new JDBC());
        connection = DriverManager.getConnection("jdbc:sqlite:server\\src\\main\\java\\server\\chatUsers.db");
        stmt = connection.createStatement();
    }

    public void fillingTheDatabase() {
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

    public void disconnectDatabase() {
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

    public Statement getStmt() {
        return stmt;
    }
}
