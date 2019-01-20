/*
 * PROSIM (PROduct SIMilarity): backend engine for comparing OpenFoodFacts products 
 * by pairs based on their score (Nutrition Score, Nova Classification, etc.).
 * Results are stored in a Mongo-Database.
 *
 * Url: https://offmatch.blogspot.com/
 * Author/Developer: Olivier Richard (oric_dev@iznogoud.neomailbox.ch)
 * License: GNU Affero General Public License v3.0
 * License url: https://github.com/oricdev/prosim/blob/master/LICENSE
 */
package org.openfoodfacts.databases;

import com.google.gson.internal.LinkedTreeMap;

/**
 *
 * Matches fields in db_stats.json (off_graph) and partially
 * those from ComputingInstance.java
 */
public class ProsimDb {

    private Boolean isActive;
    private Boolean keepPrivate;
    private Boolean isInError = false;
    private String errorMessage;

    private String dbNickname;
    private String dbDisplayName;
    private String dbName;
    private String dbSummary;
    private String dbDescriptionEn;
    private String dbDescription;
    private Double dbMaxSize;
    private Short similarityMinPercentage;
    // Graph-related data
    private Short scoreMinValue;
    private Short scoreMaxValue;
    private String scoreLabelYAxis;
    private Boolean bottomUp;
    private Short scoreNbIntervals;
    private String[] scoreIntervalsStripeColour;
    private String[] scoreIntervalsLabels;
    //Owner-related data
    private String owner;
    private String emailOwner;
    private Boolean emailVisible;
    //Other data
    private String linkComputingInstance;
    private String statsProsim;
    //Updatable fields (by the daily running task 'dbStats')
    private Double dbSize;
    private Long nbProductsExtracted;
    private Long nbProducts;
    private Float progression;
    private Long nbIntersections;

    public ProsimDb(Object obj) {
        System.out.println(obj.toString());
    }

    public ProsimDb(LinkedTreeMap l) {
        System.out.println(l.toString());
    }

    public ProsimDb(Boolean isActive,
            Boolean keepPrivate,
            // Database-related data
            String dbNickname,
            String dbDisplayName,
            String dbName,
            String dbSummary,
            String dbDescriptionEn,
            String dbDescription,
            Double dbMaxSize,
            Short similarityMinPercentage,
            // Graph-related data
            Short scoreMinValue,
            Short scoreMaxValue,
            String scoreLabelYAxis,
            Boolean bottomUp,
            Short scoreNbIntervals,
            String[] scoreIntervalsStripeColour,
            String[] scoreIntervalsLabels,
            // Owner-related data
            String owner,
            String emailOwner,
            Boolean emailVisible,
            // Other data
            String linkComputingInstance,
            String statsProsim,
            // Updatable statistics fields (by the daily running task 'dbStats')
            Double dbSize,
            Long nbProductsExtracted,
            Long nbProducts,
            Float progression,
            Long nbIntersections
    ) {
        this.isInError = false;
        this.errorMessage = "";
        this.isActive = isActive;
        this.keepPrivate = keepPrivate;

        this.dbNickname = dbNickname;
        this.dbDisplayName = dbDisplayName;
        this.dbName = dbName;
        this.dbSummary = dbSummary;
        this.dbDescriptionEn = dbDescriptionEn;
        this.dbDescription = dbDescription;
        this.dbMaxSize = dbMaxSize;
        this.similarityMinPercentage = similarityMinPercentage;
        // Graph
        this.scoreMinValue = scoreMinValue;
        this.scoreMaxValue = scoreMaxValue;
        this.scoreLabelYAxis = scoreLabelYAxis;
        this.bottomUp = bottomUp;
        this.scoreNbIntervals = scoreNbIntervals;
        this.scoreIntervalsStripeColour = scoreIntervalsStripeColour;
        this.scoreIntervalsLabels = scoreIntervalsLabels;
        // Owner
        this.owner = owner;
        this.emailOwner = emailOwner;
        this.emailVisible = emailVisible;
        // Other
        this.linkComputingInstance = linkComputingInstance;
        this.statsProsim = statsProsim;
        // Updatable statistics fields
        this.dbSize = dbSize;
        this.nbProductsExtracted = nbProductsExtracted;
        this.nbProducts = nbProducts;
        this.progression = progression;
        this.nbIntersections = nbIntersections;
    }

    /*
    * GETTERS & SETTERS
    */
    
    public Boolean getIsInError() {
        return this.isInError;
    }

    public void setIsInError(Boolean isInError) {
        this.isInError = isInError;
    }

    public String getErrorMessage() {
        return this.errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public String getDbName() {
        return this.dbName;
    }

    public void setDbName(String dbName) {
        this.dbName = dbName;
    }

    public String getDbNickname() {
        return this.dbNickname;
    }

    public void setDbNickname(String dbNickname) {
        this.dbNickname = dbNickname;
    }

    public Boolean getIsActive() {
        return this.isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    public Boolean getKeepPrivate() {
        return this.keepPrivate;
    }

    public void setKeepPrivate(Boolean keepPrivate) {
        this.keepPrivate = keepPrivate;
    }

    public Short getSimilarityMinPercentage() {
        return this.similarityMinPercentage;
    }

    public void setSimilarityMinPercentage(Short similarityMinPercentage) {
        this.similarityMinPercentage = similarityMinPercentage;
    }

    public Short getScoreMinValue() {
        return this.scoreMinValue;
    }

    public void setScoreMinValue(Short scoreMinValue) {
        this.scoreMinValue = scoreMinValue;
    }

    public Short getScoreMaxValue() {
        return this.scoreMaxValue;
    }

    public void setScoreMaxValue(Short scoreMaxValue) {
        this.scoreMaxValue = scoreMaxValue;
    }

    public Boolean getBottomUp() {
        return this.bottomUp;
    }

    public void setBottomUp(Boolean bottomUp) {
        this.bottomUp = bottomUp;
    }

    public String getDbSummary() {
        return this.dbSummary;
    }

    public void setDbSummary(String dbSummary) {
        this.dbSummary = dbSummary;
    }

    public String getDbDescriptionEn() {
        return this.dbDescriptionEn;
    }

    public void setDbDescriptionEn(String dbDescriptionEn) {
        this.dbDescriptionEn = dbDescriptionEn;
    }

    public String getDbDescription() {
        return this.dbDescription;
    }

    public void setDbDescription(String dbDescription) {
        this.dbDescription = dbDescription;
    }

    public Double getDbMaxSize() {
        return this.dbMaxSize;
    }

    public void setDbMaxSize(Double dbMaxSize) {
        this.dbMaxSize = dbMaxSize;
    }

    public String getOwner() {
        return this.owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public String getEmailOwner() {
        return this.emailOwner;
    }

    public void setEmailOwner(String emailOwner) {
        this.emailOwner = emailOwner;
    }

    public Boolean getEmailVisible() {
        return this.emailVisible;
    }

    public void setEmailVisible(Boolean emailVisible) {
        this.emailVisible = emailVisible;
    }

    public String getLinkComputingInstance() {
        return this.linkComputingInstance;
    }

    public void setLinkComputingInstance(String linkComputingInstance) {
        this.linkComputingInstance = linkComputingInstance;
    }

    public String getStatsProsim() {
        return this.statsProsim;
    }

    public void setStatsProsim(String statsProsim) {
        this.statsProsim = statsProsim;
    }

    public String getDbDisplayName() {
        return this.dbDisplayName;
    }

    public void setDbDisplayName(String dbDisplayName) {
        this.dbDisplayName = dbDisplayName;
    }

    public String getScoreLabelYAxis() {
        return this.scoreLabelYAxis;
    }

    public void setScoreLabelYAxis(String scoreLabelYAxis) {
        this.scoreLabelYAxis = scoreLabelYAxis;
    }

    public Short getScoreNbIntervals() {
        return this.scoreNbIntervals;
    }

    public void setScoreNbIntervals(Short scoreNbIntervals) {
        this.scoreNbIntervals = scoreNbIntervals;
    }

    public String[] getScoreIntervalsStripeColour() {
        return this.scoreIntervalsStripeColour;
    }

    public void setScoreIntervalsStripeColour(String[] scoreIntervalsStripeColour) {
        this.scoreIntervalsStripeColour = scoreIntervalsStripeColour;
    }

    public String[] getScoreIntervalsLabels() {
        return this.scoreIntervalsLabels;
    }

    public void setScoreIntervalsLabels(String[] scoreIntervalsLabels) {
        this.scoreIntervalsLabels = scoreIntervalsLabels;
    }

    public Double getDbSize() {
        return this.dbSize;
    }

    public void setDbSize(Double dbSize) {
        this.dbSize = dbSize;
    }

    public Long getNbProductsExtracted() {
        return this.nbProductsExtracted;
    }

    public void setNbProductsExtracted(Long nbProductsExtracted) {
        this.nbProductsExtracted = nbProductsExtracted;
    }

    public Long getNbProducts() {
        return this.nbProducts;
    }

    public void setNbProducts(Long nbProducts) {
        this.nbProducts = nbProducts;
    }

    public Float getProgression() {
        return this.progression;
    }

    public void setProgression(Float progression) {
        this.progression = progression;
    }

    public Long getNbIntersections() {
        return this.nbIntersections;
    }

    public void setNbIntersections(Long nbIntersections) {
        this.nbIntersections = nbIntersections;
    }
}
