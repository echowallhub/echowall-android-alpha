package com.echodev.echoalpha;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.support.annotation.MainThread;
import android.support.annotation.StringRes;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.echodev.echoalpha.util.ImageHelper;
import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.ErrorCodes;
import com.firebase.ui.auth.IdpResponse;
import com.firebase.ui.auth.ResultCodes;
import com.google.firebase.auth.FirebaseAuth;

import java.util.Arrays;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "EchoAlphaMain";
    private static final int RC_SIGN_IN = 100;

    @BindView(R.id.activity_main)
    View mRootView;

    @BindView(R.id.cover_image)
    ImageView coverImage;

    @BindView(R.id.sign_in_btn)
    Button signInBtn;

    @BindView(R.id.quit_btn)
    Button quitBtn;

    @BindView(R.id.debug_text_main)
    TextView debugTextMain;

    Resources localRes;

    private FirebaseAuth mAuth;
    private IdpResponse mIdpResponse;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        localRes = this.getResources();

        mAuth = FirebaseAuth.getInstance();
        if (mAuth.getCurrentUser() != null) {
            // User is signed in
            startWall();
        } else {
            // User is signed out
        }

        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        // To ensure the set-picture function is called after the activity's drawing phase is finished
        coverImage.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                coverImage.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                ImageHelper.setPicFromResources(coverImage, localRes, R.drawable.cover_lowres, debugTextMain);
            }
        });
    }

    @OnClick(R.id.sign_in_btn)
    public void signIn(View view) {
        startActivityForResult(
                AuthUI.getInstance().createSignInIntentBuilder()
                        .setTheme(R.style.EchoTheme)
                        .setLogo(R.drawable.echo_logo_200px)
                        .setProviders(Arrays.asList(
                                new AuthUI.IdpConfig.Builder(AuthUI.EMAIL_PROVIDER).build(),
                                new AuthUI.IdpConfig.Builder(AuthUI.GOOGLE_PROVIDER).build()))
                        .setIsSmartLockEnabled(false)
                        .build(),
                RC_SIGN_IN);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            handleSignInResponse(resultCode, data);
            return;
        }

        showSnackbar(R.string.unknown_response);
    }

    @MainThread
    private void handleSignInResponse(int resultCode, Intent data) {
        mIdpResponse = IdpResponse.fromResultIntent(data);

        // Successfully signed in
        if (resultCode == ResultCodes.OK) {
            startActivity(WallActivity.createIntent(this, mIdpResponse));
            finish();
            return;
        } else {
            // Sign in failed
            if (mIdpResponse == null) {
                // User pressed back button
                showSnackbar(R.string.sign_in_cancelled);
                return;
            }

            if (mIdpResponse.getErrorCode() == ErrorCodes.NO_NETWORK) {
                showSnackbar(R.string.no_internet_connection);
                return;
            }

            if (mIdpResponse.getErrorCode() == ErrorCodes.UNKNOWN_ERROR) {
                showSnackbar(R.string.unknown_error);
                return;
            }
        }

        showSnackbar(R.string.unknown_sign_in_response);
    }

    private void startWall() {
//        Intent intent = new Intent(this, WallActivity.class);
//        startActivity(intent);
        startActivity(WallActivity.createIntent(this, null));
        finish();
    }

    @MainThread
    private void showSnackbar(@StringRes int errorMessageRes) {
        Snackbar.make(mRootView, errorMessageRes, Snackbar.LENGTH_LONG).show();
    }

    public static Intent createIntent(Context context) {
        Intent intent = new Intent();
        intent.setClass(context, MainActivity.class);
        return intent;
    }

    @OnClick(R.id.quit_btn)
    public void quitApp(View view) {
        this.finishAffinity();
    }
}
