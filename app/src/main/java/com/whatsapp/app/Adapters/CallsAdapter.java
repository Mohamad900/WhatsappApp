package com.whatsapp.app.Adapters;

import android.content.Context;
import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;
import com.whatsapp.app.Models.Call;
import com.whatsapp.app.R;

import java.util.ArrayList;
import java.util.Collections;

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
		
		Call call = calls.get(calls.size() - i-1);
		String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

		if(currentUserId.equals(call.getFrom())){

			if(call.getStatus().equals("Ended")){
				callsViewHolder.imgArrow.setImageResource(R.drawable.ic_call_made_black_24dp);
			}

			FirebaseDatabase.getInstance().getReference().child("Users").child(call.getTo()).child("name").addValueEventListener(new ValueEventListener() {
				@Override
				public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
					if(dataSnapshot.exists())
						callsViewHolder.tvName.setText(dataSnapshot.getValue().toString());
				}

				@Override
				public void onCancelled(@NonNull DatabaseError databaseError) {

				}
			});

			FirebaseDatabase.getInstance().getReference().child("Users").child(call.getTo()).child("image").addValueEventListener(new ValueEventListener() {
				@Override
				public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

					if(dataSnapshot.exists())
						Picasso.get()
								.load(dataSnapshot.getValue().toString())
								.placeholder(R.drawable.profile)
								.into(callsViewHolder.image);
				}

				@Override
				public void onCancelled(@NonNull DatabaseError databaseError) {

				}
			});

		}else if(currentUserId.equals(call.getTo())){

			if(call.getStatus().equals("Ended")){
				callsViewHolder.imgArrow.setImageResource(R.drawable.ic_call_received_black_24dp);
			}

			if(call.getStatus().equals("Not Answered")){
				callsViewHolder.imgArrow.setImageResource(R.drawable.ic_call_received_red);
			}

			FirebaseDatabase.getInstance().getReference().child("Users").child(call.getFrom()).child("name").addValueEventListener(new ValueEventListener() {
				@Override
				public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
					if(dataSnapshot.exists())
						callsViewHolder.tvName.setText(dataSnapshot.getValue().toString());
				}

				@Override
				public void onCancelled(@NonNull DatabaseError databaseError) {

				}
			});

			FirebaseDatabase.getInstance().getReference().child("Users").child(call.getFrom()).child("image").addValueEventListener(new ValueEventListener() {
				@Override
				public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

					if(dataSnapshot.exists())
						Picasso.get()
								.load(dataSnapshot.getValue().toString())
								.placeholder(R.drawable.profile)
								.into(callsViewHolder.image);
				}

				@Override
				public void onCancelled(@NonNull DatabaseError databaseError) {

				}
			});

		}


		callsViewHolder.tvTime.setText(call.getTime());
		
		if (call.getType().equals("voice call")) {
			callsViewHolder.missedCallType.setBackgroundResource(R.drawable.ic_action_calls_green);

		}else if (call.getType().equals("video call")) {
			
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
		ImageView missedCallType,imgArrow;
		
		CallsViewHolder(@NonNull View itemView) {
			super(itemView);
			
			image = itemView.findViewById(R.id.profile_image);
			tvName = itemView.findViewById(R.id.tvName);
			tvTime = itemView.findViewById(R.id.tvTime);
			missedCallType = itemView.findViewById(R.id.imgType);
			imgArrow = itemView.findViewById(R.id.imgArrow);;
			
		}
	}
}
