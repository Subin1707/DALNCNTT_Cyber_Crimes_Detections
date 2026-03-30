package com.example.servingwebcontent.service;

import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.time.Instant;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class PacketCaptureService {

    private final FraudAnalysisService fraudAnalysisService;
    private final AtomicLong totalLines = new AtomicLong(0);
    private final AtomicLong totalRecords = new AtomicLong(0);
    private final AtomicLong totalSaved = new AtomicLong(0);
    private volatile Instant lastSavedAt = null;

    public PacketCaptureService(FraudAnalysisService fraudAnalysisService) {
        this.fraudAnalysisService = fraudAnalysisService;
    }

    public void startCapture() {
        try {

            ProcessBuilder pb = new ProcessBuilder(
                    "E:\\Program Files (x86)\\tshark.exe",
                    "-l",
                    "-i", "4",
                    "-Y", "dns.qry.name",
                    "-T", "fields",
                    "-e", "ip.src",
                    "-e", "ipv6.src",
                    "-e", "dns.qry.name"
            );

            pb.redirectErrorStream(true);
            Process process = pb.start();

            BufferedReader reader =
                    new BufferedReader(new InputStreamReader(process.getInputStream()));

            String line;

            System.out.println("[PacketCapture] tshark started, waiting for DNS queries...");

            while ((line = reader.readLine()) != null) {
                totalLines.incrementAndGet();

                String[] parts = line.split("\\t", -1);

                if (parts.length >= 3) {

                    String ipv4 = parts[0] == null ? "" : parts[0].trim();
                    String ipv6 = parts[1] == null ? "" : parts[1].trim();
                    String domains = parts[2] == null ? "" : parts[2].trim();

                    String ip = !ipv4.isBlank() ? ipv4 : ipv6;

                    if (ip.isBlank() || domains.isBlank()) {
                        continue;
                    }

                    for (String domain : domains.split(",")) {
                        String d = domain.trim();
                        if (d.isEmpty()) continue;

                        System.out.println("IP: " + ip + " -> Domain: " + d);
                        totalRecords.incrementAndGet();
                        try {
                            fraudAnalysisService.addNetworkConnection(ip, d);
                            totalSaved.incrementAndGet();
                            lastSavedAt = Instant.now();
                        } catch (Exception ex) {
                            System.err.println("[PacketCapture] save failed: " + ex.getMessage());
                        }
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public long getTotalLines() {
        return totalLines.get();
    }

    public long getTotalRecords() {
        return totalRecords.get();
    }

    public long getTotalSaved() {
        return totalSaved.get();
    }

    public String getLastSavedAt() {
        return lastSavedAt == null ? null : lastSavedAt.toString();
    }
}
