package com.example.movieapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.firestore.Transaction;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Transformation;

import org.w3c.dom.Document;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import jp.wasabeef.picasso.transformations.RoundedCornersTransformation;

public class MovieDetailsActivity extends AppCompatActivity {

    String movieId, userId;
    TextView ratingText, countText;
    RatingBar ratingBar;
    Button button;
    FirebaseFirestore db;

    int myRating = 0, mySavedRating = 0, count = 0;
    float totalRating = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_details);
        ImageView img = findViewById(R.id.imageView5);
        TextView name = findViewById(R.id.textView6);
        TextView year = findViewById(R.id.textView11);
        TextView lang = findViewById(R.id.textView12);
        TextView desc = findViewById(R.id.textView10);

        button = findViewById(R.id.button5);
        ratingText = findViewById(R.id.textView7);
        countText = findViewById(R.id.textView8);
        ratingBar = findViewById(R.id.my_rating);


        Intent intent = getIntent();
        movieId = intent.getStringExtra("id");

        Transformation transformation = new RoundedCornersTransformation(30, 0);
        Picasso.get().load(intent.getStringExtra("url")).centerCrop().resize(720, 960).transform(transformation).into(img);

        name.setText(intent.getStringExtra("name"));
        year.setText(intent.getStringExtra("year"));
        lang.setText(intent.getStringExtra("language"));
        desc.setText(intent.getStringExtra("description"));
    }

    @Override
    protected void onStart() {
        super.onStart();
        db = FirebaseFirestore.getInstance();
        userId = FirebaseAuth.getInstance().getUid();

        ratingBar.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
            @Override
            public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {
                myRating = (int) rating;
            }
        });

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addMyRating();
            }
        });
        getTotalRating();
        getMyRating();
    }

    private void getTotalRating(){
        LoadingDialog loadingDialog = new LoadingDialog(MovieDetailsActivity.this);
        loadingDialog.startLoading();
        db.collection("ratings").document(movieId)
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if(task.isSuccessful()){
                            DocumentSnapshot document = task.getResult();
                            if(document.exists()){
                                //document available
                                totalRating = Float.parseFloat(document.getString("totalRating"));
                                count = (int) Float.parseFloat(document.getString("count"));
                            } else {
                                //no such document => not yet rated
                                totalRating = 0;
                                count = 0;
                            }
                            //update on screen
                            displayRating();
                            loadingDialog.stopLoading(true);
                        } else {
                           //failed to load document
                            loadingDialog.stopLoading(true);
                            Toast.makeText(MovieDetailsActivity.this, "ratings sync failed", Toast.LENGTH_SHORT).show();
                        }
                    }
                });


    }

    private void getMyRating(){
        LoadingDialog loadingDialog = new LoadingDialog(MovieDetailsActivity.this);
        loadingDialog.startLoading();
        db.collection("ratings").document(movieId)
                .collection("userRatings").document(userId)
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if(task.isSuccessful()){
                            DocumentSnapshot document = task.getResult();
                            if(document.exists()){
                                //document available
                                mySavedRating = Integer.parseInt(document.getString("value"));
                            } else {
                                //no such document => not yet rated
                                mySavedRating = 0; //not rated by user
                            }
                            //update on screen
                            displayRating();
                            loadingDialog.stopLoading(true);
                        } else {
                            //failed to load document
                            loadingDialog.stopLoading(true);
                            Toast.makeText(MovieDetailsActivity.this, "my rating sync failed", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

    }

    private void addMyRating(){
        if(myRating == 0){
            Toast.makeText(MovieDetailsActivity.this, "rating can't be zero", Toast.LENGTH_SHORT).show();
            return;
        }
        LoadingDialog loadingDialog = new LoadingDialog(MovieDetailsActivity.this);
        loadingDialog.startLoading();
        db.runTransaction(new Transaction.Function<float[]>() {
            @Nullable
            @Override
            public float[] apply(@NonNull Transaction transaction) throws FirebaseFirestoreException {
                float[] values = {0f, 0f};
                DocumentReference movieRatingRef = db.collection("ratings").document(movieId);
                DocumentSnapshot document = transaction.get(movieRatingRef);
                if(document.exists()){
                    values[0] = Float.parseFloat(document.getString("totalRating"));
                    values[1] = Float.parseFloat(document.getString("count"));
                }
                 DocumentReference userRatingRef = movieRatingRef.collection("userRatings").document(userId);
                document = transaction.get(userRatingRef);
                if(document.exists()){
                    mySavedRating = Integer.parseInt(document.getString("value"));
                } else {
                    mySavedRating = 0;
                }
                float total = values[0] * values[1];
                if(mySavedRating != 0){
                    //if user has rated before
                    total = total - mySavedRating;
                    values[1] = values[1] - 1;
                }
                values[1] = values[1] + 1;
                values[0] = (total + myRating)/values[1];

                Map<String, String> userRating = new HashMap<>();
                userRating.put("value", Integer.toString(myRating));
                transaction.set(userRatingRef, userRating, SetOptions.merge());

                Map<String, String> movieRating = new HashMap<>();
                movieRating.put("totalRating", Float.toString(values[0]));
                movieRating.put("count", Float.toString(values[1]));
                transaction.set(movieRatingRef, movieRating, SetOptions.merge());

                return values;
            }
        }).addOnSuccessListener(new OnSuccessListener<float[]>() {
            @Override
            public void onSuccess(float[] values) {
                totalRating = values[0];
                count = (int) values[1];
                String msg = "New rating added";
                if(mySavedRating != 0) msg = "Rating updated";
                mySavedRating = myRating;
                displayRating();
                Toast.makeText(MovieDetailsActivity.this, msg, Toast.LENGTH_SHORT).show();
                loadingDialog.stopLoading(false);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(MovieDetailsActivity.this, "Failed adding rating", Toast.LENGTH_SHORT).show();
                loadingDialog.stopLoading(false);
            }
        });

    }

    private void displayRating(){
        String temp;
        if(totalRating != 0){
            temp = String.format(Locale.US ,"%.1f", totalRating) + "/10";
            if(totalRating == 10){
                temp = "10/10";
            }
        } else {
            temp = "--/10";
        }
        ratingText.setText(temp);
        temp = "(" + count + ")";
        countText.setText(temp);
        ratingBar.setRating(mySavedRating);
    }

}