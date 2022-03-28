package com.example.movieapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.movieapp.ui.home.HomeFragment;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Transformation;

import androidx.appcompat.view.menu.MenuView;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import jp.wasabeef.picasso.transformations.RoundedCornersTransformation;

public class HomeActivity extends AppCompatActivity {
    FirebaseUser user;
    private AppBarConfiguration mAppBarConfiguration;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.

        user = FirebaseAuth.getInstance().getCurrentUser();

        View hView = navigationView.getHeaderView(0);
        ImageView imageView = hView.findViewById(R.id.imageView);
        TextView tv1 = hView.findViewById(R.id.userName);
        TextView tv2 = hView.findViewById(R.id.userEmail);

        if(user.getPhotoUrl() == null || user.getPhotoUrl().toString().isEmpty()){
            imageView.setImageResource(R.mipmap.ic_blank_profile);
        } else {
            Transformation transformation = new RoundedCornersTransformation(25, 0);
            Picasso.get().load(user.getPhotoUrl()).centerCrop().resize(480, 480).transform(transformation).into(imageView);
        }
        if(user.getDisplayName() == null || user.getDisplayName().isEmpty()){
            tv1.setText("Username");
        } else {
            tv1.setText(user.getDisplayName());
        }
        tv2.setText(user.getEmail());

        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                HomeFragment.needOnlineLoading = false;
                Intent i = new Intent(HomeActivity.this, SetProfileActivity.class);
                startActivity(i);
            }
        });

        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home, R.id.nav_add, R.id.nav_logout)
                .setDrawerLayout(drawer)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.home, menu);
        return true;
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }
}