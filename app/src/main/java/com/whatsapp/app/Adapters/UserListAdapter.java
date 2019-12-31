package com.whatsapp.app.Adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.whatsapp.app.ChatActivity;
import com.whatsapp.app.Models.Call;
import com.whatsapp.app.Models.User;
import com.whatsapp.app.R;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import de.hdodenhof.circleimageview.CircleImageView;

public class UserListAdapter extends RecyclerView.Adapter<UserListAdapter.UserListViewHolder>{

    private Context context;
    private ArrayList<User> users;

    public UserListAdapter(Context context, ArrayList<User> users) {
        this.context = context;
        this.users = users;
    }

    @NonNull
    @Override
    public UserListViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        return new UserListViewHolder(LayoutInflater.from(context).inflate(R.layout.item_user_list, viewGroup, false));
    }

    @Override
    public void onBindViewHolder(@NonNull UserListViewHolder userViewHolder, int i) {

        final User user = users.get(i);

        Picasso.get()
                .load(user.getProfilePicUrl())
                .placeholder(R.drawable.pic)
                .into(userViewHolder.userIV);

        userViewHolder.userNameTV.setText(user.getName());

        userViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, ChatActivity.class);
                intent.putExtra("visit_user_id", user.getUid());
                intent.putExtra("visit_user_name", user.getName());
                intent.putExtra("visit_image", user.getProfilePicUrl());
                context.startActivity(intent);
            }
        });

        //callsViewHolder.tvTime.setText(user.getTime());

    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    class UserListViewHolder extends RecyclerView.ViewHolder {
        CircleImageView userIV;
        TextView userNameTV;
        TextView tvTime;
        ImageView missedCallType;

        UserListViewHolder(@NonNull View itemView) {
            super(itemView);

            userNameTV = itemView.findViewById(R.id.userNameTV);
            userIV = itemView.findViewById(R.id.userIV);;

        }
    }
}
