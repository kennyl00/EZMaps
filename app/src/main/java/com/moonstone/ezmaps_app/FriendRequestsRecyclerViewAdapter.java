package com.moonstone.ezmaps_app;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class FriendRequestsRecyclerViewAdapter extends RecyclerView.Adapter<FriendRequestsRecyclerViewAdapter.ViewHolder> {

    private ArrayList<String> contactNames = new ArrayList<>();
    private ArrayList<String> profilePics = new ArrayList<>();
    private Context mContext;
    private Activity mActivity;

    //Never rendered but information is held here
    private ArrayList<String> ids = new ArrayList<>();
    private ArrayList<String> emails = new ArrayList<>();

    private Bundle shareImageBundle;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    String TAG = "DEBUGFriendRequestsRecyclerViewAdapter";



    public FriendRequestsRecyclerViewAdapter(Context context, ArrayList<String> contactNames,
                                             ArrayList<String> profilePics, ArrayList<String> ids,
                                             ArrayList<String> emails, FirebaseFirestore db, FirebaseAuth mAuth){

        this.contactNames = contactNames;
        this.profilePics = profilePics;
        this.mContext = context;
        this.ids = ids;
        this.emails = emails;
        this.mActivity = mActivity;
        this.shareImageBundle = shareImageBundle;

    }

    //Actually recycles the view holders
    @NonNull
    @Override
    public FriendRequestsRecyclerViewAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.contactlistitem, viewGroup, false);

        FriendRequestsRecyclerViewAdapter.ViewHolder holder = new FriendRequestsRecyclerViewAdapter.ViewHolder(view);

        holder.setIsRecyclable(false);
        return holder;
    }

    //Called every time a new item is added to the list
    @Override
    public void onBindViewHolder(@NonNull FriendRequestsRecyclerViewAdapter.ViewHolder viewHolder, final int i) {
        Log.d("HERE", Integer.toString(i));

        //Gets the image and puts it into the referenced imageView

        if(mContext != null){
            Log.d("ChooseContactRecycler", "Context: " + mContext);
        }

        if(profilePics.get(i) != null){
            Log.d("ChooseContactRecycler", "profile pic: " + profilePics.get(i));
        }


        Log.d("ChooseContactRecycler", "position: " + i );

        Picasso.get().load(profilePics.get(i)).into(viewHolder.profilePic);

        /*Glide.with(mContext)
                .asBitmap()
                .load(profilePics.get(i))
                .into(viewHolder.profilePic);*/

        viewHolder.contactName.setText(contactNames.get(i));


        //Add onclicklistener to each list entry
        viewHolder.ContactParentLayout.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){

                Log.d("ChooseContactRecyclerView", "This Device token: "+ MyFirebaseMessagingService.fetchToken());
                Log.d("ChooseContactRecyclerView", "onClick: " + ids.get(i));

                ChatActivity.setToUserID(ids.get(i));
                ChatActivity.setFromUserID(MyFirebaseMessagingService.fetchToken());
                String name = contactNames.get(i);
                Intent i = new Intent(mContext, ChatActivity.class);
//                i.putExtras(shareImageBundle);
                i.putExtra("name", name);
                mContext.startActivity(i);
            }
        });

        viewHolder.id = ids.get(i);
        viewHolder.email = emails.get(i);

        //last one
        /*if(i == contactNames.size() - 1){
            Tab3Fragment.contactsLoading.setVisibility(View.GONE);
        }*/
    }


    @Override
    public int getItemCount() {
        return contactNames.size();
    }

    //Basically the class of the entry itself
    public class ViewHolder extends RecyclerView.ViewHolder {
        CircleImageView profilePic;
        TextView contactName;
        RelativeLayout ContactParentLayout;
        String email;
        String id;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            profilePic = itemView.findViewById(R.id.callerPic);
            contactName = itemView.findViewById(R.id.contactName);
            ContactParentLayout = itemView.findViewById(R.id.contactParentLayout);

        }
    }

    public void filterList(ArrayList<String> contactNames, ArrayList<String> profilePics, ArrayList<String> ids, ArrayList<String> emails){
        this.contactNames = contactNames;
        this.profilePics = profilePics;
        this.ids = ids;
        this.emails = emails;
        notifyDataSetChanged();
    }

    public void refreshData(){
        notifyDataSetChanged();
    }

    public void clear() {
        final int size = contactNames.size();
        if (size > 0) {
            for (int i = 0; i < size; i++) {
                contactNames.remove(0);
                profilePics.remove(0);
                ids.remove(0);
                emails.remove(0);
            }

            notifyItemRangeRemoved(0, size);
        }
    }


    public void addContact(String targetEmailInput){
        Log.d("DEBUG_SCANBARCODEACTIVITY", "addContact: " + targetEmailInput);

        final String Uid = mAuth.getUid();
        Log.d(TAG, "findUid: " + targetEmailInput);
        final String targetEmail = targetEmailInput;

        final String[] targetUid = new String[1];
        targetUid[0]= null;

        //Start of search portion of method.
        Log.d(TAG, "findUid: This Uid "+ Uid);
        Task<QuerySnapshot> d = db.collection("users").get();
        d.addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) { //Once list of users is retrieved,
                List<DocumentSnapshot> list = task.getResult().getDocuments(); //put into a list of users

                for (DocumentSnapshot doc : list) { //for each document in list,
                    if (!doc.getId().equals(Uid)) { //only check if not checking this user.
                        // String match.
                        String email = doc.get("email").toString();

                        if (compareContacts(targetEmail, email)) {
                            targetUid[0] = doc.getId();
                            Log.d(TAG, "onComplete: "+ targetUid[0]);
                            // If found, call the add method.
                            addContactFromUid(targetUid[0]);

                        }

                    }
                }
                Log.d(TAG, "onComplete1: "+ targetUid[0]);

            }
        });
    }



    public boolean addContactFromUid(String targetUidInput) {
        final String targetUid = targetUidInput;
        Log.d("DEBUG_SCANBARCODEACTIVITY", "addContact: " + targetUid);

        final String Uid = mAuth.getUid();
        Log.d(TAG, "addContact: line162 " + targetUid);

        if (targetUid != null) {
            Log.d("DEBUG_SCANBARCODEACTIVITY", "targetUid = " + targetUid);

            //
            db.collection("users").document(Uid).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                @Override
                public void onSuccess(DocumentSnapshot documentSnapshot) {
                    db.collection("users").document(Uid).update("contacts", FieldValue.arrayUnion(targetUid));
                    Log.d("FINDRECYCLER", "SUCCESSFULLY ADDED: " + targetUid);
                }
            });



        } else {
            Log.d("DEBUG_SCANBARCODEACTIVITY", "addContact: COULD NOT FIND CONTACT");
            return false;
        }
        return false;
    }




    private boolean compareContacts(String text, String against){

        if(against.toUpperCase().contains(text.toUpperCase())){

            Log.d("Add Contacts", "Comparing string1: " + text + " in string2: " + against + " SUCCESS");

            return true;
        }


        Log.d("Add Contacts", "Comparing string1: " + text + " in string2: " + against + " FAILED");

        return false;
    }

}