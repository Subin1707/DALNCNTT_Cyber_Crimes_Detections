package com.example.servingwebcontent.dto;

import java.util.List;

public class BulkAnalysisResponseDTO {

    private boolean success;
    private String message;

    private String sessionId;
    private int totalRecords;
    private int successCount;
    private int errorCount;

    private List<RowResultDTO> results;

    // Constructor rỗng (cần cho Jackson)
    public BulkAnalysisResponseDTO() {}

    /* ===== SUCCESS RESPONSE ===== */

    public BulkAnalysisResponseDTO(String sessionId,
                                   int totalRecords,
                                   int successCount,
                                   int errorCount,
                                   List<RowResultDTO> results) {

        this.success = true;
        this.message = null;

        this.sessionId = sessionId;
        this.totalRecords = totalRecords;
        this.successCount = successCount;
        this.errorCount = errorCount;

        this.results = results == null
                ? List.of()
                : List.copyOf(results);
    }

    /* ===== ERROR RESPONSE ===== */

    public BulkAnalysisResponseDTO(String message) {

        this.success = false;
        this.message = message;

        this.results = List.of();
    }

    /* ===== GETTERS ===== */

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    public String getSessionId() {
        return sessionId;
    }

    public int getTotalRecords() {
        return totalRecords;
    }

    public int getSuccessCount() {
        return successCount;
    }

    public int getErrorCount() {
        return errorCount;
    }

    public List<RowResultDTO> getResults() {
        return results == null ? List.of() : results;
    }
}