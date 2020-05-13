package com.example.newsapp;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.RecyclerView.ViewHolder;

import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;


public class RVAdapter extends RecyclerView.Adapter<RVAdapter.NewsViewHolder> {
    public static class NewsViewHolder extends ViewHolder {
        CardView newsCardView;
        ImageView newsImg;
        TextView newsTitle;
        TextView newsTime;
        TextView newsSection;
        ImageButton newsSavedBtn;
        Context thisContext;

        NewsViewHolder(View itemView, int displayType) {
            super(itemView);
            switch (displayType){
                case 1:
                    newsCardView = itemView.findViewById(R.id.newsCardViewFav);
                    newsTitle = itemView.findViewById(R.id.newsTitleFav);
                    newsImg = itemView.findViewById(R.id.newsImgFav);
                    newsTime = itemView.findViewById(R.id.newsTimeFav);
                    newsSection = itemView.findViewById(R.id.newsSectionFav);
                    newsSavedBtn = itemView.findViewById(R.id.newsSavedBtnFav);
                    thisContext = itemView.getContext();
                    break;
                default:
                    newsCardView = itemView.findViewById(R.id.newsCardView);
                    newsTitle = itemView.findViewById(R.id.newsTitle);
                    newsImg = itemView.findViewById(R.id.newsImg);
                    newsTime = itemView.findViewById(R.id.newsTime);
                    newsSection = itemView.findViewById(R.id.newsSection);
                    newsSavedBtn = itemView.findViewById(R.id.newsSavedBtn);
                    thisContext = itemView.getContext();
                    break;
            }

        }
    }

    ArrayList<News> NewsList;
    int displayType;

    public RVAdapter(ArrayList<News> NewsList, int displayType) {
        this.NewsList = NewsList;
        this.displayType = displayType;
    }

    @Override
    public NewsViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View v;
        switch (displayType) {
            case 1:
                v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.news_fav, viewGroup, false);
                break;
            default:
                v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.news, viewGroup, false);
        }
        return new NewsViewHolder(v, displayType);
    }


    @Override
    public void onBindViewHolder(final NewsViewHolder nvh, final int position) {
        nvh.newsTitle.setText(NewsList.get(position).title);
        Picasso.get().load(NewsList.get(position).image).fit().into(nvh.newsImg);

        String displayTime = NewsList.get(position).time;
        switch (displayType){
            case 1:
                displayTime = utils.calculateDisplayTime(NewsList.get(position).time, "dd MMM");
                break;
            default:
                displayTime = utils.calculateDisplayTime(NewsList.get(position).time, "diff");
                break;
        }
        nvh.newsTime.setText(displayTime);

        nvh.newsSection.setText(NewsList.get(position).section);
        try {
            utils.setBtn(nvh.thisContext, NewsList.get(position).articleId,nvh.newsSavedBtn);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        nvh.newsSavedBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    utils.toggleArticle(v.getContext(), NewsList.get(position).articleId, NewsList.get(position).newstoJson());
                    utils.setBtn(v.getContext(), NewsList.get(position).articleId,nvh.newsSavedBtn);
                    if (displayType == 1){
                        NewsList.remove(position);
                        notifyItemRemoved(position);
                        notifyDataSetChanged();
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
        nvh.newsCardView.setOnClickListener( new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(v.getContext(), ArticleActivity.class);
                intent.putExtra( "articleId" , NewsList.get(position).articleId );
                v.getContext().startActivity(intent); }
        });
        nvh.newsCardView.setOnLongClickListener( new View.OnLongClickListener(){

            @Override
            public boolean onLongClick(View v) {
                final Dialog dialog = new Dialog(v.getContext());
                dialog.setContentView(R.layout.news_dialog);

                TextView text = dialog.findViewById(R.id.title);
                text.setText(NewsList.get(position).title);

                ImageView diaImage = dialog.findViewById(R.id.image);

                Picasso.get().load(NewsList.get(position).image).fit().into(diaImage);

                ImageButton tBtn = dialog.findViewById(R.id.twitterButton);
                tBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent tweet = new Intent(Intent.ACTION_VIEW);
                        tweet.setData(Uri.parse("https://twitter.com/intent/tweet?text=Check out this Link: " + Uri.encode( NewsList.get(position).url) + "&hashtags=CSCI571NewsSearch"));//where message is your string message
                        v.getContext().startActivity(tweet);
                    }
                });

                final ImageButton bBtn = dialog.findViewById(R.id.bookmarkButton);
                try {
                    utils.setBtn(v.getContext(), NewsList.get(position).articleId,bBtn);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                final News curNews = NewsList.get(position);

                bBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        try {
                            utils.toggleArticle(v.getContext(), curNews.articleId, curNews.newstoJson());
                            utils.setBtn(v.getContext(), curNews.articleId, bBtn);
                            utils.setBtn(v.getContext(), curNews.articleId, nvh.newsSavedBtn);

                            if (displayType == 1){
                                NewsList.clear();
                                JSONArray jsonArticles = utils.getAllSavedArticle(v.getContext());
                                if (jsonArticles.length() > 0){
                                    for (int i = 0; i < jsonArticles.length(); i++) {
                                        JSONObject jsonArticle = jsonArticles.getJSONObject(i);
                                        String imgUrl = jsonArticle.getString("image");
                                        String title = jsonArticle.getString("title");
                                        String time = jsonArticle.getString("time");
                                        String section = jsonArticle.getString("section");
                                        String articleId = jsonArticle.getString("articleId");
                                        String url = jsonArticle.getString("url");
                                        NewsList.add(new News(imgUrl,title, time, section, articleId, url));
                                    }
                                }
                                //NewsList.remove(position);
                                //notifyItemRemoved(position);
                                notifyDataSetChanged();
                            }
                            //dialog.dismiss();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });

                dialog.show();

                return true;
            }
        });

    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }

    @Override
    public int getItemCount() {
        return NewsList.size();
    }

}
