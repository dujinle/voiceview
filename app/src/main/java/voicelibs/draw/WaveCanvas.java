package voicelibs.draw;

import java.util.ArrayList;

import android.graphics.Color;
import android.graphics.Paint;  
import android.graphics.Paint.Style;
import android.media.AudioRecord;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.Message;
import android.util.Log;
import android.view.SurfaceView;

import jni.VprocessJNI;
import voicelibs.utils.U;
import voicelibs.utils.WaveFileReader;
import voicepro.application.EchoActivity;

/**
 * 录音和写入文件使用了两个不同的线程，以免造成卡机现象
 * 录音波形绘制
 * @author cokus
 *
 */
public class WaveCanvas {
	
    private ArrayList<Short> inBuf = new ArrayList<Short>();//缓冲区数据
    public boolean isRecording = false;// 录音线程控制标记
    private boolean isWriting = false;// 录音线程控制标记

    private AudioRecord audioRecord;
	private WaveFileReader wfreader;
    int recBufSize;
	private String mFileName;
	private int readsize;
	private EchoActivity waveView;
    private Handler callBack;

	public WaveCanvas(EchoActivity main){
		this.waveView = main;
	}

	/**
     * 开始录音
     * @param audioRecord
     * @param recBufSize
     * @param audioName
     */
    public void Start(AudioRecord audioRecord, WaveFileReader wfreader, int recBufSize, String audioName, Handler callback) {
    	this.audioRecord = audioRecord;
        isRecording = true;
        isWriting = true;
		this.wfreader = wfreader;
        this.recBufSize = recBufSize;
		this.mFileName = audioName;
        this.callBack = callback;
        new Thread(new WriteRunnable()).start();//开线程写文件
        new RecordTask(audioRecord, recBufSize).execute();
    }

    /** 
     * 停止录音
     */  
    public void Stop() {
		Log.i("main","stop start");
        isRecording = false;
		audioRecord.stop();
        //inBuf.clear();// 清除
    }

	/**
	 * 清楚数据
	 */
    public void clear(){
    	inBuf.clear();// 清除  
    }
    

    
    /**
     * 异步录音程序
     * @author cokus
     *
     */
    class RecordTask extends AsyncTask<Object, Object, Object> {
    	private int recBufSize;  
        private AudioRecord audioRecord;

        public RecordTask(AudioRecord audioRecord, int recBufSize) {
            this.audioRecord = audioRecord;
            this.recBufSize = recBufSize;
            inBuf.clear();// 清除  
        }
    	
		@Override
		protected Object doInBackground(Object... params) {
			try {
				int total_size = 0;
                short[] buffer = new short[recBufSize];
                audioRecord.startRecording();// 开始录制
                while (isRecording) {
                    // 从MIC保存数据到缓冲区  
                    readsize = audioRecord.read(buffer, 0, recBufSize);
					Log.i("main","record wav data size:" + readsize);
                    synchronized (inBuf) {
	                    for (int i = 0; i < readsize; i ++) {
	                    	inBuf.add(buffer[i]);
	                    }
						total_size = total_size + readsize;
                    }
                }
				Log.i("main","read wav total data size:" + total_size);
    			isWriting = false;
            } catch (Throwable t) {
                t.printStackTrace();
            }
			return null;
		}

		@Override
		protected void onProgressUpdate(Object... values) {
			super.onProgressUpdate(values);
		}

    }

    /**
     * 异步写文件
     * @author cokus
     *
     */
    class WriteRunnable implements Runnable {
		@Override
		public void run() {
			int total_size = 0;
			long inst = wfreader.getInst();
			VprocessJNI.set_wave_writer(inst,mFileName);
			if(inst == -1){
				return;
			}
			int step = 0;
			while (isWriting || inBuf.size() > 0) {
				synchronized (inBuf) {
					if(inBuf.size() > 0) {
						Log.i("main", "start write wav data size:" + inBuf.size());
						short a[] = new short[inBuf.size()];
						for (int i = 0; i < inBuf.size(); i++) {
							a[i] = inBuf.get(i);
							total_size = total_size + 1;
						}
						VprocessJNI.write_wav(inst, a, inBuf.size(),isWriting == true?0:1);
						inBuf.clear();
					}
				}
			}

            Log.i("main","record wav data total size:" + total_size);
            VprocessJNI.close_file(inst);
            Message newMsg = new Message();
            newMsg.what = U.RECORDED;
            callBack.sendMessage(newMsg);
		}
    }
}
