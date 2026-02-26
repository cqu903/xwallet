package com.zerofinance.xwallet.service;

import jakarta.servlet.http.HttpServletResponse;
import com.zerofinance.xwallet.model.dto.LoanTransactionAdminQueryRequest;

import java.io.IOException;

/**
 * Excel 导出服务接口
 */
public interface ExcelExportService {

    /**
     * 导出交易记录到 Excel
     *
     * @param response HTTP 响应对象
     * @param request 查询条件
     * @throws IOException 导出失败时抛出
     */
    void exportTransactions(HttpServletResponse response, LoanTransactionAdminQueryRequest request) throws IOException;
}
