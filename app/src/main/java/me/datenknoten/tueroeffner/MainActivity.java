/*******************************************************************************
 * "THE VODKA-WARE LICENSE" (Revision 42):
 * Tim <tim@datenknoten.me> wrote this file.  As long as you retain this notice you
 * can do whatever you want with this stuff. If we meet some day, and you think
 * this stuff is worth it, you can buy me a vodka in return — Tim Schumacher
 ******************************************************************************/

package me.datenknoten.tueroeffner;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBar;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.os.Build;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.Toast;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.thoughtcrime.ssl.pinning.PinningTrustManager;
import org.thoughtcrime.ssl.pinning.SystemKeyStore;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.StringTokenizer;

import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;
import com.koushikdutta.ion.future.ResponseFuture;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;


public class MainActivity extends ActionBarActivity {

    final static String networkSSID = "KrautSpace";
    private DerivBroadcastReceiver receiver = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new PlaceholderFragment())
                    .commit();
        }


        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
        intentFilter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        receiver = new DerivBroadcastReceiver(this, networkSSID);
        registerReceiver(receiver, intentFilter);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(receiver);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            final View rootView = inflater.inflate(R.layout.fragment_main, container, false);

            final SharedPreferences door_pref = getActivity().getSharedPreferences("tueroeffner", Context.MODE_PRIVATE);
            String door_key = door_pref.getString(getString(R.string.door_key), "");
            final EditText key_editor = (EditText) rootView.findViewById(R.id.txtPass);
            key_editor.setText(door_key);

            key_editor.addTextChangedListener(new TextWatcher() {

                public void afterTextChanged(Editable s) {

                    // you can call or do what you want with your EditText here
                    SharedPreferences.Editor editor = door_pref.edit();
                    editor.putString(getString(R.string.door_key), key_editor.getText().toString());
                    editor.apply();

                }

                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                public void onTextChanged(CharSequence s, int start, int before, int count) {
                }
            });

            Switch s = (Switch) rootView.findViewById(R.id.switchWLAN);
            //
            if (s != null) {
                s.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        String escaped_ssid = "\"" + networkSSID + "\"";
                        Context context = rootView.getContext();
                        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
                        if (isChecked) {


                            WifiConfiguration wifiConfig = new WifiConfiguration();
                            if (Build.VERSION.SDK_INT > 17) {
                                wifiConfig.SSID = String.format("\"%s\"", networkSSID);
                            } else {
                                wifiConfig.SSID = networkSSID;
                            }

                            int netId = wifiManager.addNetwork(wifiConfig);
                            wifiManager.disconnect();
                            wifiManager.enableNetwork(netId, true);
                            wifiManager.reconnect();

                            Toast.makeText(buttonView.getContext(), getString(R.string.wlan_activated), Toast.LENGTH_SHORT).show();
                        } else {
                            List<WifiConfiguration> list = wifiManager.getConfiguredNetworks();
                            for (WifiConfiguration i : list) {
                                if (i.SSID != null && i.SSID.equals(networkSSID)) {
                                    wifiManager.removeNetwork(i.networkId);
                                }
                            }
                            Toast.makeText(buttonView.getContext(), getString(R.string.wlan_deactivated), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }

            return rootView;
        }
    }

    private String getDoorKey() {
        EditText key_editor = (EditText) findViewById(R.id.txtPass);
        return key_editor.getText().toString();
    }

    /**
     * Executes outdoor buzz
     *
     * @param v
     */
    public void buttonOpenOuterDoor(View v) {
        executeCommand("outdoor_buzz", v.getContext());
    }

    /**
     * Executes unlock indoor
     *
     * @param v
     */
    public void buttonOpenInnerDoor(View v) {
        executeCommand("indoor_unlock", v.getContext());
    }

    /**
     * Executes open indoor
     *
     * @param v
     */
    public void buttonUnlockInnerDoor(View v) {
        executeCommand("indoor_open", v.getContext());
    }

    public void buttonLockInnerDoor(View v) {
        executeCommand("indoor_lock", v.getContext());
    }

    private void executeCommand(String cmd, Context context) {
        // Trust Manager
        TrustManager[] trustAllCerts = new TrustManager[]{new X509TrustManager() {
            public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                return null;
            }

            public void checkClientTrusted(java.security.cert.X509Certificate[] certs, String authType) {
            }

            public void checkServerTrusted(java.security.cert.X509Certificate[] certs, String authType) {
            }
        }};
        //Install the all-trusting trust manager
        SSLContext sc = null;
        try {
            sc = SSLContext.getInstance("TLS");
            sc.init(null, trustAllCerts, new java.security.SecureRandom());
            sc.setDefault(sc);

        } catch (Exception e) {
        }

        /** TODO it would be better to get the PinningTrustManager to get running, because its more safe. */
        /*TrustManager[] trustManagers = new TrustManager[]{new PinningTrustManager(SystemKeyStore.getInstance(context),
                new String[]{"F1E2BB0724ACF34E60557DE95BD3DD30BCD08817"}, 0)};*/
        
        Ion.getDefault(context).getHttpClient().getSSLSocketMiddleware().setTrustManagers(trustAllCerts);
        Ion.getDefault(context).getHttpClient().getSSLSocketMiddleware().setSSLContext(sc);

        Ion.getDefault(context).getHttpClient().getSSLSocketMiddleware().setHostnameVerifier(org.apache.http.conn.ssl.SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);

        Ion.getDefault(this).configure().setLogging("iontest", Log.VERBOSE);

        Ion.with(this).load("https://tuer.hackspace-jena.de/cgi-bin/kraut.space?secret=" + this.getDoorKey() + "&cmd=" + cmd).asString();
    }
}
