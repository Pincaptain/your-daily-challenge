package com.ydc.akatosh.yourdailychallenge;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.ydc.akatosh.yourdailychallenge.Classes.User;

import java.util.Arrays;
import java.util.List;

/**
 * {@author Borjan Gjorovski}
 * {@date 17-02-2018}
 * {@link MainActivity}
 *
 * {@description {@link MainActivity} is the starting point and the
 * main menu of this application. Apart from the common start/exit options
 * this activity also requires and prompts google or email sign in.}
 */

public class MainActivity extends AppCompatActivity {

    /** Creates an instance for the database. **/
    private final FirebaseFirestore database = FirebaseFirestore.getInstance();

    /** Sign in request code **/
    private static final int RC_SIGN_IN = 123;

    /** Available authentication providers **/
    private static final List<AuthUI.IdpConfig> providers = Arrays.asList(
            new AuthUI.IdpConfig.Builder(AuthUI.EMAIL_PROVIDER).build(),
            new AuthUI.IdpConfig.Builder(AuthUI.GOOGLE_PROVIDER).build());

    /**
     * {@description Is called the first time the user enters
     * the activity and it initializes the view.}
     *
     * {@param savedInstanceState}
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button exitButton = findViewById(R.id.exit_button);
        exitButton.setOnClickListener(new OnExitActivity());

        Button optionsButton = findViewById(R.id.options_button);
        optionsButton.setOnClickListener(new OnOptions());

        Button startButton = findViewById(R.id.start_button);
        startButton.setOnClickListener(new OnStart());
    }

    /**
     * {@author Borjan Gjorovski}
     * {@date 18-02-2018}
     * {@link OnExitActivity}
     *
     * {@description Once triggered this functional interface closes
     * the activity.}
     */
    protected class OnExitActivity implements View.OnClickListener {

        @Override
        public void onClick(View view) {
            finish();
        }

    }

    /**
     * {@author Borjan Gjorovski}
     * {@date 22-02-2018}
     * {@link OnOptions}
     *
     * {@description Once triggered this functional interface starts
     * the options activity. An activity where the user can modify the
     * default preferences.}
     */
    protected class OnOptions implements View.OnClickListener {

        @Override
        public void onClick(View view) {
            // TODO - Provide a new settings/options activity
        }

    }

    /**
     * {@author Borjan Gjorovski}
     * {@date 18-02-2018}
     * {@link OnStart}
     *
     * {@description Once triggered this functional interface
     * starts the {@link ChallengeActivity}. Serves as a one way portal
     * to another activity.}
     */
    private class OnStart implements View.OnClickListener {

        @Override
        public void onClick(View view) {
            startActivity(new Intent(MainActivity.this, ChallengeActivity.class));
        }

    }

    /**
     * {@description When the activity calls this method the
     * user is directly prompt to sign in by using the {@link #signIn()} function.}
     */
    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        if (firebaseUser == null) signIn();
        else syncUser(firebaseUser);
    }

    /**
     * {@description Using the {@link AuthUI} a sign in intent is triggered.
     * The intent prompts the user to pick one from the previously set list of operators {@link #providers}
     * as his sign in operator.}
     */
    public void signIn() {
        startActivityForResult(
            AuthUI.getInstance()
            .createSignInIntentBuilder()
            .setAvailableProviders(providers)
            .build(),
            RC_SIGN_IN);
    }

    /**
     * {@description Handles the {@link #startActivityForResult(Intent, int)}
     * result and if the result is OK then a new {@link FirebaseUser} variable
     * populated by the users data is instantiated.}
     *
     * {@param requestCode}
     * {@param resultCode}
     * {@param data}
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN && resultCode == RESULT_OK) {
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            syncUser(user);
        }
    }

    /**
     * {@description Synchronizes the user to the custom made
     * database collection.}
     *
     * {@param user}
     */
    public void syncUser(final FirebaseUser user) {
        database.collection(getResources().getString(R.string.collection_users_path)).whereEqualTo("id", user.getUid()).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot documentSnapshots) {
                if (documentSnapshots.isEmpty()) {
                        database.collection(getResources().getString(R.string.collection_users_path)).document(user.getUid()).set(User.quickBuild(user));
                }
            }
        });
    }

    /**
     * {@description Using the {@link AuthUI} the user is signed out and
     * a message is printed based on the result.}
     */
    public void signOut() {
        AuthUI.getInstance().signOut(this);
    }

    /**
     * {@description Using the {@link AuthUI} the users account is deleted and a
     * message is printed based on the result.}
     */
    public void delete() {
        AuthUI.getInstance().delete(this);
    }

}
