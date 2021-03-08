package com.example.captcam1.interface_captcam;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.drm.DrmStore;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.captcam1.R;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;


public class Registro extends AppCompatActivity implements View.OnClickListener {

    DatabaseReference mRootReference;

    private EditText txtNombre;

    private EditText txtCorreo;
    private EditText txtPassword;
    private EditText txtCompPassword;

    private ProgressDialog progressDialog;
    private FirebaseAuth firebaseAuth;

    //registro usuario
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mRootReference=FirebaseDatabase.getInstance().getReference();

        setContentView(R.layout.activity_registro);

        firebaseAuth = FirebaseAuth.getInstance();
        txtNombre=findViewById(R.id.edNombre);
        txtCorreo = findViewById(R.id.edCorreo);
        txtPassword = findViewById(R.id.edPassword);
        findViewById(R.id.btnTomarFoto);

        Button btnGuardar = findViewById(R.id.btnGuardar);
         progressDialog = new ProgressDialog(this);
        btnGuardar.setOnClickListener(this);

    }


   private void registrarUsuario() {
        final String nombre=txtNombre.getText().toString().trim();
        final String email= txtCorreo.getText().toString().trim();
        final String password= txtPassword.getText().toString().trim();

        if(TextUtils.isEmpty(nombre)) {
            Toast.makeText(this,"se debe ingresar un nombre de usuario", Toast.LENGTH_LONG).show();
            return;
        }

        if(TextUtils.isEmpty(email)) {
            Toast.makeText(this,"se debe ingresar un correo", Toast.LENGTH_LONG).show();
            return;
        }
        if(TextUtils.isEmpty(password)) {
            Toast.makeText(this,"se debe ingresar una contraseña", Toast.LENGTH_LONG).show();
            return;
        }


        progressDialog.setMessage("Realizando registro en linea...");
        progressDialog.show();
        firebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    //checking if success
                    if(task.isSuccessful()){

                        Toast.makeText(Registro.this,"Se ha registrado el usuario con el email: "+ txtCorreo.getText(),Toast.LENGTH_LONG).show();
                    }else{

                        Toast.makeText(Registro.this,"No se pudo registrar el usuario ",Toast.LENGTH_LONG).show();
                    }
                    progressDialog.dismiss();
                });

               Map<String,Object> datosUsuario=new HashMap<>();
               datosUsuario.put("nombre", nombre);
               datosUsuario.put("email",email);
               mRootReference.child("Usuario").push().setValue(datosUsuario);


    }

    private void limpiarTexto (){
        txtNombre.setText("");
        txtCorreo.setText("");
        txtPassword.setText("");
    }

    private void ejecutar(){
        final Handler handler= new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                metodoEjecutar();
               // handler.postDelayed(this,10000);//se ejecutara cada 10 segundos
            }
        },7000);
    }
    public void BotonRegresar(View v){


        Intent intent=new Intent(v.getContext(), AuthActivity.class);
        startActivityForResult(intent,1);

    }
    private void metodoEjecutar() {
        limpiarTexto();


    }




    @Override
    public void onClick(View view) {

       registrarUsuario();
       ejecutar();
       BotonRegresar(view);


    }

    public void BotonFoto(View v){

        Intent intent=new Intent(v.getContext(), DetectorActivity.class);
        startActivityForResult(intent,1);


    }




}




