package com.example.captcam1;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import androidx.annotation.Nullable;

public class Segundo_plano extends Service {

 //Inicializar las variables aqui para segundo plano en este caso seria  ingresar las variables de que capture el rostro automaticamente cada 2 min de iniciadoel uso del telefono.

    @Override
    public void onCreate()
    {


    }
    @Override
    public int onStartCommand(Intent intent, int flag,int idProcess){
        return START_STICKY;


    }
    @Override
    public void onDestroy()
    {


    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
