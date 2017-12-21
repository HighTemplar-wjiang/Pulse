package wjiang.pulse;

import android.hardware.SensorEvent;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by JiangWeiwei on 13-12-1.
 */
public class Decoder {

    /**
     * mStatus shows the status of decoder
     * 0x00: decoder standby
     * 0x01: waiting for start sign
     * 0x02: start sign receiving
     * 0xF0: no signal detected
     * 0xF1: high level detected
     * 0xF2: low level detected
     */
    private int mStatus;
    private boolean mDebugSwith = true;
    public final int STATUS_STANDBY    = 0x00;
    public final int STATUS_WAITING    = 0x01;
    public final int STATUS_STARTING   = 0x02;
    public final int STATUS_NULLSIGNAL = 0xF0;
    public final int STATUS_HIGHLEVEL  = 0xF1;
    public final int STATUS_LOWLEVEL   = 0xF2;

    /**
     * mSignInterval stores the duration time of end signs (ms)
     * mCodeInterval stores the duration time of a bit (ms)
     * mHighThreshold shows how much distort can be recognized as 1
     * mHighThreshold shows how much distort can be recognized as 0
     */
    private long mSignInterval  = 0xFF;
    //private final int mCodeInterval  = 200;
    private final int mHighThreshold = 30;
    private final int mLowThreshold  = 20;

    /**
     * mlastTimeStamp stores the time of last event
     */
    private long mlastTimeStamp;

    /**
     * mStandardValue stores the value of magnetometer when there is no signal
     */
    private double mStandardValue;

    /**
     * array mBits stores the bits received
     * mCounter stores how many bits received in the base of 8
     * mLetter stores the decoded letter
     */
    private int mCounter;
    private int mBits[];
    private char mLetter;

    /**
     * mLogDir: path of log file
     * mFout: file handle for log file
     */
    private String mLogDir;
    private FileOutputStream mFout;


    Decoder() {
        this.mStatus = this.STATUS_STANDBY;
        this.mlastTimeStamp = 0;
        this.mStandardValue = 0;
        this.mCounter = 0;
        this.mBits = new int[8];
        this.mLetter = 0;

        if (this.mDebugSwith) {
            try {
                this.mLogDir = Environment.getExternalStorageDirectory().getPath() + "/debugLog";
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
                this.mFout = new FileOutputStream(logPath);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        this.mFout.close();
    }

    int getStaus() {
        return this.mStatus;
    }

    void setStatus(int status) {
        assert ((status >= 0x00 && status <= 0x02) || (status >= 0xF0 || status <= 0xF2));
        this.mStatus = status;
    }

    char getLetter() {
        return this.mLetter;
    }

    long getSignInterval() {
        return this.mSignInterval;
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
            this.mFout.write(bytesX);
            this.mFout.write(bytesY);
            this.mFout.write(bytesZ);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * when a sensor event happens, call this function
     * @param sensorEvent
     */
    int decode(SensorEvent sensorEvent) {
        int returnCode = 0xFF;
        double fluctuation = sensorEvent.values[2] - this.mStandardValue;
        this.writeLog(sensorEvent);
        switch (this.mStatus) {
            case 0x00: // standby
                this.mStatus = this.STATUS_WAITING;
                this.mStandardValue = sensorEvent.values[2];
                returnCode = 0xFF;
                break;

            case 0x01: // waiting for start sign
                if (fluctuation > this.mHighThreshold) {
                    this.mCounter = 0;
                    this.mLetter  = 0;
                    this.mStatus  = this.STATUS_STARTING;
                    this.mlastTimeStamp = sensorEvent.timestamp;
                }
                returnCode = 0xFF;
                break;

            case 0x02: // start sign receiving
                if (fluctuation < this.mHighThreshold / 2 && fluctuation > -this.mLowThreshold / 2) {
                    this.mStatus = this.STATUS_NULLSIGNAL;
                    this.mSignInterval  = sensorEvent.timestamp - this.mlastTimeStamp;
                    this.mlastTimeStamp = sensorEvent.timestamp;
                    returnCode = 0xFA;
                }
                else {
                    returnCode = 0xFF;
                }
                break;

            case 0xF0: // no signal
                if (fluctuation > this.mHighThreshold) {
                    this.mStatus = this.STATUS_HIGHLEVEL;
                    this.mlastTimeStamp = sensorEvent.timestamp;
                }
                else {
                    if (fluctuation < -this.mLowThreshold){
                        this.mStatus = this.STATUS_LOWLEVEL;
                        this.mlastTimeStamp = sensorEvent.timestamp;
                    }
                }
                returnCode = 0xFF;
                break;

            case 0xF1: // high
                if (fluctuation < this.mHighThreshold / 2 && fluctuation > -this.mLowThreshold / 2) {
                    this.mStatus = this.STATUS_NULLSIGNAL;
                    this.mBits[this.mCounter++] = 1;
                    this.mlastTimeStamp = sensorEvent.timestamp;
                    returnCode = 0x01;
                }
                else {
                    returnCode = 0xFF;
                }
                break;

            case 0xF2: // low
                if (fluctuation < this.mHighThreshold / 2 && fluctuation > -this.mLowThreshold / 2) {
                    this.mStatus = this.STATUS_NULLSIGNAL;
                    if ((sensorEvent.timestamp - this.mlastTimeStamp) > this.mSignInterval) {

                        this.mStatus = this.STATUS_WAITING;
                        returnCode = 0xFB;
                    }
                    else {
                        this.mBits[this.mCounter++] = 0;
                        returnCode = 0x00;
                    }
                    this.mlastTimeStamp = sensorEvent.timestamp;
                }
                else
                {
                    returnCode = 0xFF;
                }
                break;

            default:
                returnCode = 0xFF;
                break;
        }
        if(this.mCounter == 8) {
            char letter = 0;
            for (int x = 0; x < 8; x++) {
                letter += ((char)this.mBits[x]) << x;
            }
            this.mCounter = 0;
            this.mLetter = letter;
            returnCode |= 0xF0;
        }
        else {
            returnCode |= 0x00;
        }
        return returnCode;
    }
}

