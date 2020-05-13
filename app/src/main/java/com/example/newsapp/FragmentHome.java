package com.example.newsapp;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Rect;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class FragmentHome extends Fragment implements LocationListener {
    private static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;
    private static Context mContext;
    private static String PACKAGE_NAME;

    private CardView weatherCard;
    private ImageView weatherCardBg;
    private TextView cityText;
    private TextView stateText;
    private TextView climateText;
    private TextView summaryText;
    private RecyclerView latestNewsRV;

    private ConstraintLayout progressBar;
    private SwipeRefreshLayout newsListSwipLayout;

    // Location
    public double latitude = 0.0;
    public double longitude = 0.0;
    public LocationManager locationManager;
    public String bestProvider;

    //LatestNews
    public ArrayList<News> latestNews = new ArrayList<News>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);


        weatherCard = view.findViewById(R.id.weatherCard);
        weatherCard.setVisibility(View.GONE);
        latestNewsRV = view.findViewById(R.id.newsListRV);
        PACKAGE_NAME = view.getContext().getPackageName();
        progressBar = view.findViewById(R.id.progressBarLayout);
        newsListSwipLayout = view.findViewById(R.id.newsListSwipLayout);

        newsListSwipLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                showLatestNews();
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
        showLatestNews();
        showWeatherCard();
        return view;
    }

    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = getContext();
    }

    private void showWeatherCard() {
        if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    MY_PERMISSIONS_REQUEST_LOCATION);
        } else {
            String cityName;
            String stateName;
            locationManager = (LocationManager) mContext.getSystemService(mContext.LOCATION_SERVICE);
            bestProvider = locationManager.getBestProvider(new Criteria(), false);
            locationManager.requestLocationUpdates(bestProvider, 2000, 0, this);
            Location location = locationManager.getLastKnownLocation(bestProvider);
            if (location != null) {
                latitude = location.getLatitude();
                longitude = location.getLongitude();
                Geocoder gcd = new Geocoder(mContext, Locale.getDefault());
                List<Address> addresses = null;
                try {
                    addresses = gcd.getFromLocation(latitude, longitude, 1);
                    if (addresses.size() > 0) {
                        cityName = addresses.get(0).getLocality();
                        stateName = addresses.get(0).getAdminArea();
                        requestWeather(cityName, stateName);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void requestWeather(final String cityName, final String stateName) {

        RequestQueue queue = Volley.newRequestQueue(mContext);
        String url ="https://api.openweathermap.org/data/2.5/weather?q="+ cityName.replace(' ', '+') + "&units=metric&appid=cbdfdfa1b98d7b8fa0665f6b384e6c3a";
        final int[] temperature = {19};
        final String[] summary = {"Clear"};
        final HashMap<String, String> imgMap = new HashMap<String, String>();
        imgMap.put("Clear", "https://csci571.com/hw/hw9/images/android/clear_weather.jpg");
        imgMap.put("Clouds", "https://csci571.com/hw/hw9/images/android/cloudy_weather.jpg");
        imgMap.put("Snow", "https://csci571.com/hw/hw9/images/android/snowy_weather.jpg");
        imgMap.put("Rain", "https://csci571.com/hw/hw9/images/android/rainy_weather.jpg");
        imgMap.put("Thunderstorm", "https://csci571.com/hw/hw9/images/android/thunder_weather.jpg");
        imgMap.put("Sunny", "https://csci571.com/hw/hw9/images/android/sunny_weather.jpg");
        // Request a string response from the provided URL.
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {

                        weatherCard = getActivity().findViewById(R.id.weatherCard);
                        weatherCardBg = getActivity().findViewById(R.id.weatherCard_bg);
                        cityText = getActivity().findViewById(R.id.city);
                        stateText = getActivity().findViewById(R.id.state);
                        climateText = getActivity().findViewById(R.id.climate);
                        summaryText = getActivity().findViewById(R.id.summary);

                        try {
                            temperature[0] = response.getJSONObject("main").getInt("temp");
                            summary[0] = response.getJSONArray("weather").getJSONObject(0).getString("main");
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        if (imgMap.containsKey(summary[0])){
                            Picasso.get().load(imgMap.get(summary[0])).fit().into(weatherCardBg);
                        } else {
                            Picasso.get().load("https://csci571.com/hw/hw9/images/android/sunny_weather.jpg").fit().into(weatherCardBg);
                            Log.v("weather", "weatherCardBg");
                        }
                        cityText.setText(cityName);
                        stateText.setText(stateName);
                        climateText.setText(String.valueOf(temperature[0]) + "℃");
                        summaryText.setText(summary[0]);
                        weatherCard.setVisibility(View.VISIBLE);
                        weatherCard.invalidate();
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("HomePageError", error.toString());
            }
        });

        // Add the request to the RequestQueue.
        queue.add(jsonObjectRequest);
    }

    @Override
    public void onResume() {
        super.onResume();
        showLatestNews();
    }

    private void showLatestNews() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(mContext );
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(mContext,
                layoutManager.getOrientation());
        latestNewsRV.setLayoutManager(layoutManager);
        latestNewsRV.addItemDecoration(dividerItemDecoration);
        latestNewsRV.addItemDecoration(new utils.SimplePaddingDecoration(getContext()));


        RequestQueue queue = Volley.newRequestQueue(mContext);
        String url ="http://ec2-54-88-75-29.compute-1.amazonaws.com:4000/latest";
        final String[] imageUrl = {"https://assets.guim.co.uk/images/eada8aa27c12fe2d5afa3a89d3fbae0d/fallback-logo.png"};
        final String[] title = {"Default Title"};
        final String[] time = {"Default Time"};
        final String[] section = {"Default Section"};
        final String[] articleId = {"-1"};
        final String[] articleURL = {""};

        // Request a string response from the provided URL.
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject res) {
                        try {
                            JSONObject response = res.getJSONObject("response");
                            JSONArray results = response.getJSONArray("results");

                            for (int i = 0; i < results.length(); i++) {
                                JSONObject result = results.getJSONObject(i);
                                if (result.has("fields") && result.getJSONObject("fields").has("thumbnail")){
                                    imageUrl[0] = result.getJSONObject("fields").getString("thumbnail");
                                }
                                if (result.has("webTitle")){
                                    title[0] = result.getString("webTitle");
                                }
                                if (result.has("webPublicationDate")){
                                    time[0] = result.getString("webPublicationDate");
                                }
                                if (result.has("sectionName")){
                                    section[0] = result.getString("sectionName");
                                }
                                if (result.has("id")){
                                    articleId[0] = result.getString("id");
                                }
                                if (result.has("webUrl")){
                                    articleURL[0] = result.getString("webUrl");
                                }
                                latestNews.add(new News(imageUrl[0],title[0], time[0], section[0], articleId[0], articleURL[0]));
                            }

                            RVAdapter adapter = new RVAdapter(latestNews, 0);
                            latestNewsRV.setAdapter(adapter);
                            progressBar.setVisibility(View.GONE);

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("HomePageError", error.toString());
            }
        });

        // Add the request to the RequestQueue.
        queue.add(jsonObjectRequest);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_LOCATION: {

                Log.e("HomePage!!", "Grant Permit !");

                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted, yay! Do the
                    // location-related task you need to do.
                    if (ContextCompat.checkSelfPermission(mContext,
                            Manifest.permission.ACCESS_FINE_LOCATION)
                            == PackageManager.PERMISSION_GRANTED) {
                        Log.v("weather", "shouldshown");
                        showWeatherCard();
                        // String cityName;
                        // String stateName;
                        // locationManager = (LocationManager) mContext.getSystemService(Context.LOCATION_SERVICE);
                        // bestProvider = locationManager.getBestProvider(new Criteria(), false);
                        // locationManager.requestLocationUpdates(bestProvider, 2000, 0, this);
                        // Location location = locationManager.getLastKnownLocation(bestProvider);
                        // if (location != null) {
                        //     latitude = location.getLatitude();
                        //     longitude = location.getLongitude();
                        //     Geocoder gcd = new Geocoder(mContext, Locale.getDefault());
                        //     List<Address> addresses = null;
                        //     try {
                        //         addresses = gcd.getFromLocation(latitude, longitude, 1);
                        //     } catch (IOException e) {
                        //         e.printStackTrace();
                        //     }
                        //     if (addresses.size() > 0) {
                        //         cityName = addresses.get(0).getLocality();
                        //         stateName = addresses.get(0).getAdminArea();
                        //         requestWeather(cityName, stateName);
                        // }
                        // }
                    }
                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    Log.e("HomePageError", "Not Grant Permit !");
                }
                return;
            }
        }
    }

    @Override
    public void onLocationChanged(Location location) {
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
    }

    @Override
    public void onProviderEnabled(String provider) {
    }

    @Override
    public void onProviderDisabled(String provider) {
    }
}