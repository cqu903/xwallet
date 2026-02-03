package com.zerofinance.xwallet.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.util.Map;

@Data
public class AnalyticsEvent {
    @JsonProperty("eventId")
    private String eventId;

    @JsonProperty("deviceId")
    private String deviceId;

    @JsonProperty("userId")
    private String userId;

    @JsonProperty("eventType")
    private String eventType;

    @JsonProperty("timestamp")
    private Long timestamp;

    @JsonProperty("environment")
    private String environment;

    @JsonProperty("context")
    private EventContext context;

    @JsonProperty("properties")
    private Map<String, Object> properties;

    @JsonProperty("riskContext")
    private RiskContext riskContext;

    @Data
    public static class EventContext {
        @JsonProperty("appVersion")
        private String appVersion;

        @JsonProperty("os")
        private String os;

        @JsonProperty("osVersion")
        private String osVersion;

        @JsonProperty("deviceModel")
        private String deviceModel;

        @JsonProperty("networkType")
        private String networkType;

        @JsonProperty("carrier")
        private String carrier;

        @JsonProperty("screenSize")
        private String screenSize;

        @JsonProperty("timezone")
        private String timezone;

        @JsonProperty("language")
        private String language;
    }

    @Data
    public static class RiskContext {
        @JsonProperty("sessionId")
        private String sessionId;

        @JsonProperty("lastEventType")
        private String lastEventType;

        @JsonProperty("timeSinceLastEvent")
        private Long timeSinceLastEvent;
    }
}
