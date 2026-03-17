package com.davi.monitor_aiops.rabbitmq;

import com.davi.monitor_aiops.SiteMetric;
import com.davi.monitor_aiops.SiteMetricRepository;
import com.davi.monitor_aiops.EmailService;
import tools.jackson.databind.ObjectMapper;
import java.text.Normalizer;
import java.util.Locale;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class MetricConsumer {

    @Autowired
    private SiteMetricRepository repository;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private EmailService emailService;

    @RabbitListener(queues = RabbitConfig.QUEUE_NAME)
    public void consumeMessage(String message) {
        try {
            SiteMetric metric = objectMapper.readValue(message, SiteMetric.class);
            repository.save(metric);
            System.out.println("🐇 [RABBITMQ] Métrica consumida e salva! Latência: " + metric.getLatencyMs() + "ms");

            String riskLevelRaw = metric.getRiskLevel();
            System.out.println("[DEBUG] riskLevel raw: '" + riskLevelRaw + "'");
            System.out.println("[DEBUG] riskLevel length: " + (riskLevelRaw == null ? "null" : riskLevelRaw.length()));
            if (riskLevelRaw != null) {
                StringBuilder sb = new StringBuilder();
                riskLevelRaw.codePoints().forEach(cp -> sb.append(String.format("U+%04X ", cp)));
                System.out.println("[DEBUG] riskLevel codepoints: " + sb);
            }

            if (isCriticalRisk(riskLevelRaw)) {
                System.out.println("⚠️ Risco CRÍTICO detectado! Preparando e-mail...");

                String assunto = "🚨 ALERTA CRÍTICO: Anomalia no Servidor AIOps";
                String texto = "A Inteligência Artificial detectou um comportamento anômalo em sua infraestrutura.\n\n" +
                        "Alvo monitorado: " + metric.getTargetUrl() + "\n" +
                        "Latência detectada: " + metric.getLatencyMs() + "ms\n" +
                        "Laudo da IA: " + metric.getAiPredictionMessage() + "\n\n" +
                        "Acesse o Grafana imediatamente para mais detalhes.";

                emailService.sendEmail("davi100humberto@gmail.com", assunto, texto);
            }

        } catch (Exception e) {
            System.err.println("Erro ao processar mensagem da fila: " + e.getMessage());
        }
    }

    private boolean isCriticalRisk(String riskLevelRaw) {
        if (riskLevelRaw == null) {
            return false;
        }

        String normalizedIncoming = Normalizer.normalize(riskLevelRaw.trim(), Normalizer.Form.NFC)
                .toUpperCase(Locale.ROOT);
        String normalizedExpected = Normalizer.normalize("CRÍTICO", Normalizer.Form.NFC)
                .toUpperCase(Locale.ROOT);

        return normalizedExpected.equals(normalizedIncoming);
    }
}