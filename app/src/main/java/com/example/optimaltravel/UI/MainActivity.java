package com.example.optimaltravel.UI;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.example.optimaltravel.R;
import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.IdpResponse;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final int RC_SIGN_IN = 123;
    private SharedPreferences sharedPreferences;
    String id = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        sharedPreferences = getSharedPreferences("USER", MODE_PRIVATE);
        if (FirebaseAuth.getInstance().getCurrentUser()!=null) {
            id = FirebaseAuth.getInstance().getCurrentUser().getUid();
            if (sharedPreferences.getBoolean(id, false)) {
                startActivity(new Intent(this, CreatePath.class));
            }
        }
    }

    public void loginActivityLaunch(View view) {
        // Choose authentication providers
        // Choose authentication providers
        //new AuthUI.IdpConfig.PhoneBuilder().setDefaultCountryIso("IL").build()
        List<AuthUI.IdpConfig> providers = Arrays.asList(
                new AuthUI.IdpConfig.EmailBuilder().build()
                , new AuthUI.IdpConfig.AnonymousBuilder().build()


        );

// Create and launch sign-in intent
        startActivityForResult(
                AuthUI.getInstance()
                        .createSignInIntentBuilder()
                        .setAvailableProviders(providers).setLogo(R.drawable.logo).build(),
                RC_SIGN_IN);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            IdpResponse response = IdpResponse.fromResultIntent(data);

            if (resultCode == RESULT_OK) {
                // Successfully signed in
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                id = user.getUid();
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putBoolean(id, true);
                editor.apply();
                startActivity(new Intent(this, CreatePath.class));
                // ...
            } else {
                // Sign in failed. If response is null the user canceled the
                // sign-in flow using the back button. Otherwise check
                // response.getError().getErrorCode() and handle the error.
                // ...
            }
        }
    }
}