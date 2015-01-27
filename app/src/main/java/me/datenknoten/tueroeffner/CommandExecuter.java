/*******************************************************************************
 * "THE VODKA-WARE LICENSE" (Revision 42):
 * Tim <tim@datenknoten.me> wrote this file.  As long as you retain this notice you
 * can do whatever you want with this stuff. If we meet some day, and you think
 * this stuff is worth it, you can buy me a vodka in return — Tim Schumacher
 ******************************************************************************/

package me.datenknoten.tueroeffner;

import android.os.AsyncTask;
import android.widget.EditText;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * Created by tim on 27.01.2015.
 */
public class CommandExecuter extends AsyncTask<String, Integer, Boolean> {
    private String key = "";

    public CommandExecuter(String Key) {
        this.key = Key;
    }

    @Override
    protected Boolean doInBackground(String... params) {
        int count = params.length;

        try {
            for (String param : params) {
                executeCommand(param);
                // Escape early if cancel() is called
                if (isCancelled()) break;
            }
            return true;
        } catch (Exception e) {
            return false;

        }

    }

    /**
     * Sends a command to the door server.
     *
     * @param cmd
     */
    private Boolean executeCommand(String cmd) throws IOException, URISyntaxException {
        HttpClient client = new DefaultHttpClient();
        HttpGet request = new HttpGet();
        URI uri = new URI("https://tuer.hackspace-jena.de/cgi-bin/kraut.space?secret="+key+"&cmd="+cmd);
        request.setURI(uri);
        HttpResponse response = client.execute(request);
        return true;
    }
}
