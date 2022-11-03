package com.example.joacoses.oximap;

import static android.content.ContentValues.TAG;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

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
    // Create a child reference
    // imagesRef now points to "images"
    StorageReference imagesRef = storageRef.child("images/"+user.getEmail());

    String urlImg;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = EditarPerfilBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot() );




        binding.btnGuardar.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateDatos();
            }
        });


        binding.btnRecuperarContrasenya.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                recuperarContrasenya();
            }
        });

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
                subirImg(data.getData(), "Imagenes/" + user.getEmail());
            }
        }
    }


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
        storageRef.child("Imagenes/"+user.getEmail()).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
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
        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                .setDisplayName(binding.txtNombreEditar.getText().toString())
                .setPhotoUri(Uri.parse(urlImg))
                .build();

        user.updateProfile(profileUpdates)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "User profile updated.");
                        }
                    }
                });

        Intent i = new Intent(this,MainActivity.class);
        startActivity(i);
    }





}
