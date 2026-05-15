package com.example.servingwebcontent.service.distance;

import com.example.servingwebcontent.dto.BehaviorFeatureVector;

/**
 * Interface for distance metric calculations
 * Implementations: Euclidean, Manhattan, Minkowski, Hamming
 */
public interface DistanceMetric {

    /**
     * Calculate distance between two behavior vectors
     * @param a First behavior vector
     * @param b Second behavior vector
     * @return Distance value (0 = identical, higher = more different)
     */
    double calculate(BehaviorFeatureVector a, BehaviorFeatureVector b);

    /**
     * Get the name of the distance metric
     * @return Name of the metric
     */
    String getMetricName();
}
