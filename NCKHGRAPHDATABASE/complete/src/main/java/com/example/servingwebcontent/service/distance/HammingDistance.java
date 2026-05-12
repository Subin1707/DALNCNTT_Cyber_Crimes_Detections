package com.example.servingwebcontent.service.distance;

import com.example.servingwebcontent.dto.BehaviorFeatureVector;
import org.springframework.stereotype.Component;

/**
 * Hamming Distance Implementation
 * Formula: d(x,y) = Σ[x_i ≠ y_i]
 *
 * Use case: Detect boolean feature flag anomalies
 * Example: VPN + Blacklist + TOR flag combinations
 */
@Component
public class HammingDistance implements DistanceMetric {

    @Override
    public String getMetricName() {
        return "Hamming";
    }

    @Override
    public double calculate(BehaviorFeatureVector a, BehaviorFeatureVector b) {
        if (a == null || b == null) {
            return Double.MAX_VALUE;
        }

        boolean[] flagsA = a.toBooleanArray();
        boolean[] flagsB = b.toBooleanArray();

        int differences = 0;
        int minLength = Math.min(flagsA.length, flagsB.length);

        for (int i = 0; i < minLength; i++) {
            if (flagsA[i] != flagsB[i]) {
                differences++;
            }
        }

        // Normalize by dimension (0 = identical, 1.0 = all different)
        return (double) differences / minLength;
    }
}
