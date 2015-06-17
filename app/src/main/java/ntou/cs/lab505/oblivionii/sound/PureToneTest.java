package ntou.cs.lab505.oblivionii.sound;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Environment;
import android.os.IBinder;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.concurrent.LinkedBlockingQueue;

import ntou.cs.lab505.oblivionii.datastructure.SoundVectorUnit;
import ntou.cs.lab505.oblivionii.sound.filterbank.FilterBank;
import ntou.cs.lab505.oblivionii.sound.frequencyshift.FrequencyShift;
import ntou.cs.lab505.oblivionii.sound.gain.Gain;
import ntou.cs.lab505.oblivionii.sound.soundgeneration.HarmonicsGeneration;

/**
 * Created by alan on 6/10/15.
 */
public class PureToneTest extends Service {

    // sound parameters
    int valueFreq;
    int valueDb;
    int valueHarm;
    int valueSec;
    int valueBcLow;
    int valueBcHigh;
    int valueSemitone;
    int valueGain;
    int valueChannel = 0;
    int valueOutput = 0;
    int sampleRate = 16000;
    // sound vector
    short[] originSoundVector;
    // function objects
    HarmonicsGeneration harmonicsGeneration;
    FrequencyShift frequencyShift;
    FilterBank filterBank;
    Gain gain;
    // data queues.
    LinkedBlockingQueue<SoundVectorUnit> pureToneQueue = new LinkedBlockingQueue<>();
    LinkedBlockingQueue<SoundVectorUnit> freqShiftQueue = new LinkedBlockingQueue<>();
    LinkedBlockingQueue<SoundVectorUnit[]> filterBankQueue = new LinkedBlockingQueue<>();
    LinkedBlockingQueue<SoundVectorUnit> gainQueue = new LinkedBlockingQueue<>();


    public class PureToneTestBinder extends Binder {
        public PureToneTest getService() {
            return PureToneTest.this;
        }
    }

    private final IBinder mBinder = new PureToneTestBinder();

    @Override
    public void onCreate() {
        //Log.d("PureToneTest", "in onCreate.");
        super.onCreate();
    }

    @Override
    public void onDestroy() {
        //Log.d("PureToneTest", "in onDestroy.");
        super.onDestroy();
        if (frequencyShift.threadState()) {
            Log.d("PureToneTest", "in onDestroy. stop frequencyShift thread.");
            frequencyShift.threadStop();
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //Log.d("PureToneTest", "in onStartCommand.");
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public IBinder onBind(Intent intent) {
        //Log.d("PureToneTest", "in onBind.");
        return mBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        //Log.d("PureToneTest", "in onUnbind.");
        return super.onUnbind(intent);
    }

    public void initParameters(int valueFreq, int valueDb, int valueHarm, int valueSec, int valueBcLow, int valueBcHigh, int valueSemitone, int valueGain, int valueChannel, int valueOutput) {
        this.valueFreq = valueFreq;
        this.valueDb = valueDb;
        this.valueHarm = valueHarm;
        this.valueSec = valueSec;
        this.valueBcLow = valueBcLow;
        this.valueBcHigh = valueBcHigh;
        this.valueSemitone = valueSemitone;
        this.valueGain = valueGain;
        this.valueChannel = valueChannel;
        this.valueOutput = valueOutput;
    }

    public void runTest() {

        // check device type.  and check sample rate

        // initial object
        harmonicsGeneration = new HarmonicsGeneration(sampleRate);
        frequencyShift = new FrequencyShift(sampleRate, 1, valueSemitone, 0, 0);
        filterBank = new FilterBank(sampleRate, valueBcLow, valueBcHigh);
        gain = new Gain(sampleRate, valueGain, valueGain, valueGain);


        /**
         * algorithm:
         *  (1) generate pure tone.
         *  (2) shift frequency.
         *  (3) filter bank.
         *  (4) gain bands.
         *  (5) output sound.
         */


        // generate sound.
        originSoundVector = harmonicsGeneration.generate(valueFreq, valueSec, valueDb, valueHarm);
        Log.d("PureToneTest", "in runTest. originSoundVector length: " + originSoundVector.length);
        saveVectorToDataFile(originSoundVector, "origin");
        SoundVectorUnit soundVectorUnit = new SoundVectorUnit(originSoundVector);
        Log.d("PureToneTest", "in runTest. soundVectorUnit length: " + soundVectorUnit.getVectorLength());

        // pipe sound.
        pureToneQueue.add(soundVectorUnit);
        frequencyShift.setInputDataQueue(pureToneQueue);
        frequencyShift.setOutputDataQueue(freqShiftQueue);
        filterBank.setInputDataQueue(freqShiftQueue);
        filterBank.setOutputDataQueue(filterBankQueue);
        gain.setInputDataQueue(filterBankQueue);
        gain.setOutputDataQueue(gainQueue);


        // threads start.
        frequencyShift.threadStart();
        filterBank.threadStart();
        gain.threadStart();


        try {
            Thread.sleep(valueSec * 1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }


        frequencyShift.threadStop();
        filterBank.threadStop();
        gain.threadStop();
    }

    /**
     * return time domain sound vector to activity.
     * @return
     */
    public short[] getOriginTimeDomainVector() {
        short[] temp = null;
        return temp;

        /**
         * wait for coding.
         */

    }

    private void saveVectorToDataFile(short[] data, String fileName) {
        File file = new File(Environment.getExternalStorageDirectory().toString() + "/Download/" + fileName + ".txt");
        FileOutputStream fOut;
        OutputStreamWriter fWriter;

        if (data == null) {
            return ;
        }

        try {
            file.createNewFile();
            fOut = new FileOutputStream(file);
            fWriter = new OutputStreamWriter(fOut);
            for (int i = 0; i < data.length; i++) {
                fWriter.append(data[i] + ",");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
