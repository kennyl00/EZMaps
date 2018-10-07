package com.moonstone.ezmaps_app;

import android.content.Intent;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.moonstone.ezmaps_app.adapters.ChooseContactRecyclerViewAdapter;

import java.util.ArrayList;

public class ChooseContacts extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private View fragmentLayout;
    private ChooseContactRecyclerViewAdapter adapter;
    private boolean contactsAvailable = false;

    private EditText contactFilter;
    public ProgressBar contactsLoading;
    private ImageButton clearButton;

    private ArrayList<String> profilePics;
    private ArrayList<String> ids;
    private ArrayList<String> emails;
    private ArrayList<String> names;

    private Intent intent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_contacts);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        contactFilter = fragmentLayout.findViewById(R.id.contactFilter);
        contactsLoading = fragmentLayout.findViewById(R.id.contactsLoading);

        profilePics = new ArrayList<>();
        ids = new ArrayList<>();
        emails = new ArrayList<>();
        names = new ArrayList<>();

        // Get Intent from EZ Direction
        this.intent = getIntent();

        clearButton = (ImageButton) fragmentLayout.findViewById(R.id.clearButton);
        clearButton.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                contactFilter.getText().clear();
                clearButton.setVisibility(View.GONE);
            }
        });

        //Filter code
        contactFilter.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {

                clearButton.setVisibility(View.VISIBLE);

                // Check if there is contacts available before filtering
                if (contactsAvailable) {
                    filter(s.toString());
                }

            }
        });

        loadContactsFromDB();
        contactFilter.setSelected(false);

    }

    private void loadContactsFromDB() {

        final String Uid = mAuth.getUid();
        final DocumentReference docRef = db.collection("users").document(Uid);

        docRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot snapshot,
                                @Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    Log.w("ChooseContacts", "Listen failed.", e);
                    return;
                }

                if (snapshot != null && snapshot.exists()) {
                    Log.d("ChooseContacts", "Current data: " + snapshot.getData());

                    names.clear();
                    emails.clear();
                    ids.clear();
                    profilePics.clear();

                    try {
                        final ArrayList<String> contacts = (ArrayList<String>) snapshot.get("contacts");

                        Log.d("ChooseContacts", "CONTACTS: " + contacts);

                        if (!contacts.isEmpty()) {
                            for (String contact : contacts) {
                                db.collection("users").document(contact).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                    @Override
                                    public void onSuccess(DocumentSnapshot documentSnapshot) {

                                        profilePics.add(documentSnapshot.get("profilePic").toString());
                                        emails.add(documentSnapshot.get("email").toString());
                                        names.add(documentSnapshot.get("name").toString());
                                        ids.add(documentSnapshot.getId());

                                        if (names.size() == contacts.size()) {
                                            Log.d("ChooseContacts", "second list num: " + names.size());
                                            Log.d("ChooseContacts", "contacts size: " + contacts.size());
                                            Log.d("ChooseContacts", "contacts available: init recycler view: ");
                                            initRecyclerView();
                                            contactsAvailable = true;

                                        }
                                    }
                                });
                            }

                        } else {
                            contactsAvailable = false;
                            Log.d("ChooseContacts", "contacts NOT available: init recycler view: ");
                            initRecyclerView();
                        }

                    } catch (NullPointerException n) {
                        contactsAvailable = false;

                    }

                } else {
                    Log.d("ChooseContacts", "Current data: null");
                }
            }
        });
    }

    //Sets up the recycler view
    private void initRecyclerView() {

        RecyclerView recyclerView = fragmentLayout.findViewById(R.id.contactRecyclerView);

        Log.d("ChooseContacts", "Initialise recycler view: " + names.toString());

        adapter = new ChooseContactRecyclerViewAdapter(getApplicationContext(), names, profilePics, ids, emails, intent.getExtras());
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));

        contactFilter.setSelected(false);
        contactsLoading.setVisibility(View.GONE);
    }

    private void filter(String text) {

        //Filtered arrays
        ArrayList<String> fprofilePics = new ArrayList<>();
        ArrayList<String> fids = new ArrayList<>();
        ArrayList<String> femails = new ArrayList<>();
        ArrayList<String> fnames = new ArrayList<>();

        int counter = 0;

        for (String name : names) {
            if (name.toLowerCase().contains(text.toLowerCase())) {
                fprofilePics.add(profilePics.get(counter));
                fids.add(ids.get(counter));
                femails.add(emails.get(counter));
                fnames.add(names.get(counter));
            }
            counter += 1;
        }

        try {
            adapter.filterList(fnames, fprofilePics, fids, femails);
        } catch (NullPointerException e) {
            Log.d("ChooseContacts", "Filter " + e.getMessage());
        }

    }



}
