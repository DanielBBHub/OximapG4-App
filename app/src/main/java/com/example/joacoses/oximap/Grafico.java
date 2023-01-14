package com.example.joacoses.oximap;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.activity.result.ActivityResultLauncher;
import androidx.appcompat.app.AppCompatActivity;

import com.example.joacoses.oximap.databinding.ActivityMainBinding;
import com.example.joacoses.oximap.databinding.GraficoBinding;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;

public class Grafico extends AppCompatActivity {

    private GraficoBinding binding;

    //grafico
    private WebView myWebView;
    private WebSettings myWebSettings;

    //scan
    public String resultadoEscaneo = "PonemosPrueba";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        binding = GraficoBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot() );



        //abrir Perfil desde el boton flotante
        binding.btnfperfil.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Perfil();
            }
        });

        //abrir MainActivity desde el boton flotante
        binding.btnfmapa.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity();
            }
        });


        //abrir QR desde el boton flotante
        binding.btnfacercade.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                scanCode();
            }
        });


        //poner icono de la app en el toolbar
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.logoredondo48);// set drawable icon
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //grafico
        myWebView = binding.webViewGrafico;
        myWebSettings = myWebView.getSettings();
        myWebSettings.setJavaScriptEnabled(true);

        myWebSettings.setDomStorageEnabled(true);
        myWebView.loadUrl("file:///android_asset/grafica.html");
        Log.d("GraficaFallo", " no va la grafica ");
        myWebView.setWebViewClient(new WebViewClient());


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

    }//onCreate()

    // ...................................................................................................................................
    // Perfil() -->
    // ...................................................................................................................................
    //En esta funcion se  crea un Intent nuevo con la actividad "Perfil"
    //Posteriormente se inicializa dicha actividad
    private void Perfil()
    {
        Intent i = new Intent( this, Perfil.class);
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
        finish();
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

    ActivityResultLauncher<ScanOptions> barLauncher = registerForActivityResult(new ScanContract(), result->{
        if(result.getContents() != null){
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
