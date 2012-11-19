
package org.openmhealth.probe.tester;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;
import org.ohmage.probemanager.ProbeBuilder;
import org.ohmage.probemanager.ProbeWriter;

import java.lang.ref.WeakReference;

public class ProbeTesterActivity extends Activity {
    protected static final String TAG = "ProbeManager";

    private static final String OBSERVER_ID = "org.openmhealth.probe.tester";
    private static final int OBSERVER_VERSION = 1;

    private static final String STREAM_SIZE = "size";
    private static final int STREAM_SIZE_VERISON = 0;

    private ProbeWriter probeWriter;

    private EditText frequency;
    private EditText size;

    private static boolean running;

    private static ProbeHandler mHandler = new ProbeHandler();
    
    public static class ProbeHandler extends Handler {
        
        private static WeakReference<ProbeWriter> mProbeWriter;

        public void setProbeWriter(ProbeWriter probeWriter) {
            mProbeWriter = new WeakReference<ProbeWriter>(probeWriter);
        }
        
        @Override
        public void handleMessage(Message msg) {
            long start = System.currentTimeMillis();
            Log.d(TAG, System.currentTimeMillis() + " message happened: " + msg.arg1);
            ProbeWriter probeWriter = mProbeWriter.get();
            if(probeWriter != null)
                writeSimple(probeWriter, msg.arg2);
            else
                throw new RuntimeException("ProbeWriter was lost");
            Message m2 = new Message();
            m2.copyFrom(msg);
            mHandler.sendMessageDelayed(m2, m2.arg1 - (System.currentTimeMillis() - start));
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        probeWriter = new ProbeWriter(this);
        probeWriter.connect();

        setContentView(R.layout.activity_probe_example);

        frequency = (EditText) findViewById(R.id.frequency);
        size = (EditText) findViewById(R.id.size);

        final Button send = (Button) findViewById(R.id.send);
        send.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                running = !running;
                if (running) {
                    send.setText("Stop");
                    mHandler.setProbeWriter(probeWriter);
                    start();
                } else {
                    send.setText("Start");
                    mHandler.removeMessages(0);
                }

            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        probeWriter.close();
    }

    private void start() {
        try {
            Message msg = new Message();
            msg.arg1 = Integer.valueOf(frequency.getText().toString());
            msg.arg2 = Integer.valueOf(size.getText().toString());
            mHandler.sendMessageDelayed(msg, msg.arg1);
        } catch (NumberFormatException e) {
            running = false;
            Toast.makeText(this, "enter valid numbers", Toast.LENGTH_SHORT).show();
        }
    }

    public static int s;
    public static String data;
    
    /**
     * Write {@link count} bytes to ohmage
     * 
     * @param text
     */
    public static void writeSimple(ProbeWriter probeWriter, int count) {

        try {
            if(data == null || s != count) {
                s = count;

                JSONObject d = new JSONObject();
                d.put("data", new String(new char[count]));

                data = d.toString();
            }

            ProbeBuilder probe = new ProbeBuilder(OBSERVER_ID, OBSERVER_VERSION);
            probe.setStream(STREAM_SIZE, STREAM_SIZE_VERISON);
            probe.setData(data.toString());
            //Log.d(TAG, "data: " + data.toString());
            long start = System.currentTimeMillis();
            probe.write(probeWriter);
            long end = System.currentTimeMillis();
            Log.d(TAG, "finished probe " + System.currentTimeMillis());
            Log.d(TAG, "Duration: " + (end - start));
            

        } catch (RemoteException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
