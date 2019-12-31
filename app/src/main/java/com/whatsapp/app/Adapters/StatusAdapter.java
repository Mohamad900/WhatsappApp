package com.whatsapp.app.Adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.whatsapp.app.Activities.StoryActivity;
import com.whatsapp.app.Models.Status;
import com.whatsapp.app.R;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import de.hdodenhof.circleimageview.CircleImageView;

public class StatusAdapter extends RecyclerView.Adapter<StatusAdapter.StatusViewHolder>{
	private Context context;
	private ArrayList<Status> contactsStatusList;
	
	public StatusAdapter(Context context, ArrayList<Status> contactsStatusList) {
		this.context = context;
		this.contactsStatusList = contactsStatusList;
	}
	
	@NonNull
	@Override
	public StatusViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
		return new StatusViewHolder(LayoutInflater.from(context).inflate(R.layout.status_row, viewGroup, false));
	}
	
	@Override
	public void onBindViewHolder(@NonNull StatusViewHolder statusViewHolder, final int i) {
		
		Status status = contactsStatusList.get(i);
		
		Picasso.get()
			.load(status.getImages().get(0))
			.placeholder(R.drawable.profile)
			.into(statusViewHolder.image);
		
		statusViewHolder.tvName.setText(status.getUserNname());
		statusViewHolder.tvTime.setText(status.getTime());

		statusViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(context, StoryActivity.class);
				intent.putStringArrayListExtra("currentUserStatus",contactsStatusList.get(i).getImages());
				context.startActivity(intent);
			}
		});
		
	}
	
	@Override
	public int getItemCount() {
		return contactsStatusList.size();
	}
	
	class StatusViewHolder extends RecyclerView.ViewHolder {
		CircleImageView image;
		TextView tvName;
		TextView tvTime;
		
		StatusViewHolder(@NonNull View itemView) {
			super(itemView);
			
			image = itemView.findViewById(R.id.profile_image);
			tvName = itemView.findViewById(R.id.tvName);
			tvTime = itemView.findViewById(R.id.tvTime);
			
		}
	}
}
