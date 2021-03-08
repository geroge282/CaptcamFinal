package com.example.captcam1.interface_captcam;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.captcam1.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

public class Registro extends AppCompatActivity  {

    private EditText mEditTextName;
    private EditText mEditTextEmail;
    private EditText mEditTextPassword;
    private Button  mButtonRegister;

    //variables de datos a registrar
    private String name="";
    private String email="";
    private String password="";
    FirebaseAuth mAuth;
    DatabaseReference mDatabase;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registro);

        mAuth=FirebaseAuth.getInstance();
        mDatabase= FirebaseDatabase.getInstance().getReference();

        mEditTextName=findViewById(R.id.edNombre);
        mEditTextEmail=findViewById(R.id.edCorreo);
        mEditTextPassword=findViewById(R.id.edPassword);
        mButtonRegister=findViewById(R.id.btnGuardar);
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
                    Toast.makeText(Registro.this,"el password debe tener almenos 6 caracteres",Toast.LENGTH_SHORT).show();
                }

            }
            else{
                Toast.makeText(Registro.this,"debe completar los compos",Toast.LENGTH_SHORT).show();
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
                            startActivity(new Intent(Registro.this,AuthActivity.class));
                            finish();
                        }
                        else{
                            Toast.makeText(Registro.this,"no se pudieron crear los datos correctamente",Toast.LENGTH_SHORT).show();
                        }

                    }
                });
            }else {
                Toast.makeText(Registro.this,"No se pudo registrar este usuario", Toast.LENGTH_SHORT).show();
            }
        }
    });
    }

}




