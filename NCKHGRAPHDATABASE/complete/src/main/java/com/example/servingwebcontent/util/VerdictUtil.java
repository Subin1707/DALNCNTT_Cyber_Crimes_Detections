package com.example.servingwebcontent.util;

public class VerdictUtil {

    private VerdictUtil() {
    }

    /**
     * @param finalRisk giá trị rủi ro cuối cùng (0–100)
     * @return verdict tiếng Việt – chuẩn toàn hệ thống
     */
    public static String computeVerdict(int finalRisk) {

        if (finalRisk < 20) {
            return "AN TOÀN";
        }

        if (finalRisk < 50) {
            return "CÓ DẤU HIỆU";
        }

        if (finalRisk < 80) {
            return "ĐÁNG NGHI NGỜ";
        }

        return "GIAN LẬN";
    }
}