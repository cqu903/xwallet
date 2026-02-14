package com.zerofinance.xwallet.service.loan.application;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class MockSmsOtpGateway implements SmsOtpGateway {

    @Override
    public void sendOtp(Long customerId, String otpCode) {
        log.info("[MOCK OTP] customerId={}, otpCode={}", customerId, otpCode);
    }
}
