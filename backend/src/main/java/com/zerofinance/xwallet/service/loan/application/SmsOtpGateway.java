package com.zerofinance.xwallet.service.loan.application;

public interface SmsOtpGateway {
    void sendOtp(Long customerId, String otpCode);
}
