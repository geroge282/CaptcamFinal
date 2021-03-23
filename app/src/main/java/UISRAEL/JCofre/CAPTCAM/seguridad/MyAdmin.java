package UISRAEL.JCofre.CAPTCAM.seguridad;


import android.app.admin.DeviceAdminReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;


public class MyAdmin extends DeviceAdminReceiver {

    @Override
    public void onEnabled(Context context,  Intent intent) {
        Toast.makeText(context, "Administrador de dispositivos : ACTIVADO", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDisabled( Context context, Intent intent) {
        Toast.makeText(context, "Administrador de dispositivos : DESACTIVADO", Toast.LENGTH_SHORT).show();
    }
}
