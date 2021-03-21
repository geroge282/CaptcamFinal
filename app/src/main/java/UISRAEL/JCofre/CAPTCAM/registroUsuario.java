package UISRAEL.JCofre.CAPTCAM;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
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


import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;



public class registroUsuario extends AppCompatActivity {
    private EditText mEditTextName;
    private EditText mEditTextEmail;
    private EditText mEditTextPassword;
    private ImageView rostroP;
    //variables de datos a registrar
    private String name="";
    private String email="";
    private String password="";
    FirebaseAuth mAuth;
    DatabaseReference mDatabase;
    String rutaImagen;
    Bitmap bitmap1;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_registro);


        mAuth=FirebaseAuth.getInstance();
        mDatabase= FirebaseDatabase.getInstance().getReference();

        mEditTextName=findViewById(R.id.edNombre);
        mEditTextEmail=findViewById(R.id.edCorreo);
        mEditTextPassword=findViewById(R.id.edPassword);
        Button mButtonRegister = findViewById(R.id.btnGuardar);
        Button mButtonCamera = findViewById(R.id.btnTomarFoto);
        Button mButtonRegresar = findViewById(R.id.btnRegresar);
        Button mButtonGuardar= findViewById(R.id.guardarRostro);



        Intent intent=getIntent();

        mEditTextName.setText(intent.getStringExtra("nombre"));
        Bitmap bitmap = intent.getParcelableExtra("bitMap");
        rostroP = findViewById(R.id.imageViewId);
        rostroP.setImageBitmap(bitmap);


        rostroP.buildDrawingCache();
        bitmap1 = ((BitmapDrawable)rostroP.getDrawable()).getBitmap();

        mButtonGuardar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });




        mButtonRegresar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(registroUsuario.this, authActivity.class);
                startActivityForResult(intent,0);
            }
        });

        mButtonCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(v.getContext(), detectorActivity.class);
                startActivityForResult(intent,1);
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
    private void registerUser(){
    mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
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




