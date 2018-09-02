package com.moonstone.ezmaps_app;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ProgressBar;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;

public class UserSignUp extends AppCompatActivity implements View.OnClickListener{

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    @BindView(R.id.emailField) EditText _emailField;
    @BindView(R.id.passwordField) EditText _passwordField;
    @BindView(R.id.nameField) EditText _nameField;
    @BindView(R.id.signUpButton) Button _signUpButton;
    @BindView(R.id.textViewLogin) TextView _loginLink;
    @BindView(R.id.progressBar) ProgressBar _progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);
        ButterKnife.bind(this);

        _loginLink.setOnClickListener(this);
        _signUpButton.setOnClickListener(this);
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
    }

    private void registerUser(){
        final String email = _emailField.getText().toString().trim();
        final String password = _passwordField.getText().toString().trim();

        if(email.isEmpty()){
            _emailField.setError("Email is required");
            _emailField.requestFocus();
            return;
        }
        if(password.isEmpty()){
            _passwordField.setError("Password is required");
            _passwordField.requestFocus();
            return;
        }

        if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            _emailField.setError("Please enter valid email");
            _emailField.requestFocus();
            return;
        }

        if(password.length()<5){
            _passwordField.setError("Password is required to be longer than 5 characters");
            _passwordField.requestFocus();
            return;
        }

        _progressBar.setVisibility(View.VISIBLE);

        mAuth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            //Anonymous listener class
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                _progressBar.setVisibility(View.GONE);
                if (task.isSuccessful()){
                    Toast.makeText(getApplicationContext(), "User Register Successful", Toast.LENGTH_SHORT).show();
                    //Switch to main app
                    final Intent intent = new Intent(UserSignUp.this, MainActivity.class);
                    //Clears all activities currently active on the stack as the login stage is done now
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

                    //Set up basic parameters
                    final Map<String, Object> userMap = new HashMap<>();
                    final ArrayList<String> contacts = new ArrayList<>();
                    userMap.put("email", email);
                    userMap.put("contacts", contacts);
                    userMap.put("profilePic", "https://images.pexels.com/photos/132037/pexels-photo-132037.jpeg?auto=compress&cs=tinysrgb&h=350");
                    userMap.put("name", _nameField.getText().toString().trim());

                    db.collection("users").document(mAuth.getUid()).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                        @Override
                        public void onSuccess(DocumentSnapshot documentSnapshot) {
                            if(!documentSnapshot.exists()){
                                db.collection("users").document(mAuth.getUid()).set(userMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        //Setup complete
                                        startActivity(intent);
                                    }
                                });
                            }
                        }
                    });
                }
                else{
                    if(task.getException() instanceof FirebaseAuthUserCollisionException){
                        Toast.makeText(getApplicationContext(), "Email already registered", Toast.LENGTH_SHORT).show();
                    }
                    else{
                        Toast.makeText(getApplicationContext(), task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
    }

    @Override
    public void onClick(View view){
        switch (view.getId()) {
            case R.id.signUpButton:
                registerUser();
                break;
            case R.id.textViewLogin:
                startActivity(new Intent(this, UserLogin.class));
                break;
        }
    }
}
