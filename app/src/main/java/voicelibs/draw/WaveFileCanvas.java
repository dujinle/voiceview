package voicelibs.draw;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Rect;
import android.os.AsyncTask;
import android.util.Log;
import android.view.SurfaceView;

import java.util.ArrayList;

import voicelibs.utils.WaveFileReader;
import voicelibs.view.WaveSurfaceView;

/**
 * 波形绘制
 *
 */
public class WaveFileCanvas {

	private WaveFileReader wavread;
    private int line_off ;//上下边距的距离
    public int rateY = 1; //  Y轴缩小的比例 默认为1
    private int marginRight=30;//波形图绘制距离右边的距离
    private float divider = 0.2f;//为了节约绘画时间，每0.2个像素画一个数据
    private Paint circlePaint;
	private Paint center;
	private Paint paintLine;
	private Paint mPaint;
	/**
     * 开始录音
     * @param sfv
     * @param audioName
     */
    public void Start(SurfaceView sfv,String audioName) {
		Log.i("main","start draw WaveFileCanvas succes");
		init();
        new DrawTask(audioName, sfv, mPaint).execute();
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

    class DrawTask extends AsyncTask<Object, Object, Object> {
        private SurfaceView sfv;// 画板  
        private Paint mPaint;// 画笔
		private int line_off;
		private String filename;
		private int height;
		private ArrayList<Double> data;

        public DrawTask(String audioName,SurfaceView sfv, Paint mPaint) {
			Log.i("main","start draw DrawTask succes");
			this.filename = audioName;
            this.sfv = sfv;
			this.line_off = ((WaveSurfaceView)sfv).getLine_off();
			this.height = sfv.getHeight()/2;
            this.mPaint = mPaint;
        }
    	
		@Override
		protected Object doInBackground(Object... params) {
			Log.i("main","start draw doInBackground succes");
			wavread = new WaveFileReader(filename);
			this.data = wavread.getData();
			SimpleDraw(this.data);// 把缓冲区数据画出来
			//publishProgress();
			return null;
		}

		@Override
		protected void onProgressUpdate(Object... values) {
			super.onProgressUpdate(values);
		}
		
		/** 
         * 绘制指定区域 
         *  
         * @param buf 
         *            缓冲区
         */  
        void SimpleDraw(ArrayList<Double> buf) {
            Log.i("main", "start SimpleDraw:" + buf.size() + "succes");
			int baseLine = sfv.getHeight() / 2;
			int height = sfv.getHeight() - line_off;
			int high = line_off / 2;
			int mcenter = (int) (height * 0.5f + line_off / 2);
			int low = sfv.getHeight() - line_off / 2 - 1;
			rateY = (65535 / 2 / (sfv.getHeight() - line_off));

			Canvas canvas = sfv.getHolder().lockCanvas(new Rect(0, 0, sfv.getWidth(), sfv.getHeight()));// 关键:获取画布
			if (canvas == null) {
				Log.i("main", "start SimpleDraw get Canvas null");
				return;
			}
			// canvas.drawColor(Color.rgb(241, 241, 241));// 清除背景
			canvas.drawARGB(255, 239, 239, 239);
			float width = sfv.getWidth() - marginRight;
			divider = width / buf.size();
			int end = (int) ((buf.size()) * divider);


			if (sfv.getWidth() - end <= marginRight) {//如果超过预留的右边距距离
				end = sfv.getWidth() - marginRight;//画的位置x坐标
			}
			canvas.drawCircle(end, line_off / 4, line_off / 4, circlePaint);// 上圆
			canvas.drawCircle(end, sfv.getHeight() - line_off / 4, line_off / 4, circlePaint);// 下圆
			canvas.drawLine(end, 0, end, sfv.getHeight(), circlePaint);//垂直的线

			canvas.drawLine(0, high, sfv.getWidth(), high, paintLine);//最上面的那根线
			canvas.drawLine(0, mcenter, sfv.getWidth(), mcenter, center);//中心线
			canvas.drawLine(0, low, sfv.getWidth(), low, paintLine);//最下面的那根线

			for (int i = 0; i < buf.size(); i++) {
				float y = (float) (buf.get(i) * 10000 / rateY) + baseLine;// 调节缩小比例，调节基准线
				float x = (i) * divider;
				if (sfv.getWidth() - (i - 1) * divider <= marginRight) {
					x = sfv.getWidth() - marginRight;
				}
				//画线的方式很多，你可以根据自己要求去画。这里只是为了简单
				canvas.drawLine(x, y, x, sfv.getHeight() - y, mPaint);//中间出波形
			}

			sfv.getHolder().unlockCanvasAndPost(canvas);// 解锁画布，提交画好的图像
			Log.i("main", "draw wav info success");
        }
    }
}   
