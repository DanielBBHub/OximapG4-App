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

        binding.btnEditarImagen.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cogerImagen();
            }
        });

        binding.btnEditarImagen.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent i = new Intent(Intent.ACTION_PICK);
                        i.setType("image/*");
                        startActivityForResult(i, 1234);
                    }
                }
        );

    }


    @Override
    public void onActivityResult(final int requestCode,
                                 final int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == 1234) {
                subirImg(data.getData(), "Imagenes/" + user.getEmail());
            }
        }
    }

    private void subirImg(Uri archivo, String ref)
    {
        StorageReference ficheroRef = storageRef.child(ref);
        ficheroRef.putFile(archivo).addOnSuccessListener(
                new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        Task<Uri> downloadUri = taskSnapshot.getStorage().getDownloadUrl();
                        if(downloadUri.isSuccessful())
                        {
                            urlImg = downloadUri.getResult().toString();
                        }
                    }
                }
        );
    }

    private void cogerImagen(){
        // Get the data from an ImageView as bytes
        binding.fotoUsuario.setDrawingCacheEnabled(true);
        binding.fotoUsuario.buildDrawingCache();
        Bitmap bitmap = ((BitmapDrawable) binding.fotoUsuario.getDrawable()).getBitmap();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] data = baos.toByteArray();
    }

    private void updateDatos()
    {

        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                .setDisplayName(binding.txtNombreEditar.getText().toString())
                .setPhotoUri(Uri.parse("https://asturscore.com/wp-content/uploads/2010/09/Avatar.jpg"))
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
