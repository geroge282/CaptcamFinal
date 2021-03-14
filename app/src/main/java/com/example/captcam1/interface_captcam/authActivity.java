package com.example.captcam1.interface_captcam;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.captcam1.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class authActivity extends AppCompatActivity {
    TextView correoAuth;
    TextView passwordAuth;
    Button btnIngresar;
    Button btnSalir;
    Button btnRegistrar;
    FirebaseAuth mAuth;
    ProgressBar pbCarga;

    FirebaseAuth.AuthStateListener listener;
    final private int REQUEST_CODE_ASK_PERMISSION=111;

//aqui cambio
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        solicitarpermisos();

        setContentView(R.layout.activity_auth);
           correoAuth=findViewById(R.id.ptUsuario);
           passwordAuth=findViewById(R.id.ptPassword);
           btnIngresar=findViewById(R.id.btnIngresar);
           btnIngresar.setOnClickListener(new View.OnClickListener() {
               @Override
               public void onClick(View v) {
                   ingresar();
               }
           });

           btnRegistrar=findViewById(R.id.btnRegistrar);
           btnRegistrar.setOnClickListener(new View.OnClickListener() {
               @Override
               public void onClick(View v) {
                   Intent intent=new Intent(v.getContext(), Registro.class);
                   startActivityForResult(intent,1);
               }
           });

           btnSalir=findViewById(R.id.btnSalir);
           btnSalir.setOnClickListener(new View.OnClickListener() {
               @Override
               public void onClick(View v) {

                   Intent intent=new Intent(Intent.ACTION_MAIN);
                   intent.addCategory(Intent.CATEGORY_HOME);
                   intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                   startActivity(intent);
               }
           });
           pbCarga=findViewById(R.id.pbCarga);
           pbCarga.setVisibility(View.INVISIBLE);
           mAuth = FirebaseAuth.getInstance();
           listener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user=mAuth.getCurrentUser();
                if(user==null){
                    //no esta logueado
                    Toast.makeText(getApplicationContext(),"Revise usuario o password",Toast.LENGTH_LONG).show();

                }
                else{

                    Toast.makeText(getApplicationContext(),"CORRECTO Y LOGUEADO",Toast.LENGTH_LONG).show();

                }
            }
        };



    }

    private void solicitarpermisos() {
        int permisoMemoria= ActivityCompat.checkSelfPermission(authActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE);
        int permisoEscrituraMemoria= ActivityCompat.checkSelfPermission(authActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        int permisoCamara= ActivityCompat.checkSelfPermission(authActivity.this, Manifest.permission.CAMERA);

        if(permisoEscrituraMemoria!= PackageManager.PERMISSION_GRANTED || permisoMemoria!=PackageManager.PERMISSION_GRANTED || permisoCamara!=PackageManager.PERMISSION_GRANTED){
            if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.M){

                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.CAMERA,Manifest.permission.WRITE_EXTERNAL_STORAGE},REQUEST_CODE_ASK_PERMISSION);
            }
        }
    }

    private void abrirCuenta() {
        Intent i=new Intent(this, homeUsuario.class);
        startActivity(i);
        finish();
    }



    private void ingresar() {
        String email =correoAuth.getText().toString();
        String password= passwordAuth.getText().toString();
        if (!email.isEmpty()&& !password.isEmpty()){
            pbCarga.setVisibility(View.VISIBLE);
            mAuth.signInWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if(task.isSuccessful()){
                        Toast.makeText(getApplicationContext(),"CORRECTO",Toast.LENGTH_LONG).show();
                        abrirCuenta();
                    }else{
                        Toast.makeText(getApplicationContext(),"INCORRECTO",Toast.LENGTH_LONG).show();

                    }
                    pbCarga.setVisibility(View.INVISIBLE);
                }
            });
        }
    }

    
    protected void onStart() {

        super.onStart();
        if (mAuth.getCurrentUser() !=null){
        startActivity(new Intent(authActivity.this,homeUsuario.class));
        finish();
        }
    }


}