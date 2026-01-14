package aded.first_web_api.common.util;

import java.util.Map;

public class MapEntry {
    private static Map<String, Object> num0100() {
        return Map.of("type", "number", "minimum", 0, "maximum", 100);
    }

    private static Map<String, Object> strArray() {
        return Map.of("type", "array", "items", Map.of("type", "string"));
    }
}
