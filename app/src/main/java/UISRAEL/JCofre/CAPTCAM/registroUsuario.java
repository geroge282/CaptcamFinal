package UISRAEL.JCofre.CAPTCAM;
import androidx.annotation.NonNull;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;



import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;

import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;

import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;


import java.io.File;

import java.io.IOException;

import java.util.HashMap;
import java.util.Map;



public class registroUsuario extends AppCompatActivity {


    public static final int RESULT_OK = -1;
    public static final int RESULT_CANCELED = 0;
    private EditText mEditTextName;
    private EditText mEditTextEmail;
    private EditText mEditTextPassword;
    //variables de datos a registrar
    private String name="";
    private String email="";
    private String password="";

    private StorageReference mStorage;
    FirebaseAuth mAuth;
    DatabaseReference mDatabase;

    Bitmap bitmap1;
    String rutaImagen;
    ImageView rostroPersona;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_registro);

        mAuth=FirebaseAuth.getInstance();
        mDatabase= FirebaseDatabase.getInstance().getReference();
        mStorage= FirebaseStorage.getInstance().getReference();

        mEditTextName=findViewById(R.id.edNombre);
        mEditTextEmail=findViewById(R.id.edCorreo);
        mEditTextPassword=findViewById(R.id.edPassword);
        Button mButtonRegister = findViewById(R.id.btnGuardar);
        Button mButtonCamera = findViewById(R.id.btnTomarFoto);
        Button mButtonRegresar = findViewById(R.id.btnRegresar);
        rostroPersona=findViewById(R.id.imageViewId);


        mButtonRegresar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(registroUsuario.this, authActivity.class);
                startActivity(intent);
            }
        });

        mButtonCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                abrirCamara();

            }
        });

        mButtonRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            name=mEditTextName.getText().toString();
            email=mEditTextEmail.getText().toString();
            password=mEditTextPassword.getText().toString();

            if(!name.isEmpty() && !email.isEmpty()&& !password.isEmpty()){
                if(password.length()>=6){

                    registerUser();
                }
                else{
                    Toast.makeText(registroUsuario.this,"el password debe tener almenos 6 caracteres",Toast.LENGTH_SHORT).show();
                }
            }
            else{
                Toast.makeText(registroUsuario.this,"debe completar los compos",Toast.LENGTH_SHORT).show();
            }
            }
        });
    }

    private void abrirCamara(){
        Intent intent=new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if(intent.resolveActivity(getPackageManager()) !=null){

            File imagenArchivo=null;
            try{
                imagenArchivo=crearImagen();

            }catch(IOException ex){

                Log.e("Error", ex.toString());
            }

            if(imagenArchivo !=null) {

                Uri fotoUri= FileProvider.getUriForFile(this,"UISRAEL.JCofre.CAPTCAM.fileprovider",imagenArchivo);
                intent.putExtra(MediaStore.EXTRA_OUTPUT,fotoUri);
                startActivityForResult(intent,1);
            }

        }

    }

    protected void onActivityResult(int requestCode,int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK) {

            Bitmap imgBitmap = BitmapFactory.decodeFile(rutaImagen);
            rostroPersona.setImageBitmap(imgBitmap);

        }
        rostroPersona.buildDrawingCache();
        bitmap1 = ((BitmapDrawable)rostroPersona.getDrawable()).getBitmap();

    }
    private File crearImagen() throws IOException {
        String nombreImagen="CAPTCAM_";
        File directorio=getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File imagen=File.createTempFile(nombreImagen,".JPEG",directorio);
        rutaImagen=imagen.getAbsolutePath();
        return imagen;

    }
    private void registerUser(){


    mAuth.createUserWithEmailAndPassword(email, password ).addOnCompleteListener(new OnCompleteListener<AuthResult>() {

       @Override
        public void onComplete(@NonNull Task<AuthResult> task) {
            if(task.isSuccessful()){
                Map<String,Object>map=new HashMap<>();
                map.put("name", name);
                map.put("email",email);
                map.put("password", password);


                String id=mAuth.getCurrentUser().getUid();

                mDatabase.child("Users").child(id).setValue(map).addOnCompleteListener(new OnCompleteListener<Void>() {

                @Override
                    public void onComplete(@NonNull Task<Void> task2) {
                        if (task2.isSuccessful()){

                            Intent intentF =new Intent(registroUsuario.this,homeUsuario.class);
                            intentF.putExtra("rostroF",bitmap1);
                            startActivity(intentF);

                            Toast.makeText(registroUsuario.this,"registroUsuario exitoso",Toast.LENGTH_SHORT).show();
                        }
                        else{
                            Toast.makeText(registroUsuario.this,"no se pudieron crear los datos correctamente",Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }else {
                Toast.makeText(registroUsuario.this,"No se pudo registrar este usuario", Toast.LENGTH_SHORT).show();
            }
        }
    });
    }



}




