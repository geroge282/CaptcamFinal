package com.example.captcam1.interface_captcam;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.captcam1.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class homeUsuario extends AppCompatActivity {
    private Button btnCerrarSesion;
    private Button btnActivars;
    private TextView txtNombreFinal;
    private TextView txtCorreoFinal;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener listener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_homeusuario);
        btnCerrarSesion=findViewById(R.id.btnCerrarSesion);
        btnActivars=findViewById(R.id.btnActivar);
        txtNombreFinal=findViewById(R.id.txtNombreFinal);
        txtCorreoFinal=findViewById(R.id.txtCorreoFinal);
        mAuth=FirebaseAuth.getInstance();
        listener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user=mAuth.getCurrentUser();
                if(user==null){
                    abrirAuthActivity();
                }else
                {
                    txtCorreoFinal.setText(user.getEmail());

                }
            }
        };
      btnCerrarSesion.setOnClickListener(new View.OnClickListener() {
          @Override
          public void onClick(View view) {
              mAuth.signOut();
          }
      });
    }

    private void abrirAuthActivity() {
        Intent i =new Intent(this, AuthActivity.class);
        startActivity(i);
        finish();

    }
    @Override
    protected void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(listener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(listener!=null){
            mAuth.removeAuthStateListener(listener);
        }

    }

}