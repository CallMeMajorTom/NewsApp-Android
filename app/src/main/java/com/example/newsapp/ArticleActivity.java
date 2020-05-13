package com.example.newsapp;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.text.HtmlCompat;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Browser;
import android.text.Html;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.regex.Pattern;

public class ArticleActivity extends AppCompatActivity {

    private ScrollView article_layout;
    private ImageView article_img;
    private TextView article_title;
    private TextView article_section;
    private TextView article_date;
    private TextView article_desc;
    private TextView article_url;
    private ImageButton article_isSaved;
    private ImageButton article_share;

    private News news;
    private String article_ID;

    private ConstraintLayout progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_article);

        // Get the Intent that started this activity and extract the string
        Intent intent = getIntent();
        article_ID = intent.getStringExtra("articleId");

        // Toolbar
        ActionBar articleToolbar = getSupportActionBar();
        articleToolbar.setBackgroundDrawable(new ColorDrawable(Color.WHITE));
        articleToolbar.setTitle("");
        articleToolbar.setDisplayHomeAsUpEnabled(true);

        article_layout = findViewById(R.id.articleLayout);
        article_img = findViewById(R.id.articleImg);
        article_title = findViewById(R.id.articleTitle);
        article_section = findViewById(R.id.articleSection);
        article_date = findViewById(R.id.articleDate);
        article_desc = findViewById(R.id.articleDesc);
        article_url = findViewById(R.id.articleUrl);
        progressBar = findViewById(R.id.progressBarLayout);

        article_layout.setVisibility(View.GONE);
        requestArticle(article_ID);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_article, menu);
        article_isSaved = (ImageButton) menu.findItem(R.id.article_bar_bookmark).getActionView();
        article_isSaved.setBackgroundColor(Color.TRANSPARENT);
        try {
            if (utils.alreadySaved(ArticleActivity.this,article_ID)) {
                article_isSaved.setImageResource(R.drawable.ic_bookmark_red_24dp);
            } else {
                article_isSaved.setImageResource(R.drawable.ic_bookmark_border_red_24dp);

            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        article_share = (ImageButton) menu.findItem(R.id.article_bar_share).getActionView();
        article_share.setBackgroundColor(Color.TRANSPARENT);
        article_share.setImageResource(R.drawable.ic_twitter_logo_blue_24dp);
        return true;
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

    private void requestArticle(final String article_ID) {
        RequestQueue queue = Volley.newRequestQueue(this);
        //http://ec2-54-88-75-29.compute-1.amazonaws.com:4000/article?articleId=world/live/2020/apr/27/coronavirus-live-news-boris-johnson-back-in-number-10-as-trump-denies-he-plans-to-fire-health-secretary&isGuardian=true
        String url = "http://ec2-54-88-75-29.compute-1.amazonaws.com:4000/article?articleId="+ article_ID +"&isGuardian=true";

        final String[] Asection = {"DefaultSection"};
        final String[] Adate = {"DefaultDate"};
        final String[] Atitle = {"DefaultTitle"};
        final String[] Aurl = {"DefaultURL"};
        final String[] Aimg = {"https://assets.guim.co.uk/images/eada8aa27c12fe2d5afa3a89d3fbae0d/fallback-logo.png"};
        final String[] Adesc = {"DefaultDesc"};
        // Request a JSON response from the provided URL.

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(final JSONObject response) {
                        try {
                            Asection[0] = response.getString("section");
                            Adate[0] = response.getString("date");
                            Atitle[0] = response.getString("title");
                            Aurl[0] = response.getString("url");
                            Adesc[0] = response.getString("descHTML");
                            Aimg[0] = response.getString("img");
                            news = new News(Aimg[0],Atitle[0], Adate[0], Asection[0], article_ID, Aurl[0]);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        Picasso.get().load(Aimg[0]).fit().into(article_img);
                        article_title.setText(Atitle[0]);
                        article_section.setText(Asection[0]);
                        article_date.setText(utils.calculateDisplayTime(Adate[0], "dd MMM yyyy"));

                        article_desc.setText(
                                HtmlCompat.fromHtml(Adesc[0], HtmlCompat.FROM_HTML_MODE_LEGACY));
                        article_desc.setMovementMethod(LinkMovementMethod.getInstance());

                        article_url.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Uri uri = Uri.parse(Aurl[0]);
                                Context context = v.getContext();
                                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                                intent.putExtra(Browser.EXTRA_APPLICATION_ID, context.getPackageName());
                                context.startActivity(intent);
                            }
                        });

                        // Set ImageButton Listener
                        article_isSaved.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                try {
                                    utils.toggleArticle(v.getContext(), article_ID, news.newstoJson());
                                    utils.setBtn(v.getContext(), article_ID, article_isSaved);
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        });

                        // Set ImageButton Listener
                        article_share.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent tweet = new Intent(Intent.ACTION_VIEW);
                                tweet.setData(Uri.parse("https://twitter.com/intent/tweet?text=Check out this Link: " + Uri.encode( Aurl[0]) + "&hashtags=CSCI571NewsSearch"));//where message is your string message
                                v.getContext().startActivity(tweet);
                            }
                        });

                        // Set Toolbar Title
                        ActionBar articleToolbar = getSupportActionBar();
                        articleToolbar.setTitle(article_title.getText().toString());
                        article_layout.setVisibility(View.VISIBLE);
                        progressBar.setVisibility(View.GONE);
                        article_layout.invalidate();

                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
            }
        });

        // Add the request to the RequestQueue.
        queue.add(jsonObjectRequest);
    }
}