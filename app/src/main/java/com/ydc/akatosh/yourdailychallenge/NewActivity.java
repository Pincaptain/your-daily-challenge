package com.ydc.akatosh.yourdailychallenge;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.squareup.picasso.Picasso;
import com.ydc.akatosh.yourdailychallenge.Classes.Challenge;

import java.text.DateFormat;
import java.util.Date;

/**
 * {@author Borjan Gjorovski}
 * {@date 23-02-2018}
 * {@link NewActivity}
 *
 * {@description Renders the form view that allows the
 * user to submit his own challenge. This view also provides
 * links to other activities.}
 */

public class NewActivity extends AppCompatActivity {

    /** View items **/
    private EditText pointsEditText;
    private EditText challengeEditText;

    private ProgressBar uploadProgressBar;

    private TextView uploadValidatorTextView;

    /** User and database field **/
    private FirebaseUser user;
    private final FirebaseFirestore database = FirebaseFirestore.getInstance();

    private boolean uploading = false;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new);

        Button clearButton = findViewById(R.id.new_clear_button);
        clearButton.setOnClickListener(new OnClear());

        Button submitButton = findViewById(R.id.new_challenge_submit_button);
        submitButton.setOnClickListener(new OnSubmit());

        uploadProgressBar = findViewById(R.id.new_challenge_progress_bar);
        uploadProgressBar.setVisibility(View.INVISIBLE);

        pointsEditText = findViewById(R.id.new_challenge_points);
        pointsEditText.setOnTouchListener(new OnEditTextChanged());

        challengeEditText = findViewById(R.id.new_challenge_content);
        challengeEditText.setOnTouchListener(new OnEditTextChanged());

        uploadValidatorTextView = findViewById(R.id.new_challenge_validator_text_view);
        uploadValidatorTextView.setVisibility(View.INVISIBLE);

        user = FirebaseAuth.getInstance().getCurrentUser();
    }

    @Override
    protected void onStart() {
        super.onStart();
        updateInterface();
    }

    /**
     * {@description Updates the user interface based on the
     * user status (anonymous/signed).}
     */
    protected void updateInterface() {
        ImageView profileImage = findViewById(R.id.new_profile_image_view);
        Picasso.with(this).load(user.getPhotoUrl()).into(profileImage);

        Button profileButton = findViewById(R.id.new_profile_button);
        profileButton.setText(user.getDisplayName());
        profileButton.setOnClickListener(new OnProfile());

        EditText dateText = findViewById(R.id.new_challenge_date);
        DateFormat dateFormat = DateFormat.getDateInstance();
        dateText.setText(dateFormat.format(new Date()));
        dateText.setEnabled(false);
    }

    /**
     * {@author Borjan Gjorovski}
     * {@date 23-02-2018}
     * {@link OnProfile}
     *
     * {@description Redirects the user to his profile
     * activity.}
     */
    class OnProfile implements View.OnClickListener {

        @Override
        public void onClick(View view) {
            startActivity(new Intent(NewActivity.this, ProfileActivity.class));
        }

    }

    /**
     * {@author Borjan Gjorovski}
     * {@date 23-02-2018}
     * {@link OnClear}
     *
     * {@description Clears all the input fields instantly.}
     */
    class OnClear implements View.OnClickListener {

        @Override
        public void onClick(View view) {
            if (isUploading())
                return;

            clearEditTexts(pointsEditText, challengeEditText);
            hideValidationMessage();
        }

    }

    /**
     * {@description Clears the text from the {@link EditText}.}
     *
     * {@param editTexts}
     */
    protected void clearEditTexts(EditText... editTexts) {
        for (EditText editText : editTexts) {
            editText.setText("");
        }
    }

    /**
     * {@description Enables/disables the edit text views.}
     *
     * {@param enable}
     * {@param editTexts}
     */
    protected void toggleEditTexts(boolean enable, EditText... editTexts) {
        for (EditText editText : editTexts) {
            editText.setEnabled(enable);
        }
    }

    /**
     * {@author Borjan Gjorovski}
     * {@date 23-02-2018}
     * {@link OnSubmit}
     *
     * {@description Wraps the data in a {@link Challenge}
     * class and sends it directly to the server for further processing.}
     */
    class OnSubmit implements View.OnClickListener {

        @Override
        public void onClick(View view) {
            if (isUploading())
                return;

            if (!validateEditTexts()) {
                displayValidationError();
                return;
            }

            Challenge challenge = new Challenge(String.valueOf(challengeEditText.getText()),
                    Integer.parseInt(String.valueOf(pointsEditText.getText())));

            onUpload();
            database.collection(getResources().getString(R.string.collection_pending_path))
                    .add(challenge).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                @Override
                public void onSuccess(DocumentReference documentReference) {
                    postUpload();
                    displaySuccessMessage();
                }
            });
        }

    }

    /**
     * {@description Validates the input fields.}
     * {@return Returns a boolean based on the validity.}
     */
    protected boolean validateEditTexts() {
        int MAX_CHAR_LENGTH = 220;
        try {
            Integer.parseInt(pointsEditText.getText().toString());
        } catch (NumberFormatException e) {
            return false;
        }
        return challengeEditText.getText().length() != 0 &&
                pointsEditText.getText().length() != 0 &&
                challengeEditText.getText().length() <= MAX_CHAR_LENGTH;
    }

    /**
     * {@description If the validation returns false this
     * warning is displayed to the screen.}
     */
    protected void displayValidationError() {
        uploadValidatorTextView.setTextColor(getResources().getColor(R.color.failure));
        uploadValidatorTextView.setText(getResources().getString(R.string.challenge_new_validation_warning));
        uploadValidatorTextView.setVisibility(View.VISIBLE);
    }

    /**
     * {@description In case the validation error displays something to your
     * screen this function should be used to hide/destroy those changes.}
     */
    protected void hideValidationMessage() {
        uploadValidatorTextView.setVisibility(View.INVISIBLE);
    }

    /**
     * {@description If the upload is successful this function visually informs
     * the user of that.}
     */
    protected void displaySuccessMessage() {
        uploadValidatorTextView.setTextColor(getResources().getColor(R.color.success));
        uploadValidatorTextView.setText(getResources().getString(R.string.challenge_new_successful));
        uploadValidatorTextView.setVisibility(View.VISIBLE);
    }

    /**
     * {@description Is triggered once the data starts uploading to the
     * database.}
     */
    protected void onUpload() {
        toggleEditTexts(false, challengeEditText, pointsEditText);
        uploadProgressBar.setVisibility(View.VISIBLE);
    }

    /**
     * {@description Is triggered once the data finishes uploading.}
     */
    protected void postUpload() {
        toggleEditTexts(true, challengeEditText, pointsEditText);
        clearEditTexts(challengeEditText, pointsEditText);
        uploadProgressBar.setVisibility(View.INVISIBLE);
    }

    protected boolean isUploading() {
        return uploading;
    }


    /**
     * {@author Borjan Gjorovski}
     * {@date 24-02-2018}
     * {@link OnEditTextChanged}
     *
     * {@description Controls the changes made in the EditText views.
     * Additionally it hides the validation message.}
     */
    protected class OnEditTextChanged implements View.OnTouchListener {

        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            view.performClick();
            hideValidationMessage();
            return false;
        }

    }

}
