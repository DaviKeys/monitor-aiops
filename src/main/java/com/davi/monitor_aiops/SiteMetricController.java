package com.davi.monitor_aiops;

import com.davi.monitor_aiops.rabbitmq.RabbitConfig;
import tools.jackson.databind.ObjectMapper;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/metrics")

public class SiteMetricController {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @PostMapping
    /**
     * Recebe uma métrica e publica na fila de processamento.
     * @param metric payload já validado/desserializado pelo Spring MVC.
     * @return 200 quando publicado com sucesso; 500 quando falhar serialização/publicação.
     */
    public ResponseEntity<String> receiveMetric(@RequestBody SiteMetric metric) {
        try {
            System.out.println("[API] Métrica recebida. targetUrl=" + metric.getTargetUrl() + " latencyMs=" + metric.getLatencyMs() + " riskLevel=" + metric.getRiskLevel());

            String jsonMessage = objectMapper.writeValueAsString(metric);
            System.out.println("[API] Publicando na fila '" + RabbitConfig.QUEUE_NAME + "'. Payload=" + jsonMessage);

            rabbitTemplate.convertAndSend("", RabbitConfig.QUEUE_NAME, jsonMessage);
            System.out.println("[API] Publicação concluída na fila '" + RabbitConfig.QUEUE_NAME + "'.");

            return ResponseEntity.ok("Métrica processada e enviada para a fila com sucesso!");
        } catch (Exception e) {
            System.err.println("[API] Falha ao publicar métrica no RabbitMQ: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Falha ao publicar métrica no RabbitMQ: " + e.getMessage());
        }
    }
}