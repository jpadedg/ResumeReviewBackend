package aded.first_web_api.service;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import com.fasterxml.jackson.databind.ObjectMapper;

import aded.first_web_api.dto.ResumeReviewResponse;
import aded.first_web_api.excepction.OpenAIException;
import aded.first_web_api.model.ResumeReview;
import aded.first_web_api.repository.ResumeReviewRepository;

@Service
public class ResumeReviewService {

    public static final int REVIEW_COST = 25;

    private final ResumeTextExtractor extractor;
    private final OpenAIResumeReviewer reviewer;
    private final WalletService walletService;
    private final ResumeReviewRepository reviewRepository;

    private final ObjectMapper om = new ObjectMapper();

    public ResumeReviewService(
            ResumeTextExtractor extractor,
            OpenAIResumeReviewer reviewer,
            WalletService walletService,
            ResumeReviewRepository reviewRepository
    ) {
        this.extractor = extractor;
        this.reviewer = reviewer;
        this.walletService = walletService;
        this.reviewRepository = reviewRepository;
    }

    public ResumeReviewResponse review(Long userId, MultipartFile file, String text) throws IOException {

        String resumeText = (text != null && !text.isBlank())
                ? text.trim()
                : extractor.extractText(file);

        if (resumeText == null || resumeText.isBlank() || resumeText.length() < 200) {
            throw new OpenAIException("Texto do currículo muito curto para análise.");
        }

        String normalized = normalize(resumeText);
        String hash = sha256(normalized);
        String inputType = detectInputType(file, text);

        // 1) cria e salva o registro ANTES de debitar
        ResumeReview rr = new ResumeReview(userId, hash, inputType, REVIEW_COST);
        rr.setStatus("PENDING");
        rr.setErrorMessage(null);
        rr = reviewRepository.save(rr);

        boolean debited = false;

        try {
            // 2) debita (se não tiver saldo, cai no catch)
            walletService.debitOrThrow(userId, REVIEW_COST, rr.getId());
            debited = true;

            // 3) chama OpenAI
            ResumeReviewResponse resp = reviewer.review(resumeText);

            // 4) salva sucesso
            rr.setStatus("SUCCESS");
            rr.setScore(BigDecimal.valueOf(resp.score()));
            rr.setPositives(om.writeValueAsString(resp.positives()));
            rr.setNegatives(om.writeValueAsString(resp.negatives()));
            rr.setImprovements(om.writeValueAsString(resp.improvements()));
            rr.setRewrittenSummary(resp.rewrittenSummary());
            reviewRepository.save(rr);

            return resp;

        } catch (ResponseStatusException e) {
            rr.setStatus("FAILED");
            rr.setErrorMessage(safeError(e));
            reviewRepository.saveAndFlush(rr);

            if (debited) {
                walletService.refund(userId, REVIEW_COST, rr.getId());
            }

            throw e;
        } catch (Exception e) {
            rr.setStatus("FAILED");
            rr.setErrorMessage(safeError(e));
            reviewRepository.saveAndFlush(rr);

            if (debited) {
                walletService.refund(userId, REVIEW_COST, rr.getId());
            }

            if (e instanceof OpenAIException oe) throw oe;
            throw new OpenAIException("Falha ao analisar currículo: " + e.getMessage());
        }
    }

    private String detectInputType(MultipartFile file, String text) {
        if (text != null && !text.isBlank()) return "TEXT";
        if (file == null) return "UNKNOWN";
        String name = file.getOriginalFilename() == null ? "" : file.getOriginalFilename().toLowerCase();
        if (name.endsWith(".pdf")) return "PDF";
        if (name.endsWith(".docx")) return "DOCX";
        return "FILE";
    }

    private String normalize(String s) {
        return s.trim().replaceAll("\\s+", " ");
    }

    private String sha256(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] dig = md.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(dig.length * 2);
            for (byte b : dig) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (Exception e) {
            throw new RuntimeException("Erro ao calcular hash SHA-256", e);
        }
    }

    private String safeError(Exception e) {
        String msg = e.getMessage() == null ? "Erro desconhecido" : e.getMessage();
        msg = msg.replaceAll("[\\r\\n\\t]+", " ").trim();
        return msg.length() > 500 ? msg.substring(0, 500) : msg;
    }
}
