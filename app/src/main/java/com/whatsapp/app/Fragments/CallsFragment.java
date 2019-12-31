package com.whatsapp.app.Fragments;


import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.database.annotations.Nullable;
import com.whatsapp.app.Adapters.CallsAdapter;
import com.whatsapp.app.MainActivity;
import com.whatsapp.app.Models.Call;
import com.whatsapp.app.R;

import java.util.ArrayList;


/**
 * A simple {@link Fragment} subclass.
 */
public class CallsFragment extends Fragment {

    private ArrayList<Call> calls;
    private RecyclerView rvCalls;
    private CallsAdapter callsAdapter;

    public static final String[] profileUrls = {"https://blog.rackspace.com/wp-content/uploads/2018/09/pumping-iron-arnold-schwarzenegger-1-1108x0-c-default-696x522.jpg",
            "https://www.rollingstone.com/wp-content/uploads/2018/06/rs-213329-R1247_FEA_Rogen_A.jpg?crop=900:600&width=440",
            "https://assets.entrepreneur.com/content/3x2/2000/20170118220227-GettyImages-471763092.jpeg",
            "https://static.ffx.io/images/$zoom_0.238%2C$multiply_1%2C$ratio_1.776846%2C$width_1059%2C$x_0%2C$y_55/t_crop_custom/w_800/q_86%2Cf_auto/cf3f16e35b79207935da03f7b4a7e7d6e484ff71"
    };

    public CallsFragment() {
        // Required empty public constructor
    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_calls, container, false);

        initialize(view);
        populateCalls();
        setCallsAdapter();

        return view;
    }

    private void initialize(View view) {
        rvCalls = view.findViewById(R.id.rvCalls);
        calls = new ArrayList<>();
    }


    private void populateCalls() {
        //Population logic goes here
        calls.add(new Call(profileUrls[0], "Arnold", "2:00 PM", Call.AUDIO));
        calls.add(new Call(profileUrls[2], "Elon", "Yesterday, 8:00 PM", Call.VIDEO));
        calls.add(new Call("Rohan", "Yesterday, 10:15 AM", Call.AUDIO));
    }

    private void setCallsAdapter() {

        rvCalls.setLayoutManager(new LinearLayoutManager(getContext()));
        callsAdapter = new CallsAdapter(getContext(), calls);
        rvCalls.setAdapter(callsAdapter);

    }
}
