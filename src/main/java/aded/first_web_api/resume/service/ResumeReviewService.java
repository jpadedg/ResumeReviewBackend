package aded.first_web_api.resume.service;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import com.fasterxml.jackson.databind.ObjectMapper;

import aded.first_web_api.common.enums.ProductType;
import aded.first_web_api.common.excepction.OpenAIException;
import aded.first_web_api.common.util.ErrorUtil;
import aded.first_web_api.common.util.HashUtil;
import aded.first_web_api.common.util.TextUtil;
import aded.first_web_api.resume.dto.AtsReviewResponse;
import aded.first_web_api.resume.dto.ResumeReviewResponse;
import aded.first_web_api.resume.model.InputType;
import aded.first_web_api.resume.model.ResumeReviewModel;
import aded.first_web_api.resume.repository.ResumeReviewRepository;
import aded.first_web_api.resume.util.InputTypeDetector;
import aded.first_web_api.wallet.service.WalletService;

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

    public ResumeReviewResponse review(
        Long userId, 
        MultipartFile file, 
        String text
    ) throws IOException {

        String resumeText = (text != null && !text.isBlank())
                ? text.trim()
                : extractor.extractText(file);

        if (resumeText == null || resumeText.isBlank() || resumeText.length() < 200) {
            throw new OpenAIException("Texto do currículo muito curto para análise.");
        }

        String normalized = TextUtil.normalize(resumeText);
        String hash = HashUtil.sha256(normalized);
        InputType inputType = InputTypeDetector.detect(file, text);

        // 1) cria e salva o registro ANTES de debitar
        ResumeReviewModel rr = new ResumeReviewModel(userId, hash, inputType, REVIEW_COST);
        rr.setProductType(ProductType.GENEREAL_REVIEW);
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
            rr.setErrorMessage(ErrorUtil.safeMessage(e));
            reviewRepository.saveAndFlush(rr);

            if (debited) {
                walletService.refund(userId, REVIEW_COST, rr.getId());
            }

            throw e;
        } catch (Exception e) {
            rr.setStatus("FAILED");
            rr.setErrorMessage(ErrorUtil.safeMessage(e));
            reviewRepository.saveAndFlush(rr);

            if (debited) {
                walletService.refund(userId, REVIEW_COST, rr.getId());
            }

            if (e instanceof OpenAIException oe) throw oe;
            throw new OpenAIException("Falha ao analisar currículo: " + e.getMessage());
        }
    }

    // public AtsReviewResponse reviewWithJob(
    //     Long userId,
    //     MultipartFile file,
    //     String text,
    //     String jobDescription
    // ) throws IOException {

    //     String resumeText = (text != null && !text.isBlank())
    //             ? text.trim()
    //             : extractor.extractText(file);

    //     if (resumeText == null || resumeText.isBlank() || resumeText.length() < 200) {
    //         throw new OpenAIException("Texto do currículo muito curto para análise.");
    //     }

    //     if (jobDescription == null || jobDescription.isBlank() || jobDescription.length() < 50) {
    //         throw new OpenAIException("Descrição da vaga muito curta para análise.");
    //     }

    //     String normalizedResume = TextUtil.normalize(resumeText);
    //     String normalizedJob = TextUtil.normalize(jobDescription);

    //     String resumeHash = HashUtil.sha256(normalizedResume);
    //     String jobHash = HashUtil.sha256(normalizedJob);

    //     InputType inputType = InputTypeDetector.detect(file, text);

    //     // 1) cria e salva o registro ANTES de debitar
    //     ResumeReviewModel rr = new ResumeReviewModel(userId, resumeHash, inputType, REVIEW_COST);
    //     rr.setProductType(ProductType.ATS_REVIEW);
    //     rr.setStatus("PENDING");
    //     rr.setErrorMessage(null);

    //     // se sua entity já tiver esses campos, set aqui:
    //     rr.setJobHash(jobHash);
    //     rr.setJobDescription(jobDescription);
    //     rr.setProductType(ProductType.ATS_REVIEW);

    //     rr = reviewRepository.save(rr);

    //     boolean debited = false;

    //     try {
    //         // 2) debita (se não tiver saldo, cai no catch)
    //         walletService.debitOrThrow(userId, REVIEW_COST, rr.getId());
    //         debited = true;

    //         // 3) chama OpenAI (ATS)
    //         AtsReviewResponse resp = reviewer.reviewWithJob(resumeText, jobDescription);

    //         // 4) salva sucesso
    //         rr.setStatus("SUCCESS");
    //         rr.setScore(BigDecimal.valueOf(resp.score()));
    //         rr.setPositives(om.writeValueAsString(resp.positives()));
    //         rr.setNegatives(om.writeValueAsString(resp.negatives()));
    //         rr.setImprovements(om.writeValueAsString(resp.improvements()));
    //         rr.setRewrittenSummary(resp.rewrittenSummary());
    //         reviewRepository.save(rr);

    //         return resp;

    //     } catch (ResponseStatusException e) {
    //         rr.setStatus("FAILED");
    //         rr.setErrorMessage(ErrorUtil.safeMessage(e));
    //         reviewRepository.saveAndFlush(rr);

    //         if (debited) walletService.refund(userId, REVIEW_COST, rr.getId());
    //         throw e;

    //     } catch (Exception e) {
    //         rr.setStatus("FAILED");
    //         rr.setErrorMessage(ErrorUtil.safeMessage(e));
    //         reviewRepository.saveAndFlush(rr);

    //         if (debited) walletService.refund(userId, REVIEW_COST, rr.getId());

    //         if (e instanceof OpenAIException oe) throw oe;
    //         throw new OpenAIException("Falha ao analisar currículo com vaga: " + e.getMessage());
    //     }
    // }


}
