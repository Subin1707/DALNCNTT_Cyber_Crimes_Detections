package com.example.servingwebcontent.service;

import com.example.servingwebcontent.dto.HybridRiskScoreDTO;
import com.example.servingwebcontent.dto.SessionFeatureVectorDTO;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
public class HybridRiskScoringService {

    private static final double RULE_WEIGHT = 0.4;
    private static final double KNN_WEIGHT = 0.3;
    private static final double BAYES_WEIGHT = 0.3;
    private static final int DEFAULT_K = 5;
    private static final int HISTORY_LIMIT = 200;

    private final SessionFeatureService sessionFeatureService;

    public HybridRiskScoringService(SessionFeatureService sessionFeatureService) {
        this.sessionFeatureService = sessionFeatureService;
    }

    public HybridRiskScoreDTO scoreSession(String sessionId,
                                           GraphRiskService.GraphRiskResult graphRisk) {

        SessionFeatureVectorDTO features = sessionFeatureService.extractFeatures(sessionId);
        List<SessionFeatureService.HistoricalSessionSample> samples =
                sessionFeatureService.loadHistoricalSamples(sessionId, HISTORY_LIMIT);

        double ruleScore = clamp(graphRisk == null ? 0 : graphRisk.score);
        double knnScore = calculateKnnScore(features, samples);
        double probabilityScore = calculateBayesianScore(features, samples);
        double finalScore = clamp(
                RULE_WEIGHT * ruleScore +
                KNN_WEIGHT * knnScore +
                BAYES_WEIGHT * probabilityScore
        );

        String riskLevel = riskLevel(finalScore);
        String verdict = verdict(finalScore);

        List<String> indicators = new ArrayList<>();
        if (graphRisk != null && graphRisk.indicators != null) {
            indicators.addAll(graphRisk.indicators);
        }
        indicators.add("Hybrid model weights: rule=0.40, knn=0.30, bayes=0.30");
        indicators.add("Feature vector: emails=%d, ips=%d, urls=%d, domains=%d, sharedIps=%d, repeatedUrls=%d, highRiskNodes=%d, mediumRiskNodes=%d"
                .formatted(
                        features.getNumEmails(),
                        features.getNumIps(),
                        features.getNumUrls(),
                        features.getNumDomains(),
                        features.getNumSharedIps(),
                        features.getNumRepeatedUrls(),
                        features.getNumHighRiskNodes(),
                        features.getNumMediumRiskNodes()
                ));
        indicators.add("Rule score=" + format(ruleScore));
        indicators.add("KNN score=" + format(knnScore));
        indicators.add("Bayesian score=" + format(probabilityScore));

        if (samples.isEmpty()) {
            indicators.add("Historical dataset is small, KNN/Bayesian fallback to rule-biased estimate");
        } else {
            indicators.add("Historical sessions used for hybrid scoring=" + samples.size());
        }

        return new HybridRiskScoreDTO(
                ruleScore,
                knnScore,
                probabilityScore,
                finalScore,
                riskLevel,
                verdict,
                indicators,
                features
        );
    }

    private double calculateKnnScore(SessionFeatureVectorDTO current,
                                     List<SessionFeatureService.HistoricalSessionSample> samples) {

        if (samples == null || samples.isEmpty()) {
            return heuristicFallback(current);
        }

        int k = Math.min(DEFAULT_K, samples.size());
        List<Neighbor> neighbors = new ArrayList<>();

        for (SessionFeatureService.HistoricalSessionSample sample : samples) {
            double distance = euclideanDistance(current.toNumericVector(), sample.features().toNumericVector());
            neighbors.add(new Neighbor(distance, sample.fraud(), sample.score()));
        }

        neighbors.sort(Comparator.comparingDouble(Neighbor::distance));

        double fraudWeight = 0.0;
        double totalWeight = 0.0;
        for (int i = 0; i < k; i++) {
            Neighbor neighbor = neighbors.get(i);
            double weight = 1.0 / Math.max(0.001, neighbor.distance());
            totalWeight += weight;
            if (neighbor.fraud()) {
                fraudWeight += weight;
            }
        }

        if (totalWeight == 0.0) {
            return heuristicFallback(current);
        }

        return clamp(100.0 * fraudWeight / totalWeight);
    }

    private double calculateBayesianScore(SessionFeatureVectorDTO current,
                                          List<SessionFeatureService.HistoricalSessionSample> samples) {

        if (samples == null || samples.isEmpty()) {
            return heuristicFallback(current);
        }

        int fraudCount = 0;
        int normalCount = 0;
        int fraudSharedIp = 0;
        int normalSharedIp = 0;
        int fraudRepeatedUrl = 0;
        int normalRepeatedUrl = 0;
        int fraudHighRisk = 0;
        int normalHighRisk = 0;
        int fraudDense = 0;
        int normalDense = 0;

        for (SessionFeatureService.HistoricalSessionSample sample : samples) {
            boolean fraud = sample.fraud();
            SessionFeatureVectorDTO feature = sample.features();

            if (fraud) {
                fraudCount++;
                if (feature.getNumSharedIps() > 0) fraudSharedIp++;
                if (feature.getNumRepeatedUrls() > 0) fraudRepeatedUrl++;
                if (feature.getNumHighRiskNodes() > 0) fraudHighRisk++;
                if (feature.getNumUrls() + feature.getNumIps() >= 3) fraudDense++;
            } else {
                normalCount++;
                if (feature.getNumSharedIps() > 0) normalSharedIp++;
                if (feature.getNumRepeatedUrls() > 0) normalRepeatedUrl++;
                if (feature.getNumHighRiskNodes() > 0) normalHighRisk++;
                if (feature.getNumUrls() + feature.getNumIps() >= 3) normalDense++;
            }
        }

        double priorFraud = laplace(fraudCount, fraudCount + normalCount, 2);
        double priorNormal = laplace(normalCount, fraudCount + normalCount, 2);

        double pFraud = priorFraud;
        double pNormal = priorNormal;

        pFraud *= conditional(current.getNumSharedIps() > 0, fraudSharedIp, fraudCount);
        pNormal *= conditional(current.getNumSharedIps() > 0, normalSharedIp, normalCount);

        pFraud *= conditional(current.getNumRepeatedUrls() > 0, fraudRepeatedUrl, fraudCount);
        pNormal *= conditional(current.getNumRepeatedUrls() > 0, normalRepeatedUrl, normalCount);

        pFraud *= conditional(current.getNumHighRiskNodes() > 0, fraudHighRisk, fraudCount);
        pNormal *= conditional(current.getNumHighRiskNodes() > 0, normalHighRisk, normalCount);

        boolean dense = current.getNumUrls() + current.getNumIps() >= 3;
        pFraud *= conditional(dense, fraudDense, fraudCount);
        pNormal *= conditional(dense, normalDense, normalCount);

        double denom = pFraud + pNormal;
        if (denom <= 0.0) {
            return heuristicFallback(current);
        }

        return clamp(100.0 * pFraud / denom);
    }

    private double conditional(boolean featurePresent, int positiveCount, int totalClassCount) {
        double present = laplace(positiveCount, totalClassCount, 2);
        return featurePresent ? present : (1.0 - present);
    }

    private double laplace(int count, int total, int classes) {
        return (count + 1.0) / (total + classes);
    }

    private double heuristicFallback(SessionFeatureVectorDTO current) {
        double score =
                current.getNumHighRiskNodes() * 20.0 +
                current.getNumSharedIps() * 15.0 +
                current.getNumRepeatedUrls() * 12.0 +
                current.getNumUrls() * 4.0 +
                current.getNumIps() * 4.0 +
                current.getNumIndicators() * 2.0;
        return clamp(score);
    }

    private double euclideanDistance(List<Double> a, List<Double> b) {
        double sum = 0.0;
        int size = Math.min(a.size(), b.size());
        for (int i = 0; i < size; i++) {
            double diff = a.get(i) - b.get(i);
            sum += diff * diff;
        }
        return Math.sqrt(sum);
    }

    private double clamp(double value) {
        return Math.max(0.0, Math.min(100.0, value));
    }

    private String riskLevel(double score) {
        if (score >= 60.0) return "high";
        if (score >= 30.0) return "medium";
        return "low";
    }

    private String verdict(double score) {
        if (score >= 60.0) return "BLOCK";
        if (score >= 30.0) return "REVIEW";
        return "ALLOW";
    }

    private String format(double value) {
        return String.format(java.util.Locale.US, "%.2f", value);
    }

    private record Neighbor(double distance, boolean fraud, double score) {}
}
