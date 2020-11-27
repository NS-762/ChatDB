package client;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class History {
    private FileOutputStream outFile;
    private File fileMessageHistory;
    private String login;

    public History(String login) {
        this.login = login;
        fileMessageHistory = new File(String.format("client\\src\\main\\java\\client\\history_%s.txt", login));
        try {
            fileMessageHistory.createNewFile(); //если уже есть такой файл, он не создасться
            outFile = new FileOutputStream(fileMessageHistory, true);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void stop() { //отключение потока
        try {
            outFile.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void writingToFile(String str) {
        byte[] byteStr = str.getBytes();
        try {
            outFile.write(byteStr);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getLastMyMessages() { //получить 100 последних сообщений
        if (!fileMessageHistory.exists()) {
            return "";
        } else {
            StringBuilder sb = new StringBuilder();
            try {
                List<String> listLastMyMessages = Files.readAllLines(
                        Paths.get(String.format("client\\src\\main\\java\\client\\history_%s.txt", login)));
                int startPosition = 0;
                if (listLastMyMessages.size() > 100) {
                    startPosition = listLastMyMessages.size() - 100;
                }
                for (int i = startPosition; i < listLastMyMessages.size(); i++) {
                    sb.append(listLastMyMessages.get(i)).append(System.lineSeparator());
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return sb.toString();
        }
    }
}
