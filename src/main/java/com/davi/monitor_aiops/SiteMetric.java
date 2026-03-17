package com.davi.monitor_aiops;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "site_metrics")
public class SiteMetric {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String targetUrl;
    private Integer latencyMs;
    private Integer httpStatus;
    private String riskLevel;

    @Column(length = 500)
    private String aiPredictionMessage;

    private LocalDateTime timestamp;

    @PrePersist
    protected void onCreate() {
        this.timestamp = LocalDateTime.now();
    }

    // Getters e Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTargetUrl() { return targetUrl; }
    public void setTargetUrl(String targetUrl) { this.targetUrl = targetUrl; }
    public Integer getLatencyMs() { return latencyMs; }
    public void setLatencyMs(Integer latencyMs) { this.latencyMs = latencyMs; }
    public Integer getHttpStatus() { return httpStatus; }
    public void setHttpStatus(Integer httpStatus) { this.httpStatus = httpStatus; }
    public String getRiskLevel() { return riskLevel; }
    public void setRiskLevel(String riskLevel) { this.riskLevel = riskLevel; }
    public String getAiPredictionMessage() { return aiPredictionMessage; }
    public void setAiPredictionMessage(String aiPredictionMessage) { this.aiPredictionMessage = aiPredictionMessage; }
    public LocalDateTime getTimestamp() { return timestamp; }
}