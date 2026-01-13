package aded.first_web_api.common.util;


public final class TextUtil {
    private TextUtil() {}

    public static String normalize(String s) {
        return s.trim().replaceAll("\\s+", " ");
    }
}
