package com.moonstone.ezmaps_app;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.LinearSnapHelper;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SnapHelper;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import com.moonstone.ezmaps_app.RecyclerViewAdapter;


public class ezdirection extends AppCompatActivity implements RetrieveFeed.AsyncResponse{
    private ArrayList<String> imageUrlsList;
    private ArrayList<String> textDirectionsList;
    private View recyclerView;

    private Toolbar toolbar;
    private ActionBar actionbar;

    /* THE ONE USING RIGHTNOW */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        String url = new String();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ezdirection);

        recyclerView = findViewById(R.id.recyclerView);

        toolbar = findViewById(R.id.my_toolbar);
        setSupportActionBar(toolbar);
        actionbar = getSupportActionBar();
        actionbar.setTitle("EZMap");
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        SnapHelper helper = new LinearSnapHelper();
        helper.attachToRecyclerView((RecyclerView) recyclerView);

        //get search address from search bar
        Intent intent = getIntent();
        String destination = intent.getStringExtra("destination");
        destination = destination.replaceAll(" ", "%20");
        System.out.println("XX" + destination);

        url = "https://us-central1-it-project-moonstone-43019.cloudfunctions.net/mapRequest?text=145%20Queensberry%20Street,%20Carlton%20VIC---" + destination;

        //execute async task
        new RetrieveFeed(this).execute(url);

    }

    @Override
    public void processFinish(JSONArray output){
        //Here you will receive the result fired from async class
        //of onPostExecute(result) method.;

        if (output != null) {
            imageUrlsList = new ArrayList<>();
            textDirectionsList = new ArrayList<>();
            for (int i = 0; i < output.length(); i++) {
                try {
                    JSONObject object = output.getJSONObject(i);
                    imageUrlsList.add(object.getString("imageURL"));
                    textDirectionsList.add(object.getString("description"));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            initRecyclerView();
        }
        else{
            Intent intent = new Intent(this , error.class);
            startActivity(intent);
        }
    }



    private void initRecyclerView() {
        final String TAG = "initRecyclerView";
        Log.d(TAG, "initRecyclerView: init recyclerview");

        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(layoutManager);
        RecyclerViewAdapter adapter = new RecyclerViewAdapter(textDirectionsList, imageUrlsList, this);
        recyclerView.setAdapter(adapter);
    }


}
