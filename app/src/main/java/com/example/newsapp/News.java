package com.example.newsapp;

import org.json.JSONException;
import org.json.JSONObject;

class News {
    String image;
    String title;
    String time;
    String section;
    String articleId;
    String url;

    News(String image, String title, String time, String section, String articleId, String url) {
        this.image = image;
        this.title = title;
        this.time = time;
        this.section = section;
        this.articleId = articleId;
        this.url = url;
    }

    public JSONObject newstoJson() throws JSONException {
        JSONObject jsonData = new JSONObject();
        jsonData.put("image", this.image);
        jsonData.put("title", this.title);
        jsonData.put("time", this.time);
        jsonData.put("section", this.section);
        jsonData.put("articleId", this.articleId);
        jsonData.put("url", this.url);
        return jsonData;
    }
}