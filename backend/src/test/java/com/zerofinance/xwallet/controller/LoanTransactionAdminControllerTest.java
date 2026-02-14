package com.zerofinance.xwallet.controller;

import com.zerofinance.xwallet.model.dto.LoanTransactionAdminCreateRequest;
import com.zerofinance.xwallet.model.dto.LoanTransactionAdminItemResponse;
import com.zerofinance.xwallet.model.dto.LoanTransactionAdminQueryRequest;
import com.zerofinance.xwallet.model.dto.LoanTransactionNoteUpdateRequest;
import com.zerofinance.xwallet.model.dto.LoanTransactionReversalRequest;
import com.zerofinance.xwallet.service.LoanTransactionService;
import com.zerofinance.xwallet.util.ResponseResult;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("贷款管理控制器单元测试")
class LoanTransactionAdminControllerTest {

    @Mock
    private LoanTransactionService loanTransactionService;

    @InjectMocks
    private LoanTransactionAdminController loanTransactionAdminController;

    @Test
    @DisplayName("分页查询交易成功")
    void testListSuccess() {
        LoanTransactionAdminQueryRequest request = new LoanTransactionAdminQueryRequest();
        Map<String, Object> payload = Map.of("list", List.of(), "total", 0);
        when(loanTransactionService.getAdminTransactions(request)).thenReturn(payload);

        ResponseResult<Map<String, Object>> result = loanTransactionAdminController.list(request);

        assertEquals(200, result.getCode());
        assertSame(payload, result.getData());
    }

    @Test
    @DisplayName("分页查询交易异常")
    void testListException() {
        LoanTransactionAdminQueryRequest request = new LoanTransactionAdminQueryRequest();
        when(loanTransactionService.getAdminTransactions(request)).thenThrow(new RuntimeException("db down"));

        ResponseResult<Map<String, Object>> result = loanTransactionAdminController.list(request);

        assertEquals(500, result.getCode());
        assertEquals("查询交易记录失败", result.getMessage());
    }

    @Test
    @DisplayName("创建交易成功")
    void testCreateSuccess() {
        LoanTransactionAdminCreateRequest request = new LoanTransactionAdminCreateRequest();
        LoanTransactionAdminItemResponse item = LoanTransactionAdminItemResponse.builder()
                .transactionId("TXN001")
                .build();
        when(loanTransactionService.createAdminTransaction(request)).thenReturn(item);

        ResponseResult<LoanTransactionAdminItemResponse> result = loanTransactionAdminController.create(request);

        assertEquals(200, result.getCode());
        assertSame(item, result.getData());
    }

    @Test
    @DisplayName("创建交易参数异常")
    void testCreateIllegalArgument() {
        LoanTransactionAdminCreateRequest request = new LoanTransactionAdminCreateRequest();
        when(loanTransactionService.createAdminTransaction(request))
                .thenThrow(new IllegalArgumentException("交易类型不支持"));

        ResponseResult<LoanTransactionAdminItemResponse> result = loanTransactionAdminController.create(request);

        assertEquals(400, result.getCode());
        assertEquals("交易类型不支持", result.getMessage());
    }

    @Test
    @DisplayName("创建交易系统异常")
    void testCreateException() {
        LoanTransactionAdminCreateRequest request = new LoanTransactionAdminCreateRequest();
        when(loanTransactionService.createAdminTransaction(request)).thenThrow(new RuntimeException("db down"));

        ResponseResult<LoanTransactionAdminItemResponse> result = loanTransactionAdminController.create(request);

        assertEquals(500, result.getCode());
        assertEquals("创建交易失败", result.getMessage());
    }

    @Test
    @DisplayName("更新备注成功")
    void testUpdateNoteSuccess() {
        LoanTransactionNoteUpdateRequest request = new LoanTransactionNoteUpdateRequest("ok");

        ResponseResult<Void> result = loanTransactionAdminController.updateNote("TXN001", request);

        assertEquals(200, result.getCode());
        assertEquals("备注更新成功", result.getMessage());
        verify(loanTransactionService).updateTransactionNote("TXN001", request);
    }

    @Test
    @DisplayName("更新备注参数异常")
    void testUpdateNoteIllegalArgument() {
        LoanTransactionNoteUpdateRequest request = new LoanTransactionNoteUpdateRequest("bad");
        doThrow(new IllegalArgumentException("交易不存在"))
                .when(loanTransactionService).updateTransactionNote("TXN404", request);

        ResponseResult<Void> result = loanTransactionAdminController.updateNote("TXN404", request);

        assertEquals(400, result.getCode());
        assertEquals("交易不存在", result.getMessage());
    }

    @Test
    @DisplayName("更新备注系统异常")
    void testUpdateNoteException() {
        LoanTransactionNoteUpdateRequest request = new LoanTransactionNoteUpdateRequest("bad");
        doThrow(new RuntimeException("db down"))
                .when(loanTransactionService).updateTransactionNote("TXN500", request);

        ResponseResult<Void> result = loanTransactionAdminController.updateNote("TXN500", request);

        assertEquals(500, result.getCode());
        assertEquals("更新备注失败", result.getMessage());
    }

    @Test
    @DisplayName("冲正交易成功")
    void testReverseSuccess() {
        LoanTransactionReversalRequest request = new LoanTransactionReversalRequest("manual reverse");
        LoanTransactionAdminItemResponse item = LoanTransactionAdminItemResponse.builder()
                .transactionId("TXN-RVS")
                .build();
        when(loanTransactionService.reverseTransaction("TXN001", request)).thenReturn(item);

        ResponseResult<LoanTransactionAdminItemResponse> result =
                loanTransactionAdminController.reverse("TXN001", request);

        assertEquals(200, result.getCode());
        assertSame(item, result.getData());
    }

    @Test
    @DisplayName("冲正交易参数异常")
    void testReverseIllegalArgument() {
        LoanTransactionReversalRequest request = new LoanTransactionReversalRequest("bad");
        when(loanTransactionService.reverseTransaction("TXN404", request))
                .thenThrow(new IllegalArgumentException("不可冲正"));

        ResponseResult<LoanTransactionAdminItemResponse> result =
                loanTransactionAdminController.reverse("TXN404", request);

        assertEquals(400, result.getCode());
        assertEquals("不可冲正", result.getMessage());
    }

    @Test
    @DisplayName("冲正交易系统异常")
    void testReverseException() {
        LoanTransactionReversalRequest request = new LoanTransactionReversalRequest("bad");
        when(loanTransactionService.reverseTransaction("TXN500", request))
                .thenThrow(new RuntimeException("db down"));

        ResponseResult<LoanTransactionAdminItemResponse> result =
                loanTransactionAdminController.reverse("TXN500", request);

        assertEquals(500, result.getCode());
        assertEquals("冲正失败", result.getMessage());
    }
}
