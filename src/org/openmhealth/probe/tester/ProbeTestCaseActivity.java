
package org.openmhealth.probe.tester;

import android.app.Activity;
import android.os.Bundle;
import android.os.RemoteException;
import android.view.View;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;
import org.ohmage.probemanager.ProbeBuilder;
import org.ohmage.probemanager.ProbeWriter;

public class ProbeTestCaseActivity extends Activity {
    protected static final String TAG = "ProbeManager";

    private static final String OBSERVER_ID = "org.openmhealth.probe.tester";
    private static final int OBSERVER_VERSION = 1;

    private static final String STREAM_METADATA = "metadata";
    private static final int STREAM_METADATA_VERISON = 0;

    private ProbeWriter probeWriter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        probeWriter = new ProbeWriter(this);
        probeWriter.connect();

        setContentView(R.layout.activity_test_case);
    }

    /**
     * This tests sending a valid probe
     * 
     * @param v
     */
    public void validProbe(View v) {
        try {
            JSONObject d = new JSONObject();
            d.put("count", 0);
            ProbeBuilder probe = new ProbeBuilder(OBSERVER_ID, OBSERVER_VERSION);
            probe.setStream(STREAM_METADATA, STREAM_METADATA_VERISON);
            probe.setData(d.toString());

            sendProbe(probe);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * This tests sending a valid probe
     * 
     * @param v
     */
    public void unknownProbe(View v) {
        try {
            JSONObject d = new JSONObject();
            d.put("count", 0);
            ProbeBuilder probe = new ProbeBuilder("org.unknown.probe", 0);
            probe.setStream(STREAM_METADATA, STREAM_METADATA_VERISON);
            probe.setData(d.toString());

            sendProbe(probe);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * This tests sending a valid probe
     * 
     * @param v
     */
    public void unknownProbeVersion(View v) {
        try {
            JSONObject d = new JSONObject();
            d.put("count", 0);
            ProbeBuilder probe = new ProbeBuilder(OBSERVER_ID, -1);
            probe.setStream(STREAM_METADATA, STREAM_METADATA_VERISON);
            probe.setData(d.toString());

            sendProbe(probe);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * This tests sending a valid probe
     * 
     * @param v
     */
    public void unknownStream(View v) {
        try {
            JSONObject d = new JSONObject();
            d.put("count", 0);
            ProbeBuilder probe = new ProbeBuilder(OBSERVER_ID, OBSERVER_VERSION);
            probe.setStream("org.unknown.stream", STREAM_METADATA_VERISON);
            probe.setData(d.toString());

            sendProbe(probe);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * This tests sending a valid probe
     * 
     * @param v
     */
    public void unknownStreamVersion(View v) {
        try {
            JSONObject d = new JSONObject();
            d.put("count", 0);
            ProbeBuilder probe = new ProbeBuilder(OBSERVER_ID, OBSERVER_VERSION);
            probe.setStream(STREAM_METADATA, -1);
            probe.setData(d.toString());

            sendProbe(probe);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * This tests sending a probe with malformed metadata. This should throw an
     * exception when sent
     * 
     * @param v
     */
    public void malformedMetadata(View v) {
        try {
            JSONObject d = new JSONObject();
            d.put("count", 0);
            ProbeBuilder probe = new ProbeBuilder(OBSERVER_ID, OBSERVER_VERSION);
            probe.setStream(STREAM_METADATA, STREAM_METADATA_VERISON);
            probe.setData(d.toString());
            probe.setMetadata("blah.. this is bad");

            sendProbe(probe);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * This tests what happens to a probe which sends some metadata but not the
     * required metadata
     * 
     * @param v
     */
    public void invalidMetadata(View v) {
        try {
            JSONObject d = new JSONObject();
            d.put("count", 0);
            ProbeBuilder probe = new ProbeBuilder(OBSERVER_ID, OBSERVER_VERSION);
            probe.setStream(STREAM_METADATA, STREAM_METADATA_VERISON);
            probe.setData(d.toString());
            probe.withLocation(0, "TZ", 1, 1, 1, "fake");

            sendProbe(probe);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * This tests what happens to a probe which has extra metadata that is not
     * defined in the schema
     * 
     * @param v
     */
    public void extraMetadata(View v) {
        try {
            JSONObject d = new JSONObject();
            d.put("count", 0);
            ProbeBuilder probe = new ProbeBuilder(OBSERVER_ID, OBSERVER_VERSION);
            probe.setStream(STREAM_METADATA, STREAM_METADATA_VERISON);
            probe.setData(d.toString());
            probe.now().withId().withLocation(0, "TZ", 1, 1, 1, "fake");

            sendProbe(probe);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * This tests what happens when no metadata is supplied even though there
     * are required fields
     * 
     * @param v
     */
    public void noMetadata(View v) {
        try {
            JSONObject d = new JSONObject();
            d.put("count", 0);
            ProbeBuilder probe = new ProbeBuilder(OBSERVER_ID, OBSERVER_VERSION);
            probe.setStream(STREAM_METADATA, STREAM_METADATA_VERISON);
            probe.setData(d.toString());

            sendProbe(probe);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * This tests what happens when not all the data that is required is sent
     * 
     * @param v
     */
    public void missingMetadata(View v) {
        try {
            JSONObject d = new JSONObject();
            d.put("count", 0);
            ProbeBuilder probe = new ProbeBuilder(OBSERVER_ID, OBSERVER_VERSION);
            probe.setStream(STREAM_METADATA, STREAM_METADATA_VERISON);
            probe.setData(d.toString());
            probe.withId();

            sendProbe(probe);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * This tests what happens when data is sent is not valid JSON. This should
     * throw an exception when sent.
     * 
     * @param v
     */
    public void malformedData(View v) {
        ProbeBuilder probe = new ProbeBuilder(OBSERVER_ID, OBSERVER_VERSION);
        probe.setStream(STREAM_METADATA, STREAM_METADATA_VERISON);
        probe.setData("blah bad");
        probe.withId().now();

        sendProbe(probe);
    }

    /**
     * This tests what happens when invalid data is sent to the server
     * 
     * @param v
     */
    public void invalidData(View v) {
        try {
            JSONObject d = new JSONObject();
            d.put("bad", 0);
            ProbeBuilder probe = new ProbeBuilder(OBSERVER_ID, OBSERVER_VERSION);
            probe.setStream(STREAM_METADATA, STREAM_METADATA_VERISON);
            probe.setData(d.toString());
            probe.withId().now();

            sendProbe(probe);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * This tests what happens when no data is sent with the probe. This should
     * throw an exception when sent
     * 
     * @param v
     */
    public void noData(View v) {
        ProbeBuilder probe = new ProbeBuilder(OBSERVER_ID, OBSERVER_VERSION);
        probe.setStream(STREAM_METADATA, STREAM_METADATA_VERISON);
        probe.withId().now();

        sendProbe(probe);

    }

    /**
     * This test what happens when a probe is sent without the required data
     * 
     * @param v
     */
    public void missingData(View v) {
        JSONObject d = new JSONObject();
        ProbeBuilder probe = new ProbeBuilder(OBSERVER_ID, OBSERVER_VERSION);
        probe.setStream(STREAM_METADATA, STREAM_METADATA_VERISON);
        probe.setData(d.toString());
        probe.withId().now();

        sendProbe(probe);
    }

    /**
     * This tests what happens when a probe is completely empty. This should
     * throw an exception when sent
     * 
     * @param v
     */
    public void empty(View v) {
        ProbeBuilder probe = new ProbeBuilder(OBSERVER_ID, OBSERVER_VERSION);
        probe.setStream(STREAM_METADATA, STREAM_METADATA_VERISON);
        sendProbe(probe);
    }

    private void sendProbe(ProbeBuilder probe) {
        try {
            probe.write(probeWriter);
            Toast.makeText(this, "Probe Sent", Toast.LENGTH_SHORT).show();
        } catch (RemoteException e) {
            Toast.makeText(this, "Remote Exception", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        } catch (RuntimeException e) {
            Toast.makeText(this, "Probe Not Sent", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        probeWriter.close();
    }
}
