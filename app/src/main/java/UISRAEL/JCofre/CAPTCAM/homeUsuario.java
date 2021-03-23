package UISRAEL.JCofre.CAPTCAM;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import UISRAEL.JCofre.CAPTCAM.seguridad.MyAdmin;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class homeUsuario extends AppCompatActivity implements View.OnClickListener {

    private TextView txtNombreFinal;
    private TextView txtCorreoFinal;
    private FirebaseAuth mAuth; 
    private FirebaseAuth.AuthStateListener listener;
    private DatabaseReference mDatabase;
    private ImageView ImgRostro;

    private Button lock, disable, enable, prueba;
    public static final int RESULT_ENABLE = 11;
    private DevicePolicyManager devicePolicyManager;
    private ComponentName compName;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_homeusuario);

        Button btnCerrarSesion = findViewById(R.id.btnCerrarSesion);
        mDatabase= FirebaseDatabase.getInstance().getReference();

        txtNombreFinal=findViewById(R.id.txtNombreFinal);
        txtCorreoFinal=findViewById(R.id.txtCorreoFinal);
        devicePolicyManager = (DevicePolicyManager) getSystemService(DEVICE_POLICY_SERVICE);
        ActivityManager activityManager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        compName = new ComponentName(homeUsuario.this, MyAdmin.class);

        lock = findViewById(R.id.lock);
        enable =  findViewById(R.id.enableBtn);
        disable =  findViewById(R.id.disableBtn);
        prueba=findViewById(R.id.btnActivar);
        lock.setOnClickListener(this);
        enable.setOnClickListener(this);
        disable.setOnClickListener(this);


        mAuth=FirebaseAuth.getInstance();

        Intent intentF=getIntent();
        Bitmap bitmap1 = intentF.getParcelableExtra("rostroF");
        ImgRostro= findViewById(R.id.imgUsuario);
        ImgRostro.setImageBitmap(bitmap1);

       prueba.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(getApplicationContext(),comparacionRostros.class);
                startActivityForResult(intent, 1 );
            }
        });


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

    @Override
    protected void onResume() {
        super.onResume();
        boolean isActive = devicePolicyManager.isAdminActive(compName);
        disable.setVisibility(isActive ? View.VISIBLE : View.GONE);
        enable.setVisibility(isActive ? View.GONE : View.VISIBLE);
    }

    private void abrirAuthActivity() {
        Intent i =new Intent(this, authActivity.class);
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

    @Override
    public void onClick(View view) {
        if (view == lock) {
            boolean active = devicePolicyManager.isAdminActive(compName);

            if (active) {
                devicePolicyManager.lockNow();
            } else {
                Toast.makeText(homeUsuario.this, "Debe habilitar las funciones del dispositivo de administración", Toast.LENGTH_SHORT).show();
            }

        } else if (view == enable) {

            Intent intent = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
            intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, compName);
            intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION, "VALIDACIONES SOBRE SI ESTAMOS SEGUROS DE LOS PERMISOS QUE DAMOS A LA APP");
            startActivityForResult(intent, RESULT_ENABLE);

        } else if (view == disable) {
            devicePolicyManager.removeActiveAdmin(compName);
            disable.setVisibility(View.GONE);
            enable.setVisibility(View.VISIBLE);
        }


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch(requestCode) {
            case RESULT_ENABLE :
                if (resultCode == Activity.RESULT_OK) {
                    Toast.makeText(homeUsuario.this, "Ha habilitado las funciones del dispositivo de administración", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(homeUsuario.this, "Problema para habilitar las funciones del dispositivo de administración", Toast.LENGTH_SHORT).show();
                }
                break;
        }

        super.onActivityResult(requestCode, resultCode, data);
    }
}