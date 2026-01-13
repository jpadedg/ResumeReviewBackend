package aded.first_web_api.resume.service;

import java.io.IOException;
import java.io.InputStream;

import org.apache.tika.Tika;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import aded.first_web_api.common.excepction.TypeErrorException;

@Service
public class ResumeTextExtractor {
    
    private final Tika tika = new Tika();

    public String extractText(MultipartFile file) throws IOException {
        try(InputStream is = file.getInputStream()) {
            String raw = tika.parseToString(is);
            return raw == null ? "" : raw.replace("\u0000", "").trim();
        } catch (Exception e) {
            throw new TypeErrorException("Tipo de arquivo nao suportado");
        }
    }
}
