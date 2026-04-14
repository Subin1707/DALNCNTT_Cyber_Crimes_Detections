package com.example.servingwebcontent.dto;

import java.util.List;

public class SessionFeatureVectorDTO {

    private final int numEmails;
    private final int numIps;
    private final int numUrls;
    private final int numDomains;
    private final int numSharedIps;
    private final int numRepeatedUrls;
    private final int numHighRiskNodes;
    private final int numMediumRiskNodes;
    private final int numIndicators;

    public SessionFeatureVectorDTO(int numEmails,
                                   int numIps,
                                   int numUrls,
                                   int numDomains,
                                   int numSharedIps,
                                   int numRepeatedUrls,
                                   int numHighRiskNodes,
                                   int numMediumRiskNodes,
                                   int numIndicators) {

        this.numEmails = Math.max(0, numEmails);
        this.numIps = Math.max(0, numIps);
        this.numUrls = Math.max(0, numUrls);
        this.numDomains = Math.max(0, numDomains);
        this.numSharedIps = Math.max(0, numSharedIps);
        this.numRepeatedUrls = Math.max(0, numRepeatedUrls);
        this.numHighRiskNodes = Math.max(0, numHighRiskNodes);
        this.numMediumRiskNodes = Math.max(0, numMediumRiskNodes);
        this.numIndicators = Math.max(0, numIndicators);
    }

    public int getNumEmails() {
        return numEmails;
    }

    public int getNumIps() {
        return numIps;
    }

    public int getNumUrls() {
        return numUrls;
    }

    public int getNumDomains() {
        return numDomains;
    }

    public int getNumSharedIps() {
        return numSharedIps;
    }

    public int getNumRepeatedUrls() {
        return numRepeatedUrls;
    }

    public int getNumHighRiskNodes() {
        return numHighRiskNodes;
    }

    public int getNumMediumRiskNodes() {
        return numMediumRiskNodes;
    }

    public int getNumIndicators() {
        return numIndicators;
    }

    public List<Double> toNumericVector() {
        return List.of(
                (double) numEmails,
                (double) numIps,
                (double) numUrls,
                (double) numDomains,
                (double) numSharedIps,
                (double) numRepeatedUrls,
                (double) numHighRiskNodes,
                (double) numMediumRiskNodes,
                (double) numIndicators
        );
    }
}
