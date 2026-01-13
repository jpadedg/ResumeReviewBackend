package aded.first_web_api.resume.service;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import aded.first_web_api.common.excepction.OpenAIException;
import aded.first_web_api.resume.dto.ResumeReviewResponse;

@Service
public class OpenAIResumeReviewer {

    private final RestClient openAi;
    private final ObjectMapper om = new ObjectMapper();

    public OpenAIResumeReviewer(RestClient openAi) {
        this.openAi = openAi;
    }

    public ResumeReviewResponse review(String resumeText) {

        Map<String, Object> schema = Map.of(
            "type", "object",
            "additionalProperties", false,
            "properties", Map.of(
                "score", Map.of("type", "number", "minimum", 0, "maximum", 10),
                "positives", Map.of("type", "array", "items", Map.of("type", "string")),
                "negatives", Map.of("type", "array", "items", Map.of("type", "string")),
                "improvements", Map.of("type", "array", "items", Map.of("type", "string")),
                "rewrittenSummary", Map.of("type", "string")
            ),
            "required", List.of("score", "positives", "negatives", "improvements", "rewrittenSummary")
        );

        Map<String, Object> format = Map.of(
            "type", "json_schema",
            "name", "resume_review",
            "schema", schema
        );

        Map<String, Object> body = Map.of(
            "model", "gpt-4.1-mini",
            "input", List.of(
                Map.of("role", "system", "content",
                    "Você é um programador Senior responsável por contratar um dev junior fullstack" +
                    "Seja objetivo, específico e orientado a contratação. " +
                    "Responda estritamente no JSON schema solicitado."),
                Map.of("role", "user", "content",
                    "Analise o currículo abaixo e retorne no schema solicitado.\n\n" + resumeText)
            ),
            "text", Map.of("format", format),
            "temperature", 0.2
        );

        // 1) Leia como String (evita o erro de JSON truncado ao mapear direto para Map)
        final String raw;
        try {
            raw = openAi.post()
                .uri("/responses")
                .body(body)
                .retrieve()
                .body(String.class);
        } catch (HttpClientErrorException e) {
            throw new OpenAIException("OpenAI retornou erro " + e.getStatusCode() + ": " + e.getResponseBodyAsString());
        } catch (Exception e) {
            throw new OpenAIException("Falha ao chamar OpenAI: " + e.getMessage());
        }

        if (raw == null || raw.isBlank()) {
            throw new OpenAIException("OpenAI retornou resposta vazia.");
        }

        // 2) Parseie o JSON da OpenAI
        final JsonNode root;
        try {
            root = om.readTree(raw);
        } catch (Exception e) {
            String head = raw.substring(0, Math.min(400, raw.length()));
            throw new OpenAIException("Resposta da OpenAI não é JSON válido. Início da resposta: " + head);
        }

        JsonNode payload = extractStructuredPayload(root);

        // 4) Converta para DTO
        try {
            return om.treeToValue(payload, ResumeReviewResponse.class);
        } catch (Exception e) {
            throw new OpenAIException("Falha ao converter JSON para ResumeReviewResponse: " + e.getMessage());
        }
    }

    /**
     * Tenta extrair o conteúdo estruturado do Responses API.
     * Prioridade:
     *  - output_text (quando vier como string JSON)
     *  - output -> content -> (type=output_json) ou (type=output_text)
     */
    private JsonNode extractStructuredPayload(JsonNode root) {

        // Caso 1: output_text no topo (às vezes vem)
        JsonNode outputText = root.path("output_text");
        if (outputText.isTextual() && !outputText.asText().isBlank()) {
            String maybeJson = outputText.asText();
            try {
                return om.readTree(maybeJson);
            } catch (Exception e) {
                // Se não for JSON, não serve para o seu DTO
                throw new OpenAIException("output_text não veio como JSON parseável.");
            }
        }

        // Caso 2: procurar em output[*].content[*]
        JsonNode output = root.path("output");
        if (output.isArray()) {
            for (JsonNode outItem : output) {
                JsonNode content = outItem.path("content");
                if (content.isArray()) {
                    for (JsonNode c : content) {
                        // Preferir output_json (quando existir)
                        if ("output_json".equals(c.path("type").asText())) {
                            JsonNode json = c.path("json");
                            if (json.isObject()) return json;
                        }

                        // Alternativa: output_text contendo JSON
                        if ("output_text".equals(c.path("type").asText())) {
                            String t = c.path("text").asText("");
                            if (!t.isBlank()) {
                                try {
                                    return om.readTree(t);
                                } catch (Exception ignored) {}
                            }
                        }
                    }
                }
            }
        }

        throw new OpenAIException("Não foi possível localizar o payload estruturado no retorno da OpenAI.");
    }
}
