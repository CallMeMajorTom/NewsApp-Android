package com.example.newsapp;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Rect;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

public class utils {
    // Bookmarks Utils
    public static void toggleArticle(Context context, String id, JSONObject jsonData) throws JSONException {
        SharedPreferences preferences = context.getSharedPreferences("fav",context.MODE_PRIVATE);
        SharedPreferences.Editor editor=preferences.edit();
        if (preferences.contains(id)) {
            // Already Saved
            editor.remove(id);
            editor.commit();
            Toast.makeText(context, "\"" + jsonData.getString("title") + "\"" + " was removed to bookmarks", Toast.LENGTH_SHORT).show();
        } else {
            // Not Saved
            editor.putString(id, jsonData.toString());
            editor.commit();
            Toast.makeText(context, "\"" + jsonData.getString("title")  + "\"" + " was added from bookmark", Toast.LENGTH_SHORT).show();
        }
    }

    public static boolean alreadySaved(Context context, String id) throws JSONException {
        SharedPreferences preferences = context.getSharedPreferences("fav",context.MODE_PRIVATE);
        if (preferences.contains(id)) {
            return true;
        } else {
            return false;
        }
    }

    public  static void setBtn(Context context, String id, ImageButton btn) throws JSONException {
        if (alreadySaved(context, id)) {
            // Already Saved
            if (btn != null) {
                btn.setImageResource(R.drawable.ic_bookmark_red_24dp);
            }
        } else {
            // Not Saved
            if (btn != null) {
                btn.setImageResource(R.drawable.ic_bookmark_border_red_24dp);
            }
        }
    }

    public static JSONArray getAllSavedArticle(Context context) throws JSONException {
        SharedPreferences preferences = context.getSharedPreferences("fav",context.MODE_PRIVATE);
        Map<String, ?> allSavedArticles = preferences.getAll();
        JSONArray jsonSavedArticles = new JSONArray();
        for (Map.Entry<String, ?> savedArticle : allSavedArticles.entrySet()) {
            Log.d("map values", savedArticle.getKey() + ": " + savedArticle.getValue().toString());
            JSONObject jsonSavedArticle = new JSONObject(savedArticle.getValue().toString());
            jsonSavedArticles.put(jsonSavedArticle);
        }
        return jsonSavedArticles;
    }


    public static String calculateDisplayTime(String time, String format) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'");
            LocalDateTime localDateTime = LocalDateTime.parse(time,formatter);
            ZoneId zoneId = ZoneId.of( "Etc/UTC" );
            ZonedDateTime zdtAtUTC = localDateTime.atZone( zoneId );
            ZonedDateTime zdtAtLA = zdtAtUTC.withZoneSameInstant( ZoneId.of( "America/Los_Angeles" ) );

            switch (format){
                case "diff":
                    ZonedDateTime zdtAtLANow = ZonedDateTime.now();
                    Duration diff = Duration.between(zdtAtLA,  zdtAtLANow);
                    String displayTime = "";
                    if (diff.toDays() >= 1){
                        displayTime = diff.toDays() + "d ago";
                    } else if (diff.toHours() >= 1){
                        displayTime = diff.toHours() + "h ago";
                    } else if(diff.toMinutes() >= 1){
                        displayTime = diff.toMinutes() + "m ago";
                    } else{
                        displayTime = diff.getSeconds() + "s ago";
                    }
                    return displayTime;
                default:
                    return DateTimeFormatter.ofPattern(format).format(zdtAtLA);

            }

        }
        return time;
    }

    // Layout Decoration
    public static class SimplePaddingDecoration extends RecyclerView.ItemDecoration {

        public SimplePaddingDecoration(Context context) {
        }

        @Override
        public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
            super.getItemOffsets(outRect, view, parent, state);
            outRect.top = Integer.parseInt("10");
            outRect.bottom = Integer.parseInt("10");
        }
    }

}
