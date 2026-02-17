package com.zerofinance.xwallet.service;

import com.zerofinance.xwallet.model.dto.LoanApplicationResponse;
import com.zerofinance.xwallet.model.dto.LoanApplicationSubmitRequest;
import com.zerofinance.xwallet.model.dto.LoanApplicationAdminDetailResponse;
import com.zerofinance.xwallet.model.dto.LoanApplicationAdminQueryRequest;
import com.zerofinance.xwallet.model.dto.LoanContractExecutionRequest;
import com.zerofinance.xwallet.model.dto.LoanContractOtpSendResponse;
import com.zerofinance.xwallet.model.dto.LoanContractSignResponse;
import com.zerofinance.xwallet.model.dto.LoanOccupationOptionResponse;

import java.util.List;
import java.util.Map;

public interface LoanApplicationService {

    LoanApplicationResponse submitApplication(Long customerId, LoanApplicationSubmitRequest request);

    LoanApplicationResponse getCurrentApplication(Long customerId);

    LoanContractOtpSendResponse sendContractOtp(Long customerId, Long applicationId);

    LoanContractSignResponse signContract(Long customerId, Long applicationId, LoanContractExecutionRequest request);

    List<LoanOccupationOptionResponse> getOccupations(Long customerId);

    Map<String, Object> getAdminApplications(LoanApplicationAdminQueryRequest request);

    LoanApplicationAdminDetailResponse getAdminApplicationDetail(Long applicationId);
}
