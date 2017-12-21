package wjiang.pulse;

import android.widget.TextView;

/**
 * Created by wjiang on 2/25/14.
 */
public class DataProcessor {

    private boolean mTabFlag;
    private NoteSequencer mSequencer;
    private MainTabFragment mMainTabFragment;
    private PlotTabFragment mPlotTabFragment;
    private SequencePlayer mPlayer;
    private byte mNoteBuffer;
    private byte mCmdBuffer;
    private int mNoteCount;

    private String mStatus;
    private String mDisplay;
    private String mDebug;
    private TextView mStatusView;
    private TextView mDisplayView;
    private TextView mDebugView;

    public DataProcessor(MainTabFragment tabFragment) {
        this.mTabFlag = true;
        this.mMainTabFragment = tabFragment;
        this.mSequencer = null;
        this.mPlayer = null;
        this.mNoteBuffer = (byte)0;
        this.mCmdBuffer  = (byte)0;
        this.mNoteCount  = 0;

        this.mStatus  = new String ();
        this.mDisplay = new String ();
        this.mDebug   = new String ();
        this.mStatusView  = (TextView) this.mMainTabFragment.getView().findViewById(R.id.tvStatus);
        this.mDisplayView = (TextView) this.mMainTabFragment.getView().findViewById(R.id.tvDisplay);
        this.mDebugView   = (TextView) this.mMainTabFragment.getView().findViewById(R.id.tvDebug);
    }

    public SequencePlayer getPlayer() {
        return this.mPlayer;
    }

    public DataProcessor(PlotTabFragment tabFragment) {
        this.mTabFlag = false;
        this.mPlotTabFragment = tabFragment;
        this.mSequencer = new NoteSequencer();

        this.mStatus  = new String ();
        this.mDisplay = new String ();
        this.mDebug   = new String ();
        this.mStatusView  = (TextView) this.mPlotTabFragment.getView().findViewById(R.id.tvStatus1);
        this.mDisplayView = (TextView) this.mPlotTabFragment.getView().findViewById(R.id.tvDisplay1);
        this.mDebugView   = (TextView) this.mPlotTabFragment.getView().findViewById(R.id.tvDebug1);

        this.mPlayer = new SequencePlayer(this.mPlotTabFragment);
        this.mPlayer.setSequencer(this.mSequencer);
        this.mPlayer.start();

    }

    static public String bytetoBinary( byte[] bytes )
    {
        StringBuilder sb = new StringBuilder(bytes.length * Byte.SIZE);
        for( int i = 0; i < Byte.SIZE * bytes.length; i++ )
            sb.append((bytes[i / Byte.SIZE] << i % Byte.SIZE & 0x80) == 0 ? '0' : '1');
        return sb.toString();
    }

    void processData(byte[] data) {
        String debug = "";

        if(data == null) {
            if(this.mTabFlag == true) {
                debug += (new Boolean(this.mMainTabFragment.mDecoderController.getCalibrationFlag())).toString();
                debug += (" " + this.mMainTabFragment.mDecoderController.mCalibrationCounter.toString());
            }
            else {
                debug += (new Boolean(this.mPlotTabFragment.mDecoderController.getCalibrationFlag())).toString();
                debug += (" " + this.mPlotTabFragment.mDecoderController.mCalibrationCounter.toString());
            }
        }
        else {
            String str = new String(data);
            this.mStatus = this.mStatus + this.bytetoBinary(data);
            this.mStatus = this.mStatus.substring(0, Math.min(mStatus.length(), 30));
            if(this.mTabFlag == true) {
                this.mDisplay += str;
                this.mDisplayView.setText(this.mDisplay);
            }
            else {
                if(data != null) {
                    for(int i = 0; i < data.length; i++) {
                        if((data[i] >> 7) == 0) {
                            if(data[i] == 63) {
                                this.mNoteBuffer = 0;
                            }
                            else {
                                this.mNoteBuffer = data[i];
                            }
                        }
                        else {
                            this.mCmdBuffer  = data[i];
                        }

                        if(this.mNoteBuffer != 0 && this.mCmdBuffer != 0) {
                            this.mPlayer.getSequencer().add(this.mNoteBuffer, this.mCmdBuffer);

                            this.mNoteCount++;
                            if(this.mNoteCount == 10) {
                                this.mDisplay = this.mDisplay.substring(this.mDisplay.indexOf("\n") + 1);
                                this.mNoteCount = 9;
                            }
                            this.mDisplay += ((int)this.mNoteBuffer + "-" + (int)this.mCmdBuffer + "\n");
                            this.mNoteBuffer = 0;
                            this.mCmdBuffer  = 0;
                            this.mDisplayView.setText(this.mDisplay);
                        }
                    }
                }
            }
        }
        this.mStatusView.setText(this.mStatus);
        this.mDebugView.setText(debug);
    }
}
