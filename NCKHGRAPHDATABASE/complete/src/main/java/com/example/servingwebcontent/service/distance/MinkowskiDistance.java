package com.example.servingwebcontent.service.distance;

import com.example.servingwebcontent.dto.BehaviorFeatureVector;
import org.springframework.stereotype.Component;

/**
 * Minkowski Distance Implementation
 * Formula: d(x,y) = (Σ|x_i - y_i|^p)^(1/p)
 *
 * Use case: Multi-dimensional feature analysis with flexible sensitivity
 * p=1: Manhattan distance (sum of absolute differences)
 * p=2: Euclidean distance (straight-line distance)
 * p=3: Cubic distance (more sensitive to outliers)
 */
@Component
public class MinkowskiDistance implements DistanceMetric {

    private static final double P = 3.0; // Sensitivity parameter

    @Override
    public String getMetricName() {
        return "Minkowski";
    }

    @Override
    public double calculate(BehaviorFeatureVector a, BehaviorFeatureVector b) {
        if (a == null || b == null) {
            return Double.MAX_VALUE;
        }

        double[] vectorA = a.toNumericArray();
        double[] vectorB = b.toNumericArray();

        double sum = 0.0;
        int minLength = Math.min(vectorA.length, vectorB.length);

        // Calculate Σ|x_i - y_i|^p
        for (int i = 0; i < minLength; i++) {
            double diff = Math.abs(vectorA[i] - vectorB[i]);
            sum += Math.pow(diff, P);
        }

        // Calculate (Σ)^(1/p) and normalize
        double result = Math.pow(sum, 1.0 / P);
        return result / minLength;
    }
}
