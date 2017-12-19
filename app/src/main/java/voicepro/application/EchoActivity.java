package voicepro.application;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.PixelFormat;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.view.SurfaceView;
import android.view.View;

import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import java.io.IOException;
import java.util.ArrayList;

import jni.VprocessJNI;
import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.OnNeverAskAgain;
import permissions.dispatcher.OnPermissionDenied;
import permissions.dispatcher.OnShowRationale;
import permissions.dispatcher.PermissionRequest;
import permissions.dispatcher.RuntimePermissions;

import voicelibs.draw.WaveCanvas;
import voicelibs.draw.WaveFileCanvas;
import voicelibs.utils.U;
import voicelibs.utils.WaveFileReader;
import voicelibs.view.WaveSurfaceView;

@RuntimePermissions
public class EchoActivity extends AppCompatActivity {
    @BindView(R.id.header_t) TextView header_t;
    @BindView(R.id.back_list) TextView back;
    @BindView(R.id.title) TextView title;
    @BindView(R.id.wavesfv) WaveSurfaceView waveSfv;
    @BindView(R.id.waversfv) WaveSurfaceView waveView;
 //   @BindView(R.id.waveview) WaveformView waveView;
    @BindView(R.id.similarity) TextView similarity;
    @BindView(R.id.play) Button play;
    @BindView(R.id.record) Button record;
    @BindView(R.id.replay) Button replay;

    private static final int FREQUENCY = 8000;// 设置音频采样率，44100是目前的标准，但是某些设备仍然支持22050，16000，11025
    private static final int CHANNELCONGIFIGURATION = AudioFormat.CHANNEL_IN_MONO;// 设置单声道声道
    private static final int AUDIOENCODING = AudioFormat.ENCODING_PCM_16BIT;// 音频数据格式：每个样本16位
    public final static int AUDIO_SOURCE = MediaRecorder.AudioSource.MIC;// 音频获取源

    private MediaPlayer mediaPlayer = new MediaPlayer();
    private int recBufSize;// 录音最小buffer大小
    private AudioRecord audioRecord;
    private WaveCanvas waveCanvas;
    /*静态文件的绘制类*/
    private WaveFileCanvas wavfcavas;
    private String mFileName;
    private int wid;
    private ArrayList<Double> ori_data;
    private String context;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFormat(PixelFormat.TRANSLUCENT);

        setContentView(R.layout.active_echoer);

        ButterKnife.bind(this);
        wid = Integer.parseInt(this.getIntent().getStringExtra("wid"));
        context = this.getIntent().getStringExtra("title");
        mFileName = this.getIntent().getStringExtra("filename");
        title.setText(context);
        wavfcavas = new WaveFileCanvas();

        similarity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                compWave();
            }
        });
        if(waveSfv != null) {
            waveSfv.setMainControler(this);
            waveSfv.setFileName(mFileName);
            waveSfv.setLine_off(42);
            //解决surfaceView黑色闪动效果
            waveSfv.setZOrderOnTop(true);
            waveSfv.getHolder().setFormat(PixelFormat.TRANSLUCENT);
        }
        if(waveView != null){
            waveView.setMainControler(this);
            waveView.setLine_off(42);
            //解决surfaceView黑色闪动效果
            waveView.setZOrderOnTop(true);
            waveView.getHolder().setFormat(PixelFormat.TRANSLUCENT);
        }
        initPermission();
    }
    public Button getReplay(){
        return replay;
    }
    public void startDraw(SurfaceView waveSfv, String mFileName){
        wavfcavas.Start(waveSfv, mFileName);
    }
    //初始化录音  申请录音权限
    @NeedsPermission({Manifest.permission.RECORD_AUDIO,Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.READ_EXTERNAL_STORAGE})
    public void initAudio(){
        recBufSize = AudioRecord.getMinBufferSize(FREQUENCY,
                CHANNELCONGIFIGURATION, AUDIOENCODING);// 录音组件
        audioRecord = new AudioRecord(AUDIO_SOURCE,// 指定音频来源，这里为麦克风
                FREQUENCY, // 16000HZ采样频率
                CHANNELCONGIFIGURATION,// 录制通道
                AUDIO_SOURCE,// 录制编码格式
                recBufSize);// 录制缓冲区大小 //先修改
        U.createDirectory();
    }

    //初始化权限
    public void initPermission(){
        try {
            EchoActivityPermissionsDispatcher.initAudioWithCheck(this);
            mediaPlayer.setDataSource(mFileName);
            mediaPlayer.prepare();
            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener(){
                @Override
                public void onCompletion(MediaPlayer mp) {
                    mediaPlayer.reset();
                    record.setEnabled(true);
                    play.setEnabled(true);
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    @OnClick({R.id.back_list,R.id.play,R.id.record,R.id.replay})
    void click(View view){
        switch (view.getId()) {
            case R.id.back_list:
                finish();
                break;
            case R.id.play:
                try {
                    mediaPlayer.reset();
                    mediaPlayer.setDataSource(mFileName);
                    mediaPlayer.prepare();
                    mediaPlayer.start();
                    play.setEnabled(false);
                    record.setEnabled(false);
                    replay.setEnabled(false);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
            case R.id.record:
                if (waveCanvas == null || !waveCanvas.isRecording) {
                    replay.setEnabled(false);
                    record.setText("停止录音");
                    initAudio();
                    startAudio();
                } else {
                    replay.setEnabled(true);
                    record.setText("录音");
                    waveCanvas.Stop();
                    waveCanvas = null;
                }
                break;
            case R.id.replay:
                try {
                    record.setEnabled(false);
                    mediaPlayer.reset();
                    mediaPlayer.setDataSource(U.RECORD_DIRECTORY);
                    mediaPlayer.prepare();
                    mediaPlayer.start();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
        }
    }

    public void compWave(){
        WaveFileReader reader1 = new WaveFileReader(mFileName);
        WaveFileReader reader2 = new WaveFileReader(U.RECORD_DIRECTORY);
        ArrayList<Double> d1 = reader1.getData();
        ArrayList<Double> d2 = reader2.getData();
        double[] dd1 = new double[d1.size()];
        double[] dd2 = new double[d2.size()];
        for(int i = 0;i < d1.size();i++){
            dd1[i] = d1.get(i);
        }
        for(int i = 0;i < d2.size();i++){
            dd2[i] = d2.get(i);
        }
        long intst1 = reader1.getInst();
        long intst2 = reader2.getInst();
        VprocessJNI.pfeat_real(intst1,dd1);
        VprocessJNI.pfeat_real(intst2,dd2);
        double[] ret = VprocessJNI.compare_real(intst1,intst2);
        similarity.setText("置信度:mfcc:" + ret[0] + "\tmass:" + ret[1] + "\trms:" + ret[2]);
    }
    //开始录音
    private void startAudio(){
        waveCanvas = new WaveCanvas(this);
        waveCanvas.baseLine = waveSfv.getHeight() / 2;
        waveCanvas.Start(audioRecord, recBufSize, U.RECORD_DIRECTORY, new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                return true;
            }
        });
    }
    public void  initWaveView(){
        wavfcavas.Start(waveView, U.RECORD_DIRECTORY);
    }

    @OnShowRationale({Manifest.permission.RECORD_AUDIO,Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.READ_EXTERNAL_STORAGE})
    void showRationaleForRecord(final PermissionRequest request){
        new AlertDialog.Builder(this)
                .setPositiveButton("好的", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        request.proceed();
                    }
                })
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        request.cancel();
                    }
                })
                .setCancelable(false)
                .setMessage("是否开启录音权限")
                .show();
    }

    @OnPermissionDenied({Manifest.permission.RECORD_AUDIO,Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.READ_EXTERNAL_STORAGE})
    void showRecordDenied(){
        Toast.makeText(EchoActivity.this,"拒绝录音权限将无法进行挑战",Toast.LENGTH_LONG).show();
    }

    @OnNeverAskAgain({Manifest.permission.RECORD_AUDIO,Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.READ_EXTERNAL_STORAGE})
    void onRecordNeverAskAgain() {
        new AlertDialog.Builder(this)
            .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    // TODO: 2016/11/10 打开系统设置权限
                    dialog.cancel();
                }
            })
            .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            })
            .setCancelable(false)
            .setMessage("您已经禁止了录音权限,是否现在去开启")
            .show();
    }
}
