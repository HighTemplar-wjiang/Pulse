package wjiang.pulse;

import android.util.Log;

import java.util.LinkedList;

import static java.lang.Math.abs;

/**
 * Created by wjiang on 2/19/14.
 */
public class SeriesDecoder {

    private boolean[] mChannelFlags = new boolean[3]; // channel flag of X, Y, Z
    private int mChannels;
    private int mSampleTimes;
    private int mBitsOnce;
    private int mLevels;
    private int mConditions;

    private boolean mIsCalibrated;

    private float[] mBaseValue;
    private float[] mReferenceValuesX;
    private float[] mReferenceValuesY;
    private float[] mReferenceValuesZ;

    public SeriesDecoder() {
        this.mSampleTimes = 8;
        this.mBitsOnce = 1;
        this.mLevels = 2;
        this.mIsCalibrated = false;

        this.mChannelFlags[0] = false;
        this.mChannelFlags[1] = false;
        this.mChannelFlags[2] = true;
        this.mChannels = 1;
        this.mConditions = (int)Math.pow((double)this.mLevels, (double)this.mChannels);

        this.mBaseValue = new float[3];
        this.mReferenceValuesX = new float[this.mConditions];
        this.mReferenceValuesY = new float[this.mConditions];
        this.mReferenceValuesZ = new float[this.mConditions];
        for(int i = 0; i < this.mConditions; i++) {
            this.mReferenceValuesX[i] = 0;
            this.mReferenceValuesY[i] = 0;
            this.mReferenceValuesZ[i] = 0;
        }
    }

    public SeriesDecoder(int sampleTimes, int bitsOnce, boolean[] channelFlags) {
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
            this.mConditions = (int)Math.pow((double)this.mLevels, (double)this.mChannels);

            this.mBaseValue = new float[3];
            this.mReferenceValuesX = new float[this.mConditions];
            this.mReferenceValuesY = new float[this.mConditions];
            this.mReferenceValuesZ = new float[this.mConditions];
            for(int i = 0; i < this.mConditions; i++) {
                this.mReferenceValuesX[i] = 0;
                this.mReferenceValuesY[i] = 0;
                this.mReferenceValuesZ[i] = 0;
            }
        }
        else {
            this.mSampleTimes = 0;
            this.mBitsOnce = 0;
            this.mLevels = 0;
        }
        this.mIsCalibrated = false;
    }

    public void setCalibrationFlag(boolean flag) {
        if(flag == true) {
            this.mIsCalibrated = true;
        }
        else {
            this.mIsCalibrated = false;
            for(int i = 0; i < this.mConditions; i++) {
                this.mReferenceValuesX[i] = 0.0f;
                this.mReferenceValuesY[i] = 0.0f;
                this.mReferenceValuesZ[i] = 0.0f;
            }
        }
    }

    public boolean getCalibrationFlag() {
        return mIsCalibrated;
    }

    public boolean calibrate(float[] baseValue, float[] referenceValues, int data) {
        if(baseValue[0] != 0) {
            this.mBaseValue = baseValue.clone();
        }

        //Log.d("Calibration", "" + data + " " + referenceValues[0] + " " + referenceValues[1] + " " + referenceValues[2]);
        if(data >= 0 && data < this.mConditions) {
            Log.d("Calibration", "" + data + " " + referenceValues[0] + " " + referenceValues[1] + " " + referenceValues[2]);
            this.mReferenceValuesX[data] = referenceValues[0];
            this.mReferenceValuesY[data] = referenceValues[1];
            this.mReferenceValuesZ[data] = referenceValues[2];
        }

        this.mIsCalibrated = true;
        for(int i = 0; i < this.mConditions; i++) {
            if(this.mChannelFlags[0] && this.mReferenceValuesX[i] == 0) {
                this.mIsCalibrated = false;
            }

            if(this.mChannelFlags[1] && this.mReferenceValuesY[i] == 0) {
                this.mIsCalibrated = false;
            }

            if(this.mChannelFlags[2] && this.mReferenceValuesZ[i] == 0) {
                this.mIsCalibrated = false;
            }
        }

        return this.mIsCalibrated;
    }

    public byte[] decode(LinkedList<Long> timeStamps, LinkedList<Float> valueX, LinkedList<Float> valueY, LinkedList<Float> valueZ) {
        if(this.mSampleTimes == 0 || this.mBitsOnce == 0) {
            return null;
        }

        byte[] returnValue = new byte[this.mChannels * this.mBitsOnce * this.mSampleTimes / 8];
        Long[] timeStampArray = new Long[timeStamps.size()];
        Float[] valueArrayX = new Float[valueX.size()];
        Float[] valueArrayY = new Float[valueY.size()];
        Float[] valueArrayZ = new Float[valueZ.size()];
        timeStampArray = timeStamps.toArray(timeStampArray);
        valueArrayX = valueX.toArray(valueArrayX);
        valueArrayY = valueY.toArray(valueArrayY);
        valueArrayZ = valueZ.toArray(valueArrayZ);

        // sample data
        int sampleTotal = timeStampArray.length;
        long period = (timeStampArray[timeStampArray.length - 1] - timeStampArray[0]) / this.mSampleTimes;
        long startPoint = timeStampArray[0] + period / 2;
        long[] samplePoints = new long[this.mSampleTimes];
        for(int i = 0; i < this.mSampleTimes; i++) {
            //System.out.println(startPoint + period * i);
            samplePoints[i] = startPoint + period * i;
        }

        //System.out.println("Start sampling");
        float[] sampleX = new float[this.mSampleTimes];
        float[] sampleY = new float[this.mSampleTimes];
        float[] sampleZ = new float[this.mSampleTimes];
        long errorScale = period / 10;
        for(int i = 0, j = 0; i < sampleTotal && j < this.mSampleTimes; i++) {
            if(abs(timeStampArray[i] - samplePoints[j]) < errorScale){
                sampleX[j] = valueArrayX[i];
                sampleY[j] = valueArrayY[i];
                sampleZ[j] = valueArrayZ[i];
                Log.d("Sample", "" + sampleX[j] + " " + sampleY[j] + " " + sampleZ[j]);
                // move sample pointer out of this period
                for( ; i < sampleTotal ; i++) {
                    if(abs(timeStampArray[i] - samplePoints[j]) > errorScale) {
                        break;
                    }
                }
                j++;
            }
        }

        //System.out.println("Start decoding");
        //System.out.println(errorScale);
        //System.out.println(period);
        // start decoding
        int[] result_segments = new int[this.mSampleTimes];
        for(int i = 0; i < this.mSampleTimes; i++) {
            float minDifferences = 1000.0f;
            float curDifferences = 0.0f;
            int segment_bits = 0x00;
            for(int j = 0; j < this.mConditions; j++) {
                curDifferences = abs(this.mReferenceValuesX[j] - sampleX[i]) +
                        abs(this.mReferenceValuesY[j] - sampleY[i]) +
                        abs(this.mReferenceValuesZ[j] - sampleZ[i]);
                if(curDifferences < minDifferences) {
                    segment_bits = j;
                    minDifferences = curDifferences;
                }
            }
            Log.d("Diff", "" + minDifferences);
            result_segments[i] = segment_bits;
            //System.out.printf("%d", segment_bits);
        }
        //System.out.printf("\n");

        // convert bit sequence to characters
        byte one_byte;
        byte rest_bits;
        long bits_buffer = 0x00000000L;
        int resultMask = (int)(((0x00000001 << this.mBitsOnce * this.mChannels) - 1) & (int)0xFFFFFFFF);
        long bufferMask = 0x000000FFL;
        int iCount = 0;
        int index = 0;
        for(int i = 0; i < this.mSampleTimes; i++) {
            if(iCount >= 8) {
                //System.out.println(Long.toBinaryString(bits_buffer));
                one_byte = (byte)(bits_buffer & bufferMask);
                returnValue[index++] = one_byte;
                //System.out.println(one_byte);
                bits_buffer = bits_buffer >>> 8;
                iCount -= 8;
                i--; // no array pointer movement
            } // in case of buffer overflow, keep buffer usage lower than 8
            else {
                bits_buffer = bits_buffer << (this.mBitsOnce * this.mChannels);
                bits_buffer |= (result_segments[i] & resultMask);
                iCount += (this.mBitsOnce * this.mChannels);
            }
        }
        while(iCount >= 8) { // take the last bits segment
            one_byte = (byte)(bits_buffer & bufferMask);
            returnValue[index++] = one_byte;
            //System.out.println((char)(one_byte & 0xFF));
            bits_buffer = bits_buffer >>> 8;
            iCount -= 8;
        }

        Log.d("Decoding", "" + (char)returnValue[0]);
        return returnValue;
    }
}
