package com.example.assignment5;

import static android.view.View.INVISIBLE;
import static android.view.View.VISIBLE;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.ANRequest;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    List<String> watchlist;
    ListView watchlistLV;
    EditText searchET;
    Button searchB;
    Button clearB;
    ImageView iv;
    ConstraintLayout profile;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        searchET = findViewById(R.id.searchET);
        searchB = findViewById(R.id.searchB);
        searchB.setOnClickListener(searchListener);
        clearB = findViewById(R.id.clearB);
        clearB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                profile.setVisibility(INVISIBLE);
                searchET.setText("");
            }
        });
        iv = findViewById(R.id.logoView);
        profile = findViewById(R.id.profileLayout);
        profile.setVisibility(INVISIBLE);
        watchlistLV = findViewById(R.id.watchlistLV);
        watchlistLV.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String s = watchlist.get(position);
                getPkmn(s.substring(0, s.indexOf(" ")));
            }
        });
        watchlist = new ArrayList<>();
    }
    private void getPkmn(String s) {
        ANRequest req = AndroidNetworking.get("https://pokeapi.co/api/v2/pokemon/" + s + "/").setPriority(Priority.LOW).build();
        req.getAsJSONObject(new JSONObjectRequestListener() {
            @Override
            public void onResponse(JSONObject jsonObject) {
                try {
                    // Populate listview on left side (if pokemon is not a duplicate)
                    int id = jsonObject.getInt("id");
                    String row = String.format("%-10s", id) + jsonObject.getString("name");
                    if (!watchlist.contains(row)) {
                        watchlist.add(row);
                        ArrayAdapter adapter = new ArrayAdapter(getApplicationContext(), android.R.layout.simple_list_item_1, watchlist);
                        watchlistLV.setAdapter(adapter);
                    }
                    // Fill out fields on right side
                    ((TextView) findViewById(R.id.pokeNameTV)).setText(jsonObject.getString("name").toUpperCase());
                    ((TextView) findViewById(R.id.numberField)).setText(String.valueOf(id));
                    ((TextView) findViewById(R.id.weightField)).setText(String.valueOf(jsonObject.getInt("weight")));
                    ((TextView) findViewById(R.id.heightField)).setText(String.valueOf(jsonObject.getInt("height")));
                    ((TextView) findViewById(R.id.baseXPField)).setText(String.valueOf(jsonObject.getInt("base_experience")));
                    String move = jsonObject.getJSONArray("moves").getJSONObject(0).getJSONObject("move").getString("name");
                    ((TextView) findViewById(R.id.moveField)).setText(move);
                    String ab = jsonObject.getJSONArray("abilities").getJSONObject(0).getJSONObject("ability").getString("name");
                    ((TextView) findViewById(R.id.abilityField)).setText(ab);
                    // Load image
                    String imageURL = "https://raw.githubusercontent.com/HybridShivam/Pokemon/master/assets/images/" + String.format("%03d", id) + ".png";
                    ImageView iv = findViewById(R.id.logoView);
                    Picasso.get().load(imageURL).into(iv);
                    // Show profile
                    profile.setVisibility(VISIBLE);
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
            }

            @Override
            public void onError(ANError anError) {
                Toast.makeText(getApplicationContext(), "Pokemon not found", Toast.LENGTH_SHORT).show();
                Log.i("ERR", req.getUrl());
                Log.i("ERR", anError.getErrorDetail());
                Log.i("ERR", anError.getErrorBody());
                Log.i("ERR", anError.getErrorCode() + "");
                Log.i("ERR", anError.getResponse().toString());
            }
        });

    }


    View.OnClickListener searchListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            String s = searchET.getText().toString();
            if (validSearch(s)) {
                getPkmn(s);
                searchET.setText("");
            }
        }
    };
    private boolean validSearch(String s) {
        if (s.isBlank()) {
            return false;
        }
        else if (!s.matches("^[^%&*(@)!;:<>]*$")) {
            Toast.makeText(getApplicationContext(), "Search contains invalid characters", Toast.LENGTH_SHORT).show();
            return false;
        }
        try {
            int i = Integer.parseInt(s);
            if (i < 1 || i > 1010) {
                Toast.makeText(getApplicationContext(), "ID number out of range", Toast.LENGTH_SHORT).show();
                return false;
            }
        } catch (NumberFormatException e) {
        }
        return true;
    }
}