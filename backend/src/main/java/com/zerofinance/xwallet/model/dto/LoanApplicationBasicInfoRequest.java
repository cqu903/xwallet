package com.zerofinance.xwallet.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "贷款申请基础信息")
public class LoanApplicationBasicInfoRequest {

    @Schema(description = "客户姓名", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "客户姓名不能为空")
    private String fullName;

    @Schema(description = "香港身份证号码(HKID)", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "HKID不能为空")
    private String hkid;

    @Schema(description = "家庭住址", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "家庭住址不能为空")
    private String homeAddress;

    @Schema(description = "年龄", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "年龄不能为空")
    @Min(value = 18, message = "年龄不能小于18")
    @Max(value = 70, message = "年龄不能大于70")
    private Integer age;
}
