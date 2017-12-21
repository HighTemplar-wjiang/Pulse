package wjiang.pulse;

import android.hardware.SensorEvent;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;

/**
 * Created by wjiang on 2/19/14.
 */
public class SensorValueBuffer {

    private long mMaxSize = 1000;

    private LinkedList<Long> mTimeStamp;

    private LinkedList<Float> mValueX;
    private LinkedList<Float> mValueY;
    private LinkedList<Float> mValueZ;

    private boolean mDebugSwith = false;
    private String mLogDir;
    private FileOutputStream mFout;

    public SensorValueBuffer() {
        this.mTimeStamp = new LinkedList<Long>();

        this.mValueX = new LinkedList<Float>();
        this.mValueY = new LinkedList<Float>();
        this.mValueZ = new LinkedList<Float>();

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

                String logPath = this.mLogDir + "/Buffer_" +
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

    public void empty() {
        this.mTimeStamp.clear();

        this.mValueX.clear();
        this.mValueY.clear();
        this.mValueZ.clear();
    }

    public boolean add(SensorEvent event) {
        long timeStamp = event.timestamp;

        float valueX = event.values[0];
        float valueY = event.values[1];
        float valueZ = event.values[2];

        if(this.mValueX.size() >= this.mMaxSize ||
                this.mValueY.size() >= this.mMaxSize ||
                this.mValueZ.size() >= this.mMaxSize) {
            return false;
        }
        else {
            try {
                this.mTimeStamp.add(timeStamp);
                this.mValueX.add(valueX);
                this.mValueY.add(valueY);
                this.mValueZ.add(valueZ);
                return true;
            }
            catch (Exception e) {
                return false;
            }
        }
    }

    public LinkedList<Long> getTimeStampBuffer() {
        LinkedList<Long> buffer = new LinkedList<Long>();
        buffer = (LinkedList<Long>)this.mTimeStamp.clone();
        return buffer;
    }

    public LinkedList<Float> getValueBuffer(int index) {
        LinkedList<Float> buffer = null;
        switch(index){
            case 0:
                buffer = new LinkedList<Float>(this.mValueX);
                break;

            case 1:
                buffer = new LinkedList<Float>(this.mValueY);
                break;

            case 2:
                buffer = new LinkedList<Float>(this.mValueZ);
                break;

            default:
                break;
        }
        return buffer;
    }

    public void writeLog(SensorEvent sensorEvent) {
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
}
