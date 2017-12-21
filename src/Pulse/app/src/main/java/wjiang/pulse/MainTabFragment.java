package wjiang.pulse;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import java.io.File;
import java.io.FileOutputStream;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by wjiang on 1/25/14.
 */
public class MainTabFragment extends Fragment {

    private SensorManager mSensorManager;
    private Sensor mMagnetometer;
    private SensorEventListener mMagnetometerListener;
    private TextView mMagnetometerView;
    public DecoderController mDecoderController;
    private DataProcessor mDataProcessor;
    //private Decoder mDecoder;

    private boolean mCalibrationFlag;
    private Button mCalibrate;

    private Boolean mDebugSwitch = false;
    private FileOutputStream mFout;


    /**
     * write data to log file
     * @param bit
     */
    private void writeLog(String bit) {
        if (this.mDebugSwitch) {
            try {
                byte [] bytes = bit.getBytes();
                this.mFout.write(bytes);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_tab_0, container, false);

        return rootView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (this.mDebugSwitch) {
            try {
                String logDir = Environment.getExternalStorageDirectory().getPath() + "/AfterStory/debugLog";
                File path = new File(logDir);
                if(!path.exists()) {
                    try {
                        Log.v("MakeDir", "" + path.mkdir());
                    } catch (Exception e) {
                        Log.v("MakeDir", "Exception");
                        e.printStackTrace();
                    }
                }
                else {
                    Log.v("MakeDir", "Path Exists");
                }

                String logPath = logDir + "/Bit_" +
                        (new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss")).format(new Date()) + ".log";
                Log.v("logPath", logPath);
                File logFile = new File(logPath);
                if(!logFile.exists()) {
                    try {
                        logFile.createNewFile();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                this.mFout = new FileOutputStream(logPath);
            } catch (Exception e){
                e.printStackTrace();
            }
        }

        this.mCalibrationFlag = false;
        this.mDataProcessor = new DataProcessor(this);
        this.mSensorManager = (SensorManager) getActivity().getSystemService(Context.SENSOR_SERVICE);
        this.mMagnetometer  = this.mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        //this.mDecoder = new Decoder();
        boolean[] channel_flags = new boolean[3];
        channel_flags[0] = true;
        channel_flags[1] = false;
        channel_flags[2] = true;
        this.mDecoderController = new DecoderController(8, 2, channel_flags);
        this.mMagnetometerView = (TextView) getActivity().findViewById(R.id.tvMagnetometer);
        this.mMagnetometerListener = new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent sensorEvent) {
                if (sensorEvent.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD)
                {
                    NumberFormat ddf = NumberFormat.getNumberInstance();
                    ddf.setMinimumFractionDigits(2);
                    ddf.setMaximumFractionDigits(2);
                    mMagnetometerView.setText("Magnetometer\nX: " + ddf.format(sensorEvent.values[0]) + " uT"
                            + " | Y: " + ddf.format(sensorEvent.values[1]) + " uT"
                            + " | Z: " + ddf.format(sensorEvent.values[2]) + " uT");
                    mDataProcessor.processData(mDecoderController.sensorEventCallback(sensorEvent));
                }
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int i) {
            }
        };

        this.mCalibrate = (Button) getView().findViewById(R.id.btnStart);
        this.mCalibrate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("DecoderEvent", "Start calibrating");
               mDecoderController.setCalibrationFlag(false);
            }
        });
    }

    @Override
    public void onPause() {
        super.onPause();
        this.mSensorManager.unregisterListener(this.mMagnetometerListener);
    }

    @Override
    public void onResume() {
        super.onResume();
        this.mSensorManager.registerListener(this.mMagnetometerListener, this.mMagnetometer, SensorManager.SENSOR_DELAY_FASTEST);
    }

    void initialSensor() {
    }

}
