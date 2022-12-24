package com.example.joacoses.oximap;

import static android.content.ContentValues.TAG;

import android.app.Activity;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.bumptech.glide.Glide;
import com.example.joacoses.oximap.databinding.EditarPerfilBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;


public class EditarPerfil extends AppCompatActivity {
    private EditarPerfilBinding binding;
    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

    FirebaseStorage storage = FirebaseStorage.getInstance();

    // Create a storage reference from our app
    StorageReference storageRef = storage.getReference();

    //imagen
    String urlImg;


    //notificaciones
    private Button boton;

    private int notificationId = 0;

    private String CHANNEL_ID = "4444";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = EditarPerfilBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot() );

        //notificaciones

        createNotificationChannel();

        //poner imagen el el imageview de editar perfil
        try {
            Glide.with(this).load(user.getPhotoUrl().toString()).into(binding.fotoUsuario);
        }
        catch (NullPointerException e)
        {
            Toast.makeText(this, "No tienes imagen de perfil",
                    Toast.LENGTH_SHORT).show();
        }


        //poner icono de la app en el toolbar
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.logoredondo48);// set drawable icon
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //guardar datos
        binding.btnGuardar.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateDatos();
            }
        });

        //recuperar contraseña
        binding.btnRecuperarContrasenya.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                recuperarContrasenya();
                //-----------------------------------
                //se muestra una notificacion para que revises tu correo electronico
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_VIEW);
                intent.setData(Uri.parse("http://"+"mail.google.com/mail/u/0/?tab=rm&ogbl#inbox"));
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, intent, PendingIntent.FLAG_IMMUTABLE);

                NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), CHANNEL_ID)
                        .setSmallIcon(R.drawable.logoredondo)
                        .setContentTitle("Oximap")
                        .setContentText("Se ha enviado un correo a tu email para cambiar la contraseña")
                        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                        .setContentIntent(pendingIntent)
                        .setAutoCancel(true);

                NotificationManagerCompat notificationManager = NotificationManagerCompat.from(getApplicationContext());
                notificationManager.notify(notificationId, builder.build());
                //fin notificacion
                //-----------------------------------

            }
        });

        //mostrar nombre actual del usuario en el editText
        binding.txtNombreEditar.setText(user.getDisplayName());


        //clicks que editan la imagen
        binding.imgEditarImagen.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent i = new Intent(Intent.ACTION_PICK,
                                android.provider.MediaStore.Images.Media.INTERNAL_CONTENT_URI);
                        final int ACTIVITY_SELECT_IMAGE = 1234;
                        startActivityForResult(i, ACTIVITY_SELECT_IMAGE);
                    }
                }
        );

        binding.btnEditarImagen.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent i = new Intent(Intent.ACTION_PICK,
                                android.provider.MediaStore.Images.Media.INTERNAL_CONTENT_URI);
                        final int ACTIVITY_SELECT_IMAGE = 1234;
                        startActivityForResult(i, ACTIVITY_SELECT_IMAGE);
                    }
                }
        );

    }


    @Override
    public void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == 1234) {
                subirImg(data.getData(), "Imagenes/" + user.getUid());
            }
        }
    }

    // .................................................................
    // createNotificationChannel() -->
    // .................................................................
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



    // .................................................................
    // archivo: uri, ref: str -->
    // subirImg() -->
    // .................................................................
    //En este metodo subimos la imagen de perfil que seleccionamos desde la galeria o camara  al storage de firebase,
    //mas concretamente en la carpeta "Imagenes". Para que sea unica dicha imagen, al subirla le ponemos como nombre
    //el correo del usuario, ya que este va a ser unico
    private void subirImg(Uri archivo, String ref)
    {
        StorageReference ficheroRef = storageRef.child(ref);
        ficheroRef.putFile(archivo);
        storageRef.child("Imagenes/"+user.getUid()).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                // Got the download URL for 'users/me/profile.png'
                urlImg=uri.toString();
                Log.d("urlImagen", urlImg);
            }
        });
    }


    // .................................................................
    // recuperarContrasenya() -->
    // .................................................................
    //Este metodo nos envia un email para recuperar la contraseña
    public void recuperarContrasenya(){
        FirebaseAuth auth = FirebaseAuth.getInstance();
        String emailAddress = user.getEmail();

        auth.sendPasswordResetEmail(emailAddress)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "Email enviado.");
                            Toast.makeText(EditarPerfil.this, "Se ha enviado un correo a: "+user.getEmail(), Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }


    // .................................................................
    // updateDatos() -->
    // .................................................................
    //Este metodo sirve para actualizar los datos del usuario, el que se encuentrar en su textbox e imageview
    //posteriormente nos llevará a la actividad MainActivity
    private void updateDatos()
    {

        UserProfileChangeRequest profileUpdates;
        if(urlImg != null)
        {
            profileUpdates = new UserProfileChangeRequest.Builder()
                    .setDisplayName(binding.txtNombreEditar.getText().toString())
                    .setPhotoUri(Uri.parse(urlImg))
                    .build();
        }
        else{
            profileUpdates = new UserProfileChangeRequest.Builder()
                    .setDisplayName(binding.txtNombreEditar.getText().toString())
                    .build();
        }

        user.updateProfile(profileUpdates)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "User profile updated.");
                            Intent i = new Intent(com.example.joacoses.oximap.EditarPerfil.this,MainActivity.class);
                            startActivity(i);
                            finish();
                        }
                    }
                });
    }

}
