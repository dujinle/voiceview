package voicelibs.utils;


import android.util.Log;

import java.util.ArrayList;

import jni.VprocessJNI;

/**
 * Created by yingy on 2017/12/15.
 */

public class WaveFileReader {
    private String filename = null;
    private ArrayList<Double> dataArray = new ArrayList<Double>();
    private long inst;
    public WaveFileReader(String filename){
        this.filename = filename;
        inst = VprocessJNI.get_handler(filename);
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
        Log.i("main","start WaveFileReader data size:" + dataArray.size() + "succes");
    }
    public long getInst(){
        return this.inst;
    }
    public ArrayList<Double> getData(){
        return this.dataArray;
    }
}
