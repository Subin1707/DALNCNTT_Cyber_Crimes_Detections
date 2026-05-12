package com.example.servingwebcontent.service.distance;

import com.example.servingwebcontent.dto.BehaviorFeatureVector;
import org.springframework.stereotype.Component;

/**
 * Euclidean Distance Implementation
 * Formula: d(x,y) = √(Σ(x_i - y_i)²)
 *
 * Use case: Detect numeric feature anomalies
 * Example: Unusual IP count, URL count combinations
 */
@Component
public class EuclideanDistance implements DistanceMetric {

    @Override
    public String getMetricName() {
        return "Euclidean";
    }

    @Override
    public double calculate(BehaviorFeatureVector a, BehaviorFeatureVector b) {
        if (a == null || b == null) {
            return Double.MAX_VALUE;
        }

        double[] vectorA = a.toNumericArray();
        double[] vectorB = b.toNumericArray();

        double sumSquares = 0.0;
        int minLength = Math.min(vectorA.length, vectorB.length);

        for (int i = 0; i < minLength; i++) {
            double diff = vectorA[i] - vectorB[i];
            sumSquares += diff * diff;
        }

        // Normalize by dimension to handle scale
        return Math.sqrt(sumSquares / minLength);
    }
}
