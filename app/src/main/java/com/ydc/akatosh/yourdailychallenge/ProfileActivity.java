package com.ydc.akatosh.yourdailychallenge;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.squareup.picasso.Picasso;

public class ProfileActivity extends AppCompatActivity {

    private ImageView profileImage;

    private TextView profileName;
    private TextView profilePoints;

    private ProgressBar profileProgress;

    private boolean fetching;

    private FirebaseUser user;
    private FirebaseFirestore database = FirebaseFirestore.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        user = FirebaseAuth.getInstance().getCurrentUser();

        profileImage = findViewById(R.id.profile_profile_image_view);
        Picasso.with(ProfileActivity.this).load(user.getPhotoUrl()).into(profileImage);

        profileName = findViewById(R.id.profile_name);
        profileName.setText(user.getDisplayName());
        profilePoints = findViewById(R.id.profile_points);

        profileProgress = findViewById(R.id.profile_progress_bar);
        profileProgress.setVisibility(View.INVISIBLE);

        Button challengeButton = findViewById(R.id.profile_to_challenge_button);
        challengeButton.setOnClickListener(new ToChallenge());

        fetching = false;
    }

    @Override
    protected void onStart() {
        super.onStart();
        updateInterface();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        updateInterface();
    }

    protected void updateInterface() {
        user = FirebaseAuth.getInstance().getCurrentUser();

        onFetch();
        database.collection(getResources().getString(R.string.collection_users_path))
                .document(user.getUid())
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if (documentSnapshot == null) {
                            postFetch();
                            return;
                        }

                        Picasso.with(ProfileActivity.this).load(user.getPhotoUrl()).into(profileImage);

                        profileName.setText(user.getDisplayName());
                        profilePoints.setText(String.valueOf(documentSnapshot.get("points")));

                        postFetch();
                    }
                });
    }

    protected void onFetch() {
        fetching = true;

        profileProgress.setVisibility(View.VISIBLE);
    }

    protected void postFetch() {
        fetching = false;

        profileProgress.setVisibility(View.INVISIBLE);
    }

    protected boolean isFetching() {
        return fetching;
    }

    class ToChallenge implements View.OnClickListener {

        @Override
        public void onClick(View view) {
            if (isFetching())
                return;

            startActivity(new Intent(ProfileActivity.this, ChallengeActivity.class));
        }

    }

}
