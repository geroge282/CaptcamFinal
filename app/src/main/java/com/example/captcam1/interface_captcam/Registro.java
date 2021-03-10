package com.example.captcam1.interface_captcam;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.hardware.camera2.CameraCharacteristics;
import android.media.ImageReader;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Size;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.captcam1.R;
import com.example.captcam1.interface_captcam.customview.OverlayView;
import com.example.captcam1.interface_captcam.env.BorderedText;
import com.example.captcam1.interface_captcam.env.ImageUtils;
import com.example.captcam1.interface_captcam.env.Logger;
import com.example.captcam1.interface_captcam.tflite.SimilarityClassifier;
import com.example.captcam1.interface_captcam.tflite.TFLiteObjectDetectionAPIModel;
import com.example.captcam1.interface_captcam.tracking.MultiBoxTracker;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.face.Face;
import com.google.mlkit.vision.face.FaceDetection;
import com.google.mlkit.vision.face.FaceDetector;
import com.google.mlkit.vision.face.FaceDetectorOptions;


import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;



public class Registro extends AppCompatActivity {
    private EditText mEditTextName;
    private EditText mEditTextEmail;
    private EditText mEditTextPassword;
    private Button  mButtonRegister;
    private Button mButtonCamera;
    private ImageView rostroP;
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
        mButtonCamera=findViewById(R.id.btnTomarFoto);


        Intent intent=getIntent();
        mEditTextName.setText(intent.getStringExtra("nombre"));
        Bitmap bitmap = intent.getParcelableExtra("bitMap");
        rostroP = (ImageView) findViewById(R.id.imageViewId);
        rostroP.setImageBitmap(bitmap);








        mButtonCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(v.getContext(), DetectorActivity.class);
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
                            Toast.makeText(Registro.this,"Registro exitoso ingrese con correo y password",Toast.LENGTH_SHORT).show();


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




