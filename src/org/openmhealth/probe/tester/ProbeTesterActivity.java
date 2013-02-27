
package org.openmhealth.probe.tester;

import android.app.Activity;
import android.os.Bundle;
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

public class ProbeTesterActivity extends Activity {
    protected static final String TAG = "ProbeManager";

    private static final String OBSERVER_ID = "org.openmhealth.probe.tester";
    private static final int OBSERVER_VERSION = 1;

    private static final String STREAM_SIZE = "size";
    private static final int STREAM_SIZE_VERISON = 0;

    private ProbeWriter probeWriter;

    private EditText frequency;
    private EditText size;
    private EditText threadCount;

    private ProbeThread[] tasks;

    public static class ProbeThread extends Thread {

        private final ProbeWriter mProbeWriter;
        private final long mFrequency;
        private String mData;
        private final int mSize;
        private boolean stop;

        public ProbeThread(ProbeWriter probeWriter, int size, long frequency) {
            mProbeWriter = probeWriter;
            mFrequency = frequency;
            mSize = size;
        }

        @Override
        public void run() {
            try {

                try {
                    JSONObject d = new JSONObject();
                    StringBuilder builder = new StringBuilder(mSize);
                    for(int i=0;i<mSize;i++) {
                        builder.append(0);
                    }
                    d.put("data", builder.toString());
                    mData = d.toString();
                } catch (JSONException e) {

                }

                while (!stop) {
                    long start = System.currentTimeMillis();
                    writeProbe();
                    long wait = mFrequency - (System.currentTimeMillis() - start);
                    if (wait < 0) {
                        Log.d(TAG, "Could not send fast enough by: " + (-wait) + "ms");
                        wait = 0;
                    }
                    sleep(wait);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        /**
         * Write {@link count} bytes to ohmage
         * 
         * @param text
         */
        public void writeProbe() {
            try {
                ProbeBuilder probe = new ProbeBuilder(OBSERVER_ID, OBSERVER_VERSION);
                probe.setStream(STREAM_SIZE, STREAM_SIZE_VERISON);
                probe.setData(mData);
                probe.withId();
                // Log.d(TAG, "data: " + data.toString());
                long start = System.currentTimeMillis();
                probe.write(mProbeWriter);
                long end = System.currentTimeMillis();
                Log.d(TAG, "finished probe " + System.currentTimeMillis());
                Log.d(TAG, "Duration (" + mSize +  "): " + (end - start));
            } catch (RemoteException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        public void finish() {
            stop = true;
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
        threadCount = (EditText) findViewById(R.id.thread_count);

        final Button send = (Button) findViewById(R.id.send);
        send.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (tasks == null) {
                    send.setText("Stop");
                    start();
                } else {
                    send.setText("Start");
                    for (ProbeThread t : tasks) {
                        t.finish();
                    }
                    tasks = null;
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
            int tc = Integer.valueOf(threadCount.getText().toString());
            tasks = new ProbeThread[tc];
            int byteSize = Integer.valueOf(size.getText().toString());
            int freq = Integer.valueOf(frequency.getText().toString());
            for (int i = 0; i < tc; i++)
                tasks[i] = new ProbeThread(probeWriter, byteSize, freq);
            for (ProbeThread t : tasks)
                t.start();
        } catch (NumberFormatException e) {
            tasks = null;
            Toast.makeText(this, "enter valid numbers", Toast.LENGTH_SHORT).show();
        }
    }

}
