package aded.first_web_api.common.util;

public final class ErrorUtil {
    private ErrorUtil() {}

    public static String safeMessage(Throwable t) {
        if (t == null) return "Erro desconhecido";

        String msg = t.getMessage();
        if (msg == null || msg.isBlank()) return "Erro desconhecido";

        msg = msg.replaceAll("[\\r\\n\\t]+", " ").trim();
        return msg.length() > 500 ? msg.substring(0, 500) : msg;
    }
}
