# KNN Architecture & Data Flow

## System Architecture Diagram

```
┌─────────────────────────────────────────────────────────────────┐
│                    CYBER CRIMES DETECTION SYSTEM                 │
├─────────────────────────────────────────────────────────────────┤
│                                                                  │
│  ┌──────────────────┐                                           │
│  │   GRAPH DATA     │                                           │
│  │  (Neo4j Store)   │                                           │
│  │                  │                                           │
│  │ • Historical     │                                           │
│  │   Sessions       │                                           │
│  │ • Fraud Labels   │                                           │
│  │ • Risk Scores    │                                           │
│  └────────┬─────────┘                                           │
│           │                                                      │
│           ▼                                                      │
│  ┌──────────────────────────────────────────────────┐          │
│  │   SessionFeatureService                          │          │
│  │   ┌────────────────────────────────────────┐    │          │
│  │   │ • Extract Features                     │    │          │
│  │   │ • Load Historical Samples              │    │          │
│  │   │ • Feature Vector: [8 dimensions]       │    │          │
│  │   └────────────────────────────────────────┘    │          │
│  └────────┬──────────────────────────────────────┬─┘          │
│           │                                      │               │
│           │ Current Session                     │               │
│           │ + Historical Samples                │               │
│           ▼                                      ▼               │
│    ┌─────────────────┐            ┌───────────────────────────┐│
│    │  Current User   │            │ KNNEnhancedAnalysisService││
│    │  Features       │            │                           ││
│    │                 │            │ ┌─────────────────────┐   ││
│    │ [5, 12, 3, 2]   │            │ │ THREE METRICS:      │   ││
│    │ [T, F, T]       │────────────►│ │                    │   ││
│    │                 │            │ │ 1️⃣ EUCLIDEAN      │   ││
│    └─────────────────┘            │ │    sqrt(Σ(diffs)²) │   ││
│                                    │ │                    │   ││
│                                    │ │ 2️⃣ MINKOWSKI      │   ││
│                                    │ │    (Σ|diffs|^p)^1/p│   ││
│                                    │ │                    │   ││
│                                    │ │ 3️⃣ HAMMING        │   ││
│                                    │ │    count(diffs)    │   ││
│                                    │ └─────────────────────┘   ││
│                                    │           │               ││
│                                    │ Find K=7 Nearest Neighbors│
│                                    │ for each metric           ││
│                                    │           │               ││
│                                    │ Weighted Voting           ││
│                                    │ (Inverse Distance)        ││
│                                    │           │               ││
│                                    │ Score Reconciliation      ││
│                                    │ • Euclidean Score        ││
│                                    │ • Minkowski Score        ││
│                                    │ • Hamming Score          ││
│                                    │ • Confidence Level       ││
│                                    │ • Divergence Detection   ││
│                                    └───────────────┬───────────┘│
│                                                   │              │
│                                                   ▼              │
│                                    ┌──────────────────────────┐ │
│                                    │  KNNAnalysisResult       │ │
│                                    │                          │ │
│                                    │ • euclideanScore: 85%    │ │
│                                    │ • minkowskiScore: 82%    │ │
│                                    │ • hammingScore: 88%      │ │
│                                    │ • finalScore: 85%        │ │
│                                    │ • confidence: 91.4%      │ │
│                                    │ • recommendation: ⚠️     │ │
│                                    │ • details: [...]         │ │
│                                    └──────────────┬───────────┘ │
│                                                   │              │
│                                                   ▼              │
│                                    ┌──────────────────────────┐ │
│                                    │ HybridRiskScoringService │ │
│                                    │                          │ │
│                                    │ Final Score Calculation: │ │
│                                    │ = 0.4 × Rule Score      │ │
│                                    │ + 0.3 × KNN Score       │ │
│                                    │ + 0.3 × Bayes Score     │ │
│                                    └──────────────┬───────────┘ │
│                                                   │              │
│                                                   ▼              │
│                                    ┌──────────────────────────┐ │
│                                    │ HybridRiskScoreDTO       │ │
│                                    │ + KNN Analysis Details   │ │
│                                    └──────────────┬───────────┘ │
│                                                   │              │
│                                                   ▼              │
│                                    ┌──────────────────────────┐ │
│                                    │ DecisionService          │ │
│                                    │                          │ │
│                                    │ Make Decision:           │ │
│                                    │ • BLOCK (score ≥ 70)    │ │
│                                    │ • MONITOR (40-69)       │ │
│                                    │ • ALLOW (< 40)          │ │
│                                    └──────────────┬───────────┘ │
│                                                   │              │
└───────────────────────────────────────────────────┼──────────────┘
                                                    │
                                                    ▼
                                    ┌──────────────────────────┐
                                    │  API Response            │
                                    │ /admin/node-analysis     │
                                    │                          │
                                    │ {                        │
                                    │   verdict: "GIAN LẬN"   │
                                    │   riskScore: 75          │
                                    │   riskLevel: "HIGH"      │
                                    │   decision: "BLOCK"      │
                                    │   indicators: [...]      │
                                    │ }                        │
                                    └──────────────────────────┘
```

---

## Feature Extraction Process

```
SESSION DATA
├─ emails: 5
├─ ips: 12
├─ urls: 8
├─ domains: 3
├─ sharedIps: 2 ──► true
├─ repeatedUrls: 1 ──► true
├─ highRiskNodes: 3 ──► true
└─ mediumRiskNodes: 2

                │
                ▼

NUMERIC VECTOR              BOOLEAN VECTOR
┌───────────────────────┐  ┌──────────────────┐
│ [5, 12, 8, 3]        │  │ [true, true, true]│
│ for Euclidean &      │  │ for Hamming      │
│ Minkowski metrics    │  │ metric           │
└───────────────────────┘  └──────────────────┘
```

---

## Distance Calculation Example

### Scenario: Comparing Current User with Historical Fraudster

```
CURRENT USER:     [5 IPs,   12 URLs,  8 Domains,  3]
HISTORICAL FRAUD: [4 IPs,   10 URLs,  7 Domains,  2]
                  ────────────────────────────────────

EUCLIDEAN:
  d = sqrt((5-4)² + (12-10)² + (8-7)² + (3-2)²)
  d = sqrt(1 + 4 + 1 + 1)
  d = sqrt(7)
  d ≈ 2.65  ──► Score: HIGH similarity

MINKOWSKI (p=2):
  d = (|5-4|² + |12-10|² + |8-7|² + |3-2|²)^(1/2)
  d = (1 + 4 + 1 + 1)^0.5
  d ≈ 2.65  ──► Same as Euclidean when p=2

HAMMING:
  Current:  [VPN=true,  Blacklist=false, SpamEmail=false]
  History:  [VPN=true,  Blacklist=true,  SpamEmail=true ]
            ──────────────────────────────────────────────
  Differences: 2 positions differ
  d = 2  ──► HIGH risk (security flags mismatch)
```

---

## K-Nearest Neighbors Selection (K=7)

```
STEP 1: Calculate distances to all historical samples

Sample   Euclidean  Minkowski  Hamming  IsFraud
────────────────────────────────────────────────
1        0.5        0.5        0        NO
2        1.2        1.1        1        NO
3        2.1        2.0        2        YES    ◄── Close!
4        2.3        2.2        2        YES    ◄── Close!
5        2.5        2.4        1        YES    ◄── Close!
6        2.7        2.6        3        YES    ◄── Close!
7        3.1        3.0        2        YES    ◄── Close!
8        3.5        3.4        1        NO
9        4.2        4.1        0        NO
10       5.0        4.9        2        NO

STEP 2: Sort by distance and select K=7 nearest

For Euclidean:  [Sample1(0.5), Sample2(1.2), Sample3(2.1), Sample4(2.3), 
                 Sample5(2.5), Sample6(2.7), Sample7(3.1)]

For Minkowski:  [Sample1(0.5), Sample2(1.1), Sample3(2.0), Sample4(2.2), 
                 Sample5(2.4), Sample6(2.6), Sample7(3.0)]

For Hamming:    [Sample1(0), Sample3(2), Sample4(2), Sample5(1),
                 Sample6(3), Sample7(2), Sample8(1)]

STEP 3: Weighted voting for fraud probability

For Euclidean:
  weight(S1) = 1/(0.5+0.001) = 1.998
  weight(S2) = 1/(1.2+0.001) = 0.832
  ...
  totalWeight = sum of all weights
  fraudWeight = sum of weights where IsFraud=YES
  score = fraudWeight / totalWeight = 0.85 (85%)
```

---

## Confidence Assessment

```
METRIC SCORES:
├─ Euclidean:  85.5%
├─ Minkowski:  82.3%
└─ Hamming:    88.2%

DIVERGENCE CALCULATION:
  max = 88.2%
  min = 82.3%
  divergence = 88.2% - 82.3% = 5.9%

CONFIDENCE CALCULATION:
  confidence = 1.0 - (divergence × 2.5)
  confidence = 1.0 - (0.059 × 2.5)
  confidence = 1.0 - 0.1475
  confidence = 0.8525 ≈ 85.3%

INTERPRETATION:
  85.3% confidence means:
  "We are 85% sure the metrics agree"
  
  "If divergence > 30%, confidence < 0% ──► ANOMALY DETECTED"
  "If divergence < 5%, confidence > 87.5% ──► HIGH CONFIDENCE"
```

---

## Safety-First Decision Logic

```
FINAL SCORE CALCULATION:

if any_metric > 80%:
   ┌────────────────────────────────┐
   │ USE MAXIMUM SCORE FOR SAFETY   │
   │ finalScore = max(85, 82, 88)   │
   │ finalScore = 88%               │
   │                                │
   │ Reasoning:                     │
   │ If ANY detector says "DANGER", │
   │ trust it to avoid false negatives│
   └────────────────────────────────┘
else:
   ┌────────────────────────────────┐
   │ USE AVERAGE SCORE              │
   │ finalScore = (E+M+H) / 3       │
   │ finalScore = 85.33%            │
   └────────────────────────────────┘

WHY SAFETY-FIRST?
  ❌ False Negative (miss attacker)  = CATASTROPHIC
  ✅ False Positive (block innocent) = INCONVENIENT

Therefore: When in doubt, block! 🛡️
```

---

## Anomaly Detection Example

```
SCENARIO: Obfuscated Attack

Attacker pretends to be normal user:
├─ Normal IP patterns (5 IPs, similar to legit users)
├─ Normal URL patterns (12 URLs, typical behavior)
└─ BUT uses VPN, appears on blacklist, sends spam emails

METRIC RESULTS:
├─ Euclidean:  15% (numeric patterns look normal) ✓
├─ Minkowski:  20% (multi-dimensional looks normal) ✓
└─ Hamming:    92% (security flags SCREAM DANGER) ⚠️

DIVERGENCE:
  divergence = 92% - 15% = 77%
  confidence = 1.0 - (0.77 × 2.5) = 0.075 (7.5%)

DETECTION:
  ⚠️ WARNING: Significant divergence detected!
  
  This pattern suggests:
  ✓ Normal numeric behavior (tried to evade)
  ✗ Extreme security flags (failed to hide)
  
  Recommendation: INVESTIGATE for obfuscated attack
  
  System still blocks because:
  max(15, 20, 92) = 92% > 80%
  ──► Use safety-first: finalScore = 92% ──► BLOCK
```

---

## Performance Timeline

```
┌─────────────────────────────────────────────────────────────┐
│ Time (milliseconds) │ Operation                             │
├─────────────────────────────────────────────────────────────┤
│ 0-1ms               │ Feature Extraction                   │
│                     │ • Read session data                  │
│                     │ • Convert to vectors                 │
├─────────────────────────────────────────────────────────────┤
│ 1-3ms               │ Load Historical Samples              │
│                     │ • Query Neo4j (200 samples)         │
│                     │ • Deserialize                        │
├─────────────────────────────────────────────────────────────┤
│ 3-13ms              │ Distance Calculations                │
│                     │ • Euclidean × 200 samples           │
│                     │ • Minkowski × 200 samples           │
│                     │ • Hamming × 200 samples             │
├─────────────────────────────────────────────────────────────┤
│ 13-18ms             │ KNN Selection & Voting               │
│                     │ • Sort by distance                   │
│                     │ • Weighted voting                    │
│                     │ • Confidence calculation             │
├─────────────────────────────────────────────────────────────┤
│ TOTAL: ~16-20ms     │ Ready to return result               │
└─────────────────────────────────────────────────────────────┘

✅ Well within real-time SLA (<100ms per request)
```

---

## Integration with Hybrid Scoring

```
HYBRID RISK SCORING FORMULA:

finalScore = (0.4 × Rule) + (0.3 × KNN) + (0.3 × Bayes)
           = (0.4 × 60) + (0.3 × 85) + (0.3 × 78)
           = 24 + 25.5 + 23.4
           = 72.9%

WHERE:
├─ Rule Score (40%): Graph-based rules, blacklists
├─ KNN Score (30%):  Historical behavior comparison
│                    (now enhanced with 3 metrics)
└─ Bayes Score (30%): Probabilistic analysis

RESULT:
  riskScore = 72.9% ──► HIGH (≥70%)
  decision = "BLOCK"
  
WITH KNN DETAILS VISIBLE:
  ├─ Euclidean Score: 85%
  ├─ Minkowski Score: 82%
  ├─ Hamming Score: 88%
  ├─ Confidence: 91.4%
  └─ Recommendation: ⚠️ HIGH
```

---

## Conclusion

The KNN system provides:

1. **Three Independent Metrics** checking simultaneously
2. **Confidence Scoring** showing metric agreement
3. **Anomaly Detection** when metrics diverge
4. **Safety-First Logic** prioritizing fraud detection
5. **Real-Time Performance** (~16ms per analysis)
6. **Clear Explainability** showing numeric vs security assessment

This makes the cyber crimes detection system significantly more robust and harder to evade.
