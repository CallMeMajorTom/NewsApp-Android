package com.example.newsapp;

import android.content.Context;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.provider.SyncStateContract;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
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
import com.google.android.material.tabs.TabLayout;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class FragmentHeadlines extends Fragment {

    private static final String[] tabTitles = new String[]{"world", "business", "politics", "sport", "technology", "science"};
    private TabLayout headlinesTab ;
    private RecyclerView headlinesRV;
    private ConstraintLayout progressBar;
    private SwipeRefreshLayout newsListSwipLayout;

    private String currentTag = "world";
    private ArrayList<News> headlines =  new ArrayList<News>();
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_headlines, container, false);
        // Find views
        headlinesTab = view.findViewById(R.id.headlinesTab);
        headlinesRV = view.findViewById(R.id.newsListRV);
        progressBar = view.findViewById(R.id.progressBarLayout);
        newsListSwipLayout = view.findViewById(R.id.newsListSwipLayout);

        //Progress Bar
        newsListSwipLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                requestHeadlines(currentTag);
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

        for (int i = 0; i < tabTitles.length; i++) {
            headlinesTab.addTab(headlinesTab.newTab().setText(tabTitles[i]).setTag(i));
        }

        final LinearLayoutManager layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(getContext(),
                layoutManager.getOrientation());
        headlinesRV.setLayoutManager(layoutManager);
        headlinesRV.addItemDecoration(dividerItemDecoration);
        headlinesRV.addItemDecoration(new utils.SimplePaddingDecoration(getContext()));

        headlinesTab.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                currentTag = tab.getText().toString();
                layoutManager.scrollToPositionWithOffset(tab.getPosition(), 0);
                requestHeadlines(currentTag);
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
        requestHeadlines(currentTag);
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        requestHeadlines(currentTag);
    }

    private void requestHeadlines(String tab) {
        String url = "http://ec2-54-88-75-29.compute-1.amazonaws.com:4000/" + tab + "?isGuardian=true";
        RequestQueue responseQueue = Volley.newRequestQueue(getContext());
        headlines.clear();
        progressBar.setVisibility(View.VISIBLE);
        progressBar.invalidate();
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
                                headlines.add(new News(res.getString("img"),
                                        res.getString("title"),
                                        res.getString("date"),
                                        res.getString("section"),
                                        res.getString("id"),
                                        res.getString("url")));
                            }
                            RVAdapter adapter = new RVAdapter(headlines, 0);
                            headlinesRV.setAdapter(adapter);
                            progressBar.setVisibility(View.GONE);
                        } catch (JSONException e){
                            e.printStackTrace();
                            Log.d("Headlines Error", e.toString());
                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d("Headlines Error", error.toString());
                    }
        });

        responseQueue.add(jsonObjectRequest);
    }

}
