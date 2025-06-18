package br.ifsp.film_catalog.dto;

public class ReviewAveragesDTO {
    private double directionAverage;
    private double screenplayAverage;
    private double cinematographyAverage;
    private double generalAverage;

    public ReviewAveragesDTO(double directionAverage, double screenplayAverage, double cinematographyAverage, double generalAverage) {
        this.directionAverage = directionAverage;
        this.screenplayAverage = screenplayAverage;
        this.cinematographyAverage = cinematographyAverage;
        this.generalAverage = generalAverage;
    }

    public double getDirectionAverage() {
        return directionAverage;
    }

    public void setDirectionAverage(double directionAverage) {
        this.directionAverage = directionAverage;
    }

    public double getScreenplayAverage() {
        return screenplayAverage;
    }

    public void setScreenplayAverage(double screenplayAverage) {
        this.screenplayAverage = screenplayAverage;
    }

    public double getCinematographyAverage() {
        return cinematographyAverage;
    }

    public void setCinematographyAverage(double cinematographyAverage) {
        this.cinematographyAverage = cinematographyAverage;
    }

    public double getGeneralAverage() {
        return generalAverage;
    }

    public void setGeneralAverage(double generalAverage) {
        this.generalAverage = generalAverage;
    }
}