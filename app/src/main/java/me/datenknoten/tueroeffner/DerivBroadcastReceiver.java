package me.datenknoten.tueroeffner;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.NetworkInfo;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

/**
 * Created by tim on 24.01.15.
 */
public class DerivBroadcastReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        final String action = intent.getAction();
        if (action.equals(WifiManager.NETWORK_STATE_CHANGED_ACTION)) {
            NetworkInfo nwInfo = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
            if (nwInfo.getExtraInfo().equals(MainActivity.networkSSID) && nwInfo.isConnected()) {
                View rootView = ((Activity)context).getWindow().getDecorView().findViewById(android.R.id.content);
                Button button = (Button) rootView.findViewById(R.id.button2);
                button.setEnabled(true);
                button = (Button) rootView.findViewById(R.id.button3);
                button.setEnabled(true);
                button = (Button) rootView.findViewById(R.id.button);
                button.setEnabled(true);
                Toast.makeText(context, context.getString(R.string.wlan_connected), Toast.LENGTH_SHORT).show();
            } else {
                View rootView = ((Activity)context).getWindow().getDecorView().findViewById(android.R.id.content);
                Button button = (Button) rootView.findViewById(R.id.button2);
                button.setEnabled(false);
                button = (Button) rootView.findViewById(R.id.button3);
                button.setEnabled(false);
                button = (Button) rootView.findViewById(R.id.button);
                button.setEnabled(false);
            }
        }
    }
}
