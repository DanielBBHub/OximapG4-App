package com.example.joacoses.oximap;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.example.joacoses.oximap.databinding.ActivityMainBinding;
import com.example.joacoses.oximap.databinding.PerfilBinding;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;

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
    }

    private void Perfil()
    {
        Intent i = new Intent( this, Perfil.class);
        startActivity(i);
    }


}