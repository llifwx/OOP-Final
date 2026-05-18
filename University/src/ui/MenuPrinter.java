package ui;

import java.util.List;

import static i18n.I18n.t;

public final class MenuPrinter {
    private static final int WIDTH = 38;

    private MenuPrinter() {
    }

    public static void print(String title, String subtitle, List<String> options) {
        System.out.println();
        printTop();
        printCentered(title);
        if (subtitle != null && !subtitle.isBlank()) {
            printCentered(subtitle);
        }
        printSeparator();
        for (String option : options) {
            printLine(option);
        }
        printBottom();
        System.out.print(t("menu.choice") + ": ");
    }

    public static void printPromptBox(String title) {
        System.out.println();
        printTop();
        printCentered(title);
        printBottom();
    }

    private static void printTop() {
        System.out.println("╔" + "═".repeat(WIDTH) + "╗");
    }

    private static void printSeparator() {
        System.out.println("╠" + "═".repeat(WIDTH) + "╣");
    }

    private static void printBottom() {
        System.out.println("╚" + "═".repeat(WIDTH) + "╝");
    }

    private static void printCentered(String text) {
        String value = trim(text);
        int left = Math.max(0, (WIDTH - value.length()) / 2);
        int right = Math.max(0, WIDTH - value.length() - left);
        System.out.println("║" + " ".repeat(left) + value + " ".repeat(right) + "║");
    }

    private static void printLine(String text) {
        String value = trim(text);
        System.out.printf("║  %-" + (WIDTH - 2) + "s║%n", value);
    }

    private static String trim(String text) {
        if (text == null) return "";
        return text.length() <= WIDTH ? text : text.substring(0, WIDTH);
    }
}
