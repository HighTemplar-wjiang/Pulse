package wjiang.pulse;

import android.hardware.SensorEvent;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import static java.lang.Math.abs;

public class DecoderController {

    private boolean[] mChannelFlags = new boolean[3]; // channel flag of X, Y, Z
    private int mChannels;
    private int mSampleTimes;
    private int mBitsOnce;
    private int mLevels;
    private boolean mIsCalibrated;
    public Integer mCalibrationCounter;
    private float[] mLastValue;

    // definition of status
    private final int _NOSIGNAL  = 0;
    private final int _RECEIVING = 1;

    private final float _THRESHOLD = 4.0f;

    private int mStatus;
    private float[] mNullReferences;

    public SensorValueBuffer mBuffer;
    public SeriesDecoder mDecoder;

    private int mDecoderCounter;

    /**
     * mLogDir: path of log file
     * mFout: file handle for log file
     */
    private boolean mDebugSwith = true;
    private String mLogDir;
    private FileOutputStream mFoutRaw;
    private FileOutputStream mFoutBit;

    DecoderController() {
        this.mSampleTimes = 8;
        this.mBitsOnce = 1;
        this.mLevels = 2;
        this.mIsCalibrated = false;
        this.mCalibrationCounter = -2;
        this.mLastValue = new float[3];

        this.mChannelFlags[0] = false;
        this.mChannelFlags[1] = false;
        this.mChannelFlags[2] = true;
        this.mChannels = 1;

        this.mStatus = this._NOSIGNAL;
        this.mBuffer = new SensorValueBuffer();
        this.mDecoder = new SeriesDecoder(this.mSampleTimes, this.mBitsOnce, this.mChannelFlags);

        this.mBuffer.empty();
        this.mDecoderCounter = 0;

        this.mNullReferences = new float[3];

        if (this.mDebugSwith) {
            try {
                this.mLogDir = Environment.getExternalStorageDirectory().getPath() + "/AfterStory/debugLog";
                Log.v("LogPath", this.mLogDir);
                File path = new File(this.mLogDir);
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

                String logPath = this.mLogDir + "/Raw_" +
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
                this.mFoutRaw = new FileOutputStream(logPath);

                logPath = this.mLogDir + "/Bit_" +
                        (new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss")).format(new Date()) + ".log";
                Log.v("logPath", logPath);
                logFile = new File(logPath);
                if(!logFile.exists()) {
                    try {
                        logFile.createNewFile();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                this.mFoutBit = new FileOutputStream(logPath);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        /*
        // fake calibration values
        float[] calvals = new float[3];
        calvals[0] = 0.0f;
        calvals[1] = 0.0f;
        calvals[2] = -7.0f;
        this.mDecoder.calibrate(-7.0f, calvals, 0); // debug
        calvals[2] = -24.0f;
        this.mDecoder.calibrate(-7.0f, calvals, 1); // debug*/
    }

    DecoderController(int sampleTimes, int bitsOnce, boolean[] channelFlags) {
        this.mChannels = 0;
        if(sampleTimes > 0 && bitsOnce > 0) {
            for(int i = 0; i < 3; i ++) {
                this.mChannelFlags[i] = channelFlags[i];
                if(channelFlags[i] == true) {
                    this.mChannels ++;
                }
            }

            this.mSampleTimes = sampleTimes;
            this.mBitsOnce = bitsOnce;
            this.mLevels = 1 << this.mBitsOnce;
        }
        else {
            this.mSampleTimes = 0;
            this.mBitsOnce = 0;
            this.mLevels = 0;
        }
        this.mIsCalibrated = false;
        this.mCalibrationCounter = -2;
        this.mLastValue = new float[3];

        this.mStatus = this._NOSIGNAL;
        this.mBuffer = new SensorValueBuffer();
        this.mDecoder = new SeriesDecoder(this.mSampleTimes, this.mBitsOnce, this.mChannelFlags);

        this.mBuffer.empty();
        this.mDecoderCounter = 0;

        this.mNullReferences = new float[3];

        if (this.mDebugSwith) {
            try {
                this.mLogDir = Environment.getExternalStorageDirectory().getPath() + "/AfterStory/debugLog";
                Log.v("LogPath", this.mLogDir);
                File path = new File(this.mLogDir);
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

                String logPath = this.mLogDir + "/Raw_" +
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
                this.mFoutRaw = new FileOutputStream(logPath);

                logPath = this.mLogDir + "/Bit_" +
                        (new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss")).format(new Date()) + ".log";
                Log.v("logPath", logPath);
                logFile = new File(logPath);
                if(!logFile.exists()) {
                    try {
                        logFile.createNewFile();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                this.mFoutBit = new FileOutputStream(logPath);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        /*
        float[] calvals = new float[3];
        calvals[0] = 0.0f;
        calvals[1] = 0.0f;
        calvals[2] = -7.0f;
        this.mDecoder.calibrate(-7.0f, calvals, 0); // debug
        calvals[2] = -24.0f;
        this.mDecoder.calibrate(-7.0f, calvals, 1); // debug
        */
    }

    /**
     * write data to log file
     * @param sensorEvent
     */
    private void writeLog(SensorEvent sensorEvent) {
        try {
            byte [] bytesX =
                    ("X," + sensorEvent.timestamp + "," + sensorEvent.values[0] + "\n").getBytes();
            byte [] bytesY =
                    ("Y," + sensorEvent.timestamp + "," + sensorEvent.values[1] + "\n").getBytes();
            byte [] bytesZ =
                    ("Z," + sensorEvent.timestamp + "," + sensorEvent.values[2] + "\n").getBytes();
            this.mFoutRaw.write(bytesX);
            this.mFoutRaw.write(bytesY);
            this.mFoutRaw.write(bytesZ);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * write data to log file
     * @param data
     */
    private void writeLog(byte[] data) {
        byte[] one = new byte[1];
        try {
            for(int i = 0; i < data.length; i++) {
                one[0] = data[i];
                this.mFoutBit.write(DataProcessor.bytetoBinary(one).getBytes());
                this.mFoutBit.write("\n".getBytes());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean getCalibrationFlag() {
        return this.mIsCalibrated;
    }

    public void setCalibrationFlag(boolean flag) {
        if(flag == false) {
            this.mIsCalibrated = false;
            this.mCalibrationCounter = -1;
            this.mDecoder.setCalibrationFlag(false);
        }
        else{
            this.mDecoder.setCalibrationFlag(true);
            this.mIsCalibrated = true;
        }
    }

    public void calibrate(SensorEvent event) {
        float[] calibrationValue = new float[3];

        if(this.mCalibrationCounter == -3) {
            if(abs(this.mNullReferences[0] - event.values[0]) < this._THRESHOLD &&
                    abs(this.mNullReferences[1] - event.values[1]) < this._THRESHOLD &&
                    abs(this.mNullReferences[2] - event.values[2]) < this._THRESHOLD) {
                this.mIsCalibrated = true;
            }
        }

        if(this.mCalibrationCounter == -1) {
            this.mNullReferences[0] = event.values[0];
            this.mNullReferences[1] = event.values[1];
            this.mNullReferences[2] = event.values[2];
            this.mCalibrationCounter = 0;
        }

        if(this.mCalibrationCounter > -1) {
            if(abs(this.mLastValue[0] - event.values[0]) < this._THRESHOLD &&
                    abs(this.mLastValue[1] - event.values[1]) < this._THRESHOLD &&
                    abs(this.mLastValue[2] - event.values[2]) < this._THRESHOLD) {
                ;
            }
            else {
                calibrationValue = event.values.clone();
                //Log.d("Calibration" ,"" + this.mNullReferences[2]);
                //Log.d("Calibration" ,"" + "" + calibrationValue[0] + "" + calibrationValue[1] + "" + calibrationValue[2]);
                if(true ==
                        this.mDecoder.calibrate(this.mNullReferences, calibrationValue, this.mCalibrationCounter)) {
                    this.mCalibrationCounter = -3;
                }
                else {
                    this.mCalibrationCounter++;
                }
            }
        }

        this.mLastValue = event.values.clone();
    }

    public byte[] sensorEventCallback(SensorEvent event) {
        byte[] returnValue = null;

        if(this.mDebugSwith) {
            this.writeLog(event);
        }

        if(this.mIsCalibrated == false) {
            this.calibrate(event);
            this.mStatus = this._NOSIGNAL;
            return returnValue;
        }

        if(abs(this.mNullReferences[0] - event.values[0]) < this._THRESHOLD &&
                abs(this.mNullReferences[1] - event.values[1]) < this._THRESHOLD &&
                abs(this.mNullReferences[2] - event.values[2]) < this._THRESHOLD)
        {
            if(this.mStatus == this._RECEIVING) {
                //System.out.println("decoding");
                this.mDecoderCounter++;
                returnValue = this.mDecoder.decode(this.mBuffer.getTimeStampBuffer(),
                        this.mBuffer.getValueBuffer(0),
                        this.mBuffer.getValueBuffer(1),
                        this.mBuffer.getValueBuffer(2));
                this.mBuffer.empty();

                if(this.mDebugSwith) {
                    this.writeLog(returnValue);
                }
                //System.out.println(this.mDecoderCounter);
            }

            this.mStatus = this._NOSIGNAL;
            this.mNullReferences[0] = this.mNullReferences[0] * 0.8f + event.values[0] * 0.2f;
            this.mNullReferences[1] = this.mNullReferences[1] * 0.8f + event.values[1] * 0.2f;
            this.mNullReferences[2] = this.mNullReferences[2] * 0.8f + event.values[2] * 0.2f;
        }
        else
        {
            this.mStatus = this._RECEIVING;
            this.mBuffer.add(event);
        }

        return returnValue;
    }
}