package UISRAEL.JCofre.CAPTCAM;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.Handler;
import android.view.View;
import android.view.WindowManager;

public class presentacionAct extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_presentacion);




        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent =new Intent(presentacionAct.this,authActivity.class);
                startActivity(intent);
                finish();
            }
        }, 4000);
    }





}