package com.example.newsapp;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


import java.util.ArrayList;

public class SearchActivity extends AppCompatActivity {
    private String keyword;
    private ArrayList<News> searchNews =  new ArrayList<>();
    private RecyclerView searchNewsRV;
    private ConstraintLayout progressBar;
    private SwipeRefreshLayout newsListSwipLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        // Get the Intent that started this activity and extract the string
        Intent intent = getIntent();
        keyword = intent.getStringExtra("keyword");

        //Set Toolbars
        ActionBar searchToolbar = getSupportActionBar();
        searchToolbar.setBackgroundDrawable(new ColorDrawable(Color.WHITE));
        searchToolbar.setTitle("Search Results for " + keyword);
        searchToolbar.setDisplayHomeAsUpEnabled(true);

        // Find Views
        searchNewsRV =  findViewById(R.id.newsListRV);
        newsListSwipLayout = findViewById(R.id.newsListSwipLayout);
        progressBar =  findViewById(R.id.progressBarLayout);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        searchNewsRV.setLayoutManager(layoutManager);

        //Progress Bar
        newsListSwipLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                requestSearchNews();
                final Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if(newsListSwipLayout.isRefreshing()) {
                            newsListSwipLayout.setRefreshing(false);
                        }
                    }
                }, 1000);
            }
        });

        requestSearchNews();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        getFragmentManager().popBackStack("MainActivity", 0);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                // app icon in action bar clicked; go home
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    void requestSearchNews(){
        String url = "http://ec2-54-88-75-29.compute-1.amazonaws.com:4000/search?keyword=" + keyword + "&isGuardian=true";
        RequestQueue responseQueue = Volley.newRequestQueue(this);

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            for (int i = 0; i < response.length(); i++) {
                                JSONObject res =  response.getJSONObject(String.valueOf(i));
                                searchNews.add(new News(res.getString("img"),
                                        res.getString("title"),
                                        res.getString("date"),
                                        res.getString("section"),
                                        res.getString("id"),
                                        res.getString("url")));
                            }

                            RVAdapter adapter = new RVAdapter(searchNews, 0);
                            searchNewsRV.setAdapter(adapter);
                            progressBar.setVisibility(View.GONE);
                        } catch (JSONException e){
                            e.printStackTrace();
                            Log.d("Search Error", e.toString());
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("Search Error", error.toString());
            }
        });
        responseQueue.add(jsonObjectRequest);

    }

}
