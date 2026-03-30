package com.example.servingwebcontent.service;

import com.example.servingwebcontent.dto.FraudInputDTO;
import com.example.servingwebcontent.model.AnalysisSession;
import com.example.servingwebcontent.repository.AnalysisSessionRepository;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.*;
import java.util.regex.Pattern;

@Service
public class ExcelImportService {

    private static final int MAX_ROWS = 5000;
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5MB

    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");

    private static final Pattern IPV4_PATTERN =
            Pattern.compile(
                    "^((25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)\\.){3}" +
                            "(25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)$"
            );

    private static final Pattern HASH_PATTERN =
            Pattern.compile("^[a-fA-F0-9]{32,64}$");

    private final AnalysisSessionRepository analysisSessionRepository;

    public ExcelImportService(AnalysisSessionRepository repository) {
        this.analysisSessionRepository = repository;
    }

    /* ======================================================
       MAIN METHOD
       ====================================================== */

    public List<FraudInputDTO> parseExcelFile(
            MultipartFile file,
            String createdBy,
            long inputTime
    ) throws Exception {

        if (file == null || file.isEmpty())
            throw new Exception("File trống");

        if (file.getSize() > MAX_FILE_SIZE)
            throw new Exception("File vượt quá 5MB");

        String filename = file.getOriginalFilename();

        if (filename == null ||
                (!filename.endsWith(".xlsx") && !filename.endsWith(".xls")))
            throw new Exception("Chỉ hỗ trợ file .xlsx hoặc .xls");

        String creator = (createdBy == null || createdBy.isBlank())
                ? "unknown"
                : createdBy.trim().toLowerCase();

        AnalysisSession session = new AnalysisSession(
                UUID.randomUUID().toString(),
                filename,
                inputTime,
                creator,
                0,
                "PROCESSING"
        );

        analysisSessionRepository.save(session);

        List<FraudInputDTO> results = new ArrayList<>();
        Set<String> duplicateCheck = new HashSet<>();

        try (InputStream in = file.getInputStream();
             Workbook workbook = filename.endsWith(".xlsx")
                     ? new XSSFWorkbook(in)
                     : new HSSFWorkbook(in)) {

            Sheet sheet = workbook.getSheetAt(0);

            if (sheet == null)
                throw new Exception("File không có sheet");

            HeaderSpec headerSpec = validateHeader(sheet.getRow(0));

            DataFormatter formatter = new DataFormatter();

            FormulaEvaluator evaluator =
                    workbook.getCreationHelper().createFormulaEvaluator();

            int rowCount = 0;

            for (int i = 1; i <= sheet.getLastRowNum(); i++) {

                if (rowCount >= MAX_ROWS)
                    throw new Exception("File vượt quá " + MAX_ROWS + " dòng");

                Row row = sheet.getRow(i);

                if (row == null) continue;

                FraudInputDTO dto =
                        parseRow(row, formatter, evaluator, headerSpec);

                if (dto == null) continue;

                dto = normalize(dto);

                if (!isValid(dto)) continue;

                String key =
                        (dto.getEmail() == null ? "" : dto.getEmail()) + "|" +
                                (dto.getIp() == null ? "" : dto.getIp()) + "|" +
                                (dto.getUrl() == null ? "" : dto.getUrl()) + "|" +
                                (dto.getDomain() == null ? "" : dto.getDomain()) + "|" +
                                (dto.getFileNode() == null ? "" : dto.getFileNode()) + "|" +
                                (dto.getFileHash() == null ? "" : dto.getFileHash()) + "|" +
                                (dto.getVictimAccount() == null ? "" : dto.getVictimAccount());

                if (duplicateCheck.contains(key))
                    continue;

                duplicateCheck.add(key);

                dto.setSessionId(session.getId());

                results.add(dto);

                rowCount++;
            }

            if (results.isEmpty()) {

                session.setStatus("FAILED");
                session.setTotalRows(0);

                analysisSessionRepository.save(session);

                throw new Exception("Không có dữ liệu hợp lệ");
            }

            session.setStatus("DONE");
            session.setTotalRows(results.size());

            analysisSessionRepository.save(session);

            return results;

        } catch (Exception ex) {

            session.setStatus("FAILED");
            session.setTotalRows(0);

            analysisSessionRepository.save(session);

            throw ex;
        }
    }

    /* ======================================================
       HEADER VALIDATION
       ====================================================== */

    private HeaderSpec validateHeader(Row header) throws Exception {

        if (header == null)
            throw new Exception("Thiếu header");

        HeaderSpec spec = new HeaderSpec();

        short last = header.getLastCellNum();
        for (int i = 0; i < last; i++) {
            String raw = getRaw(header.getCell(i));
            String key = normalizeHeader(raw);
            if (key == null) continue;

            if (isHeader(key, "email")) spec.email = i;
            else if (isHeader(key, "ip", "ipaddress", "ip_address")) spec.ip = i;
            else if (isHeader(key, "url")) spec.url = i;
            else if (isHeader(key, "domain")) spec.domain = i;
            else if (isHeader(key, "filenode", "file", "filename", "file_name")) spec.fileNode = i;
            else if (isHeader(key, "filehash", "hash", "file_hash")) spec.fileHash = i;
            else if (isHeader(key, "victimaccount", "victim", "account", "victim_account")) spec.victim = i;
            // AnalysisSession column (if any) is ignored
        }

        if (!spec.hasAny()) {
            throw new Exception("Header cần có ít nhất một cột: Email | IP | URL | Domain | FileNode | FileHash | VictimAccount");
        }

        return spec;
    }

    /* ======================================================
       PARSE ROW
       ====================================================== */

    private FraudInputDTO parseRow(
            Row row,
            DataFormatter formatter,
            FormulaEvaluator evaluator,
            HeaderSpec headerSpec
    ) {

        String email = getCellString(row, headerSpec.email, formatter, evaluator);
        String ip = getCellString(row, headerSpec.ip, formatter, evaluator);
        String url = getCellString(row, headerSpec.url, formatter, evaluator);
        String domain = getCellString(row, headerSpec.domain, formatter, evaluator);
        String fileNode = getCellString(row, headerSpec.fileNode, formatter, evaluator);
        String fileHash = getCellString(row, headerSpec.fileHash, formatter, evaluator);
        String victim = getCellString(row, headerSpec.victim, formatter, evaluator);

        if (email.isBlank() && ip.isBlank() && url.isBlank()
                && domain.isBlank() && fileNode.isBlank()
                && fileHash.isBlank() && victim.isBlank())
            return null;

        FraudInputDTO dto = new FraudInputDTO();
        dto.setEmail(email);
        dto.setIp(ip);
        dto.setUrl(url);
        dto.setDomain(domain);
        dto.setFileNode(fileNode);
        dto.setFileHash(fileHash);
        dto.setVictimAccount(victim);
        return dto;
    }

    /* ======================================================
       NORMALIZE
       ====================================================== */

    private FraudInputDTO normalize(FraudInputDTO dto) {

        if (dto.getEmail() != null)
            dto.setEmail(dto.getEmail().trim().toLowerCase());

        if (dto.getIp() != null)
            dto.setIp(dto.getIp().trim());

        if (dto.getUrl() != null)
            dto.setUrl(
                    dto.getUrl()
                            .trim()
                            .replaceAll("\\s+", "")
            );

        if (dto.getDomain() != null)
            dto.setDomain(dto.getDomain().trim().toLowerCase());

        if (dto.getDomain() == null && dto.getUrl() != null) {
            String extracted = extractDomainFromUrl(dto.getUrl());
            if (extracted != null) dto.setDomain(extracted);
        }

        if (dto.getFileNode() != null)
            dto.setFileNode(dto.getFileNode().trim());

        if (dto.getFileHash() != null)
            dto.setFileHash(dto.getFileHash().trim().toLowerCase());

        if (dto.getVictimAccount() != null)
            dto.setVictimAccount(dto.getVictimAccount().trim().toLowerCase());

        return dto;
    }

    /* ======================================================
       VALIDATION
       ====================================================== */

    private boolean isValid(FraudInputDTO dto) {

        if (dto == null || dto.isEmpty()) return false;

        boolean validEmail =
                dto.getEmail() != null &&
                        EMAIL_PATTERN.matcher(dto.getEmail()).matches();

        boolean validIp =
                dto.getIp() != null &&
                        IPV4_PATTERN.matcher(dto.getIp()).matches();

        boolean validUrl =
                dto.getUrl() != null &&
                        (dto.getUrl().startsWith("http://")
                                || dto.getUrl().startsWith("https://"));

        boolean validDomain =
                dto.getDomain() != null && !dto.getDomain().isBlank();

        boolean validFileNode =
                dto.getFileNode() != null && !dto.getFileNode().isBlank();

        boolean validFileHash =
                dto.getFileHash() != null &&
                        HASH_PATTERN.matcher(dto.getFileHash()).matches();

        boolean validVictim =
                dto.getVictimAccount() != null && !dto.getVictimAccount().isBlank();

        return validEmail || validIp || validUrl
                || validDomain || validFileNode || validFileHash || validVictim;
    }

    /* ======================================================
       UTIL
       ====================================================== */

    private String getRaw(Cell cell) {

        if (cell == null)
            return "";

        return cell.toString().trim();
    }

    private String extractDomainFromUrl(String url) {
        if (url == null) return null;
        String s = url.trim();
        if (s.isEmpty()) return null;
        try {
            if (!s.startsWith("http://") && !s.startsWith("https://")) {
                s = "https://" + s;
            }
            java.net.URI uri = java.net.URI.create(s);
            String host = uri.getHost();
            if (host == null || host.isBlank()) return null;
            return host.toLowerCase();
        } catch (Exception e) {
            return null;
        }
    }

    private String getCellString(Row row,
                                 Integer idx,
                                 DataFormatter formatter,
                                 FormulaEvaluator evaluator) {
        if (idx == null || idx < 0) return "";
        return formatter.formatCellValue(
                row.getCell(idx, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK),
                evaluator
        );
    }

    private String normalizeHeader(String raw) {
        if (raw == null) return null;
        String s = raw.trim().toLowerCase();
        if (s.isEmpty()) return null;
        return s.replaceAll("\\s+", "")
                .replace("_", "")
                .replace("-", "");
    }

    private boolean isHeader(String key, String... candidates) {
        for (String c : candidates) {
            if (key.equals(c)) return true;
        }
        return false;
    }

    private static class HeaderSpec {
        Integer email;
        Integer ip;
        Integer url;
        Integer domain;
        Integer fileNode;
        Integer fileHash;
        Integer victim;

        boolean hasAny() {
            return email != null || ip != null || url != null
                    || domain != null || fileNode != null
                    || fileHash != null || victim != null;
        }
    }
}
