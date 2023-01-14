package com.example.joacoses.oximap;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.joacoses.oximap.databinding.ActivityMainBinding;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;


import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;


public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;

    //Firebase
    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

    //Beacon
    private TextView elTexto;
    private Button elBotonEnviar;
    private JSONObject datos_muestra = new JSONObject();
    private String string_json;
    private int major = 0;
    private int minor = 0;
    private String uuid = "OximapPrueba";
    private String nombreBeacon = "OximapPrueba";
    private int contadorMuestras = 0;
    private int muestraPeligrosa = 60;//es 0,06 ppm

    // Variables para el codigo de BLT
    private static final String ETIQUETA_LOG = ">>>>";
    private static final int CODIGO_PETICION_PERMISOS = 11223344;

    // Instancias de el scanner BLT
    private BluetoothLeScanner elEscanner;
    private ScanCallback callbackDelEscaneo = null;

    //notificaciones
    private int notificationId = 0;
    private String CHANNEL_ID = "4444";

    //horas
    private long horaMuestraEnviada = System.currentTimeMillis();
    private long tiempoMinimoSinDetectarSensor = 10000;
    private String horaString = "";

    //pulsar dos veces para salir de la app
    private static final int INTERVALO = 2000; //2 segundos para salir
    private long tiempoPrimerClick;

    //latitud y longitud
    //38.947821, -0.178772
    //final double random = new Random().nextInt(1); // [0, 60] + 20 => [20, 80]
    private double random;
    private double latitud;
    private double longitud;

    //scan
    public String resultadoEscaneo = "PonemosPrueba";

    //mapa
    private WebView myWebView;
    private WebSettings myWebSettings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot() );

        //notificaciones
        createNotificationChannel();

        //abrir Perfil desde el boton flotante
        binding.btnfperfil.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Perfil();
            }
        });

        //abrir Grafico desde el boton flotante
        binding.btnfinfo.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                abrirGrafico();
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

        //mapa
        myWebView = binding.webViewGrafico;
        myWebSettings = myWebView.getSettings();
        myWebSettings.setJavaScriptEnabled(true);

        myWebSettings.setDomStorageEnabled(true);
        myWebView.loadUrl("file:///android_asset/mapa.html");
        Log.d("MapaFallo1", " no va el mapa ");
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


        //vemos si se ponen los datos en datos_muestras
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                //datos_muestra.put("muestra", 1238);
                //datos_muestra.put("fecha", Date.valueOf(LocalDate.now().toString()));
                Log.d("debug", String.valueOf(datos_muestra));

            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        //inicializar bluetooth
        Log.d(ETIQUETA_LOG, " onCreate(): empieza ");
        inicializarBlueTooth();
        Log.d(ETIQUETA_LOG, " onCreate(): termina ");

        buscarDispositivosBTLEPulsado(binding.getRoot());

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
    // quien: View -->
    // boton_enviar_muestra() -->
    // ...................................................................................................................................
    //En este metodo asignamos los datos a las variables y las subimos al servidor mediante un post
    @SuppressLint("LongLogTag")
    public void enviarMuestra(View quien) {
        //ponemos los datos
        Date c = Calendar.getInstance().getTime();
        SimpleDateFormat curFormater = new SimpleDateFormat("yyyy-MM-dd");
        String formattedDate = curFormater.format(c);
        Log.d("pulsado", "-----------------------------------------------------");
        Log.d("pulsado", "boton_enviar_pulsado");
        Log.d("pulsado", "-----------------------------------------------------");
        try {
            random = ThreadLocalRandom.current().nextDouble(0, 1.5);
            latitud = 38.947821 + random;
            longitud = -0.178772 + random;
            //muestra, usuario, fecha, latitud, longitud
            datos_muestra.put("id", minor);
            datos_muestra.put("muestra", major);
            datos_muestra.put("fecha",formattedDate);
            datos_muestra.put("usuario",user.getDisplayName());
            datos_muestra.put("latitud",latitud);
            datos_muestra.put("longitud",longitud);

            Log.d("pulsado", String.valueOf(datos_muestra));
            string_json = String.valueOf(datos_muestra);
        } catch (JSONException e) {
            Log.d("pulsado",e.toString());

        }
        Log.d("Este es el resultado del escaneo",resultadoEscaneo);

        //Wifi joan: 172.20.10.2
        //wifi carlos:192.168.1.144
        //Wifi casa Joan: 192.168.1.187
        //hacemos el post
        try {
            if(nombreBeacon.contains(resultadoEscaneo) && contadorMuestras != minor){
                //hora de envio
                SimpleDateFormat hora = new SimpleDateFormat("HH:mm:ss", Locale.UK);
                horaString = hora.format(c);
                Log.d("HoraPrueba",horaString);

                //comparar horas
                horaMuestraEnviada = System.currentTimeMillis();
                Log.d("horaMuestraEnviada",String.valueOf(horaMuestraEnviada));


                contadorMuestras = minor;
                //Prueba POST /alta
                PeticionarioREST elPeticionario = new PeticionarioREST();
                elPeticionario.hacerPeticionREST("POST", "http://192.168.1.187:8080/alta", string_json,
                        new PeticionarioREST.RespuestaREST() {
                            @Override
                            public void callback(int codigo, String cuerpo) {
                                Log.d("clienterestandroid", "POST /alta completado");
                            }
                        }
                );

                if(major >= muestraPeligrosa){
                    //-----------------------------------
                    //se muestra una notificacion porque hay una muestra fuera de lo comun
                    Intent intent = new Intent();
                    intent.setAction(Intent.ACTION_VIEW);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, intent, PendingIntent.FLAG_IMMUTABLE);

                    NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), CHANNEL_ID)
                            .setSmallIcon(R.drawable.logoredondo)
                            .setContentTitle("Oximap")
                            .setContentText("Se ha detectado una muestra peligrosa")
                            .setPriority(NotificationCompat.PRIORITY_HIGH)
                            .setContentIntent(pendingIntent)
                            .setAutoCancel(true);

                    NotificationManagerCompat notificationManager = NotificationManagerCompat.from(getApplicationContext());
                    notificationManager.notify(notificationId, builder.build());
                    //fin de la notificacion
                    //-----------------------------
                }


            }else{
                Log.d("clienterestandroid", "No ha hecho el post");
                Log.d("clienterestandroid", uuid);
                Log.d("clienterestandroid", nombreBeacon);
            }
        }
        catch(Exception e) { }

        //5 minutos a milis = 300000
        if(System.currentTimeMillis()-horaMuestraEnviada > tiempoMinimoSinDetectarSensor){
            //-----------------------------------
            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_VIEW);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, intent, PendingIntent.FLAG_IMMUTABLE);

            NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), CHANNEL_ID)
                    .setSmallIcon(R.drawable.logoredondo)
                    .setContentTitle("Oximap")
                    .setContentText("La última muestra se ha enviado a las: " + horaString)
                    .setPriority(NotificationCompat.PRIORITY_MAX)
                    .setContentIntent(pendingIntent)
                    .setAutoCancel(true);

            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(getApplicationContext());
            notificationManager.notify(notificationId, builder.build());
            //fin de la notificacion
            //-----------------------------
        }


    }//()


    // ...................................................................................................................................
    // inicializarBlueTooth() -->
    // ...................................................................................................................................
    //En este metodo iniciamos bluetooth, chequeamos los permisos.
    //lo mismo para LeScanner
    private void inicializarBlueTooth() {

        Log.d(ETIQUETA_LOG, " inicializarBlueTooth(): obtenemos adaptador BT ");
        BluetoothAdapter bta = BluetoothAdapter.getDefaultAdapter();

        Log.d(ETIQUETA_LOG, " inicializarBlueTooth(): habilitamos adaptador BT ");
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            //return;
        }
        bta.enable();

        Log.d(ETIQUETA_LOG, " inicializarBlueTooth(): habilitado =  " + bta.isEnabled() );
        Log.d(ETIQUETA_LOG, " inicializarBlueTooth(): estado =  " + bta.getState() );
        Log.d(ETIQUETA_LOG, " inicializarBlueTooth(): obtenemos escaner btle ");

        this.elEscanner = bta.getBluetoothLeScanner();

        if ( this.elEscanner == null ) {
            Log.d(ETIQUETA_LOG, " inicializarBlueTooth(): Socorro: NO hemos obtenido escaner btle  !!!!");

        }
        Log.d(ETIQUETA_LOG, " inicializarBlueTooth(): voy a perdir permisos (si no los tuviera) !!!!");
        if (
                ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED
                        ||ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH) != PackageManager.PERMISSION_GRANTED
                        || ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_ADMIN) != PackageManager.PERMISSION_GRANTED
                        || ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
        )
        {
            ActivityCompat.requestPermissions(
                    MainActivity.this,
                    new String[]{Manifest.permission.BLUETOOTH,Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_ADMIN, Manifest.permission.ACCESS_FINE_LOCATION},
                    CODIGO_PETICION_PERMISOS);
        }
        else {
            Log.d(ETIQUETA_LOG, " inicializarBlueTooth(): parece que YA tengo los permisos necesarios !!!!");

        }
    } // inicializarBlueTooth()





    // ...................................................................................................................................
    // detenerBusquedaDispositivosBTLE() -->
    // ...................................................................................................................................
    //Con este metodo podemos detener la busqueda de dispositivos beatle
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void detenerBusquedaDispositivosBTLE() {

        if (this.callbackDelEscaneo == null) {
            return;
        }

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {

        }
        this.elEscanner.stopScan(this.callbackDelEscaneo);
        this.callbackDelEscaneo = null;

    } // detenerBusquedaDispositivosBTLE()




    // ...................................................................................................................................
    // resultado: ScanResult -->
    // mostrarInformacionDispositivoBTLE() -->
    // ...................................................................................................................................
    //Con este metodo obtenemos los diferentes datos que emite el micro
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void mostrarInformacionDispositivoBTLE(ScanResult resultado) {

        BluetoothDevice bluetoothDevice = resultado.getDevice();
        byte[] bytes = resultado.getScanRecord().getBytes();
        int rssi = resultado.getRssi();

        Log.d(ETIQUETA_LOG, " ****************************************************");
        Log.d(ETIQUETA_LOG, " ****** DISPOSITIVO DETECTADO BTLE ****************** ");
        Log.d(ETIQUETA_LOG, " ****************************************************");
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {

        }
        Log.d(ETIQUETA_LOG, " nombre = " + bluetoothDevice.getName());
        nombreBeacon = bluetoothDevice.getName();
        Log.d(ETIQUETA_LOG, " toString = " + bluetoothDevice.toString());

        TramaIBeacon tib = new TramaIBeacon(bytes);
        Log.d(ETIQUETA_LOG, " uuid  = " + Utilidades.bytesToHexString(tib.getUUID()));
        Log.d(ETIQUETA_LOG, " uuid  = " + Utilidades.bytesToString(tib.getUUID()));
            uuid = Utilidades.bytesToString(tib.getUUID());
        Log.d(ETIQUETA_LOG, " major  = " + Utilidades.bytesToHexString(tib.getMajor()) + "( "
                + Utilidades.bytesToInt(tib.getMajor()) + " ) ");
        major = Utilidades.bytesToInt(tib.getMajor());
        Log.d(ETIQUETA_LOG, " minor  = " + Utilidades.bytesToHexString(tib.getMinor()) + "( "
                + Utilidades.bytesToInt(tib.getMinor()) + " ) ");
        minor = Utilidades.bytesToInt(tib.getMinor());

        Log.d(ETIQUETA_LOG, " txPower  = " + Integer.toHexString(tib.getTxPower()) + " ( " + tib.getTxPower() + " )");
        Log.d(ETIQUETA_LOG, " ****************************************************");

    } // mostrarInformacionDispositivoBTLE()




    // ...................................................................................................................................
    // v: View -->
    // BuscarDispositivosBTLEPulsado() -->
    // ...................................................................................................................................
    //Con este metodo llamamos a buscarTodosLosDispositivosBTLE()
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void buscarDispositivosBTLEPulsado(View v) {
        Log.d(ETIQUETA_LOG, " boton buscar dispositivos BTLE Pulsado");
        this.buscarTodosLosDispositivosBTLE();
    } // ()




    // ...................................................................................................................................
    // buscarTodosLosDispositivosBTLE() -->
    // ...................................................................................................................................
    //Con este metodo empezamos a escanear los dispositivos beatle y les pasamos el resultado a mostrarInformacionDispositivoBTLE()
    //llamamos a enviarMuestra() para subirlo a la base de datos mediante un POST
    //Mostramos un log en caso de error
    //chequeamos permisos
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void buscarTodosLosDispositivosBTLE() {

        Log.d(ETIQUETA_LOG, " buscarTodosLosDispositivosBTL(): empieza ");
        Log.d(ETIQUETA_LOG, " buscarTodosLosDispositivosBTL(): instalamos scan callback ");

        this.callbackDelEscaneo = new ScanCallback() {
            @Override
            public void onScanResult(int callbackType, ScanResult resultado) {
                super.onScanResult(callbackType, resultado);
                Log.d(ETIQUETA_LOG, " buscarTodosLosDispositivosBTL(): onScanResult() ");
                mostrarInformacionDispositivoBTLE(resultado);
                enviarMuestra(binding.getRoot());

            }

            @Override
            public void onBatchScanResults(List<ScanResult> results) {
                super.onBatchScanResults(results);
                Log.d(ETIQUETA_LOG, " buscarTodosLosDispositivosBTL(): onBatchScanResults() ");

            }

            @Override
            public void onScanFailed(int errorCode) {
                super.onScanFailed(errorCode);
                Log.d(ETIQUETA_LOG, " buscarTodosLosDispositivosBTL(): onScanFailed() ");

            }
        };

        Log.d(ETIQUETA_LOG, " buscarTodosLosDispositivosBTL(): empezamos a escanear ");

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {

        }
        this.elEscanner.startScan(this.callbackDelEscaneo);

    } // buscarTodosLosDispositivosBTLE()






    // ...................................................................................................................................
    // createNotificationChannel() -->
    // ...................................................................................................................................
    //En esta funcion creamos la notificacion que se mostrará en la app
    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, "name", importance);
            channel.setDescription("description");
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
        else {
            Toast.makeText(getApplicationContext(),"Se te ha enviado un correo",Toast.LENGTH_LONG);
        }

    }//clase



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