package com.example.newsapp;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.android.volley.toolbox.JsonObjectRequest;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.google.android.material.snackbar.Snackbar;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import static java.lang.Float.valueOf;

public class FragmentTrending extends Fragment {

    private EditText input;
    private LineChart mChart;
    private List<Entry> entries = new ArrayList<Entry>();


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)  {
        View view = inflater.inflate(R.layout.fragment_trend, container, false);
        mChart = view.findViewById(R.id.trendChart);

        mChart.getAxisRight().setDrawGridLines(false);
        mChart.getAxisLeft().setDrawGridLines(false);
        mChart.getXAxis().setDrawGridLines(false);
        mChart.getAxisLeft().setDrawAxisLine(false);
        mChart.setHighlightPerDragEnabled(false);
        mChart.setHighlightPerTapEnabled(false);
        mChart.getLegend().setTextSize(15);

        input = view.findViewById(R.id.input);

        input.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                boolean handled = false;
                if (actionId == EditorInfo.IME_ACTION_SEND) {
                    requestTrend(input.getText().toString());
                    handled = true;
                }
                return handled;
            }
        });

        input.setOnKeyListener(new View.OnKeyListener() {
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER || keyCode == KeyEvent.KEYCODE_DPAD_CENTER)) {
                    requestTrend(input.getText().toString());
                    return true;
                }
                return false;
            }
        });

        requestTrend("Coronavirus");
        return view;
    }

    private void requestTrend(final String keyword) {
        RequestQueue queue = Volley.newRequestQueue(getContext());
        String url ="http://ec2-54-88-75-29.compute-1.amazonaws.com:4000/trend?keyword=" + keyword.replace(' ', '+');
        JsonObjectRequest jsonArrayRequest = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try{
                            entries.clear();
                            for(int i=0;i<response.length();i++){
                                JSONObject data = response.getJSONObject(String.valueOf(i));
                                String value = data.getString("value");
                                entries.add(new Entry(i, valueOf(value)));
                            }
                            LineDataSet dataSet = new LineDataSet(entries, "Trending Chart for "+ keyword); // add entries to dataset
                            dataSet.setColor(R.attr.colorPrimary);
                            dataSet.setCircleColor(R.attr.colorPrimary);
                            dataSet.setCircleHoleColor(R.attr.colorPrimary);
                            dataSet.setValueTextColor(R.attr.colorPrimary);
                            LineData lineData = new LineData(dataSet);
                            mChart.setData(lineData);
                            mChart.invalidate(); // refresh
                        }catch (JSONException e){
                            e.printStackTrace();
                            Log.e("Trending Error", String.valueOf(e));
                        }
                    }
                },
                new Response.ErrorListener(){
                    @Override
                    public void onErrorResponse(VolleyError error){
                       Log.e("Trending Error", String.valueOf(error));
                    }
                }
        );

        // Add JsonArrayRequest to the RequestQueue
        queue.add(jsonArrayRequest);
    }
}
