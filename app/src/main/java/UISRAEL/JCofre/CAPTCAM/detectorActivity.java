/*
 * Copyright 2019 The TensorFlow Authors. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package UISRAEL.JCofre.CAPTCAM;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.hardware.camera2.CameraCharacteristics;
import android.media.ImageReader.OnImageAvailableListener;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.SystemClock;
import android.util.Log;
import android.util.Size;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.content.FileProvider;

import com.airbnb.lottie.LottieAnimationView;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.face.Face;
import com.google.mlkit.vision.face.FaceDetection;
import com.google.mlkit.vision.face.FaceDetector;
import com.google.mlkit.vision.face.FaceDetectorOptions;

import UISRAEL.JCofre.CAPTCAM.customview.OverlayView;
import UISRAEL.JCofre.CAPTCAM.customview.OverlayView.DrawCallback;
import UISRAEL.JCofre.CAPTCAM.env.BorderedText;
import UISRAEL.JCofre.CAPTCAM.env.ImageUtils;
import UISRAEL.JCofre.CAPTCAM.env.Logger;
import UISRAEL.JCofre.CAPTCAM.tflite.SimilarityClassifier;
import UISRAEL.JCofre.CAPTCAM.tflite.TFLiteObjectDetectionAPIModel;
import UISRAEL.JCofre.CAPTCAM.tracking.MultiBoxTracker;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

public class detectorActivity extends cameraActivity implements OnImageAvailableListener {
  private static final Logger LOGGER = new Logger();

  private static final int TF_OD_API_INPUT_SIZE = 112;
  private static final boolean TF_OD_API_IS_QUANTIZED = false;
  private static final String TF_OD_API_MODEL_FILE = "mobile_face_net.tflite";


  private static final String TF_OD_API_LABELS_FILE = "file:///android_asset/labelmap.txt";

  private static final DetectorMode MODE = DetectorMode.TF_OD_API;

  private static final float MINIMUM_CONFIDENCE_TF_OD_API = 0.5f;
  private static final boolean MAINTAIN_ASPECT = false;
  private static final Size DESIRED_PREVIEW_SIZE = new Size(640, 480);

  private static final boolean SAVE_PREVIEW_BITMAP = false;
  private static final float TEXT_SIZE_DIP = 10;
  OverlayView trackingOverlay;
  private Integer sensorOrientation;

  private SimilarityClassifier detector;

  private long lastProcessingTimeMs;
  private Bitmap rgbFrameBitmap = null;
  private Bitmap croppedBitmap = null;
  private Bitmap cropCopyBitmap = null;

  private boolean computingDetection = false;
  private boolean addPending = false;


  private long timestamp = 0;

  private Matrix frameToCropTransform;
  private Matrix cropToFrameTransform;


  private MultiBoxTracker tracker;

  private BorderedText borderedText;


  private FaceDetector faceDetector;


  private Bitmap portraitBmp = null;

  private Bitmap faceBmp = null;

  private LottieAnimationView fabAdd;
  String rutaImagen;



  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    fabAdd = findViewById(R.id.fab_add);
    fabAdd.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        onAddClick();
      }
    });


    FaceDetectorOptions options =
            new FaceDetectorOptions.Builder()
                    .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
                    .setContourMode(FaceDetectorOptions.LANDMARK_MODE_NONE)
                    .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_NONE)
                    .build();


    FaceDetector detector = FaceDetection.getClient(options);

    faceDetector = detector;




  }



  private void onAddClick() {

    addPending = true;


  }

  @Override
  public void onPreviewSizeChosen(final Size size, final int rotation) {
    final float textSizePx =
            TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP, TEXT_SIZE_DIP, getResources().getDisplayMetrics());
    borderedText = new BorderedText(textSizePx);
    borderedText.setTypeface(Typeface.MONOSPACE);

    tracker = new MultiBoxTracker(this);


    try {
      detector =
              TFLiteObjectDetectionAPIModel.create(
                      getAssets(),
                      TF_OD_API_MODEL_FILE,
                      TF_OD_API_LABELS_FILE,
                      TF_OD_API_INPUT_SIZE,
                      TF_OD_API_IS_QUANTIZED);
      //cropSize = TF_OD_API_INPUT_SIZE;
    } catch (final IOException e) {
      e.printStackTrace();
      LOGGER.e(e, "Exception initializing classifier!");
      Toast toast =
              Toast.makeText(
                      getApplicationContext(), "Classifier could not be initialized", Toast.LENGTH_SHORT);
      toast.show();
      finish();
    }

    previewWidth = size.getWidth();
    previewHeight = size.getHeight();

    sensorOrientation = rotation - getScreenOrientation();
    LOGGER.i("Camera orientation relative to screen canvas: %d", sensorOrientation);

    LOGGER.i("Initializing at size %dx%d", previewWidth, previewHeight);
    rgbFrameBitmap = Bitmap.createBitmap(previewWidth, previewHeight, Config.ARGB_8888);


    int targetW, targetH;
    if (sensorOrientation == 90 || sensorOrientation == 270) {
      targetH = previewWidth;
      targetW = previewHeight;
    }
    else {
      targetW = previewWidth;
      targetH = previewHeight;
    }
    int cropW = (int) (targetW / 2.0);
    int cropH = (int) (targetH / 2.0);

    croppedBitmap = Bitmap.createBitmap(cropW, cropH, Config.ARGB_8888);

    portraitBmp = Bitmap.createBitmap(targetW, targetH, Config.ARGB_8888);
    faceBmp = Bitmap.createBitmap(TF_OD_API_INPUT_SIZE, TF_OD_API_INPUT_SIZE, Config.ARGB_8888);

    frameToCropTransform =
            ImageUtils.getTransformationMatrix(
                    previewWidth, previewHeight,
                    cropW, cropH,
                    sensorOrientation, MAINTAIN_ASPECT);

//

    cropToFrameTransform = new Matrix();
    frameToCropTransform.invert(cropToFrameTransform);


    Matrix frameToPortraitTransform =
            ImageUtils.getTransformationMatrix(
                    previewWidth, previewHeight,
                    targetW, targetH,
                    sensorOrientation, MAINTAIN_ASPECT);



    trackingOverlay = (OverlayView) findViewById(R.id.tracking_overlay);
    trackingOverlay.addCallback(
            new DrawCallback() {
              @Override
              public void drawCallback(final Canvas canvas) {
                tracker.draw(canvas);
                if (isDebug()) {
                  tracker.drawDebug(canvas);
                }
              }
            });

    tracker.setFrameConfiguration(previewWidth, previewHeight, sensorOrientation);
  }


  @Override
  protected void processImage() {
    ++timestamp;
    final long currTimestamp = timestamp;
    trackingOverlay.postInvalidate();


    if (computingDetection) {
      readyForNextImage();
      return;
    }
    computingDetection = true;

    LOGGER.i("Preparing image " + currTimestamp + " for detection in bg thread.");

    rgbFrameBitmap.setPixels(getRgbBytes(), 0, previewWidth, 0, 0, previewWidth, previewHeight);

    readyForNextImage();

    final Canvas canvas = new Canvas(croppedBitmap);
    canvas.drawBitmap(rgbFrameBitmap, frameToCropTransform, null);
    // For examining the actual TF input.
    if (SAVE_PREVIEW_BITMAP) {
      ImageUtils.saveBitmap(croppedBitmap);
    }

    InputImage image = InputImage.fromBitmap(croppedBitmap, 0);
    faceDetector
            .process(image)
            .addOnSuccessListener(new OnSuccessListener<List<Face>>() {
              @Override
              public void onSuccess(List<Face> faces) {
                if (faces.size() == 0) {
                  updateResults(currTimestamp, new LinkedList<>());
                  return;
                }
                runInBackground(
                        new Runnable() {
                          @Override
                          public void run() {
                            onFacesDetected(currTimestamp, faces, addPending);
                            addPending = false;
                          }
                        });
              }

            });


  }

  @Override
  protected int getLayoutId() {
    return R.layout.tfe_od_camera_connection_fragment_tracking;
  }

  @Override
  protected Size getDesiredPreviewFrameSize() {
    return DESIRED_PREVIEW_SIZE;
  }


  private enum DetectorMode {
    TF_OD_API;
  }

  @Override
  protected void setUseNNAPI(final boolean isChecked) {
    runInBackground(() -> detector.setUseNNAPI(isChecked));
  }

  @Override
  protected void setNumThreads(final int numThreads) {
    runInBackground(() -> detector.setNumThreads(numThreads));
  }



  private Matrix createTransform(
          final int srcWidth,
          final int srcHeight,
          final int dstWidth,
          final int dstHeight,
          final int applyRotation) {

    Matrix matrix = new Matrix();
    if (applyRotation != 0) {
      if (applyRotation % 90 != 0) {
        LOGGER.w("Rotation of %d % 90 != 0", applyRotation);
      }


      matrix.postTranslate(-srcWidth / 2.0f, -srcHeight / 2.0f);


      matrix.postRotate(applyRotation);
    }



    if (applyRotation != 0) {


      matrix.postTranslate(dstWidth / 2.0f, dstHeight / 2.0f);
    }

    return matrix;

  }

  private void showAddFaceDialog(SimilarityClassifier.Recognition rec) {

    AlertDialog.Builder builder = new AlertDialog.Builder(this);
    LayoutInflater inflater = getLayoutInflater();
    View dialogLayout = inflater.inflate(R.layout.image_edit_dialog, null);
   ImageView ivFace = dialogLayout.findViewById(R.id.dlg_image);



    TextView tvTitle = dialogLayout.findViewById(R.id.dlg_title);
    EditText etName = dialogLayout.findViewById(R.id.dlg_input);

    tvTitle.setText("Agregar rostro");



    ivFace.setImageBitmap(rec.getCrop());//donde se muestra el rostro



    BitmapDrawable draw = (BitmapDrawable) ivFace.getDrawable();
    Bitmap bitmap = draw.getBitmap();


    etName.setHint("Ingrese el nombre");

    builder.setPositiveButton("OK", new DialogInterface.OnClickListener(){



      @Override
      public void onClick(DialogInterface dlg, int i) {

          String name = etName.getText().toString();/** se envia el nombre al registro*/



          if (name.isEmpty()) {
              return;
          }
          detector.register(name, rec);



          dlg.dismiss();

        FileOutputStream outStream = null;                                        /** ALAMACEN DE ROSTROS  **/
        File sdCard = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File dir = new File(sdCard.getAbsolutePath() + "/CAPTCAM");
        dir.mkdirs();
        String fileName = String.format("%d.jpg", System.currentTimeMillis());
        File outFile = new File(dir, fileName);
        try {
          outStream = new FileOutputStream(outFile);
        } catch (FileNotFoundException e) {
          e.printStackTrace();
        }
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outStream);
        try {
          outStream.flush();
        } catch (IOException e) {
          e.printStackTrace();
        }
        try {
          outStream.close();
        } catch (IOException e) {
          e.printStackTrace();
        }

        Intent intent=new Intent(getApplicationContext(), registroUsuario.class);/** aqui se envia los datos al registro como el rostro y el nombre*/
        intent.putExtra("nombre",etName.getText().toString());
        intent.putExtra("bitMap",bitmap);
        startActivity(intent);






      }
    });
    builder.setView(dialogLayout);
    builder.show();

  }



  private void updateResults(long currTimestamp, final List<SimilarityClassifier.Recognition> mappedRecognitions) {

    tracker.trackResults(mappedRecognitions, currTimestamp);
    trackingOverlay.postInvalidate();
    computingDetection = false;
    //adding = false;


    if (mappedRecognitions.size() > 0) {
       LOGGER.i("Adding results");
       SimilarityClassifier.Recognition rec = mappedRecognitions.get(0);
       if (rec.getExtra() != null) {
         showAddFaceDialog(rec);
       }

    }

    runOnUiThread(
            new Runnable() {
              @Override
              public void run() {
                showFrameInfo(previewWidth + "x" + previewHeight);
                showCropInfo(croppedBitmap.getWidth() + "x" + croppedBitmap.getHeight());
                showInference(lastProcessingTimeMs + "ms");
              }
            });

  }

  private void onFacesDetected(long currTimestamp, List<Face> faces, boolean add) {

    cropCopyBitmap = Bitmap.createBitmap(croppedBitmap);
    final Canvas canvas = new Canvas(cropCopyBitmap);
    final Paint paint = new Paint();
    paint.setColor(Color.RED);
    paint.setStyle(Style.STROKE);
    paint.setStrokeWidth(2.0f);

    float minimumConfidence = MINIMUM_CONFIDENCE_TF_OD_API;
    switch (MODE) {
      case TF_OD_API:
        minimumConfidence = MINIMUM_CONFIDENCE_TF_OD_API;
        break;
    }

    final List<SimilarityClassifier.Recognition> mappedRecognitions =
            new LinkedList<SimilarityClassifier.Recognition>();



    int sourceW = rgbFrameBitmap.getWidth();
    int sourceH = rgbFrameBitmap.getHeight();
    int targetW = portraitBmp.getWidth();
    int targetH = portraitBmp.getHeight();
    Matrix transform = createTransform(
            sourceW,
            sourceH,
            targetW,
            targetH,
            sensorOrientation);
    final Canvas cv = new Canvas(portraitBmp);

    cv.drawBitmap(rgbFrameBitmap, transform, null);

    final Canvas cvFace = new Canvas(faceBmp);

    boolean saved = false;

    for (Face face : faces) {

      LOGGER.i("FACE" + face.toString());
      LOGGER.i("Running detection on face " + currTimestamp);
      //results = detector.recognizeImage(croppedBitmap);

      final RectF boundingBox = new RectF(face.getBoundingBox());


      final boolean goodConfidence = true;
      if (boundingBox != null && goodConfidence) {


        cropToFrameTransform.mapRect(boundingBox);


        RectF faceBB = new RectF(boundingBox);
        transform.mapRect(faceBB);



        float sx = ((float) TF_OD_API_INPUT_SIZE) / faceBB.width();
        float sy = ((float) TF_OD_API_INPUT_SIZE) / faceBB.height();
        Matrix matrix = new Matrix();
        matrix.postTranslate(-faceBB.left, -faceBB.top);
        matrix.postScale(sx, sy);

        cvFace.drawBitmap(portraitBmp, matrix, null);



        String label = "";
        float confidence = -1f;
        Integer color = Color.BLUE;
        Object extra = null;
        Bitmap crop = null;

        if (add) {
          crop = Bitmap.createBitmap(portraitBmp,
                            (int) faceBB.left,
                            (int) faceBB.top,
                            (int) faceBB.width(),
                            (int) faceBB.height());
        }

        final long startTime = SystemClock.uptimeMillis();
        final List<SimilarityClassifier.Recognition> resultsAux = detector.recognizeImage(faceBmp, add);
        lastProcessingTimeMs = SystemClock.uptimeMillis() - startTime;

        if (resultsAux.size() > 0) {

          SimilarityClassifier.Recognition result = resultsAux.get(0);

          extra = result.getExtra();
//
//          }

          float conf = result.getDistance();
          if (conf < 1.0f) {

            confidence = conf;
            label = result.getTitle();
            if (result.getId().equals("0")) {
              color = Color.GREEN;
            }
            else {
              color = Color.RED;
            }
          }

        }

        if (getCameraFacing() == CameraCharacteristics.LENS_FACING_FRONT) {


          Matrix flip = new Matrix();
          if (sensorOrientation == 90 || sensorOrientation == 270) {
            flip.postScale(1, -1, previewWidth / 2.0f, previewHeight / 2.0f);
          }
          else {
            flip.postScale(-1, 1, previewWidth / 2.0f, previewHeight / 2.0f);
          }
          //flip.postScale(1, -1, targetW / 2.0f, targetH / 2.0f);
          flip.mapRect(boundingBox);

        }

        final SimilarityClassifier.Recognition result = new SimilarityClassifier.Recognition(
                "0", label, confidence, boundingBox);

        result.setColor(color);
        result.setLocation(boundingBox);
        result.setExtra(extra);
        result.setCrop(crop);
        mappedRecognitions.add(result);

      }


    }



    updateResults(currTimestamp, mappedRecognitions);


  }


}
