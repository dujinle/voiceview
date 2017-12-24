package voicelibs.utils;


import android.util.Log;

import java.nio.ShortBuffer;
import java.util.ArrayList;

import jni.VprocessJNI;

/**
 * Created by yingy on 2017/12/15.
 */

public class WaveFileReader {
    private String filename = null;

    private long inst;
    public WaveFileReader(String conf){
        this.inst = VprocessJNI.init_real(conf);
    }
    public int setWavReader(String filename){
       return VprocessJNI.set_wave_reader(inst,filename);
    }
    public int setWavWriter(String filename){
        return VprocessJNI.set_wave_writer(inst,filename);
    }

    public ArrayList<Double> getData(){
        ArrayList<Double> dataArray = new ArrayList<Double>();
        while(true){
            double [] data = VprocessJNI.read_wav(inst,2048);
            int size = (int)data[0];
            for(int i = 1;i <= size;i++){
                dataArray.add(data[i]);
            }
            if(size <2048){
                break;
            }
        }
        VprocessJNI.close_file(inst);
        Log.i("main","start WaveFileReader getData size:" + dataArray.size() + "succes");
        return dataArray;
    }

    public ShortBuffer getShortData(int size){
        short rdata[] = new short[size];
        int id = 0;
        while(id < size){
            short [] data = VprocessJNI.read_short_wav(inst,2048);
            int rsize = (int)data[0];
            for(int i = 1;i <= rsize;i++){
                rdata[id++] = data[i];
            }
        }
        VprocessJNI.close_file(inst);
        Log.i("main","start WaveFileReader getShortData size:" + rdata.length + "succes");
        return ShortBuffer.wrap(rdata);
    }
    public long getInst(){
        return this.inst;
    }

    public int writeData(short data[],int size,int flg){
       return VprocessJNI.write_wav(inst,data,size,flg);
    }
}
