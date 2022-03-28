package com.example.movieapp.ui.Logout;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.example.movieapp.MainActivity;
import com.example.movieapp.R;
import com.google.firebase.auth.FirebaseAuth;

public class LogoutFragment extends Fragment {

    private LogoutViewModel logoutViewModel;

    @Override
    public void onStart() {
        super.onStart();
        Toast.makeText( getContext(), "Logging out", Toast.LENGTH_SHORT).show();
        FirebaseAuth.getInstance().signOut();
        Intent i = new Intent(getContext(), MainActivity.class);
        i.setFlags( Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(i);

    }
}