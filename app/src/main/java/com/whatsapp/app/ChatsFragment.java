package com.whatsapp.app;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;
import com.whatsapp.app.Activities.UserListActivity;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import de.hdodenhof.circleimageview.CircleImageView;


/**
 * A simple {@link Fragment} subclass.
 */
public class ChatsFragment extends Fragment
{
    private View PrivateChatsView;
    private RecyclerView chatsList;

    private DatabaseReference ChatsRef, UsersRef;
    private FirebaseAuth mAuth;
    private String currentUserID="";

    FloatingActionButton fab;


    public ChatsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        PrivateChatsView = inflater.inflate(R.layout.fragment_chats, container, false);

        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser() != null? mAuth.getCurrentUser().getUid() : null;
        if(currentUserID != null)ChatsRef = FirebaseDatabase.getInstance().getReference().child("Messages").child(currentUserID);
        UsersRef = FirebaseDatabase.getInstance().getReference().child("Users");

        fab = PrivateChatsView.findViewById(R.id.fab);
        chatsList = (RecyclerView) PrivateChatsView.findViewById(R.id.chats_list);
        chatsList.setLayoutManager(new LinearLayoutManager(getContext()));

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getContext(), UserListActivity.class);
                startActivity(intent);
            }
        });

        return PrivateChatsView;
    }

    @Override
    public void onStart()
    {
        super.onStart();

        FirebaseRecyclerOptions<Contacts> options = new FirebaseRecyclerOptions.Builder<Contacts>()
                        .setQuery(ChatsRef, Contacts.class)
                        .build();

        FirebaseRecyclerAdapter<Contacts, ChatsViewHolder> adapter =
                new FirebaseRecyclerAdapter<Contacts, ChatsViewHolder>(options) {
                    @Override
                    protected void onBindViewHolder(@NonNull final ChatsViewHolder holder, int position, @NonNull Contacts model)
                    {
                        final String usersIDs = getRef(position).getKey();
                        final String[] retImage = {"default_image"};

                        Query lastQuery = ChatsRef.child(usersIDs).orderByKey().limitToLast(1);
                        lastQuery.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {

                                if (dataSnapshot.exists()) {

                                    Map<String,Messages>  map = (Map<String,Messages>)dataSnapshot.getValue();

                                    Map.Entry<String,Messages> entry = map.entrySet().iterator().next();
                                    Map<String,String> lastMsgObject = (Map<String,String>)entry.getValue();

                                    String date = lastMsgObject.get("date");
                                    String time  = lastMsgObject.get("time");

                                    SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy");

                                    Date c = Calendar.getInstance().getTime();
                                    String today = sdf.format(c);

                                    try {
                                        Date strDate = sdf.parse(date);
                                        Date now = sdf.parse(today);
                                        if (now.equals(strDate)) {
                                            holder.userStatus.setText(time);
                                        }else{
                                            holder.userStatus.setText(date);
                                        }

                                    } catch (ParseException e) {
                                        e.printStackTrace();
                                    }


                                    if (lastMsgObject.get("type").equals("text")) {

                                        String lastMsg = lastMsgObject.get("message");
                                        holder.tvLastMsg.setText(lastMsg);

                                    }else if(lastMsgObject.get("type").equals("image")){

                                        holder.tvLastMsg.setText("Photo");

                                        Drawable dr = getResources().getDrawable(R.drawable.ib_camera);
                                        Bitmap bitmap = ((BitmapDrawable) dr).getBitmap();
                                        Drawable d = new BitmapDrawable(getResources(), Bitmap.createScaledBitmap(bitmap, 45, 45, true));
                                        holder.tvLastMsg.setCompoundDrawablesWithIntrinsicBounds(d,null,null,null);

                                    }else if(lastMsgObject.get("type").equals("audio")){

                                        String dd = String.valueOf(lastMsgObject.get("duration"));
                                        long duration = Long.parseLong(dd);

                                        String durationFormatted = String.format("%02d:%02d",
                                                TimeUnit.MILLISECONDS.toMinutes(duration),
                                                TimeUnit.MILLISECONDS.toSeconds(duration) -
                                                        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(duration)));

                                        holder.tvLastMsg.setText(durationFormatted);

                                        Drawable dr = getResources().getDrawable(R.drawable.mic_blue);
                                        Bitmap bitmap = ((BitmapDrawable) dr).getBitmap();
                                        Drawable d = new BitmapDrawable(getResources(), Bitmap.createScaledBitmap(bitmap, 45, 45, true));
                                        holder.tvLastMsg.setCompoundDrawablesWithIntrinsicBounds(d,null,null,null);


                                    }else if(lastMsgObject.get("type").equals("pdf")){

                                        String pdfName = lastMsgObject.get("name");
                                        holder.tvLastMsg.setText(pdfName);

                                        Drawable dr = getResources().getDrawable(R.drawable.file);
                                        Bitmap bitmap = ((BitmapDrawable) dr).getBitmap();
                                        Drawable d = new BitmapDrawable(getResources(), Bitmap.createScaledBitmap(bitmap, 45, 45, true));
                                        holder.tvLastMsg.setCompoundDrawablesWithIntrinsicBounds(d,null,null,null);

                                    }

                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {
                                // Handle possible errors.
                            }
                        });

                        UsersRef.child(usersIDs).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot)
                            {
                                if (dataSnapshot.exists())
                                {
                                    if (dataSnapshot.hasChild("image"))
                                    {
                                        retImage[0] = dataSnapshot.child("image").getValue().toString();
                                        Picasso.get().load(retImage[0]).into(holder.profileImage);
                                    }

                                    final String retName = dataSnapshot.child("name").getValue().toString();
                                    //final String retStatus = dataSnapshot.child("status").getValue().toString();

                                    holder.userName.setText(retName);


                                    if (dataSnapshot.child("userState").hasChild("state"))
                                    {
                                        String state = dataSnapshot.child("userState").child("state").getValue().toString();
                                        String date = dataSnapshot.child("userState").child("date").getValue().toString();
                                        String time = dataSnapshot.child("userState").child("time").getValue().toString();

                                        if (state.equals("online"))
                                        {
                                            //holder.userStatus.setText("online");
                                        }
                                        else if (state.equals("offline"))
                                        {
                                            //holder.userStatus.setText("Last Seen: " + date + " " + time);
                                            //holder.userStatus.setText(time);
                                        }
                                    }
                                    else
                                    {
                                        //holder.userStatus.setText("offline");
                                    }

                                    holder.itemView.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view)
                                        {
                                            Intent chatIntent = new Intent(getContext(), ChatActivity.class);
                                            chatIntent.putExtra("visit_user_id", usersIDs);
                                            chatIntent.putExtra("visit_user_name", retName);
                                            chatIntent.putExtra("visit_image", retImage[0]);
                                            startActivity(chatIntent);
                                        }
                                    });
                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });
                    }

                    @NonNull
                    @Override
                    public ChatsViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i)
                    {
                        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.chat_row, viewGroup, false);
                        return new ChatsViewHolder(view);
                    }
                };

        chatsList.setAdapter(adapter);
        adapter.startListening();
    }




    public static class  ChatsViewHolder extends RecyclerView.ViewHolder
    {
        CircleImageView profileImage;
        TextView userStatus, userName,tvLastMsg;


        public ChatsViewHolder(@NonNull View itemView)
        {
            super(itemView);

            profileImage = itemView.findViewById(R.id.users_profile_image);
            userStatus = itemView.findViewById(R.id.user_status);
            userName = itemView.findViewById(R.id.user_profile_name);
            tvLastMsg = itemView.findViewById(R.id.tvLastMsg);
        }
    }
}