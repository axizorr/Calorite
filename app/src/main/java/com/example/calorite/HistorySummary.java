package com.example.calorite; // Sesuaikan dengan package kamu

public class HistorySummary {
    private String date;
    private String totalCalories;
    private String percentage;

    private String imageBase64;

    public HistorySummary(String date, String totalCalories, String percentage, String imageBase64) {
        this.date = date;
        this.totalCalories = totalCalories;
        this.percentage = percentage;
        this.imageBase64 = imageBase64;
    }

    public String getDate() { return date; }
    public String getTotalCalories() { return totalCalories; }
    public String getPercentage() { return percentage; }

    public String getImageBase64() {
        return imageBase64;
    }
}