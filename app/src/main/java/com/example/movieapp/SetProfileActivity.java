package com.example.movieapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.movieapp.ui.home.HomeFragment;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Transformation;

import jp.wasabeef.picasso.transformations.RoundedCornersTransformation;

public class SetProfileActivity extends AppCompatActivity {
    FirebaseUser user;
    StorageReference storageRef;
    ImageView proPic;
    EditText name;
    Button btn1, btn2;
    LoadingDialog loadingDialog;
    Uri imgUri;
    boolean imgSet;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_profile);
        btn1 = findViewById(R.id.button3);
        btn2 = findViewById(R.id.button4);
        proPic = findViewById(R.id.imageView3);
        name = findViewById(R.id.addUserName);
        user = FirebaseAuth.getInstance().getCurrentUser();
        storageRef = FirebaseStorage.getInstance().getReference();
        loadingDialog = new LoadingDialog(SetProfileActivity.this);
        imgSet = false;

        if(user.getPhotoUrl() != null && !user.getPhotoUrl().toString().isEmpty()){
            Transformation transformation = new RoundedCornersTransformation(40, 0);
            Picasso.get().load(user.getPhotoUrl()).centerCrop().resize(720, 720).transform(transformation).into(proPic);
        }
        if(user.getDisplayName() != null && !user.getDisplayName().isEmpty()){
            name.setText(user.getDisplayName());
        }
        btn1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent();
                i.setType("image/*");
                i.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(i, "Profile Pic"), 2);
            }
        });

        btn2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = name.getText().toString();
                loadingDialog.startLoading();
                String userId = user.getUid();
                StorageReference imageRef = storageRef.child("profile/"+userId);
                if(imgSet){
                    imageRef.putFile(imgUri)
                            .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                    //Toast.makeText(getContext(), "Image uploaded", Toast.LENGTH_SHORT).show();
                                    imageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                        @Override
                                        public void onSuccess(Uri uri) {
                                            updateProfileDetails(username, uri.toString());
                                        }
                                    });
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(SetProfileActivity.this, "Upload failed! try again", Toast.LENGTH_SHORT).show();
                                    loadingDialog.stopLoading(false);
                                }
                            });
                } else {
                    updateProfileDetails(username, "");
                }

            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode != RESULT_OK) return;
        try{
            if(requestCode == 2){
                assert data != null;
                imgUri = data.getData();
                Transformation transformation = new RoundedCornersTransformation(40, 0);
                Picasso.get().load(imgUri).centerCrop().resize(720, 720).transform(transformation).into(proPic);
                imgSet = true;
            }
        } catch(Error e){
            //no handler necessary
        }

    }
    private void updateProfileDetails(String userName, String url) {
        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                .setDisplayName(userName)
                .setPhotoUri(Uri.parse(url))
                .build();
        user.updateProfile(profileUpdates)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            Toast.makeText(SetProfileActivity.this, "Profile successfully updated", Toast.LENGTH_SHORT).show();
                            loadingDialog.stopLoading(true);
                            Intent i = new Intent(SetProfileActivity.this, HomeActivity.class);
                            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK |Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(i);
                        }
                    }
                });
    }

}