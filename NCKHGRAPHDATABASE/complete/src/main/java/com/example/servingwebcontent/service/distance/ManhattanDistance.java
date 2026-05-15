package com.example.servingwebcontent.service.distance;

import com.example.servingwebcontent.dto.BehaviorFeatureVector;
import org.springframework.stereotype.Component;

/**
 * Manhattan Distance Implementation
 * Formula: d(x,y) = sum(|x_i - y_i|)
 *
 * Use case: numeric behavior comparison that is less sensitive to large outliers
 * than squared-distance metrics.
 */
@Component
public class ManhattanDistance implements DistanceMetric {

    @Override
    public String getMetricName() {
        return "Manhattan";
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

        for (int i = 0; i < minLength; i++) {
            sum += Math.abs(vectorA[i] - vectorB[i]);
        }

        return sum / minLength;
    }
}
