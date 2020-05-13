package com.example.newsapp;
import android.annotation.SuppressLint;
import android.app.SearchManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.text.Editable;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Toast;
import android.view.Menu;
import android.view.MenuItem;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements LocationListener {

    private static final String TAG = "MyActivity";
    private MenuItem searchItem;
    private SearchView searchView;
    private ArrayAdapter<String> newsAdapter;
    private SearchView.SearchAutoComplete searchAutoComplete;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.NoActionBar);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        BottomNavigationView bottomNav = findViewById(R.id.bottomNavBar);
        bottomNav.setOnNavigationItemSelectedListener(navListener);
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.fragmentContainer, new FragmentHome());
        fragmentTransaction.addToBackStack("MainActivity");
        fragmentTransaction.commit();

        // Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitleTextColor(Color.BLACK);
        setSupportActionBar(toolbar);
        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.app_bar_search:
                        // TODO: SEARCH
                        Toast.makeText(MainActivity.this, "Search !", Toast.LENGTH_SHORT).show();
                        break;
                }
                return true;
            }
        });
    }

    private BottomNavigationView.OnNavigationItemSelectedListener navListener =
            new BottomNavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                    Log.v(TAG, String.valueOf(menuItem.getItemId()));
                    switch (menuItem.getItemId()){
                        case R.id.nav_home:
                            getSupportFragmentManager().beginTransaction().replace(R.id.fragmentContainer, new FragmentHome()).commit();
                            break;
                        case R.id.nav_headlines:
                            getSupportFragmentManager().beginTransaction().replace(R.id.fragmentContainer, new FragmentHeadlines()).commit();
                            break;
                        case R.id.nav_trend:
                            getSupportFragmentManager().beginTransaction().replace(R.id.fragmentContainer, new FragmentTrending()).commit();
                            break;
                        case R.id.nav_bookmarks:
                            getSupportFragmentManager().beginTransaction().replace(R.id.fragmentContainer, new FragmentBookmarks()).commit();
                            break;
                    }
                    return true;
                }
            };


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);

        // Search Item
        searchItem = menu.findItem(R.id.app_bar_search);
        searchView = (SearchView) searchItem.getActionView();
        SearchManager searchManager = (SearchManager)getSystemService(Context.SEARCH_SERVICE);
        ComponentName componentName = getComponentName();
        searchView.setSearchableInfo(searchManager.getSearchableInfo(componentName));

        // Get SearchView autocomplete object.
        searchAutoComplete = searchView.findViewById(R.id.search_src_text);

        // Create a new ArrayAdapter and add data to search auto complete object.
        // String dataArr[] = {};
        // newsAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_dropdown_item_1line, dataArr);
        // searchAutoComplete.setAdapter(newsAdapter);

        // Listen to search view item on click event.
        searchAutoComplete.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int itemIndex, long id) {
                String selectedKeyword=(String)adapterView.getItemAtPosition(itemIndex);
                searchAutoComplete.setText("" + selectedKeyword);
            }
        });

        // Below event is triggered when submit search query.
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                Intent intent = new Intent(getApplicationContext(),SearchActivity.class);
                Bundle bundle = new Bundle();
                bundle.putString("keyword", query);
                intent.putExtras(bundle);
                startActivity(intent);
                return true;
            }


            @Override
            public boolean onQueryTextChange(String newText) {
                if (newText.length() >= 3){
                    requestSugestionWords(newText);
                } else {
                    String[] emptyArr = {};
                    newsAdapter = new ArrayAdapter<String>(MainActivity.this, android.R.layout.simple_dropdown_item_1line, emptyArr);
                    searchAutoComplete.setAdapter(newsAdapter);
                }
                return false;
            }
        });

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar news clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.app_bar_search) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    void requestSugestionWords(String query){
        String url = "https://api.cognitive.microsoft.com/bing/v7.0/suggestions?q="+query;
        RequestQueue responseQueue = Volley.newRequestQueue(getApplicationContext());
        JsonObjectRequest autoSuggestionRequest = new JsonObjectRequest
                (Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            ArrayList<String> suggestionWordsList = new ArrayList<>();
                            JSONArray suggestionWordsRes = response.getJSONArray("suggestionGroups").getJSONObject(0).getJSONArray("searchSuggestions");
                            for (int i = 0; i < suggestionWordsRes.length() && i < 5; i++) {
                                suggestionWordsList.add(suggestionWordsRes.getJSONObject(i).getString("displayText"));
                            }
                            newsAdapter = new ArrayAdapter<String>(MainActivity.this, android.R.layout.simple_dropdown_item_1line, suggestionWordsList);
                            searchAutoComplete.setAdapter(newsAdapter);
                            Editable text = searchAutoComplete.getText();
                            searchAutoComplete.setText(text);
                            searchAutoComplete.setSelection(text.length());
                        } catch (JSONException e) {
                            Log.e("SuggestionJsonError", e.toString());
                        }

                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("SuggestionError", error.toString());
                    }
                }) {
                    @Override
                    public Map<String, String> getHeaders() throws AuthFailureError {
                        Map<String, String> params = new HashMap<String, String>();
                        params.put("Ocp-Apim-Subscription-Key", "fa641ec7cf8b4cf881eaf7dfacced523");
                        return params;
                    }
        };
        responseQueue.add(autoSuggestionRequest);
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
