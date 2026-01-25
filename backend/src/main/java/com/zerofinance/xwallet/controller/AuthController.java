package com.zerofinance.xwallet.controller;

import com.zerofinance.xwallet.model.dto.LoginRequest;
import com.zerofinance.xwallet.model.dto.LoginResponse;
import com.zerofinance.xwallet.model.dto.RegisterRequest;
import com.zerofinance.xwallet.model.dto.SendCodeRequest;
import com.zerofinance.xwallet.service.AuthService;
import com.zerofinance.xwallet.service.VerificationCodeService;
import com.zerofinance.xwallet.util.ResponseResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 认证控制器
 * 处理登录、登出、注册等认证相关请求
 */
@Tag(name = "认证", description = "登录、登出、注册、验证码、Token 校验；登录/注册/发送验证码无需 Token")
@Slf4j
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final VerificationCodeService verificationCodeService;

    @Operation(summary = "用户登录", description = "系统用户：userType=SYSTEM，account=工号，password；顾客：userType=CUSTOMER，account=邮箱，password。返回 JWT 与用户信息。")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "成功，data 含 token、userInfo"),
            @ApiResponse(responseCode = "400", description = "参数错误或账号/密码错误"),
            @ApiResponse(responseCode = "500", description = "系统错误")
    })
    @SecurityRequirements()
    @PostMapping("/login")
    public ResponseResult<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        log.info("收到登录请求 - 用户类型: {}", request.getUserType());
        try {
            LoginResponse response = authService.login(request);
            return ResponseResult.success(response);
        } catch (IllegalArgumentException e) {
            log.warn("登录失败: {}", e.getMessage());
            return ResponseResult.error(400, e.getMessage());
        } catch (Exception e) {
            log.error("登录异常", e);
            return ResponseResult.error(500, "系统错误，请稍后重试");
        }
    }

    @Operation(summary = "用户登出", description = "将当前 JWT 加入黑名单。请求头可带 Authorization: Bearer <token>，不带也可成功（用于前端清除本地 token）。")
    @ApiResponses({ @ApiResponse(responseCode = "200", description = "成功"), @ApiResponse(responseCode = "500", description = "系统错误") })
    @PostMapping("/logout")
    public ResponseResult<Void> logout(@RequestHeader(value = "Authorization", required = false) String token) {
        log.info("收到登出请求");
        try {
            if (token != null && token.startsWith("Bearer ")) {
                token = token.substring(7);
                authService.logout(token);
            }
            return ResponseResult.success(null);
        } catch (Exception e) {
            log.error("登出异常", e);
            return ResponseResult.error(500, "系统错误，请稍后重试");
        }
    }

    @Operation(summary = "验证 Token", description = "校验请求头 Authorization 中的 JWT 是否有效、未过期、未登出。无 token 或格式错误返回 false。")
    @ApiResponses({ @ApiResponse(responseCode = "200", description = "成功，data 为 true/false") })
    @GetMapping("/validate")
    public ResponseResult<Boolean> validateToken(
            @RequestHeader(value = "Authorization", required = false) String token) {
        try {
            if (token == null || !token.startsWith("Bearer ")) {
                return ResponseResult.success(false);
            }
            token = token.substring(7);
            boolean isValid = authService.validateToken(token);
            return ResponseResult.success(isValid);
        } catch (Exception e) {
            log.error("验证token异常", e);
            return ResponseResult.success(false);
        }
    }

    @Operation(summary = "发送验证码", description = "向指定邮箱发送 6 位数字验证码，用于注册。同一邮箱有频率限制。")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "发送成功"),
            @ApiResponse(responseCode = "400", description = "邮箱格式错误或发送过于频繁"),
            @ApiResponse(responseCode = "500", description = "系统或邮件服务错误")
    })
    @SecurityRequirements()
    @PostMapping("/send-code")
    public ResponseResult<Void> sendVerificationCode(@Valid @RequestBody SendCodeRequest request) {
        log.info("收到发送验证码请求 - 邮箱: {}", request.getEmail());
        try {
            verificationCodeService.sendVerificationCode(request.getEmail(), "REGISTER");
            return ResponseResult.success(null);
        } catch (IllegalArgumentException e) {
            log.warn("发送验证码失败: {}", e.getMessage());
            return ResponseResult.error(400, e.getMessage());
        } catch (Exception e) {
            log.error("发送验证码异常", e);
            return ResponseResult.error(500, "系统错误，请稍后重试");
        }
    }

    @Operation(summary = "用户注册", description = "顾客注册。需先调用「发送验证码」获取验证码。成功后自动登录并返回 JWT。")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "注册并登录成功"),
            @ApiResponse(responseCode = "400", description = "参数错误、验证码错误或邮箱已注册"),
            @ApiResponse(responseCode = "500", description = "系统错误")
    })
    @SecurityRequirements()
    @PostMapping("/register")
    public ResponseResult<LoginResponse> register(@Valid @RequestBody RegisterRequest request) {
        log.info("收到注册请求 - 邮箱: {}", request.getEmail());
        try {
            LoginResponse response = authService.register(request);
            return ResponseResult.success(response);
        } catch (IllegalArgumentException e) {
            log.warn("注册失败: {}", e.getMessage());
            return ResponseResult.error(400, e.getMessage());
        } catch (Exception e) {
            log.error("注册异常", e);
            return ResponseResult.error(500, "系统错误，请稍后重试");
        }
    }
}
