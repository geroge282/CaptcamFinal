package com.example.captcam1.interface_captcam;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.captcam1.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class homeUsuario extends AppCompatActivity {
    private Button btnCerrarSesion;
    private Button btnActivar;
    private TextView txtNombreFinal;
    private TextView txtCorreoFinal;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener listener;
    private DatabaseReference mDatabase;
    private ImageView ImgRostro;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mDatabase= FirebaseDatabase.getInstance().getReference();

        setContentView(R.layout.activity_homeusuario);
        btnCerrarSesion=findViewById(R.id.btnCerrarSesion);
        btnActivar=findViewById(R.id.btnActivar);
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
        getUserInfo();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(listener!=null){
            mAuth.removeAuthStateListener(listener);
        }

    }

    private void getUserInfo(){
        String id =mAuth.getCurrentUser().getUid();
        mDatabase.child("Users").child(id).addValueEventListener(new ValueEventListener()
        {
            public void onDataChange (@NonNull DataSnapshot dataSnapshot){
                if(dataSnapshot.exists()){
                    String name =dataSnapshot.child("name").getValue().toString();
                    txtNombreFinal.setText(name);

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

}