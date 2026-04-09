package com.example.calorite; // Sesuaikan dengan package kamu

import java.io.Serializable;

public class Recipe implements Serializable {
    private String title;
    private String calories;
    private String protein;
    private String details;

    public Recipe(String title, String calories, String protein, String details) {
        this.title = title;
        this.calories = calories;
        this.protein = protein;
        this.details = details;
    }

    public String getTitle() { return title; }
    public String getCalories() { return calories; }
    public String getProtein() { return protein; }
    public String getDetails() { return details; }
}