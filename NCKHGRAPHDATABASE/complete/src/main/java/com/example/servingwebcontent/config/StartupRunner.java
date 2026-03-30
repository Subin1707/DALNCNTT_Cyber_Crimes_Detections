package com.example.servingwebcontent.config;

import com.example.servingwebcontent.service.PacketCaptureService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class StartupRunner implements CommandLineRunner {

    private final PacketCaptureService packetCaptureService;

    public StartupRunner(PacketCaptureService packetCaptureService) {
        this.packetCaptureService = packetCaptureService;
    }

    @Override
    public void run(String... args) {
        new Thread(() -> packetCaptureService.startCapture()).start();
    }
}
