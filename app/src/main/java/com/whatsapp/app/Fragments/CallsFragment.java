package com.whatsapp.app.Fragments;


import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.annotations.Nullable;
import com.whatsapp.app.Adapters.CallsAdapter;
import com.whatsapp.app.Models.Call;
import com.whatsapp.app.R;

import java.util.ArrayList;


/**
 * A simple {@link Fragment} subclass.
 */
public class CallsFragment extends Fragment {

    private RecyclerView rvCalls;
    private CallsAdapter callsAdapter;
    String currentUserID;
    private DatabaseReference RootRef;
    private final ArrayList<Call> callsList = new ArrayList<>();

    public CallsFragment() {
        // Required empty public constructor
    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_calls, container, false);

        initialize(view);
        setCallsAdapter();
        populateCalls();

        return view;
    }

    private void initialize(View view) {
        rvCalls = view.findViewById(R.id.rvCalls);
        currentUserID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        RootRef = FirebaseDatabase.getInstance().getReference();
    }


    private void populateCalls() {

        RootRef.child("Calls").child(currentUserID).addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(DataSnapshot dataSnapshot, String s)
                    {
                        for (DataSnapshot child:dataSnapshot.getChildren()) {

                            Call call = child.getValue(Call.class);
                            callsList.add(call);
                        }
                        callsAdapter.notifyDataSetChanged();

                    }

                    @Override
                    public void onChildChanged(DataSnapshot dataSnapshot, String s) {

                    }

                    @Override
                    public void onChildRemoved(DataSnapshot dataSnapshot) {

                    }

                    @Override
                    public void onChildMoved(DataSnapshot dataSnapshot, String s) {

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

    }

    private void setCallsAdapter() {

        rvCalls.setLayoutManager(new LinearLayoutManager(getContext()));
        callsAdapter = new CallsAdapter(getContext(), callsList);
        rvCalls.setAdapter(callsAdapter);

    }
}
