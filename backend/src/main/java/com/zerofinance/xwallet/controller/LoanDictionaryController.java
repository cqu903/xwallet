package com.zerofinance.xwallet.controller;

import com.zerofinance.xwallet.model.dto.LoanOccupationOptionResponse;
import com.zerofinance.xwallet.service.LoanApplicationService;
import com.zerofinance.xwallet.util.ResponseResult;
import com.zerofinance.xwallet.util.UserContext;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "贷款字典", description = "贷款申请相关字典")
@Slf4j
@RestController
@RequestMapping("/loan/dictionaries")
@RequiredArgsConstructor
public class LoanDictionaryController {

    private final LoanApplicationService loanApplicationService;

    @Operation(summary = "职业字典", description = "返回贷款申请职业选项")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "成功"),
            @ApiResponse(responseCode = "403", description = "非顾客用户"),
            @ApiResponse(responseCode = "500", description = "系统错误")
    })
    @GetMapping("/occupations")
    public ResponseResult<List<LoanOccupationOptionResponse>> getOccupations() {
        try {
            Long customerId = requireCustomer();
            return ResponseResult.success(loanApplicationService.getOccupations(customerId));
        } catch (SecurityException e) {
            log.warn("获取职业字典失败 - {}", e.getMessage());
            return ResponseResult.error(403, e.getMessage());
        } catch (IllegalArgumentException e) {
            log.warn("获取职业字典失败 - {}", e.getMessage());
            return ResponseResult.error(400, e.getMessage());
        } catch (IllegalStateException e) {
            log.warn("获取职业字典失败 - {}", e.getMessage());
            return ResponseResult.error(409, e.getMessage());
        } catch (Exception e) {
            log.error("获取职业字典失败", e);
            return ResponseResult.error(500, "获取职业字典失败");
        }
    }

    private Long requireCustomer() {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            throw new IllegalArgumentException("未登录");
        }
        String userType = UserContext.getUserType();
        if (!"CUSTOMER".equalsIgnoreCase(userType)) {
            throw new SecurityException("仅支持顾客访问");
        }
        return userId;
    }
}
