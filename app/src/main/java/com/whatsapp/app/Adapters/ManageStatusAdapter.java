package com.whatsapp.app.Adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;
import com.whatsapp.app.Activities.StoryActivity;
import com.whatsapp.app.Models.Status;
import com.whatsapp.app.R;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.PopupMenu;
import androidx.recyclerview.widget.RecyclerView;
import de.hdodenhof.circleimageview.CircleImageView;


public class ManageStatusAdapter extends RecyclerView.Adapter<ManageStatusAdapter.StatusViewHolder>{
    private Context context;
    private ArrayList<Status> statusList;

    public ManageStatusAdapter(Context context, ArrayList<Status> statusList) {
        this.context = context;
        this.statusList = statusList;
    }

    @NonNull
    @Override
    public StatusViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        return new StatusViewHolder(LayoutInflater.from(context).inflate(R.layout.manage_status_row, viewGroup, false));
    }

    @Override
    public void onBindViewHolder(@NonNull StatusViewHolder statusViewHolder, final int i) {

        Status status = statusList.get(i);

        Picasso.get()
                .load(status.getImage())
                .placeholder(R.drawable.profile)
                .into(statusViewHolder.image);

        statusViewHolder.tvName.setText("Status "+i);
        statusViewHolder.status_more.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopupMenu popup = new PopupMenu(context, v);
                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    public boolean onMenuItemClick(MenuItem item) {

                        FirebaseDatabase.getInstance().getReference().child("Stories").child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                .child(status.getId()).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {

                                /*if(statusList.size()==1){
                                    notifyItemRemoved(i);
                                    statusList.remove(i);
                                }*/
                                //statusList.remove(i);
                                //notifyItemRemoved(i);
                                //notifyItemRangeChanged(i, statusList.size());
                            }
                        });
                        return true;
                    }
                });
                popup.inflate(R.menu.manage_status_menu);
                popup.show();
            }
        });

    }

    @Override
    public int getItemCount() {
        return statusList.size();
    }

    class StatusViewHolder extends RecyclerView.ViewHolder {
        CircleImageView image;
        TextView tvName;
        TextView tvTime;
        ImageView status_more;

        StatusViewHolder(@NonNull View itemView) {
            super(itemView);

            image = itemView.findViewById(R.id.profile_image);
            tvName = itemView.findViewById(R.id.tvName);
            tvTime = itemView.findViewById(R.id.tvTime);
            status_more = itemView.findViewById(R.id.status_more);

        }
    }
}
