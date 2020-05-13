package com.example.newsapp;

import android.content.Context;
import android.graphics.Rect;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class FragmentBookmarks extends Fragment {

    private TextView emptyMsg;
    private RecyclerView savedArticleRV;
    private RVAdapter adapter;

    //LatestNews
    public ArrayList<News> savedArticlesList = new ArrayList<News>();
    private RecyclerView.AdapterDataObserver mObserver;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_bookmarks, container, false);
        emptyMsg = view.findViewById(R.id.emptyMsg);
        emptyMsg.setVisibility(View.GONE);
        emptyMsg.invalidate();
        savedArticleRV = view.findViewById(R.id.savedArticle);
        adapter = new RVAdapter(savedArticlesList,1);
        setupEmptyViewObserver(adapter);
        savedArticleRV.setAdapter(adapter);
        try {
            showSavedNews();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        try {
            requestSavedNews();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void showSavedNews() throws JSONException {
        GridLayoutManager layoutManager = new GridLayoutManager(getContext(), 2  );
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(getContext(),
                layoutManager.getOrientation());


        savedArticleRV.setLayoutManager(layoutManager);
        savedArticleRV.addItemDecoration(dividerItemDecoration);
        savedArticleRV.addItemDecoration(new utils.SimplePaddingDecoration(getContext()));

        requestSavedNews();
    }

    private void requestSavedNews() throws JSONException {
        savedArticlesList.clear();
        JSONArray jsonArticles = utils.getAllSavedArticle(getContext());
        if (jsonArticles.length() > 0){
            for (int i = 0; i < jsonArticles.length(); i++) {
                JSONObject jsonArticle = jsonArticles.getJSONObject(i);
                String imgUrl = jsonArticle.getString("image");
                String title = jsonArticle.getString("title");
                String time = jsonArticle.getString("time");
                String section = jsonArticle.getString("section");
                String articleId = jsonArticle.getString("articleId");
                String url = jsonArticle.getString("url");
                savedArticlesList.add(new News(imgUrl,title, time, section, articleId, url));
                adapter.notifyDataSetChanged();
            }
        } else {
            emptyMsg.setVisibility(View.VISIBLE);
            emptyMsg.invalidate();
        }
    }

    private void setupEmptyViewObserver(final RecyclerView.Adapter paramAdapter)
    {
        if (paramAdapter != null)
        {
            if (this.mObserver != null)
            {
                paramAdapter.unregisterAdapterDataObserver(this.mObserver);
                this.mObserver = null;
            }
            this.mObserver = new RecyclerView.AdapterDataObserver()
            {
                public final void onChanged()
                {
                    if (paramAdapter.getItemCount() == 0){
                        emptyMsg.setVisibility(View.VISIBLE);
                        emptyMsg.invalidate();
                    }
                }
            };
            paramAdapter.registerAdapterDataObserver(this.mObserver);
        }
    }

}
