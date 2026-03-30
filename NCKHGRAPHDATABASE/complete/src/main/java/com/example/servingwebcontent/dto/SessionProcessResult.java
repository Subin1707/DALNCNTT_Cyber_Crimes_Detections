package com.example.servingwebcontent.dto;

import java.util.List;

public class SessionProcessResult {

    private final AnalysisResultDTO sessionResult;

    private final List<AnalysisResultDTO> rowResults;

    public SessionProcessResult(AnalysisResultDTO sessionResult,
                                List<AnalysisResultDTO> rowResults) {

        this.sessionResult = sessionResult;

        this.rowResults = rowResults == null
                ? List.of()
                : List.copyOf(rowResults);
    }

    public AnalysisResultDTO getSessionResult() {
        return sessionResult;
    }

    public List<AnalysisResultDTO> getRowResults() {
        return rowResults == null ? List.of() : rowResults;
    }
}