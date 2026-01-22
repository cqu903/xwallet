package com.zerofinance.xwallet.service;

import com.zerofinance.xwallet.model.dto.AnalyticsEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class RiskEvaluationService {

    public void evaluate(AnalyticsEvent event) {
        log.info("Evaluating risk for event: {} from user: {}",
            event.getEventType(), event.getUserId());

        // TODO: 实现具体的风控规则
        // 1. 检查行为序列
        // 2. 检查地理位置异常
        // 3. 检查设备指纹
        // 4. 决策：放行 / 拦截 / 人工审核

        // 示例规则：短时间多次登录失败
        if ("login_failed".equals(event.getEventType())) {
            // 触发风控规则
            log.warn("Risk alert: multiple login failures for device: {}", event.getDeviceId());
        }

        // 示例规则：异地登录
        if ("login".equals(event.getEventType()) && event.getRiskContext() != null) {
            log.info("Checking location for login: {}", event.getDeviceId());
        }

        // 示例规则：大额支付
        if ("payment_success".equals(event.getEventType())) {
            Object amount = event.getProperties().get("amount");
            if (amount != null) {
                double amountValue = ((Number) amount).doubleValue();
                if (amountValue > 10000) {
                    log.warn("Risk alert: large payment {} from user: {}", amountValue, event.getUserId());
                }
            }
        }
    }
}
