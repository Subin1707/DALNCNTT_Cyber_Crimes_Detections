package com.example.servingwebcontent.service;

import com.example.servingwebcontent.dto.BehaviorFeatureVector;
import com.example.servingwebcontent.dto.FraudInputDTO;
import com.example.servingwebcontent.model.RegionType;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;

@Service
public class AlgorithmComparisonService {

    private static final String DATASET_PATH = "data/algorithm-evaluation-samples.csv";
    private static final int MIN_TEST_RUNS = 30;
    private static final long RUN_SEED = 20260605L;

    private final MultiRegionAnalysisService multiRegionService;
    private final FraudAnalysisService fraudAnalysisService;

    public AlgorithmComparisonService(MultiRegionAnalysisService multiRegionService,
                                      FraudAnalysisService fraudAnalysisService) {
        this.multiRegionService = multiRegionService;
        this.fraudAnalysisService = fraudAnalysisService;
    }

    public ComparisonReport compareAlgorithms(int k, int repeatCount) {
        int safeK = Math.max(1, Math.min(k, 15));
        int testRuns = Math.max(MIN_TEST_RUNS, Math.min(repeatCount, 200));
        EvaluationDataset dataset = loadDataset();

        List<AlgorithmResult> results = new ArrayList<>();
        results.add(evaluateRuleBased(dataset, testRuns));
        results.add(evaluateMultiRegion(dataset, testRuns));

        for (String metric : List.of("euclidean", "manhattan", "minkowski", "hamming")) {
            results.add(evaluateKnn(dataset, safeK, metric, testRuns));
        }

        double fastest = results.stream().mapToDouble(AlgorithmResult::averageProcessingMs).min().orElse(0.0);
        List<AlgorithmResult> ranked = results.stream()
                .map(result -> result.withOptimizationScore(calculateOptimizationScore(result, fastest)))
                .sorted(Comparator.comparingDouble(AlgorithmResult::optimizationScore).reversed())
                .toList();

        return new ComparisonReport(
                safeK,
                testRuns,
                DATASET_PATH,
                dataset.sourceName(),
                dataset.trainSamples().size(),
                dataset.testSamples().size(),
                dataset.testSampleSizePerRun(),
                dataset.labelDistribution(),
                "ground_truth_label",
                "email,ip,url,domain,file_node,file_hash,victim_account",
                "Mỗi thuật toán chỉ nhận dữ liệu thô giống file Excel. Hệ thống tự trích BehaviorFeatureVector trước khi dự đoán; ground_truth_label được ẩn khỏi classifier và chỉ dùng sau khi đã dự đoán để tính Accuracy, Precision, Recall, F1 và confusion matrix.",
                ranked.isEmpty() ? null : ranked.get(0),
                ranked,
                "optimizationScore = 70% accuracy + 25% F1 + 5% speed"
        );
    }

    private AlgorithmResult evaluateRuleBased(EvaluationDataset dataset, int testRuns) {
        return evaluateRepeated(
                "Rule-based scoring",
                "RULE_BASE",
                "Rule-based scoring",
                "Average over " + testRuns + " repeated stratified tests; prediction is produced from feature columns, then checked against ground_truth_label.",
                dataset,
                testRuns,
                sample -> classifyByRuleScore(sample.vector())
        );
    }

    private AlgorithmResult evaluateMultiRegion(EvaluationDataset dataset, int testRuns) {
        return evaluateRepeated(
                "Multi-Region",
                "DISTANCE_REGION",
                "Distance to region centers",
                "Average over " + testRuns + " repeated stratified tests; compares d_safe, d_nghingo and d_gianlan, then checks against ground_truth_label.",
                dataset,
                testRuns,
                sample -> multiRegionService.analyzeAgainstRegions(sample.vector()).getPrimaryRegion()
        );
    }

    private AlgorithmResult evaluateKnn(EvaluationDataset dataset, int k, String metric, int testRuns) {
        return evaluateRepeated(
                "KNN " + metric.toUpperCase(Locale.ROOT),
                "KNN",
                "K-nearest neighbors",
                "Average over " + testRuns + " repeated stratified tests; train split is fixed, prediction uses feature vector only, metric=" + metric + ".",
                dataset,
                testRuns,
                sample -> multiRegionService.classifyWithKnn(sample.vector(), dataset.trainSamples(), k, metric)
                        .getPredictedRegion()
        );
    }

    private AlgorithmResult evaluateRepeated(String name,
                                             String family,
                                             String methodType,
                                             String note,
                                             EvaluationDataset dataset,
                                             int testRuns,
                                             SampleClassifier classifier) {
        List<RunMetric> runMetrics = new ArrayList<>();
        Map<RegionType, Map<RegionType, Integer>> aggregateMatrix = initializeConfusionMatrix();

        long startedAt = System.nanoTime();
        for (int runIndex = 0; runIndex < testRuns; runIndex++) {
            List<MultiRegionAnalysisService.LabeledBehaviorSample> runSamples =
                    createRunSamples(dataset.testSamples(), runIndex);
            MultiRegionAnalysisService.EvaluationResult evaluation =
                    evaluateSamples("run_" + (runIndex + 1), runSamples, classifier);

            mergeMatrix(aggregateMatrix, evaluation.getConfusionMatrix());
            runMetrics.add(new RunMetric(
                    runIndex + 1,
                    evaluation.getCorrectPredictions(),
                    evaluation.getTotalPredictions(),
                    evaluation.getAccuracy(),
                    evaluation.getMacroPrecision(),
                    evaluation.getMacroRecall(),
                    evaluation.getMacroF1Score(),
                    copyMatrix(evaluation.getConfusionMatrix())
            ));
        }
        long elapsed = System.nanoTime() - startedAt;

        double totalPredictions = runMetrics.stream().mapToInt(RunMetric::totalPredictions).sum();
        double averageProcessingMs = elapsed / 1_000_000.0 / Math.max(1.0, totalPredictions);
        int correctTotal = runMetrics.stream().mapToInt(RunMetric::correctPredictions).sum();
        int predictionTotal = runMetrics.stream().mapToInt(RunMetric::totalPredictions).sum();

        return new AlgorithmResult(
                name,
                family,
                methodType,
                predictionTotal,
                correctTotal,
                average(runMetrics, "accuracy"),
                average(runMetrics, "precision"),
                average(runMetrics, "recall"),
                average(runMetrics, "f1"),
                averageProcessingMs,
                0.0,
                min(runMetrics, "accuracy"),
                max(runMetrics, "accuracy"),
                stdDev(runMetrics, "accuracy"),
                testRuns,
                note,
                aggregateMatrix,
                runMetrics
        );
    }

    private List<MultiRegionAnalysisService.LabeledBehaviorSample> createRunSamples(
            List<MultiRegionAnalysisService.LabeledBehaviorSample> testSamples,
            int runIndex) {
        List<MultiRegionAnalysisService.LabeledBehaviorSample> selected = new ArrayList<>();
        for (RegionType label : RegionType.values()) {
            List<MultiRegionAnalysisService.LabeledBehaviorSample> bucket = testSamples.stream()
                    .filter(sample -> sample.label() == label)
                    .collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
            Collections.shuffle(bucket, new Random(RUN_SEED + runIndex * 31L + label.ordinal()));
            int take = Math.max(1, (int) Math.ceil(bucket.size() * 0.70));
            selected.addAll(bucket.subList(0, Math.min(take, bucket.size())));
        }
        Collections.shuffle(selected, new Random(RUN_SEED + runIndex));
        return selected;
    }

    private void mergeMatrix(Map<RegionType, Map<RegionType, Integer>> target,
                             Map<RegionType, Map<RegionType, Integer>> source) {
        for (RegionType actual : RegionType.values()) {
            for (RegionType predicted : RegionType.values()) {
                int value = source.getOrDefault(actual, Map.of()).getOrDefault(predicted, 0);
                target.get(actual).put(predicted, target.get(actual).get(predicted) + value);
            }
        }
    }

    private Map<RegionType, Map<RegionType, Integer>> copyMatrix(Map<RegionType, Map<RegionType, Integer>> source) {
        Map<RegionType, Map<RegionType, Integer>> copy = new EnumMap<>(RegionType.class);
        for (RegionType actual : RegionType.values()) {
            Map<RegionType, Integer> predictedMap = new EnumMap<>(RegionType.class);
            for (RegionType predicted : RegionType.values()) {
                predictedMap.put(predicted, source.getOrDefault(actual, Map.of()).getOrDefault(predicted, 0));
            }
            copy.put(actual, predictedMap);
        }
        return copy;
    }

    private EvaluationDataset loadDataset() {
        ClassPathResource resource = new ClassPathResource(DATASET_PATH);
        List<MultiRegionAnalysisService.LabeledBehaviorSample> trainSamples = new ArrayList<>();
        List<MultiRegionAnalysisService.LabeledBehaviorSample> testSamples = new ArrayList<>();
        Map<String, Integer> labelDistribution = new LinkedHashMap<>();
        String sourceName = "unknown";

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            boolean header = true;
            while ((line = reader.readLine()) != null) {
                if (header) {
                    header = false;
                    continue;
                }
                if (line.isBlank()) {
                    continue;
                }

                String[] columns = line.split(",", -1);
                if (columns.length < 11) {
                    throw new IllegalStateException("Dataset row has fewer than 11 columns: " + line);
                }

                String nodeId = columns[0].trim();
                RegionType label = RegionType.valueOf(columns[1].trim().toUpperCase(Locale.ROOT));
                String split = columns[2].trim().toLowerCase(Locale.ROOT);
                String rowSource = columns[3].trim();
                if (!rowSource.isBlank()) {
                    sourceName = rowSource;
                }

                FraudInputDTO input = new FraudInputDTO();
                input.setEmail(blankToNull(columns[4]));
                input.setIp(blankToNull(columns[5]));
                input.setUrl(blankToNull(columns[6]));
                input.setDomain(blankToNull(columns[7]));
                input.setFileNode(blankToNull(columns[8]));
                input.setFileHash(blankToNull(columns[9]));
                input.setVictimAccount(blankToNull(columns[10]));

                BehaviorFeatureVector vector = fraudAnalysisService.extractBehaviorFeatures(input);
                MultiRegionAnalysisService.LabeledBehaviorSample sample =
                        new MultiRegionAnalysisService.LabeledBehaviorSample(nodeId, label, vector);

                if ("train".equals(split)) {
                    trainSamples.add(sample);
                } else if ("test".equals(split)) {
                    testSamples.add(sample);
                } else {
                    throw new IllegalStateException("Unknown dataset split: " + split);
                }
                labelDistribution.merge(label.name(), 1, Integer::sum);
            }
        } catch (IOException e) {
            throw new IllegalStateException("Cannot read evaluation dataset: " + DATASET_PATH, e);
        }

        if (trainSamples.isEmpty() || testSamples.isEmpty()) {
            throw new IllegalStateException("Evaluation dataset must contain both train and test rows");
        }
        int testSampleSizePerRun = RegionType.values().length *
                Math.max(1, (int) Math.ceil(testSamples.stream().filter(s -> s.label() == RegionType.SAFE).count() * 0.70));
        return new EvaluationDataset(sourceName, trainSamples, testSamples, testSampleSizePerRun, labelDistribution);
    }

    private MultiRegionAnalysisService.EvaluationResult evaluateSamples(
            String method,
            List<MultiRegionAnalysisService.LabeledBehaviorSample> samples,
            SampleClassifier classifier) {
        Map<RegionType, Map<RegionType, Integer>> matrix = initializeConfusionMatrix();
        List<MultiRegionAnalysisService.EvaluationRow> rows = new ArrayList<>();
        int correct = 0;

        for (MultiRegionAnalysisService.LabeledBehaviorSample sample : samples) {
            RegionType predicted = classifier.classify(sample);
            boolean isCorrect = sample.label() == predicted;
            if (isCorrect) correct++;
            matrix.get(sample.label()).put(predicted, matrix.get(sample.label()).get(predicted) + 1);
            rows.add(new MultiRegionAnalysisService.EvaluationRow(sample.nodeId(), sample.label(), predicted, isCorrect));
        }

        double accuracy = samples.isEmpty() ? 0.0 : (double) correct / samples.size();
        return new MultiRegionAnalysisService.EvaluationResult(method, samples.size(), correct, accuracy, matrix, rows);
    }

    private RegionType classifyByRuleScore(BehaviorFeatureVector vector) {
        if (vector.isBlacklist() && (vector.isTorNetwork() || vector.isSuspiciousUrl() || vector.isSpamPattern())) {
            return RegionType.FRAUD;
        }
        if (vector.isTorNetwork() && (vector.isSuspiciousUrl() || vector.isSpamPattern())) {
            return RegionType.FRAUD;
        }
        if (vector.isBlacklist() || vector.isTorNetwork() || vector.isSuspiciousUrl() || vector.isSpamPattern()) {
            return RegionType.SUSPICIOUS;
        }

        int score = 0;
        score += Math.min(20, vector.getIpCount());
        score += Math.min(20, vector.getUrlCount());
        score += Math.min(12, vector.getFailedLoginCount() * 2);
        score += Math.min(15, (int) Math.round(vector.getRequestFrequency()));
        if (vector.isVpn()) score += 10;
        if (vector.isSuspiciousUrl()) score += 15;
        if (vector.isSpamPattern()) score += 12;
        if (vector.isAbnormalAccessTime()) score += 8;
        if (vector.isBlacklist()) score += 35;
        if (vector.isTorNetwork()) score += 30;
        if (score >= 55) return RegionType.FRAUD;
        if (score >= 20) return RegionType.SUSPICIOUS;
        return RegionType.SAFE;
    }

    private double calculateOptimizationScore(AlgorithmResult result, double fastestMs) {
        double speedScore = result.averageProcessingMs() <= 0.0 ? 1.0 : Math.min(1.0, fastestMs / result.averageProcessingMs());
        return 0.70 * result.accuracy() + 0.25 * result.f1Score() + 0.05 * speedScore;
    }

    private double average(List<RunMetric> metrics, String field) {
        return metrics.stream().mapToDouble(metric -> metricValue(metric, field)).average().orElse(0.0);
    }

    private double min(List<RunMetric> metrics, String field) {
        return metrics.stream().mapToDouble(metric -> metricValue(metric, field)).min().orElse(0.0);
    }

    private double max(List<RunMetric> metrics, String field) {
        return metrics.stream().mapToDouble(metric -> metricValue(metric, field)).max().orElse(0.0);
    }

    private double stdDev(List<RunMetric> metrics, String field) {
        double avg = average(metrics, field);
        double variance = metrics.stream()
                .mapToDouble(metric -> Math.pow(metricValue(metric, field) - avg, 2))
                .average()
                .orElse(0.0);
        return Math.sqrt(variance);
    }

    private double metricValue(RunMetric metric, String field) {
        return switch (field) {
            case "precision" -> metric.precision();
            case "recall" -> metric.recall();
            case "f1" -> metric.f1Score();
            default -> metric.accuracy();
        };
    }

    private Map<RegionType, Map<RegionType, Integer>> initializeConfusionMatrix() {
        Map<RegionType, Map<RegionType, Integer>> matrix = new EnumMap<>(RegionType.class);
        for (RegionType actual : RegionType.values()) {
            Map<RegionType, Integer> predictedMap = new EnumMap<>(RegionType.class);
            for (RegionType predicted : RegionType.values()) {
                predictedMap.put(predicted, 0);
            }
            matrix.put(actual, predictedMap);
        }
        return matrix;
    }

    private String blankToNull(String value) {
        if (value == null) return null;
        String normalized = value.trim();
        return normalized.isBlank() ? null : normalized;
    }

    @FunctionalInterface
    private interface SampleClassifier {
        RegionType classify(MultiRegionAnalysisService.LabeledBehaviorSample sample);
    }

    public record ComparisonReport(int k,
                                   int repeatCount,
                                   String datasetPath,
                                   String datasetSource,
                                   int trainCount,
                                   int testCount,
                                   int testSampleSizePerRun,
                                   Map<String, Integer> labelDistribution,
                                   String groundTruthField,
                                   String classifierInputFields,
                                   String evaluationProtocol,
                                   AlgorithmResult bestAlgorithm,
                                   List<AlgorithmResult> algorithms,
                                   String rankingFormula) {
    }

    private record EvaluationDataset(String sourceName,
                                     List<MultiRegionAnalysisService.LabeledBehaviorSample> trainSamples,
                                     List<MultiRegionAnalysisService.LabeledBehaviorSample> testSamples,
                                     int testSampleSizePerRun,
                                     Map<String, Integer> labelDistribution) {
    }

    public record RunMetric(int runIndex,
                            int correctPredictions,
                            int totalPredictions,
                            double accuracy,
                            double precision,
                            double recall,
                            double f1Score,
                            Map<RegionType, Map<RegionType, Integer>> confusionMatrix) {
    }

    public record AlgorithmResult(String name,
                                  String family,
                                  String methodType,
                                  int totalPredictions,
                                  int correctPredictions,
                                  double accuracy,
                                  double precision,
                                  double recall,
                                  double f1Score,
                                  double averageProcessingMs,
                                  double optimizationScore,
                                  double accuracyMin,
                                  double accuracyMax,
                                  double accuracyStdDev,
                                  int runCount,
                                  String note,
                                  Map<RegionType, Map<RegionType, Integer>> confusionMatrix,
                                  List<RunMetric> runMetrics) {

        public AlgorithmResult withOptimizationScore(double score) {
            return new AlgorithmResult(
                    name, family, methodType, totalPredictions, correctPredictions,
                    accuracy, precision, recall, f1Score, averageProcessingMs, score,
                    accuracyMin, accuracyMax, accuracyStdDev, runCount, note,
                    new LinkedHashMap<>(confusionMatrix), runMetrics
            );
        }
    }
}
