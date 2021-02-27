package com.example.captcam1;

import android.app.AlertDialog;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.captcam1.Helper.GraphicOverlay;
import com.example.captcam1.Helper.RectOverlay;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.face.FirebaseVisionFace;
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetector;
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetectorOptions;
import com.wonderkiln.camerakit.CameraKit;
import com.wonderkiln.camerakit.CameraKitError;
import com.wonderkiln.camerakit.CameraKitEvent;
import com.wonderkiln.camerakit.CameraKitEventListener;
import com.wonderkiln.camerakit.CameraKitImage;
import com.wonderkiln.camerakit.CameraKitVideo;
import com.wonderkiln.camerakit.CameraView;

import java.util.List;

import dmax.dialog.SpotsDialog;

public class camaraDetect extends AppCompatActivity {

    CameraView cameraView;


    GraphicOverlay graphicOverlay;
    Button btnDetect;

    AlertDialog waitingDialog;

    @Override
    protected  void onResume(){
        super.onResume();
        cameraView.setFacing(CameraKit.Constants.FACING_BACK);
        cameraView.start();


    }


    protected  void onPausa() {
        super.onPause();
        cameraView.setFacing(CameraKit.Constants.FACING_BACK);
        cameraView.stop();


    }



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        cameraView=(CameraView)findViewById(R.id.camera_view);

        graphicOverlay=(GraphicOverlay)findViewById(R.id.graphic_overlay);
        btnDetect=(Button)findViewById(R.id.btn_detect);
        waitingDialog=new SpotsDialog.Builder().setContext(this)
                .setMessage("Espere por favor")
                .setCancelable(false)
                .build();

        //evento
        btnDetect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cameraView.start();
                cameraView.captureImage();

                graphicOverlay.clear();
            }
        });

        cameraView.addCameraKitListener(new CameraKitEventListener() {

            @Override
            public void onEvent(CameraKitEvent cameraKitEvent) {

            }

            @Override
            public void onError(CameraKitError cameraKitError) {

            }

            @Override
            public void onImage(CameraKitImage cameraKitImage) {
                waitingDialog.show();

                Bitmap bitmap= cameraKitImage.getBitmap();
                bitmap=Bitmap.createScaledBitmap(bitmap,cameraView.getWidth(),cameraView.getHeight(),false);
                cameraView.stop();

                runFaceDetector(bitmap);

            }

            @Override
            public void onVideo(CameraKitVideo cameraKitVideo) {

            }
        });
    }

    private void runFaceDetector(Bitmap bitmap) {

        FirebaseVisionImage image= FirebaseVisionImage.fromBitmap(bitmap);

        FirebaseVisionFaceDetectorOptions options =new FirebaseVisionFaceDetectorOptions.Builder()
                .build();
        FirebaseVisionFaceDetector detector =FirebaseVision.getInstance()
                .getVisionFaceDetector(options);

        detector.detectInImage(image)
                .addOnSuccessListener(new OnSuccessListener<List<FirebaseVisionFace>>() {
                    @Override
                    public void onSuccess(List<FirebaseVisionFace> firebaseVisionFaces) {
                        processFaceResult(firebaseVisionFaces);
                    }
                })

                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(camaraDetect.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });



    }

    private void processFaceResult(List<FirebaseVisionFace> firebaseVisionFaces) {
        int count=0;
        for(FirebaseVisionFace face : firebaseVisionFaces)
        {
            Rect bounds = face.getBoundingBox();
            //Dibujar el rectangulo en la cara
            RectOverlay rect = new RectOverlay(graphicOverlay, bounds);
            graphicOverlay.add(rect);
            count++;
        }
        waitingDialog.dismiss();
        Toast.makeText(this, String.format("Detectando %d Rostro en imagen ",count), Toast.LENGTH_SHORT).show();

    }
}