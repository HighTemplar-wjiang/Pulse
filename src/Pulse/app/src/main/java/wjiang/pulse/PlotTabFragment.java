package wjiang.pulse;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
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
public class PlotTabFragment extends Fragment implements MediaPlayer.OnErrorListener, MediaPlayer.OnPreparedListener {

    private Context mContext;
    private SensorManager mSensorManager;
    private Sensor mMagnetometer;
    private SensorEventListener mMagnetometerListener;
    private TextView mMagnetometerView;
    public DecoderController mDecoderController;
    private DataProcessor mDataProcessor;
    //private Decoder mDecoder;

    private boolean mCalibrationFlag;
    private Button mCalibrate;

    private boolean mDebugSwitch;
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

    public Context getViewContext() {
        return this.mContext;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_tab_1, container, false);

        this.mContext = getActivity().getApplicationContext();
        return rootView;
    }

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
        this.mMagnetometerView = (TextView) getActivity().findViewById(R.id.tvMagnetometer1);
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

        this.mCalibrate = (Button) getView().findViewById(R.id.btnStart1);
        this.mCalibrate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("DecoderEvent", "Start calibrating");
                mDecoderController.setCalibrationFlag(false);

                /*byte[] test = new byte[4];
                test[0] = (byte)84;
                test[1] = (byte)192;
                test[2] = (byte)84;
                test[3] = (byte)192;
                mDataProcessor.processData(test);*/
                /*
                String srcPath = Environment.getExternalStorageDirectory()+"/AfterStory/";
                NoteSequencer seq = new NoteSequencer();
                seq.readNoteFile(srcPath + "The Party in the Reference Room.txt");

                SequencePlayer player = new SequencePlayer();
                player.setSequencer(seq);
                player.start();*/

                //mDataProcessor.getPlayer().test();
            }
        });
    }

    public void play() {
        //MediaPlayer player = MediaPlayer.create(getViewContext(), Uri.parse("/storage/emulated/0/AfterStory/piano_src/140.mp3"));
        MediaPlayer player = new MediaPlayer();
        try{
            player.reset();
            player.setAudioStreamType(AudioManager.STREAM_MUSIC);
            player.setDataSource("/storage/emulated/0/AfterStory/piano_src/12!.mp3");
            player.setOnErrorListener(this);
            player.setOnPreparedListener(this);
            player.prepare();
            player.start();
        }
        catch (Exception e) {
            ;
        }
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

    @Override
    public void onPrepared(MediaPlayer play) {
        play.start();
    }
    @Override
    public boolean onError(MediaPlayer arg0, int arg1, int arg2) {
        return false;
    }
}