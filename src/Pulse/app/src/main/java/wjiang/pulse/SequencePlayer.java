package wjiang.pulse;

/**
 * Created by wjiang on 2/28/14.
 */
// format: Note    0AAA BBBC, A - Height | B - Step | C - Rising Flag
//         Command 1AAA ABBB, A - Length (from 001 (1/8) to 111(8/1 with point)
//                    B - Tone (reserved)

import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Environment;
import android.util.Log;

public class SequencePlayer implements MediaPlayer.OnErrorListener, MediaPlayer.OnPreparedListener  {

    private int mSpeed;
    private long mBaseTime; // 1/8 step
    private NoteSequencer mSequencer;
    private PlotTabFragment mFragment;

    private final Object mLock = new Object();
    private PlayerThread mPlayerThread;

    private int mSampleCount;
    private MediaPlayer mSamples0;
    private MediaPlayer mSamples1;
    private MediaPlayer mSamples2;

    public SequencePlayer(PlotTabFragment fragment) {
        super();

        this.mSpeed = 170;
        this.mBaseTime = 60000 / this.mSpeed;
        this.mSequencer = null;
        this.mFragment = fragment;

        this.mSampleCount = 0;
        this.mSamples0 = new MediaPlayer();
        this.mSamples1 = new MediaPlayer();
        this.mSamples2 = new MediaPlayer();

        this.mPlayerThread = new PlayerThread(this.mSequencer, this.mLock, false, this.mFragment);
    }

    public final Object getLock() {
        return this.mLock;
    }

    public NoteSequencer getSequencer() {
        return this.mSequencer;
    }

    public PlayerThread getPlayerThread() {
        return this.mPlayerThread;
    }

    public void setSequencer(NoteSequencer newSequencer) {
        this.mSequencer = newSequencer;
        this.pause();
        this.mPlayerThread = new PlayerThread(this.mSequencer, this.mLock, false, this.mFragment);
    }

    public void setSpeed(int speed) {
        if(speed > 0 && speed < 1000) {
            this.mSpeed = speed;
            this.mBaseTime = 1000 / this.mSpeed;
        }
    }

    public class PlayerThread extends Thread {

        private int mIdleCount;
        public Boolean mRunFlag;
        private Object mLock;
        private NoteSequencer mSequencer;
        private PlotTabFragment mFragment;

        PlayerThread(NoteSequencer sequencer, Object lock, Boolean runFlag, PlotTabFragment fragment) {
            super();

            this.mIdleCount = 0;
            this.mLock = lock;
            this.mRunFlag = runFlag;
            this.mSequencer = sequencer;
            this.mFragment = fragment;
        }

        public int getIdleCount(){
            return this.mIdleCount;
        }

        public void run() {
            int noteCount = 0;
            String display = "";
            System.out.println("Thread: start " + this.mRunFlag);
            try{
                synchronized(this.mLock) {
                    while(this.mRunFlag) {
                        Byte[] one = this.mSequencer.poll();
                        if(one != null && one.length == 2) {
                            Log.d("MediaPlayer", one[0] + "-" + one[1]);
                            playOneNote(one[0], one[1]);
                            if(this.mFragment != null) {
                                display += ("" + (int)one[0] + "-" + (int)one[1] + "\n");
                                noteCount++;
                                if(noteCount == 10) {
                                    display = display.substring(display.indexOf("\n") + 1);
                                    noteCount = 9;
                                }
                                //Log.d("MediaPlayer", display);
                                //TextView displayView = (TextView) this.mFragment.getView().findViewById(R.id.tvDisplay1);
                                //displayView.setText(display);
                            }
                            this.mIdleCount = 0;
                        }
                        else
                        {
                            this.mIdleCount++;
                            this.sleep(100);
                        }
                    }
                }
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void start() {
        synchronized(this.mLock) {
            this.mPlayerThread.mRunFlag = true;
        }
        this.mPlayerThread.start();
    }

    public void pause() {
        synchronized(this.mLock) {
            this.mPlayerThread.mRunFlag = false;
        }
    }

    public void reset() {
        ;
    }

    private void playOneSample(String samplePath) {
        try{
            MediaPlayer sample = null;
            this.mSampleCount++;
            this.mSampleCount %= 3;
            switch (this.mSampleCount) {
                case 0:
                    sample = this.mSamples0;
                    break;

                case 1:
                    sample = this.mSamples1;
                    break;

                case 2:
                    sample = this.mSamples2;
                    break;

                default:
                    sample = this.mSamples0;
                    break;
            }
                sample.reset();
                sample.setAudioStreamType(AudioManager.STREAM_MUSIC);
                sample.setDataSource(samplePath);
                sample.setOnErrorListener(this);
                sample.setOnPreparedListener(this);
                sample.prepare();
                sample.start();
            //player.release();
        }
        catch (Exception e) {
            Log.d("MediaPlayer", e.toString() + samplePath);
        }
    }

    public void test() {
        for(int i = 1; i <= 7; i++) {
            for(int j = 1; j <= 7; j++) {
                Byte note = (byte)((i << 4) | (j << 1) & 0xFF);
                Byte cmd  = (byte)((2 << 4) & 0xFF);
                this.playOneNote(note, cmd);
            }
        }
    }

    public void playOneNote(Byte note, Byte command) {
        int scale  = (note >> 4) & 0x07;
        int step   = (note >> 1) & 0x07;
        int rising = note & 0x01;
        long length = this.mBaseTime * 8 / (0x01 << (7 - ((command >> 4) & 0x07)));
        if (((command >> 3) & 0x01) != 0) {
            length *= 1.5;
        }

        try {
            if(scale == 0 || step == 0) {
                ;
            }
            else {
                String oneSampleFile = "";
                String srcPath = Environment.getExternalStorageDirectory()+"/AfterStory/piano_src/";

                if(rising == 0) {
                    oneSampleFile = srcPath + scale + step + "!.mp3";
                }
                else {
                    oneSampleFile = srcPath + scale + step + "#.mp3";
                }
                this.playOneSample(oneSampleFile);
            }
            Thread.sleep(length);
        }
        catch (Exception e) {
            Log.d("MediaPlayer", e.toString() + " " + scale + " " + step + " " + rising);
            System.out.println(e.toString() + " " + scale + " " + step + " " + rising);
            e.printStackTrace();
        }
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

