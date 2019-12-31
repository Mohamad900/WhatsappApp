package com.whatsapp.app.Adapters;

import android.content.Context;
import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.whatsapp.app.Models.Call;
import com.whatsapp.app.R;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.recyclerview.widget.RecyclerView;
import de.hdodenhof.circleimageview.CircleImageView;

public class CallsAdapter extends RecyclerView.Adapter<CallsAdapter.CallsViewHolder>{
	private Context context;
	private ArrayList<Call> calls;
	
	public CallsAdapter(Context context, ArrayList<Call> calls) {
		this.context = context;
		this.calls = calls;
	}
	
	@NonNull
	@Override
	public CallsViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
		return new CallsViewHolder(LayoutInflater.from(context).inflate(R.layout.calls_row, viewGroup, false));
	}
	
	@Override
	public void onBindViewHolder(@NonNull CallsViewHolder callsViewHolder, int i) {
		
		Call call = calls.get(i);
		
		Picasso.get()
			.load(call.getProfilePic())
			.placeholder(R.drawable.profile)
			.into(callsViewHolder.image);
		
		callsViewHolder.tvName.setText(call.getName());
		callsViewHolder.tvTime.setText(call.getTime());
		
		if (call.getType() == Call.AUDIO) {
			callsViewHolder.missedCallType.setBackgroundResource(R.drawable.ic_action_calls_green);

		}else if (call.getType() == Call.VIDEO) {
			
			callsViewHolder.missedCallType.setBackgroundResource(R.drawable.ic_action_video);
			
		}

	}
	
	@Override
	public int getItemCount() {
		if (calls != null) {
			return calls.size();
		}
		return 0;
	}
	
	class CallsViewHolder extends RecyclerView.ViewHolder {
		CircleImageView image;
		TextView tvName;
		TextView tvTime;
		ImageView missedCallType;
		
		CallsViewHolder(@NonNull View itemView) {
			super(itemView);
			
			image = itemView.findViewById(R.id.profile_image);
			tvName = itemView.findViewById(R.id.tvName);
			tvTime = itemView.findViewById(R.id.tvTime);
			missedCallType = itemView.findViewById(R.id.imgType);
			
		}
	}
}
