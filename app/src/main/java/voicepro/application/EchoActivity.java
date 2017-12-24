package voicepro.application;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.PixelFormat;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;

import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import java.nio.ShortBuffer;
import java.util.ArrayList;

import jni.VprocessJNI;
import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.OnNeverAskAgain;
import permissions.dispatcher.OnPermissionDenied;
import permissions.dispatcher.OnShowRationale;
import permissions.dispatcher.PermissionRequest;
import permissions.dispatcher.RuntimePermissions;

import voicelibs.draw.WaveCanvas;
import voicelibs.utils.SamplePlayer;
import voicelibs.utils.U;
import voicelibs.utils.WaveFileReader;
import voicelibs.view.WaveView;

@RuntimePermissions
public class EchoActivity extends AppCompatActivity {
    @BindView(R.id.header_t) TextView header_t;
    @BindView(R.id.back_list) TextView back;
    @BindView(R.id.title) TextView title;
    @BindView(R.id.waveview) WaveView waveView;
    @BindView(R.id.waveRview) WaveView waveRiew;
    @BindView(R.id.similarity) TextView similarity;
    @BindView(R.id.play) Button play;
    @BindView(R.id.record) Button record;
    @BindView(R.id.replay) Button replay;

    private static final int FREQUENCY = 8000;// 设置音频采样率，44100是目前的标准，但是某些设备仍然支持22050，16000，11025
    private static final int CHANNELCONGIFIGURATION = AudioFormat.CHANNEL_IN_MONO;// 设置单声道声道
    private static final int AUDIOENCODING = AudioFormat.ENCODING_PCM_16BIT;// 音频数据格式：每个样本16位
    public final static int AUDIO_SOURCE = MediaRecorder.AudioSource.MIC;// 音频获取源

    private WaveFileReader reader1,reader2;
    private SamplePlayer mediaPlayer = new SamplePlayer();
    private int recBufSize;// 录音最小buffer大小
    private AudioRecord audioRecord;
    private WaveCanvas waveCanvas;
    private ArrayList<Double> baseData;
    /*静态文件的绘制类*/

    private String mFileName,confName;
    private String context;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFormat(PixelFormat.TRANSLUCENT);

        setContentView(R.layout.active_echoer);

        ButterKnife.bind(this);
        //int wid = Integer.parseInt(this.getIntent().getStringExtra("wid"));
        context = this.getIntent().getStringExtra("title");
        mFileName = this.getIntent().getStringExtra("filename");
        confName =  U.CONF_DIRECTORY;

        reader1 = new WaveFileReader(confName);
        reader2 = new WaveFileReader(confName);
        title.setText(context);

        similarity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                compWave();
            }
        });
        if(waveView != null) {
            waveView.setLine_offset(42);
        }
        if(waveRiew != null){
            waveRiew.setLine_offset(42);
        }
        initPermission();
        loadWavForm(mFileName,waveView);
    }
    public void loadWavForm(final String fileName, final WaveView wavDraw){
        Thread mLoadSoundFileThread = new Thread() {
            public void run() {
                try {
                    //异步加载波形
                    wavDraw.setStatus(U.RECORDED);
                    reader1.setWavReader(fileName);
                    baseData = reader1.getData();
                    wavDraw.setData(baseData);

                    Message newMsg = new Message();
                    newMsg.what = U.READED;
                    newMsg.obj = wavDraw;
                    if(fileName == U.RECORD_DIRECTORY){
                        newMsg.arg1 = R.id.replay;
                    }else {
                        newMsg.arg1 = R.id.play;
                    }
                    SyncHandler.sendMessage(newMsg);
                } catch (final Exception e) {
                    e.printStackTrace();
                    return;
                }
            }
        };
        mLoadSoundFileThread.start();
    }
    Handler SyncHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what){
                case U.RECORDED:
                    waveRiew.setStatus(U.RECORDED);
                    loadWavForm(U.RECORD_DIRECTORY,waveRiew);
                    break;
                case U.READED:
                    WaveView wavDraw =  (WaveView)(msg.obj);
                    wavDraw.invalidate();
                    if(msg.arg1 == R.id.play){
                        play.setEnabled(true);
                    }else if(msg.arg1 == R.id.replay){
                        replay.setEnabled(true);
                    }
                    break;
            }
        };
    };
    public Button getReplay(){
        return replay;
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

            mediaPlayer.setOnCompletionListener(new SamplePlayer.OnCompletionListener(){
                public void onCompletion() {
                    updateTime.removeMessages(UPDATE_WAV);
                    replay.setEnabled(true);
                    record.setEnabled(true);
                    play.setEnabled(true);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private final int UPDATE_WAV = 100;

    public synchronized void  playerAudio(final int type){

        if(type == R.id.play) {
            waveView.setStatus(U.PLAYING);
            int size = reader1.setWavReader(mFileName);
            ShortBuffer data = reader1.getShortData(size);
            mediaPlayer.setDataSource(data,FREQUENCY,1,size);
            mediaPlayer.start();
            Message msg = new Message();
            msg.what = UPDATE_WAV;
            msg.arg1 = type;
            updateTime.sendMessage(msg);
        }else if(type == R.id.replay){
            waveRiew.setStatus(U.PLAYING);
            int size = reader1.setWavReader(U.RECORD_DIRECTORY);
            ShortBuffer data = reader1.getShortData(size);
            mediaPlayer.setDataSource(data,FREQUENCY,1,size);
            mediaPlayer.start();
            Message msg = new Message();
            msg.what = UPDATE_WAV;
            msg.arg1 = type;
            updateTime.sendMessage(msg);
        }
    }

    Handler updateTime = new Handler() {
        public void handleMessage(Message msg) {
            updateDisplay(msg.arg1);
            Message newMsg = new Message();
            newMsg.what = UPDATE_WAV;
            newMsg.arg1 = msg.arg1;
            updateTime.sendMessageDelayed(newMsg, 10);
        };
    };

    private int preOff;
    /**更新updateview 中的播放进度*/
    private void updateDisplay(int type) {
        int position = mediaPlayer.getCurrentPosition();
        if(preOff != position) {
            Log.i("main", "updateDisplay:" + position);
            if(type == R.id.play) {
                waveView.setCurPos(position);
                waveView.invalidate();
            }else if(type == R.id.replay){
                waveRiew.setCurPos(position);
                waveRiew.invalidate();
            }
        }
        preOff = position;
    }

    @OnClick({R.id.back_list,R.id.play,R.id.record,R.id.replay})
    void click(View view){
        switch (view.getId()) {
            case R.id.back_list:
                finish();
                break;
            case R.id.play:
                playerAudio(R.id.play);
                record.setEnabled(false);
                replay.setEnabled(false);
                break;
            case R.id.record:
                if (waveCanvas == null || !waveCanvas.isRecording) {
                    replay.setEnabled(false);
                    record.setText("停止录音");
                    initAudio();
                    startAudio();
                } else {
                    record.setText("录音");
                    waveCanvas.Stop();
                    waveCanvas = null;
                }
                break;
            case R.id.replay:
                try {
                    replay.setEnabled(false);
                    record.setEnabled(false);
                    playerAudio(R.id.replay);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
        }
    }

    public void compWave(){
        reader1.setWavReader(mFileName);
        reader2.setWavReader(U.RECORD_DIRECTORY);
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
        waveRiew.setStatus(U.PREPARE_RECORD);
        waveRiew.invalidate();
        waveCanvas = new WaveCanvas(this);
        waveCanvas.Start(audioRecord,reader1, recBufSize, U.RECORD_DIRECTORY,SyncHandler);

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
