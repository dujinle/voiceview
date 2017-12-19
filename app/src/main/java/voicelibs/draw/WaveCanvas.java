package voicelibs.draw;

import java.util.ArrayList;

import android.graphics.Color;
import android.graphics.Paint;  
import android.graphics.Paint.Style;
import android.media.AudioRecord;
import android.os.AsyncTask;
import android.os.Handler.Callback;
import android.os.Message;
import android.util.Log;
import android.view.SurfaceView;

import jni.VprocessJNI;
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

    public int baseLine = 0;// Y轴基线
    private AudioRecord audioRecord;
    int recBufSize;
    private Paint circlePaint;
	private Paint center;
	private Paint paintLine;
	private Paint mPaint;
	private String mFileName;
	private int readsize;
	private EchoActivity waveView;

	public WaveCanvas(EchoActivity main){
		this.waveView = main;
	}

	/**
     * 开始录音
     * @param audioRecord
     * @param recBufSize
     * @param audioName
     */
    public void Start(AudioRecord audioRecord, int recBufSize,String audioName,Callback callback) {
    	this.audioRecord = audioRecord;
        isRecording = true;
        isWriting = true;
        this.recBufSize = recBufSize;
		this.mFileName = audioName;
		init();
        new Thread(new WriteRunnable()).start();//开线程写文件
        new RecordTask(audioRecord, recBufSize,callback).execute();
    }

	public  void init(){
		circlePaint = new Paint();//画圆
		circlePaint.setColor(Color.rgb(246, 131, 126));//设置上圆的颜色
		center = new Paint();
		center.setColor(Color.rgb(39, 199, 175));// 画笔为color
		center.setStrokeWidth(1);// 设置画笔粗细
		center.setAntiAlias(true);
		center.setFilterBitmap(true);
		center.setStyle(Style.FILL);
		paintLine =new Paint();
		paintLine.setColor(Color.rgb(169, 169, 169));
		mPaint = new Paint();
		mPaint.setColor(Color.rgb(39, 199, 175));// 画笔为color
		mPaint.setStrokeWidth(1);// 设置画笔粗细
		mPaint.setAntiAlias(true);
		mPaint.setFilterBitmap(true);
		mPaint.setStyle(Style.FILL);
	}

    
    /** 
     * 停止录音
     */  
    public void Stop() {
		Log.e("test","stop start");
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
        private SurfaceView sfv;// 画板  
        private Paint mPaint;// 画笔  
        private Callback callback;
        private boolean isStart =false;
        

        public RecordTask(AudioRecord audioRecord, int recBufSize,Callback callback) {
            this.audioRecord = audioRecord;
            this.recBufSize = recBufSize;
            this.callback = callback;
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
					Log.i("main","read wav data size:" + readsize);
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
            	Message msg = new Message();
            	msg.arg1 =-2;
            	msg.obj=t.getMessage();
            	callback.handleMessage(msg); 
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
			long inst = VprocessJNI.get_write_handler(mFileName,8000,16,1);
			if(inst == -1){
				return;
			}
			int step = 0;
			while (isWriting || inBuf.size() > 0) {
				synchronized (inBuf) {
					if(inBuf.size() > 0) {
						Log.i("main", "write wav data size:" + inBuf.size());
						short a[] = new short[inBuf.size()];
						for (int i = 0; i < inBuf.size(); i++) {
							a[i] = inBuf.get(i);
							total_size = total_size + 1;
						}
						VprocessJNI.write_wav(inst, a, inBuf.size());
						inBuf.clear();
					}
				}
			}
			Log.i("main","write wav data total size:" + total_size);
			VprocessJNI.close_file(inst);
			waveView.initWaveView();
		}
    }
}
