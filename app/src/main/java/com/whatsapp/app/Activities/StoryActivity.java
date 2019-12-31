package com.whatsapp.app.Activities;

import androidx.appcompat.app.AppCompatActivity;
import jp.shts.android.storiesprogressview.StoriesProgressView;

import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;
import com.whatsapp.app.R;

import java.util.ArrayList;

public class StoryActivity extends AppCompatActivity implements StoriesProgressView.StoriesListener {

    private static final int PROGRESS_COUNT = 4;

    private StoriesProgressView storiesProgressView;
    private ImageView image;
    ArrayList<String> currentUserStatus = new ArrayList<>();

    private int counter = 0;
    private final int[] resources = new int[]{
            R.drawable.pic,
            R.drawable.profile,
            R.drawable.profile,
            R.drawable.profile
    };

    private final long[] durations = new long[]{
            500L, 1000L, 1500L, 4000L
    };

    long pressTime = 0L;
    long limit = 500L;

    private View.OnTouchListener onTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    pressTime = System.currentTimeMillis();
                    storiesProgressView.pause();
                    return false;
                case MotionEvent.ACTION_UP:
                    long now = System.currentTimeMillis();
                    storiesProgressView.resume();
                    return limit < now - pressTime;
            }
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_story);

        image = (ImageView) findViewById(R.id.image);

        if(getIntent()!=null){

            currentUserStatus = getIntent().getStringArrayListExtra("currentUserStatus");
        }

        storiesProgressView = findViewById(R.id.stories);
        storiesProgressView.setStoriesCount(currentUserStatus.size());
        storiesProgressView.setStoryDuration(3000L);




        // or
        // storiesProgressView.setStoriesCountWithDurations(durations);
        storiesProgressView.setStoriesListener(this);
//        storiesProgressView.startStories();
        counter = 0;
        storiesProgressView.startStories(counter);

        Picasso.get().load(currentUserStatus.get(counter)).into(image);
        //image.setImageResource(resources[counter]);

        // bind reverse view
        View reverse = findViewById(R.id.reverse);
        reverse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                storiesProgressView.reverse();
            }
        });
        reverse.setOnTouchListener(onTouchListener);

        // bind skip view
        View skip = findViewById(R.id.skip);
        skip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                storiesProgressView.skip();
            }
        });
        skip.setOnTouchListener(onTouchListener);
    }

    @Override
    public void onNext() {
        Picasso.get().load(currentUserStatus.get(++counter)).into(image);

        //image.setImageResource(resources[++counter]);
    }

    @Override
    public void onPrev() {
        if ((counter - 1) < 0) return;
        Picasso.get().load(currentUserStatus.get(--counter)).into(image);

        //image.setImageResource(resources[--counter]);
    }

    @Override
    public void onComplete() {
        finish();
    }

    @Override
    protected void onDestroy() {
        // Very important !
        storiesProgressView.destroy();
        super.onDestroy();
    }
}
