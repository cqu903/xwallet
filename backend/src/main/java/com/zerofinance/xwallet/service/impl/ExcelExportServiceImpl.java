package com.zerofinance.xwallet.service.impl;

import com.zerofinance.xwallet.model.dto.LoanTransactionAdminItemResponse;
import com.zerofinance.xwallet.model.dto.LoanTransactionAdminQueryRequest;
import com.zerofinance.xwallet.model.entity.LoanTransaction;
import com.zerofinance.xwallet.repository.LoanTransactionMapper;
import com.zerofinance.xwallet.service.ExcelExportService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Excel 导出服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ExcelExportServiceImpl implements ExcelExportService {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final LoanTransactionMapper loanTransactionMapper;

    @Override
    public void exportTransactions(HttpServletResponse response, LoanTransactionAdminQueryRequest request) throws IOException {
        List<LoanTransaction> transactions = loanTransactionMapper.findAllAdminByCondition(request);
        if (transactions == null || transactions.isEmpty()) {
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"code\":400,\"message\":\"无数据可导出\"}");
            return;
        }

        List<LoanTransactionAdminItemResponse> items = transactions.stream()
                .map(this::toAdminItem)
                .collect(Collectors.toList());

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("交易记录");

            // 创建标题行样式
            CellStyle headerStyle = createHeaderStyle(workbook);

            // 创建数据行样式
            CellStyle dataStyle = createDataStyle(workbook);

            // 创建表头
            Row headerRow = sheet.createRow(0);
            String[] headers = {
                    "交易号", "交易类型", "交易状态", "发生时间", "交易金额",
                    "本金拆分", "利息拆分", "客户ID", "客户邮箱", "合同号",
                    "幂等键", "来源", "创建人", "备注", "冲正原交易号"
            };

            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            // 填充数据
            int rowNum = 1;
            for (LoanTransactionAdminItemResponse item : items) {
                Row row = sheet.createRow(rowNum++);

                createCell(row, 0, item.getTransactionId(), dataStyle);
                createCell(row, 1, item.getType(), dataStyle);
                createCell(row, 2, item.getStatus(), dataStyle);
                createCell(row, 3, formatDate(item.getOccurredAt()), dataStyle);
                createCell(row, 4, item.getAmount(), dataStyle);
                createCell(row, 5, item.getPrincipalComponent(), dataStyle);
                createCell(row, 6, item.getInterestComponent(), dataStyle);
                createCell(row, 7, item.getCustomerId() != null ? item.getCustomerId().toString() : "", dataStyle);
                createCell(row, 8, item.getCustomerEmail(), dataStyle);
                createCell(row, 9, item.getContractId(), dataStyle);
                createCell(row, 10, item.getIdempotencyKey(), dataStyle);
                createCell(row, 11, item.getSource(), dataStyle);
                createCell(row, 12, item.getCreatedBy(), dataStyle);
                createCell(row, 13, item.getNote(), dataStyle);
                createCell(row, 14, item.getReversalOf(), dataStyle);
            }

            // 自动调整列宽
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
                // 设置最小列宽
                if (sheet.getColumnWidth(i) < 2000) {
                    sheet.setColumnWidth(i, 2000);
                }
                // 设置最大列宽
                if (sheet.getColumnWidth(i) > 8000) {
                    sheet.setColumnWidth(i, 8000);
                }
            }

            // 设置响应头
            String filename = "交易记录_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".xlsx";
            String encodedFilename = URLEncoder.encode(filename, StandardCharsets.UTF_8).replaceAll("\\+", "%20");

            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setHeader("Content-Disposition", "attachment; filename*=UTF-8''" + encodedFilename);

            // 写入响应
            workbook.write(response.getOutputStream());
            response.getOutputStream().flush();

            log.info("导出交易记录成功，共 {} 条", items.size());
        }
    }

    /**
     * 创建表头样式
     */
    private CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();

        // 背景色
        style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);

        // 字体
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 11);
        style.setFont(font);

        // 边框
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);

        // 对齐
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);

        return style;
    }

    /**
     * 创建数据样式
     */
    private CellStyle createDataStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();

        // 边框
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);

        // 对齐
        style.setVerticalAlignment(VerticalAlignment.CENTER);

        return style;
    }

    /**
     * 创建单元格
     */
    private void createCell(Row row, int column, String value, CellStyle style) {
        Cell cell = row.createCell(column);
        cell.setCellValue(value != null ? value : "");
        cell.setCellStyle(style);
    }

    /**
     * 创建数值单元格
     */
    private void createCell(Row row, int column, Object value, CellStyle style) {
        Cell cell = row.createCell(column);
        if (value == null) {
            cell.setCellValue("");
        } else {
            cell.setCellValue(value.toString());
        }
        cell.setCellStyle(style);
    }

    /**
     * 格式化日期
     */
    private String formatDate(LocalDateTime dateTime) {
        return dateTime != null ? dateTime.format(DATE_FORMATTER) : "";
    }

    /**
     * 转换为管理后台响应对象
     */
    private LoanTransactionAdminItemResponse toAdminItem(LoanTransaction transaction) {
        return LoanTransactionAdminItemResponse.builder()
                .transactionId(transaction.getTxnNo())
                .type(transaction.getTxnType())
                .status(transaction.getStatus())
                .occurredAt(transaction.getCreatedAt())
                .amount(transaction.getAmount())
                .principalComponent(transaction.getPrincipalComponent())
                .interestComponent(transaction.getInterestComponent())
                .customerId(transaction.getCustomerId())
                .customerEmail(transaction.getCustomerEmail())
                .contractId(transaction.getContractNo())
                .idempotencyKey(transaction.getIdempotencyKey())
                .source(transaction.getSource())
                .createdBy(transaction.getCreatedBy())
                .note(transaction.getNote())
                .reversalOf(transaction.getReversalOf())
                .build();
    }
}
