package com.example.schedule;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class NewsScraper {
    private static final String URL = "https://college.ks.ua/";

    public static List<NewsItem> fetchNews() throws IOException {
        List<NewsItem> newsList = new ArrayList<>();
        newsList.addAll(fetchNewsFromPage(URL));
        newsList.addAll(fetchNewsFromPage(URL + "?PAGEN_2=2"));

        return newsList; // Возвращаем все новости без фильтрации
    }

    private static List<NewsItem> fetchNewsFromPage(String url) throws IOException {
        Document doc = Jsoup.connect(url).get();
        Elements newsItems = doc.select("div.news_item");

        List<NewsItem> newsList = new ArrayList<>();
        for (Element newsItem : newsItems) {
            String dateText = newsItem.select("div.news_item__header--date").text();
            String summary = newsItem.select("div.news_item__content p").text();
            String imageUrl = newsItem.select("div.news_item__content img").attr("src");
            if (!imageUrl.startsWith("http")) {
                imageUrl = URL + imageUrl;
            }
            String title = ""; // На вашем сайте не указан явный заголовок, используйте часть текста или добавьте по необходимости

            newsList.add(new NewsItem(dateText, title, summary, imageUrl));
        }

        return newsList;
    }
}
