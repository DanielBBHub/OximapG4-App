package com.example.joacoses.oximap;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.joacoses.oximap.databinding.ActivityMainBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

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

    // Variables para el codigo de BLT
    private static final String ETIQUETA_LOG = ">>>>";
    private static final int CODIGO_PETICION_PERMISOS = 11223344;

    // Instancias de el scanner BLT
    private BluetoothLeScanner elEscanner;
    private ScanCallback callbackDelEscaneo = null;

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
    // quien: View -->
    // boton_enviar_muestra() -->
    // .................................................................
    //En este metodo asignamos los datos a las variables y las subimos al servidor mediante un post
    public void enviarMuestra(View quien) {
        //ponemos los datos
        Date c = Calendar.getInstance().getTime();
        SimpleDateFormat curFormater = new SimpleDateFormat("yyyy-MM-dd");
        String formattedDate = curFormater.format(c);
        Log.d("pulsado", "-----------------------------------------------------");
        Log.d("pulsado", "boton_enviar_pulsado");
        Log.d("pulsado", "-----------------------------------------------------");
        try {
            datos_muestra.put("id", major);
            datos_muestra.put("muestra", minor);
            datos_muestra.put("fecha",formattedDate);
            datos_muestra.put("usuario",user.getDisplayName());
            Log.d("pulsado", String.valueOf(datos_muestra));
            string_json = String.valueOf(datos_muestra);
        } catch (JSONException e) {
            Log.d("pulsado",e.toString());

        }

        //hacemos el post
        if(uuid.contains("OXIMAPG4_SPRINT1")){
            //Prueba POST /alta
            PeticionarioREST elPeticionario = new PeticionarioREST();
            elPeticionario.hacerPeticionREST("POST", "http://192.168.1.144:8080/alta", string_json,
                    new PeticionarioREST.RespuestaREST() {
                        @Override
                        public void callback(int codigo, String cuerpo) {
                            Log.d("clienterestandroid", "POST /alta completado");
                        }
                    }
            );

        }else{
            Log.d("clienterestandroid", "No ha hecho el post");
            Log.d("clienterestandroid", uuid);
        }


    }//boton_enviar_muestra


    // .................................................................
    // inicializarBlueTooth() -->
    // .................................................................
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

    // .................................................................
    // detenerBusquedaDispositivosBTLE() -->
    // .................................................................
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


    // .................................................................
    // resultado: ScanResult -->
    // mostrarInformacionDispositivoBTLE() -->
    // .................................................................
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


    // .................................................................
    // v: View -->
    // BuscarDispositivosBTLEPulsado() -->
    // .................................................................
    //Con este metodo llamamos a buscarTodosLosDispositivosBTLE()
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void buscarDispositivosBTLEPulsado(View v) {
        Log.d(ETIQUETA_LOG, " boton buscar dispositivos BTLE Pulsado");
        this.buscarTodosLosDispositivosBTLE();
    } // ()

    // .................................................................
    // buscarTodosLosDispositivosBTLE() -->
    // .................................................................
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




    //--------------------------------------------------------------------------------------------------------------------------
    //NO LO USAMOS POR EL MOMENTO
    //--------------------------------------------------------------------------------------------------------------------------
/*
    // --------------------------------------------------------------
    // --------------------------------------------------------------
    private void buscarEsteDispositivoBTLE(final String dispositivoBuscado) {

        Log.d(ETIQUETA_LOG, " buscarEsteDispositivoBTLE(): empieza ");
        Log.d(ETIQUETA_LOG, "  buscarEsteDispositivoBTLE(): instalamos scan callback ");

        this.callbackDelEscaneo = new ScanCallback() {
            @Override
            public void onScanResult(int callbackType, ScanResult resultado) {
                super.onScanResult(callbackType, resultado);
                Log.d(ETIQUETA_LOG, "  buscarEsteDispositivoBTLE(): onScanResult() ");
                mostrarInformacionDispositivoBTLE(resultado);
            }

            @Override
            public void onBatchScanResults(List<ScanResult> results) {
                super.onBatchScanResults(results);
                Log.d(ETIQUETA_LOG, "  buscarEsteDispositivoBTLE(): onBatchScanResults() ");

            }

            @Override
            public void onScanFailed(int errorCode) {
                super.onScanFailed(errorCode);
                Log.d(ETIQUETA_LOG, "  buscarEsteDispositivoBTLE(): onScanFailed() ");

            }
        };

        ScanFilter sf = new ScanFilter.Builder().setDeviceName(dispositivoBuscado).build();

        Log.d(ETIQUETA_LOG, "  buscarEsteDispositivoBTLE(): empezamos a escanear buscando: " + dispositivoBuscado);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {

            //return;
        }
        this.elEscanner.startScan(this.callbackDelEscaneo);
    } // buscarEsteDispositivoBTLE()

    // --------------------------------------------------------------
    // --------------------------------------------------------------
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void botonBuscarNuestroDispositivoBTLEPulsado(View v) {
        Log.d(ETIQUETA_LOG, " boton nuestro dispositivo BTLE Pulsado");
        //this.buscarEsteDispositivoBTLE( Utilidades.stringToUUID( "EPSG-GTI-PROY-3A" ) );

        //this.buscarEsteDispositivoBTLE( "EPSG-GTI-PROY-3A" );

        this.buscarEsteDispositivoBTLE("BeaconRuben");

    } // ()

    // --------------------------------------------------------------
    // --------------------------------------------------------------
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void botonDetenerBusquedaDispositivosBTLEPulsado(View v) {
        Log.d(ETIQUETA_LOG, " boton detener busqueda dispositivos BTLE Pulsado");
        this.detenerBusquedaDispositivosBTLE();
    } // ()


*/
}