package com.whatsapp.app.Activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.MenuItemCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.reflect.TypeToken;
import com.sinch.gson.Gson;
import com.whatsapp.app.Adapters.CountryAdapter;
import com.whatsapp.app.Models.Country;
import com.whatsapp.app.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class CountryActivity extends AppCompatActivity {

    RecyclerView countriesRecyclerView;
    Toolbar toolbar;
    CountryAdapter adapter;
    ArrayList<Country> countriesList = new ArrayList<>();
    private ProgressDialog loadingBar;
    private String countriesUrl = "https://restcountries.eu/rest/v2/all";
    ArrayList<Country> CountriesList;
    private SearchView searchView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_country);
        initViews();

    }
    private void initViews() {
        if (getIntent() != null) {

            String countriesJSON = getIntent().getStringExtra("countriesList");

            if (countriesJSON != null && !countriesJSON.equals(""))
                countriesList = new Gson().fromJson(countriesJSON, new TypeToken<ArrayList<Country>>() {
                }.getType());

        }
        CountriesList = new ArrayList<>();
        countriesRecyclerView = findViewById(R.id.countriesRecyclerView);
        loadingBar = new ProgressDialog(this);
        toolbar = findViewById(R.id.toolbarCountry);
        toolbar.setTitle("Choose a Country");
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null){
            getSupportActionBar().setTitle("Choose a Country");
            getSupportActionBar().setDisplayShowTitleEnabled(true);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        adapter = new CountryAdapter(CountryActivity.this,countriesList);
        countriesRecyclerView.setLayoutManager(new LinearLayoutManager(this,LinearLayoutManager.VERTICAL,false));
        countriesRecyclerView.setAdapter(adapter);

        GetCountriesAndCodes();

    }

    private void GetCountriesAndCodes() {

        loadingBar.setMessage("loading...");
        loadingBar.show();
        JsonArrayRequest req = new JsonArrayRequest(countriesUrl, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {

                try {
                    String code="";

                    for (int i = 0; i < response.length(); i++) {

                        JSONObject jsonobject = response.getJSONObject(i);
                        String name = jsonobject.getString("name");
                        String image = jsonobject.getString("flag");

                        JSONArray codes = jsonobject.getJSONArray("callingCodes");

                        if(codes.length ()> 0){
                            code = codes.getString(0);
                        }
                        Country countryObj = new Country(name, code, image);
                        CountriesList.add(countryObj);
                    }
                    adapter.updateModels(CountriesList);

                    loadingBar.dismiss();

                } catch (JSONException e) {
                    loadingBar.dismiss();
                    Toast.makeText(getApplicationContext(), "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                }

            }
        },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        loadingBar.dismiss();
                        Toast.makeText(CountryActivity.this,error.getMessage(),Toast.LENGTH_LONG).show();
                    }
                });
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(req);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.country_menu, menu);

        MenuItem menuItem = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(menuItem);

        // listening to search query text change
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                // filter recycler view when query submitted
                adapter.getFilter().filter(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String query) {
                // filter recycler view when text is changed
                adapter.getFilter().filter(query);
                return false;
            }
        });
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        //noinspection SimplifiableIfStatement
        if (id == R.id.action_search) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        // close search view on back button pressed
        if (!searchView.isIconified()) {
            searchView.setIconified(true);
            return;
        }
        super.onBackPressed();
    }

}
