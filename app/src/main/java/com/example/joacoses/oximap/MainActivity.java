package com.example.joacoses.oximap;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.example.joacoses.oximap.databinding.ActivityMainBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    FirebaseUser currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot() );

        binding.btnPerfil.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Perfil();
            }
        });

        //poner icono de la app en el toolbar
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.logoredondo48);// set drawable icon
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


    }


    // .................................................................
    // Perfil() -->
    // .................................................................
    //En esta funcion se  crea un Intent nuevo con la actividad "Perfil"
    //Posteriormente se inicializa dicha actividad
    private void Perfil()
    {
        Intent i = new Intent( this, Perfil.class);
        startActivity(i);
    }


}