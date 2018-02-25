package com.ydc.akatosh.yourdailychallenge.Classes;

import android.net.Uri;

import com.google.firebase.auth.FirebaseUser;

import java.util.HashMap;
import java.util.Map;

/**
 * {@author Borjan Gjorovski}
 * {@date 18-02-2018}
 * {@link User}
 *
 * {@description Stores a local copy of the users attributes.}
 */

public class User {

    private String name;
    private String id;
    private String email;
    private Uri photo;
    private String provider;
    private boolean anonymous;
    private Challenge challenge;

    public User() {}

    public User(String name, String id, String email, Uri photo, String provider, boolean anonymous,
                Challenge challenge) {
        this.name = name;
        this.id = id;
        this.email = email;
        this.photo = photo;
        this.provider = provider;
        this.anonymous = anonymous;
        this.challenge = challenge;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Uri getPhoto() {
        return photo;
    }

    public void setPhoto(Uri photo) {
        this.photo = photo;
    }

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public boolean isAnonymous() {
        return anonymous;
    }

    public void setAnonymous(boolean anonymous) {
        this.anonymous = anonymous;
    }

    public Challenge getChallenge() {
        return challenge;
    }

    public void setChallenge(Challenge challenge) {
        this.challenge = challenge;
    }

    /** {@description Builds a new local instance of the currently signed in user.} */
    public static User build(FirebaseUser firebaseUser) {
        User user = new User();

        if (firebaseUser == null) {
            user.name = "Anonymous Player";
            user.id = "0";
            user.email = "0";
            user.photo = Uri.parse("https://cdn.pixabay.com/photo/2016/08/08/09/17/avatar-1577909_960_720.png");
            user.provider = "Anonymous";
            user.anonymous = true;
            user.challenge = new Challenge();
        } else {
            user.name = firebaseUser.getDisplayName();
            user.id = firebaseUser.getUid();
            user.email = firebaseUser.getEmail();
            user.photo = firebaseUser.getPhotoUrl();
            user.provider = firebaseUser.getProviderId();
            user.anonymous = false;
            user.challenge = new Challenge();
        }

        return user;
    }

    /** Builds a new quick local instance of the currently signed in user **/
    public static Map<String, Object> quickBuild(FirebaseUser firebaseUser) {
        Map<String, Object> user = new HashMap<>();
        user.put("id", firebaseUser.getUid());
        user.put("points", 0);
        user.put("day", -1);
        return user;
    }

}
