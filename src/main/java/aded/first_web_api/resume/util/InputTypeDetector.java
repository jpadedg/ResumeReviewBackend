package aded.first_web_api.resume.util;

import aded.first_web_api.resume.model.InputType;
import org.springframework.web.multipart.MultipartFile;

public final class InputTypeDetector {
    private InputTypeDetector() {}

    public static InputType detect(MultipartFile file, String text) {
        if (text != null && !text.isBlank()) return InputType.TEXT;
        if (file == null) return InputType.UNKNOWN;

        String name = file.getOriginalFilename();
        name = (name == null) ? "" : name.toLowerCase();

        if (name.endsWith(".pdf")) return InputType.PDF;
        if (name.endsWith(".docx")) return InputType.DOCX;
        return InputType.FILE;
    }
}
