package info;

public class Info {

    private String botToken;
    private String chatID;

    public Info() {
        this.botToken = "*";
        this.chatID = "*";
    }

    public String getBotToken() {
        return botToken;
    }

    public String getChatID() {
        return chatID;
    }
}
