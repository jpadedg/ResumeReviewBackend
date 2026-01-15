package aded.first_web_api.resume.service;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import aded.first_web_api.common.excepction.BusinessException;
import aded.first_web_api.common.excepction.OpenAIException;
import aded.first_web_api.common.util.ErrorUtil;
import aded.first_web_api.common.util.HashUtil;
import aded.first_web_api.common.util.TextUtil;
import aded.first_web_api.resume.dto.AtsReviewResponse;
import aded.first_web_api.resume.model.AtsResumeReviewModel;
import aded.first_web_api.resume.model.InputType;
import aded.first_web_api.resume.repository.AtsReviewRepository;
import aded.first_web_api.resume.util.InputTypeDetector;
import aded.first_web_api.wallet.service.WalletService;

@Service
public class AtsReviewService {

    // Defina um custo próprio (pode ser igual ao da review normal)
    public static final int ATS_COST = 35;

    // Use versionamento para você poder mudar prompt/model sem quebrar cache/dedupe
    private static final String ATS_PROMPT_VERSION = "ats_v1";
    private static final String ATS_MODEL = "gpt-4.1-mini";

    private final ResumeTextExtractor extractor;
    private final OpenAIResumeReviewer reviewer;
    private final WalletService walletService;
    private final AtsReviewRepository atsRepo;

    private final ObjectMapper om = new ObjectMapper();

    public AtsReviewService(
            ResumeTextExtractor extractor,
            OpenAIResumeReviewer reviewer,
            WalletService walletService,
            AtsReviewRepository atsRepo) {
        this.extractor = extractor;
        this.reviewer = reviewer;
        this.walletService = walletService;
        this.atsRepo = atsRepo;
    }

    public AtsReviewResponse review(Long userId, MultipartFile file, String text, String jobDescription)
            throws IOException {

        if (userId == null) {
            throw new BusinessException("userId é obrigatório.");
        }

        if ((file == null || file.isEmpty()) && (text == null || text.isBlank())) {
            throw new BusinessException("Envie 'file' (PDF/DOCX) ou 'text'.");
        }

        if (jobDescription == null || jobDescription.isBlank()) {
            throw new BusinessException("jobDescription é obrigatório.");
        }

        // 1) Extrair texto do currículo
        String resumeText = (text != null && !text.isBlank())
                ? text.trim()
                : extractor.extractText(file);

        if (resumeText == null || resumeText.isBlank() || resumeText.length() < 200) {
            throw new OpenAIException("Texto do currículo muito curto para análise.");
        }

        // 2) Normalizar + hashes
        String normalizedResume = TextUtil.normalize(resumeText);
        String normalizedJob = TextUtil.normalize(jobDescription);

        String resumeHash = HashUtil.sha256(normalizedResume);
        String jobHash = HashUtil.sha256(normalizedJob);

        // 3) Detectar inputType
        InputType inputType = InputTypeDetector.detect(file, text);

        // 4) combinedHash para dedupe (inclui versão/model)
        String combinedHash = HashUtil
                .sha256(resumeHash + ":" + jobHash + ":ATS:" + ATS_PROMPT_VERSION + ":" + ATS_MODEL);

        // (Opcional) cache: se já existir SUCCESS para esse par + versão, você pode
        // retornar sem gastar
        // Comente se não quiser cache agora.
        var cached = atsRepo.findByCombinedHash(combinedHash);
        if (cached.isPresent() && "SUCCESS".equalsIgnoreCase(cached.get().getStatus())) {
            AtsResumeReviewModel c = cached.get();

            // Reconstruir resposta mínima a partir do que está no banco
            // (Se quiser reconstruir tudo, você precisa também persistir
            // positives/negatives/etc)
            return new AtsReviewResponse(
                    c.getOverallScore(),
                    c.getKeywordMatchRate(),
                    c.getHardSkillCoverage(),
                    c.getSeniorityMatch(),
                    c.getTitleAlignment(),
                    c.getAtsReadability(),
                    c.getSectionCompleteness(),
                    toMap(c.getAtsMetrics()),
                    null, null, null,
                    c.getRewrittenSummary(),
                    toStringList(c.getRewrittenBullets())
            );
        }

        // 5) Criar registro PENDING antes de debitar
        AtsResumeReviewModel ar = new AtsResumeReviewModel();
        ar.setId(UUID.randomUUID());
        ar.setUserId(userId);
        ar.setResumeHash(resumeHash);
        ar.setJobHash(jobHash);
        ar.setCombinedHash(combinedHash);
        ar.setJobDescription(jobDescription);
        ar.setInputType(inputType);
        ar.setCostCoins(ATS_COST);
        ar.setStatus("PENDING");
        ar.setCreatedAt(OffsetDateTime.now());
        ar.setErrorMessage(null);

        ar = atsRepo.save(ar);

        boolean debited = false;

        try {
            // 6) Debitar
            walletService.debitOrThrow(userId, ATS_COST, ar.getId());
            debited = true;

            // 7) Chamar IA (use o método novo que retorna AtsReviewResponse)
            // IMPORTANTE: o reviewer.reviewWithJob precisa retornar AtsReviewResponse
            AtsReviewResponse resp = reviewer.reviewWithJob(resumeText, jobDescription);

            // 8) Persistir sucesso
            ar.setStatus("SUCCESS");
            ar.setFinishedAt(OffsetDateTime.now());

            ar.setOverallScore(resp.overallScore());
            ar.setKeywordMatchRate(resp.keywordMatchRate());
            ar.setHardSkillCoverage(resp.hardSkillCoverage());
            ar.setSeniorityMatch(resp.seniorityMatch());
            ar.setTitleAlignment(resp.titleAlignment());
            ar.setAtsReadability(resp.atsReadability());
            ar.setSectionCompleteness(resp.sectionCompleteness());
            ar.setAtsReadability(resp.atsReadability());

            // ats_metrics JSONB
            ar.setAtsMetrics(om.valueToTree(resp.atsMetrics()));

            // Outputs (se você adicionou esses campos no model)
            if (resp.positives() != null)
                ar.setPositives(om.writeValueAsString(resp.positives()));
            if (resp.negatives() != null)
                ar.setNegatives(om.writeValueAsString(resp.negatives()));
            if (resp.improvements() != null)
                ar.setImprovements(om.writeValueAsString(resp.improvements()));

            ar.setRewrittenSummary(resp.rewrittenSummary());
            if (resp.rewrittenBullets() != null)
                ar.setRewrittenBullets(om.writeValueAsString(resp.rewrittenBullets()));

            atsRepo.save(ar);

            return resp;

        } catch (Exception e) {
            ar.setStatus("FAILED");
            ar.setFinishedAt(OffsetDateTime.now());
            ar.setErrorMessage(ErrorUtil.safeMessage(e));
            atsRepo.saveAndFlush(ar);

            if (debited) {
                walletService.refund(userId, ATS_COST, ar.getId());
            }

            if (e instanceof OpenAIException oe)
                throw oe;
            throw new OpenAIException("Falha ao analisar ATS: " + e.getMessage());
        }
    }

    private Map<String, Object> toMap(Object raw) {
        if (raw == null)
            return null;

        try {
            if (raw instanceof Map<?, ?> m) {
                @SuppressWarnings("unchecked")
                Map<String, Object> casted = (Map<String, Object>) m;
                return casted;
            }

            if (raw instanceof JsonNode node) {
                return om.convertValue(node, new TypeReference<Map<String, Object>>() {
                });
            }

            if (raw instanceof String s && !s.isBlank()) {
                return om.readValue(s, new TypeReference<Map<String, Object>>() {
                });
            }

            throw new IllegalArgumentException("Tipo inesperado para atsMetrics: " + raw.getClass());

        } catch (Exception e) {
            throw new RuntimeException("Erro ao converter atsMetrics para Map", e);
        }
    }

    private List<String> toStringList(String json) {
        if (json == null || json.isBlank())
            return null;

        try {
            return om.readValue(json, new TypeReference<List<String>>() {
            });
        } catch (Exception e) {
            throw new RuntimeException("Erro ao converter lista JSON", e);
        }
    }
}