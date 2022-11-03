package com.example.joacoses.oximap;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

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
    private Map<String, String> datosUsuario = new HashMap<>();


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

        cogerDatosUsuario(currentUser);
        comprobarUsuario(datosUsuario);
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


    // .................................................................
    // datosASubir: Map<String, String> -->
    // comprobarUsuario() -->
    // .................................................................
    //En esta funcion comprobamos si los los datos del usuario estan en la colleccion Usuarios o no
    //en caso de que este, no hace nada, en caso contrario llama a la funcion subirDatosUsuario()
    private void comprobarUsuario(Map<String, String> datosASubir)
    {

        try {
            db.collection("Usuarios").whereEqualTo("Mail", datosASubir.get("Mail")).get()
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if (task.isSuccessful()) {
                                Log.d("Query", task.getResult().getDocuments().toString());
                                Log.d("Datos Usuario", datosUsuario.toString());


                            }
                            else
                            {
                                subirDatosUsuario(datosUsuario);
                            }
                        }
                    });
        }
        catch (NullPointerException e)
        {

        }

    }


    // .................................................................
    // currentUser: Firebase -->
    // cogerDatosUsuario() -->
    // .................................................................
    //En esta funcion cogemos los datos de los usuarios con sus claves (Nombre, Mail,...)
    public void cogerDatosUsuario(FirebaseUser currentUser)
    {

        try
        {
            Log.d("Datos usuario", currentUser.getDisplayName());

            datosUsuario.put("Nombre", currentUser.getDisplayName());
            datosUsuario.put("Mail", currentUser.getEmail());
            datosUsuario.put("Foto", currentUser.getPhotoUrl().toString());
            Log.d("Datos usuario", datosUsuario.toString());
        }
        catch (NullPointerException e)
        {
            /*Toast.makeText(Mapa.this, "Fallo al descargar la información",
                    Toast.LENGTH_SHORT).show();*/
        }
    }

    // .................................................................
    // datosASubir: Map<String, String> -->
    // subirDatosUsuario() -->
    // .................................................................
    //En esta funcion realizamos la conexion con Firebase y ponemos en la collección Usuarios,
    //los datos del usuario con el que se ha iniciado sesion
    private void subirDatosUsuario(Map<String, String> datosASubir )
    {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("Usuarios").document(currentUser.getUid()).set(datosASubir);
    }


}