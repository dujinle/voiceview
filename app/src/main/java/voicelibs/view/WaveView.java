/*
 * Copyright (C) 2008 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package voicelibs.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import java.util.ArrayList;

import voicelibs.utils.U;
import voicepro.application.R;


/**
 * WaveformView 这个根据你的音频进行处理成完整的波形
 * 如果文件很大可能会很慢哦
 */
public class WaveView extends View {
	private int line_offset;
	private int marginRight=30;//波形图绘制距离右边的距离
	private Paint paintLine;
	private Paint circlePaint;
	private Paint center;
	private Paint mPaint;
	private Bitmap pBitMap;
    private int status;
    private int curPos;
	private int end;
	private ArrayList<Double> data;

	private float rateY;
	private float divider;
	// Colors
	public WaveView(Context context, AttributeSet attrs) {
		super(context, attrs);

		// We don't want keys, the markers get these
		setFocusable(false);
		circlePaint = new Paint();//画圆
		circlePaint.setColor(Color.rgb(246, 131, 126));//设置上圆的颜色
		center = new Paint();
		center.setColor(Color.rgb(39, 199, 175));// 画笔为color
		center.setStrokeWidth(1);// 设置画笔粗细
		center.setAntiAlias(true);
		center.setFilterBitmap(true);
		center.setStyle(Paint.Style.FILL);
		paintLine =new Paint();
		paintLine.setColor(Color.rgb(169, 169, 169));
		mPaint = new Paint();
		mPaint.setColor(Color.rgb(39, 199, 175));// 画笔为color
		mPaint.setStrokeWidth(1);// 设置画笔粗细
		mPaint.setAntiAlias(true);
		mPaint.setFilterBitmap(true);
		mPaint.setStyle(Paint.Style.FILL);

	}

	public void setCurPos(int curPos){
        this.curPos = curPos;
    }

    public void setStatus(int status){
		this.status = status;
	}
	public void setData(ArrayList<Double> data){
		this.data = data;
	}
	public void setLine_offset(int line_offset){
		this.line_offset = line_offset;
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);

		int measuredWidth = getMeasuredWidth();
		int measuredHeight = getMeasuredHeight();
		float width = measuredWidth - marginRight;

		int height = measuredHeight - line_offset;
		int high = line_offset / 2;
		int mcenter = (int) (height * 0.5f + line_offset / 2);
		int low = measuredHeight - line_offset / 2 - 1;

		int baseLine = measuredHeight / 2;
		rateY = (65535 / 2 / (measuredHeight - line_offset));
		Log.i("main","go into ondraw status:" + status);
        /*准备开始录音则清空画布的内容*/
		if(status == U.PREPARE_RECORD){
			pBitMap = null;
			data = null;
		}
		if(pBitMap == null) {
			pBitMap = Bitmap.createBitmap(measuredWidth, measuredHeight, Bitmap.Config.ARGB_4444);
			Canvas formCanvas = new Canvas(pBitMap);
			formCanvas.drawLine(0, high, measuredWidth, high, paintLine);//最上面的那根线
			formCanvas.drawLine(0, mcenter, measuredWidth, mcenter, center);//中心线
			formCanvas.drawLine(0, low, measuredWidth, low, paintLine);//最下面的那根线
			canvas.drawBitmap(pBitMap, 0, 0, paintLine);
		}
		if(status == U.RECORDED && data != null){
			Canvas formCanvas = new Canvas(pBitMap);
			divider = width / data.size();
			end = (int)(divider * data.size());
			for (int i = 0; i < data.size(); i++) {
				float y = (float) (data.get(i) * 10000 / rateY) + baseLine;// 调节缩小比例，调节基准线
				float x = (i) * divider;
				if (measuredWidth - (i - 1) * divider <= marginRight) {
					x = measuredWidth - marginRight;
				}
				//画线的方式很多，你可以根据自己要求去画。这里只是为了简单
				formCanvas.drawLine(x, y, x, measuredHeight - y, mPaint);//中间出波形
			}
			canvas.drawBitmap(pBitMap,0,0,paintLine);
		}
		if(status == U.PLAYING){
			canvas.drawBitmap(pBitMap,0,0,paintLine);
			float px = curPos * divider;
			if(px < end) {
				canvas.drawCircle(px, high, line_offset / 4, circlePaint);// 上圆
				canvas.drawCircle(px, low, line_offset / 4, circlePaint);// 下圆
				canvas.drawLine(px, 0, px, height, circlePaint);//垂直的线
			}else {
				canvas.drawCircle(end, high, line_offset / 4, circlePaint);// 上圆
				canvas.drawCircle(end, low, line_offset / 4, circlePaint);// 下圆
				canvas.drawLine(end, 0, end, height, circlePaint);//垂直的线
			}
			Log.i("main","curpos:" + curPos + " px:" + px + " data:" + data.size());
		}
	}
}
