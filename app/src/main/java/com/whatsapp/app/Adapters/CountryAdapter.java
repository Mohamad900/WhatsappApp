package com.whatsapp.app.Adapters;


import android.app.Activity;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import com.guardanis.imageloader.ImageRequest;
import com.guardanis.imageloader.ImageUtils;
import com.whatsapp.app.Models.Country;
import com.whatsapp.app.R;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import static android.app.Activity.RESULT_OK;

public class CountryAdapter extends RecyclerView.Adapter<CountryAdapter.CountryViewHolder> implements Filterable {

    private ArrayList<Country> countries,countryListFiltered;
    private Activity activity;

    public CountryAdapter(Activity  activity, ArrayList<Country> countries) {
        this.activity = activity;
        this.countries = countries;
        this.countryListFiltered=countries;
    }

    public void updateModels(ArrayList<Country> countriesList) {
        countries.clear();
        countryListFiltered.clear();
        countries = countriesList;
        countryListFiltered = countriesList;
        notifyDataSetChanged();
    }


    @NonNull
    @Override
    public CountryViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        return new CountryViewHolder(LayoutInflater.from(activity).inflate(R.layout.country_item, viewGroup, false));
    }

    @Override
    public void onBindViewHolder(@NonNull CountryViewHolder countryViewHolder, int i) {

        final Country country = countryListFiltered.get(i);

        if(country.getImage() != null && !country.getImage().equals(""))
        ImageRequest.create(countryViewHolder.countryImage)
                .setTargetUrl(country.getImage())
                .execute();


        countryViewHolder.countryName.setText(country.getName());
        countryViewHolder.countryCode.setText("+"+country.getCode());

        countryViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.putExtra("countryCode", country.getCode());
                intent.putExtra("countryName", country.getName());
                activity.setResult(RESULT_OK, intent);
                activity.finish();
            }
        });

    }

    @Override
    public int getItemCount() {
        return countryListFiltered.size();
    }

    class CountryViewHolder extends RecyclerView.ViewHolder {
        ImageView countryImage;
        TextView countryName,countryCode;

        CountryViewHolder(@NonNull View itemView) {
            super(itemView);

            countryName = itemView.findViewById(R.id.countryName);
            countryCode = itemView.findViewById(R.id.countryCode);
            countryImage = itemView.findViewById(R.id.countryImage);

        }
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence charSequence) {
                String charString = charSequence.toString();
                if (charString.isEmpty()) {
                    countryListFiltered = countries;
                } else {
                    ArrayList<Country> filteredList = new ArrayList<>();
                    for (Country row : countries) {
                        if (row.getName().toLowerCase().contains(charString.toLowerCase()) || row.getCode().contains(charSequence)) {
                            filteredList.add(row);
                        }
                    }
                    countryListFiltered = filteredList;
                }

                FilterResults filterResults = new FilterResults();
                filterResults.values = countryListFiltered;
                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
                countryListFiltered = (ArrayList<Country>) filterResults.values;
                notifyDataSetChanged();
                //updateModelsFilter(countryListFiltered);
            }
        };
    }

}
