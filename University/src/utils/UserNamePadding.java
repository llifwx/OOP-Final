package utils;

public class UserNamePadding {
    public String padRight(String text, int length) {
        if (text.length() >= length) return text.substring(0, length);
        return text + " ".repeat(length - text.length());
    }
}
