package com.example.joacoses.oximap;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.joacoses.oximap.databinding.PerfilBinding;
import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

//Solo se van a explicar los metodos ajenos a android studio

public class Perfil extends AppCompatActivity {

    private FirebaseAuth mAuth;
    FirebaseUser currentUser;
    private Map<String, String> datosUsuario = new HashMap<>();
    private PerfilBinding binding;


    //pulsar dos veces para salir de la app
    private static final int INTERVALO = 2000; //2 segundos para salir
    private long tiempoPrimerClick;

    //scan
    public String resultadoEscaneo = "PonemosPrueba";

    @Override public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = PerfilBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot() );
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();


        //poner icono de la app en el toolbar
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.logoredondo48);// set drawable icon
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //floating button
        FloatingActionButton boton = findViewById(R.id.btnfcentral);
        FloatingActionButton botonMapa = findViewById(R.id.btnfmapa);
        FloatingActionButton botonPerfil = findViewById(R.id.btnfperfil);
        FloatingActionButton botonAcercade = findViewById(R.id.btnfacercade);
        FloatingActionButton botonInfo = findViewById(R.id.btnfinfo);

        boton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(botonMapa.getVisibility() == View.VISIBLE){
                    botonMapa.setVisibility(View.GONE);
                    botonMapa.setClickable(false);

                    botonPerfil.setVisibility(View.GONE);
                    botonPerfil.setClickable(false);

                    botonAcercade.setVisibility(View.GONE);
                    botonAcercade.setClickable(false);

                    botonInfo.setVisibility(View.GONE);
                    botonInfo.setClickable(false);
                }
                else {
                    botonMapa.setVisibility(View.VISIBLE);
                    botonMapa.setClickable(true);

                    botonPerfil.setVisibility(View.VISIBLE);
                    botonPerfil.setClickable(true);

                    botonAcercade.setVisibility(View.VISIBLE);
                    botonAcercade.setClickable(true);

                    botonInfo.setVisibility(View.VISIBLE);
                    botonInfo.setClickable(true);
                }

            }
        });

        //onClick
        binding.btnfmapa.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity();
            }
        });

        binding.btnfinfo.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                abrirGrafico();
            }
        });

        binding.btnfacercade.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                scanCode();
            }
        });

        binding.btnEditar.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               editarPerfil();
            }
        });

        binding.btnCerrarSesion.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cerrarSesion(null);
            }
        });

        cogerDatosUsuario(currentUser);

    }


    // ...................................................................................................................................
    // editarPerfil() -->
    // ...................................................................................................................................
    //En esta funcion se  crea un Intent nuevo con la actividad "EditarPerfil"
    //Posteriormente se inicializa dicha actividad

    private void editarPerfil()
    {
        Intent i = new Intent( this, EditarPerfil.class);
        startActivity(i);
    }

    // ...................................................................................................................................
    // Grafico() -->
    // ...................................................................................................................................
    //En esta funcion se  crea un Intent nuevo con la actividad "Grafico"
    //Posteriormente se inicializa dicha actividad
    private void abrirGrafico()
    {
        Intent i = new Intent( this, Grafico.class);
        startActivity(i);
        finish();
    }

    // ...................................................................................................................................
    // MainActivity() -->
    // ...................................................................................................................................
    //En esta funcion se  crea un Intent nuevo con la actividad "MainActivity"
    //Posteriormente se inicializa dicha actividad
    private void MainActivity()
    {
        Intent i = new Intent( this, MainActivity.class);
        startActivity(i);
    }



    // ...................................................................................................................................
    // currentUser: Firebase -->
    // cogerDatosUsuario() -->
    // ...................................................................................................................................
    //En esta funcion cogemos los datos de los usuarios con sus claves (Nombre, Mail,...)
    //y luego cargamos esos datos con la funcion cargarDatosUsuario()
    public void cogerDatosUsuario(FirebaseUser currentUser)
    {

        datosUsuario.put("Nombre", currentUser.getDisplayName());
        datosUsuario.put("Mail", currentUser.getEmail());
        datosUsuario.put("Fecha", Long.toString( currentUser.getMetadata().getCreationTimestamp()));
        try
        {
            datosUsuario.put("Foto", currentUser.getPhotoUrl().toString());
        }
        catch (NullPointerException e)
        {
            /*Toast.makeText(Perfil.this, "Fallo al descargar la información",
                    Toast.LENGTH_SHORT).show();*/
        }

        cargarDatosUsuario(datosUsuario);
    }


    // ...................................................................................................................................
    // datosUsuarios: Map<String, String> -->
    // cargarDatosUsuario() -->
    // ...................................................................................................................................
    //En esta funcion se encuentra los textbox e imagen del xml
    //posteriormente se cargar los datos en dichos campos
    //parseamos la fecha Long a String
    //y establecemos un placeholder en caso de que no cargue la foto o no disponga de una
    private void cargarDatosUsuario(Map<String, String> datosUsuario)
    {
        binding.txtNombreUsuario.setText(datosUsuario.get("Nombre"));
        binding.txtMailUsuario.setText(datosUsuario.get("Mail"));

        Date d=new Date(Long.parseLong(datosUsuario.get("Fecha")));
        binding.txtFechaUsuario.setText(d.toString());

        Glide.with(this).load(datosUsuario.get("Foto")).into(binding.fotoUsuario);
        try
        {
            Glide.with(this).load(datosUsuario.get("Foto")).placeholder(R.drawable.logoredondo).into(binding.fotoUsuario);
        }
        catch (NullPointerException e)
        {
            binding.fotoUsuario.setImageResource(R.drawable.logoredondo);
            /*Toast.makeText(Perfil.this, "Fallo al descargar la información",
                    Toast.LENGTH_SHORT).show();*/
        }
    }



    // ...................................................................................................................................
    // view: View -->
    // cargarDatosUsuario() -->
    // ...................................................................................................................................
    //En esta funcion cerramos la sesion del usuario y lo devolvemos al MainActivity
    public void cerrarSesion(View view) {
        AuthUI.getInstance().signOut(this)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override public void onComplete(@NonNull Task<Void> task) {
                        Intent i = new Intent(Perfil.this,Login.class);
                        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(i);
                        Perfil.this.finish();
                    }
                });
    }



    // ...................................................................................................................................
    // onBackPressed() -->
    // ...................................................................................................................................
    //En esta funcion se comprobamos el tiempo desde que se pulsa el boton de volver atras del dispositivo
    //si el tiempo en que se pulsa el segundo click a dicho boton es menor que 2 segundos, se cierra la aplicacion
    //en caso contrario la app se queda abierta
    @Override
    public void onBackPressed(){
        if (tiempoPrimerClick + INTERVALO > System.currentTimeMillis()){
            super.onBackPressed();
            return;
        }else {
            Toast.makeText(this, "Vuelve a presionar para salir", Toast.LENGTH_SHORT).show();
        }
        tiempoPrimerClick = System.currentTimeMillis();
    }

    //SCAN
    private void scanCode(){
        ScanOptions options = new ScanOptions();
        options.setPrompt("Sube el volumen para activar el flash");
        options.setBeepEnabled(true);
        options.setOrientationLocked(true);
        options.setCaptureActivity(CaptureAct.class);
        barLauncher.launch(options);
    }

    ActivityResultLauncher<ScanOptions> barLauncher = registerForActivityResult(new ScanContract(), result-> {
        if (result.getContents() != null) {
            /*
            //pop-up
            //AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            //titulo de pop-up
            //builder.setTitle("Titulo: Resultado");
            //mensaje del pop-up
            //builder.setMessage(result.getContents());
            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialogInterface.dismiss();
                }
            }).show();
            */

            //resultado de lo que se escanea
            resultadoEscaneo = String.valueOf(result.getContents());
            Log.d("ResultadoScan:", resultadoEscaneo);


        }
    });
}