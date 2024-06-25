package com.example.schedule;

public class NewsItem {
    private String date;
    private String title;
    private String summary;
    private String imageUrl;

    public NewsItem(String date, String title, String summary, String imageUrl) {
        this.date = date;
        this.title = title;
        this.summary = summary;
        this.imageUrl = imageUrl;
    }

    public String getDate() {
        return date;
    }

    public String getTitle() {
        return title;
    }

    public String getSummary() {
        return summary;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    @Override
    public String toString() {
        return "Date: " + date + "\nTitle: " + title + "\nSummary: " + summary + "\nImage URL: " + imageUrl + "\n";
    }
}
