package com.ydc.akatosh.yourdailychallenge;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.squareup.picasso.Picasso;
import com.ydc.akatosh.yourdailychallenge.Classes.Challenge;
import com.ydc.akatosh.yourdailychallenge.Classes.User;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Random;
import java.util.TimeZone;

/**
 * {@author Borjan Gjorovski}
 * {@date 19-02-2018}
 * {@link ChallengeActivity}
 *
 * {@description Game activity responsible for displaying
 * the challenge text, handling the challenge events and providing
 * a link to your user specific profile.}
 */

public class ChallengeActivity extends AppCompatActivity {

    /** True when and only when the user is fetching data from the database **/
    private boolean fetching = false;

    /** View items **/
    private Button challengeFinishedButton;
    private Button reRollButton;
    private Button profileButton;
    private Button newChallengeButton;

    private TextView challengeTextView;

    private ProgressBar challengeProgressBar;

    /** User field **/
    private User user;
    private FirebaseUser firebaseUser;

    /** Database field **/
    private final FirebaseFirestore database = FirebaseFirestore.getInstance();

    private InterstitialAd interstitialAd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_challenge);

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        user = new User(firebaseUser.getUid(), -1, 0, new Challenge());

        interstitialAd = new InterstitialAd(this);
        interstitialAd.setAdUnitId(getResources().getString(R.string.interstitial_ad_id));
        interstitialAd.loadAd(new AdRequest.Builder().build());

        challengeFinishedButton = findViewById(R.id.challenge_finished_button);
        challengeFinishedButton.setOnClickListener(new OnChallengeFinished());

        reRollButton = findViewById(R.id.re_roll_button);
        reRollButton.setOnClickListener(new OnReRoll());

        profileButton = findViewById(R.id.profile_button);
        profileButton.setOnClickListener(new OnProfile());

        newChallengeButton = findViewById(R.id.new_challenge_button);
        newChallengeButton.setOnClickListener(new OnNewChallenge());

        challengeProgressBar = findViewById(R.id.challenge_progress_bar);
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

    /**
     * {@description Updates the user interface based on the
     * user status (anonymous/signed).}
     */
    protected void updateInterface() {
        onFetch();
        user.setChallenge(loadChallenge());
        loadReRollStatus();

        challengeTextView = findViewById(R.id.challenge_text_view);
        challengeTextView.setText(user.getChallenge().getChallenge());

        ImageView profileImage = findViewById(R.id.profile_image_view);
        Picasso.with(this).load(firebaseUser.getPhotoUrl()).into(profileImage);

        profileButton.setText(firebaseUser.getDisplayName());

        if (user.getChallenge().isFinished()) {
            disableButton(challengeFinishedButton,
                    getResources().getString(R.string.challenge_completed_text));
        } else {
            enableButton(challengeFinishedButton,
                    getResources().getString(R.string.challenge_finished_text));
        }

        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            updateChallenge();
        }
    }

    /**
     * {@description Once triggered the function runs multiple conditions and
     * decides whether it should update the challenge or not.}
     */
    protected void updateChallenge() {
        database.collection(getResources().getString(R.string.collection_users_path))
                .whereEqualTo("id", user.getId())
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot documentSnapshots) {
                if (documentSnapshots.isEmpty()) {
                    // Invalid user id
                    postFetch();
                    return;
                }

                DocumentSnapshot userSnapshot = documentSnapshots.getDocuments().get(0);
                long userDay = (long) userSnapshot.get("day");
                Calendar calendar = Calendar.getInstance(TimeZone.getDefault());

                if (userDay == calendar.get(Calendar.MINUTE)) {
                    // Too early to change the challenge
                    postFetch();
                    return;
                }

                // Once here the code will modify the users current challenge
                userSnapshot.getReference().update("day", calendar.get(Calendar.MINUTE));

                // Enables the re-roll button since a day passed
                enableReRoll();
                saveReRollStatus();


                // Applies visual changes and stores the new challenge
                addChallenge();
            }
        });
    }

    /**
     * {@description Once decided this function updates the visuals and
     * stores the new challenge information.}
     */
    protected void addChallenge() {
        fetching = true;
        database.collection(getResources().getString(R.string.collection_challenges_path))
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot documentSnapshots) {
                // Grabs all the challenges form the database
                ArrayList<DocumentSnapshot> snapshots =
                        (ArrayList<DocumentSnapshot>) documentSnapshots.getDocuments();
                // Randomly selects one
                DocumentSnapshot snapshot =
                        snapshots.get(new Random().nextInt(documentSnapshots.size()));

                // Populates the user challenge object with the selected database challenge
                user.setChallenge(snapshot.toObject(Challenge.class));
                // Updates the text view with the new challenge and some other visual changes
                challengeTextView.setText(user.getChallenge().getChallenge());
                enableButton(challengeFinishedButton, getResources()
                        .getString(R.string.challenge_finished_text));

                // Stores the challenge locally
                saveChallenge(user.getChallenge().isFinished());

                // When loading ends
                postFetch();

                fetching = false;
            }
        });
    }

    /**
     * {@description Saves the challenge locally using the
     * {@link SharedPreferences}.}
     *
     * {@param finished}
     */
    protected void saveChallenge(boolean finished) {
        // Local save
        SharedPreferences sharedPreferences = ChallengeActivity.this.getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.putBoolean(getResources().getString(R.string.saved_challenge_finished), finished);
        editor.putString(getResources().getString(R.string.saved_challenge_text), user.getChallenge().getChallenge());
        editor.putLong(getResources().getString(R.string.saved_challenge_points), user.getChallenge().getPoints());
        editor.apply();
    }

    /**
     * {@description Loads a locally saved file. Using
     * {@link SharedPreferences}.}
     *
     * {@return {@link Challenge}}
     */
    protected Challenge loadChallenge() {
        SharedPreferences sharedPreferences = ChallengeActivity.this.getPreferences(Context.MODE_PRIVATE);
        String challenge = sharedPreferences.getString(getResources().getString(R.string.saved_challenge_text),
                getResources().getString(R.string.challenge_placeholder));
        long points = sharedPreferences.getLong(getResources().getString(R.string.saved_challenge_points), 0);
        boolean finished = sharedPreferences.getBoolean(getResources().getString(R.string.saved_challenge_finished), false);

        Challenge result = new Challenge(challenge, points);
        result.setFinished(finished);

        return result;
    }

    protected void onFetch() {
        challengeProgressBar.setVisibility(View.VISIBLE);

        newChallengeButton.setTextColor(getResources().getColor(R.color.colorAccentDarker));
        reRollButton.setTextColor(getResources().getColor(R.color.colorAccentDarker));
        challengeFinishedButton.setTextColor(getResources().getColor(R.color.colorAccentDarker));
        profileButton.setTextColor(getResources().getColor(R.color.colorAccentDarker));
    }

    protected void postFetch() {
        challengeProgressBar.setVisibility(View.INVISIBLE);

        newChallengeButton.setTextColor(getResources().getColor(R.color.colorAccent));
        reRollButton.setTextColor(getResources().getColor(R.color.colorAccent));
        challengeFinishedButton.setTextColor(getResources().getColor(R.color.colorAccent));
        profileButton.setTextColor(getResources().getColor(R.color.colorAccent));
    }

    protected boolean isFetching() {
        return fetching;
    }

    /**
     * {@author Borjan Gjorovski}
     * {@date 19-02-2018}
     * {@link OnReRoll}
     *
     * {@description Handles the outcome of the re-roll button
     * that will provide the service of changing the daily challenge.
     * May only be used once per day.}
     */
    class OnReRoll implements View.OnClickListener {

        @Override
        public void onClick(View view) {
            if (isFetching())
                return;

            if (interstitialAd.isLoaded()) {
                interstitialAd.show();
            }

            onFetch();
            addChallenge();
            disableReRoll();
            saveReRollStatus();
        }

    }

    /**
     * {@description Once clicked this function disables the functionality
     * of the re-roll.}
     */
    public void disableReRoll() {
        reRollButton.setEnabled(false);
        reRollButton.setVisibility(View.INVISIBLE);
    }

    /**
     * {@description This function enables the functionality of the
     * re-roll.}
     */
    public void enableReRoll() {
        reRollButton.setEnabled(true);
        reRollButton.setVisibility(View.VISIBLE);
    }

    /**
     * {@description This functions stores the re-roll status locally.}
     */
    public void saveReRollStatus() {
        SharedPreferences sharedPreferences = ChallengeActivity.this.getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.putBoolean(getResources().getString(R.string.saved_re_roll_status), reRollButton.isEnabled());
        editor.apply();
    }

    /**
     * {@description This function loads the re-roll status and
     * populates the view.}
     */
    public void loadReRollStatus() {
        SharedPreferences sharedPreferences = ChallengeActivity.this.getPreferences(Context.MODE_PRIVATE);
        boolean reRollStatus = sharedPreferences.getBoolean(getResources().getString(R.string.saved_re_roll_status), true);

        if (reRollStatus) {
            enableReRoll();
        } else {
            disableReRoll();
        }
    }

    /**
     * {@author Borjan Gjorovski}
     * {@date 19-02-2018}
     * {@link OnChallengeFinished}
     *
     * {@description Handles what happens once the user
     * finishes the challenge.}
     */
    class OnChallengeFinished implements View.OnClickListener {

        @Override
        public void onClick(View view) {
            if (isFetching())
                return;

            disableButton(challengeFinishedButton,
                    getResources().getString(R.string.challenge_completed_text));

            user.getChallenge().setFinished(true);

            saveChallenge(true);
            updatePoints();

            disableReRoll();
            saveReRollStatus();
        }

    }

    /**
     * {@description Disables the functionality of the {@link #challengeFinishedButton}
     * and displays a message congratulating the user.}
     *
     * {@param button}
     * {@param disabledText}
     */
    protected void disableButton(Button button, String disabledText) {
        button.setText(disabledText);
        button.setEnabled(false);
        button.setBackgroundColor(Color.TRANSPARENT);
        button.setTextSize(20);
    }

    /**
     * {@description Enables the functionality of the {@link #challengeFinishedButton}.}
     *
     * {@param button}
     * {@param enabledText}
     */
    protected void enableButton(Button button, String enabledText) {
        button.setText(enabledText);
        button.setEnabled(true);
        button.setBackground(getResources().getDrawable(R.drawable.bind_button));
        button.setTextSize(16);
    }

    /**
     * {@description Triggered once the player finishes the daily
     * challenge. This function will update the current number of user points.}
     */
    protected void updatePoints() {
        database.collection(getResources().getString(R.string.collection_users_path))
            .whereEqualTo("id", user.getId())
            .get()
            .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                @Override
                public void onSuccess(QuerySnapshot documentSnapshots) {
                    if (documentSnapshots.isEmpty()) {
                        return;
                    }

                    DocumentSnapshot userSnapshot = documentSnapshots.getDocuments().get(0);

                    if (user.getChallenge() != null && user.getChallenge().isFinished()) {
                        userSnapshot.getReference().update("points",
                                ((long)userSnapshot.get("points")) + user.getChallenge().getPoints());
                    }
                }
            });
    }

    /**
     * {@author Borjan Gjorovski}
     * {@date 20-02-2018}
     * {@link OnProfile}
     *
     * {@description Redirects the user to his profile
     * activity.}
     */
    class OnProfile implements View.OnClickListener {

        @Override
        public void onClick(View view) {
            startActivity(new Intent(ChallengeActivity.this, ProfileActivity.class));
        }

    }

    /**
     * {@author Borjan Gjorovski}
     * {@date 22-02-2018}
     * {@link OnNewChallenge}
     *
     * {@description Opens a new form activity that allows the user
     * to submit his own challenge. The challenge of course must me
     * approved by our team.}
     */
    class OnNewChallenge implements View.OnClickListener {

        @Override
        public void onClick(View view) {
            if (isFetching())
                return;

            startActivity(new Intent(ChallengeActivity.this, NewActivity.class));
        }

    }

}
