package everysight.phoneapp;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.io.IOException;

public class LoginActivity extends AppCompatActivity  {

    private static final String TAG = "EmailPassword";

    // [START declare_auth]
    private FirebaseAuth mAuth;

    private FirebaseAuth.AuthStateListener mAuthListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login2);

        mAuth = FirebaseAuth.getInstance();

        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // User is signed in
                    Log.d(TAG, "onAuthStateChanged:signed_in:" + user.getUid());
                } else {
                    // User is signed out
                    Log.d(TAG, "onAuthStateChanged:signed_out");
                }
                // ...
            }
        };
    }

    @Override
    public void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }

    public void OnClickSignIn(View v)
    {
        EditText email = (EditText) findViewById(R.id.emailText);
        EditText pass = (EditText) findViewById(R.id.PassText);

        SignIn(email.getText().toString(),pass.getText().toString());
    }

    public void OnClickSignUp(View v)
    {
        Button b = (Button) findViewById(R.id.button3);
        EditText name = (EditText) findViewById(R.id.nameText);
        if (b.getText().toString().equals("Register"))
        {
            name.setVisibility(View.VISIBLE);
            b.setText("Sign up");
            return;
        }

        EditText email = (EditText) findViewById(R.id.emailText);
        EditText pass = (EditText) findViewById(R.id.PassText);

        CreateAccount(name.getText().toString(),email.getText().toString(),pass.getText().toString());
    }


    public void CreateAccount(final String name,final String email,final String password)
    {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.d(TAG, "createUserWithEmail:onComplete:" + task.isSuccessful());

                        // If sign in fails, display a message to the user. If sign in succeeds
                        // the auth state listener will be notified and logic to handle the
                        // signed in user can be handled in the listener.
                        if (!task.isSuccessful()) {
                            Toast.makeText(LoginActivity.this, R.string.auth_failed,
                                    Toast.LENGTH_SHORT).show();
                        }
                        else
                        {
                            Intent resultIntent = new Intent();
                            resultIntent.putExtra("Name", name);
                            resultIntent.putExtra("Email", email);
                            resultIntent.putExtra("Password", password);
                            setResult(Activity.RESULT_OK, resultIntent);
                            finish();
                        }
                    }
                });
    }

    public void SignIn(final String email,final String password)
    {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.d(TAG, "signInWithEmail:onComplete:" + task.isSuccessful());

                        // If sign in fails, display a message to the user. If sign in succeeds
                        // the auth state listener will be notified and logic to handle the
                        // signed in user can be handled in the listener.
                        if (!task.isSuccessful()) {
                            Log.w(TAG, "signInWithEmail:failed", task.getException());
                            Toast.makeText(LoginActivity.this, R.string.auth_failed,
                                    Toast.LENGTH_SHORT).show();
                        }
                        else {
                            Intent resultIntent = new Intent();
                            resultIntent.putExtra("Name", "user");
                            resultIntent.putExtra("Email", email);
                            resultIntent.putExtra("Password", password);
                            setResult(Activity.RESULT_OK, resultIntent);
                            finish();
                        }
                        // ...
                    }
                });
    }


}
