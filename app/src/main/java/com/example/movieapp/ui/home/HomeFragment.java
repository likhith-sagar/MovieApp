package com.example.movieapp.ui.home;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.movieapp.LoadingDialog;
import com.example.movieapp.MovieDetailsActivity;
import com.example.movieapp.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Transformation;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.Inflater;

import jp.wasabeef.picasso.transformations.RoundedCornersTransformation;

import static android.content.ContentValues.TAG;

public class HomeFragment extends Fragment {

    static List<String> categories = new ArrayList<>();
    static Map<String, List<Object>> moviesMap = new HashMap<>();
    public static boolean needOnlineLoading = true;

    FirebaseFirestore db;
    LoadingDialog loadingDialog;
    LinearLayout layout;
    SwipeRefreshLayout swipeRefreshLayout;
    Handler handler;
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_home, container, false);
        layout = root.findViewById(R.id.scroll_linear);
        swipeRefreshLayout = root.findViewById(R.id.swipe_layout);
        handler = new Handler();
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        loadDataFromOnline(true);
                    }
                }, 1000);
            }
        });

        return root;
    }

    @Override
    public void onStart() {
        super.onStart();
        
        db = FirebaseFirestore.getInstance();
        loadingDialog = new LoadingDialog(getActivity());
        if(needOnlineLoading)
            loadDataFromOnline(false);
        else
            displayOnScreen(false);
        needOnlineLoading = true;
    }

    private void loadDataFromOnline(boolean useRefresh){
        if(!useRefresh)
            loadingDialog.startLoading();

        db.collection("movies")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if(task.isSuccessful()){
                            //fetched successfully
                            categories.clear();
                            moviesMap.clear();
                            for(QueryDocumentSnapshot document : task.getResult()){
                                Map<String, Object> temp = document.getData();
                                temp.put("id", document.getId());

                                List<String> catList = (ArrayList<String>) temp.get("categories");
                                for(String s : catList){
                                    if(!categories.contains(s)){
                                        categories.add(s);
                                        moviesMap.put(s, new ArrayList<>());
                                    }
                                    List<Object> list = moviesMap.get(s);
                                    list.add(temp);
                                    moviesMap.put(s, list);
                                }
                            }
                            //call function to display
                            displayOnScreen(useRefresh);
                        } else {
                            Toast.makeText(getContext(), "Error loading documents", Toast.LENGTH_SHORT).show();
                            loadingDialog.stopLoading(true);
                        }
                    }
                });
    }

    private void displayOnScreen(boolean useRefresh){
        if(!categories.isEmpty()){
            layout.removeAllViews();
            LayoutInflater inflater = getActivity().getLayoutInflater();
            if(needOnlineLoading)
                Collections.shuffle(categories);
            for(String s : categories){
                View wrap = inflater.inflate(R.layout.box_container, null);
                TextView txt = wrap.findViewById(R.id.category_text);
                txt.setText(s);
                LinearLayout innerLayout = wrap.findViewById(R.id.box_container);
                List<Object> list = moviesMap.get(s);
                if(needOnlineLoading)
                    Collections.shuffle(list);
                for(Object obj : list){
                    Map<String, Object> movie = (HashMap<String, Object>) obj;
                    View movieBox = inflater.inflate(R.layout.movie_box, null);
                    ImageView img = movieBox.findViewById(R.id.imageView4);

                    img.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            openDetails(movie);
                        }
                    });

                    Transformation transformation = new RoundedCornersTransformation(20, 0);
                    Picasso.get().load(movie.get("url").toString()).centerCrop().resize(480, 640).transform(transformation).into(img);

                    txt = movieBox.findViewById(R.id.textView);
                    txt.setText(movie.get("name").toString());

                    innerLayout.addView(movieBox);
                }
                layout.addView(wrap);
            }
        }
        if(!useRefresh)
            loadingDialog.stopLoading(true);
        else
            swipeRefreshLayout.setRefreshing(false);
    }
    private void openDetails(Map<String, Object> movie){
        needOnlineLoading = false;

        Intent i = new Intent(getActivity(), MovieDetailsActivity.class);
        i.putExtra("id", movie.get("id").toString());
        i.putExtra("name", movie.get("name").toString());
        i.putExtra("language", movie.get("language").toString());
        i.putExtra("year", movie.get("year").toString());
        i.putExtra("description", movie.get("description").toString());
        i.putExtra("url", movie.get("url").toString());
        startActivity(i);
    }
}