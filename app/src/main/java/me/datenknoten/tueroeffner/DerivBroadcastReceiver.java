package me.datenknoten.tueroeffner;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class DerivBroadcastReceiver extends BroadcastReceiver {

    private final Context appContext;
    private final String targetSSID;


    public DerivBroadcastReceiver(Context appContext, String networkSSID) {
        this.appContext = appContext;
        this.targetSSID = networkSSID;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
//        final String action = intent.getAction();

        WifiManager wifiManager = (WifiManager) appContext.getSystemService(Context.WIFI_SERVICE);
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();

        NetworkInfo nwInfo = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);

        if (nwInfo != null && nwInfo.isConnectedOrConnecting()) {
            String ssid = wifiInfo.getSSID();


            // Build.VERSION.SDK_INT is incompatible with device API lvl < 4 use String.toInteger(Build.VERSION.SDK) or maybe find some other way ^_^
            if (Build.VERSION.SDK_INT > 17){
                targetSSID.replace("\"", "");
            }

             if (ssid.equals(targetSSID)){
                buttonActivator((Activity) context, true);
                Toast.makeText(context, context.getString(R.string.wlan_connected), Toast.LENGTH_SHORT).show();
            } else {
                buttonActivator((Activity) context, false);
            }

        }
    }

    private void buttonActivator(Activity context, boolean ssidTrue) {
        View rootView = context.getWindow().getDecorView().findViewById(android.R.id.content);
        Button button = (Button) rootView.findViewById(R.id.button_buzzer);
        button.setEnabled(ssidTrue);
        button = (Button) rootView.findViewById(R.id.button_door_open);
        button.setEnabled(ssidTrue);
        button = (Button) rootView.findViewById(R.id.button_door_unlock);
        button.setEnabled(ssidTrue);
        button = (Button) rootView.findViewById(R.id.button);
        button.setEnabled(ssidTrue);
    }
}
