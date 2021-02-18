package com.example.captcam1;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;



public class Registro extends AppCompatActivity implements View.OnClickListener {

    private EditText txtcorreo;
    private EditText txtPassword;
    private Button btnGuardar;
    private ProgressDialog progressDialog;
    private FirebaseAuth firebaseAuth;
    private Button btnSalir;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registro);

        firebaseAuth = FirebaseAuth.getInstance();
        txtcorreo = findViewById(R.id.edCorreo);
        txtPassword = findViewById(R.id.edPassword);
        Button btnGuardar = findViewById(R.id.btnGuardar);

        progressDialog = new ProgressDialog(this);
        btnGuardar.setOnClickListener(this);

    }

    private void registrarUsuario() {
        final String email= txtcorreo.getText().toString().trim();
        final String password= txtPassword.getText().toString().trim();

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
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        //checking if success
                        if(task.isSuccessful()){

                            Toast.makeText(Registro.this,"Se ha registrado el usuario con el email: "+ txtcorreo.getText(),Toast.LENGTH_LONG).show();
                        }else{

                            Toast.makeText(Registro.this,"No se pudo registrar el usuario ",Toast.LENGTH_LONG).show();
                        }
                        progressDialog.dismiss();
                    }
                });


    }


    @Override
    public void onClick(View view) {
        //Invocamos al método:
        registrarUsuario();

    }
    public void BotonRegresar(View v){


        Intent intent=new Intent(v.getContext(), AuthActivity.class);
        startActivityForResult(intent,1);

    }




}




