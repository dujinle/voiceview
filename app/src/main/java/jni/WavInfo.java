package jni;

import android.util.Log;

/**
 * Created by yingy on 2017/12/12.
 */

public class WavInfo {

    private short size;         //音频数据的大小
    private short fs;           //采样率
    private short fsize;        //分帧大小256
    private short fmove;        //帧移大小80
    private short bank_num;     //一帧数据子带的个数 默认32
    private short fbank_num;    //滤波器的个数 24
    private short mfcc_size;    //mfcc 参数个数 12
    private short frame_num;    //帧个数
    private short start;        //开始处理的有效帧
    private short end;          //结束的有效帧
    private long mfccs;         //返回的结果指针
    private long mass;          //质心参数存储指针
    private long rms;           //均方根参数存储指针
    private long wdata;         //wav数据的首地址指针
    private byte[] inst;
    private int idx;
    public WavInfo(byte[] inst){
        this.inst = inst;
        this.idx = 0;
        feat(inst);
    }
    public byte[] getInst(){
        return this.inst;
    }
    public int getStatus(){
        return 0;
    }
    private void feat(byte[] inst){
        this.idx = 0;
        this.size = byteArrayToShort(inst);
        this.fs = byteArrayToShort(inst);
        this.fsize = byteArrayToShort(inst);
        this.fmove = byteArrayToShort(inst);
        this.bank_num = byteArrayToShort(inst);
        this.mfcc_size = byteArrayToShort(inst);
        this.frame_num = byteArrayToShort(inst);
        this.start = byteArrayToShort(inst);
        this.end = byteArrayToShort(inst);
    }

    public void refeat(byte[] inst){
        this.idx = 0;
        this.inst = inst;
        feat(inst);
    }

    public static int byteArrayToInt(byte[] b) {
        return   b[3] & 0xFF |
                (b[2] & 0xFF) << 8 |
                (b[1] & 0xFF) << 16 |
                (b[0] & 0xFF) << 24;
    }
    private short byteArrayToShort(byte[] b) {
        int step = this.idx;
        int rec = (b[step] & 0xFF)|(b[step + 1] & 0xFF) << 8;
        this.idx = step + 2;
        return (short)rec;
    }

    public String toStruct(){
        String res = String.valueOf(this.idx) + ": size:" + this.size + "\n";
        res = res + "fs:" + this.fs + "\n";
        res = res + "fsize:" + this.fsize + "\n";
        return res;
    }
}
