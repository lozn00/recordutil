package cn.qssq666.voiceutiltest;

import android.content.ClipboardManager;
import android.content.Context;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.util.Locale;

import cn.qssq666.audio.AudioManager;
import cn.qssq666.voiceutil.record.MediaType;
import cn.qssq666.voiceutil.record.RecordFactory;
import cn.qssq666.voiceutil.record.RecordManagerI;
import cn.qssq666.voiceutil.utils.MediaDirectoryUtils;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private RecordManagerI recordManager;
    private File mAudioFile;
    private String TAG = "RecordTest";
    private int mDuration;
    private TextView tvTitle;
    private UploadVoice ivVoice;
    private TextView tvPath;
    private boolean recordState;
    private TextView tvStart;
    MediaType mediaType = MediaType.MP3;
    private TextView tvPlay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tvTitle = ((TextView) findViewById(android.R.id.text1));
        tvPlay = ((TextView) findViewById(R.id.btn_play));
        tvStart = ((TextView) findViewById(R.id.btn_start));
        tvPlay.setOnClickListener(this);
        tvStart.setOnClickListener(this);
        findViewById(R.id.btn_start).setOnClickListener(this);
        tvPath = ((TextView) findViewById(R.id.tv_path));
        tvPath.setOnClickListener(this);
        ivVoice = ((UploadVoice) findViewById(R.id.iv_voice));
        findViewById(R.id.tv_aac).setOnClickListener(this);
        findViewById(R.id.tv_amr).setOnClickListener(this);
        findViewById(R.id.tv_wav_mp3).setOnClickListener(this);
        findViewById(R.id.tv_mp3).setOnClickListener(this);
        findViewById(R.id.tv_wav).setOnClickListener(this);
        boolean mkdirs = MediaDirectoryUtils.getCachePath().mkdirs();
        if (!mkdirs) {
            Toast.makeText(this, "没有创建文件的权限", Toast.LENGTH_SHORT).show();
        }
    }

    public RecordManagerI getRecordManager() {
        if (recordManager == null) {
//                    recordManager = AudioManager.isErrorLoadSo() ? RecordFactory.getAAcRocrdInstance() : RecordFactory.getMp3RecordInstance();

            switch (mediaType) {
                case AAC:
                    recordManager = RecordFactory.getAAcRocrdInstance();
                    break;
                case WAV:
                    recordManager = RecordFactory.getWavRecordInstance();
                    break;
                case MP3:
                    recordManager = RecordFactory.getMp3RecordInstance();
                    break;
                case WAV_TO_MP3:
                    recordManager = RecordFactory.getWavRecordMp3OutInstance();
                    break;
                case AMR:
                    recordManager = RecordFactory.getAmrRocrdInstance();
                    break;
            }
            recordManager = AudioManager.isErrorLoadSo() ? RecordFactory.getAAcRocrdInstance() : RecordFactory.getMp3RecordInstance();
            recordManager.setOnTimeSecondChanage(new RecordManagerI.OnTimeSecondChanage() {
                @Override
                public void onSecondChnage(int duration) {
                    int time = duration * 1000;
                    String s = generateTime(time);
                    tvTitle.setText("" + s + ",time:" + time);
                    Log.w(TAG, "" + s);
                    mDuration = duration;
                }
            });
            recordManager.setOnTimeOutStopListener(new RecordManagerI.OnTimeOutStopListener() {
                @Override
                public void onStop() {
                    mAudioFile = recordManager.getFile();
                    tvPath.setText("audioPth:" + (mAudioFile == null ? null : mAudioFile.getAbsolutePath()));
                    File tempCacheMp3FileName = MediaDirectoryUtils.getTempMp3FileName();
                    try {
                        tempCacheMp3FileName.createNewFile();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    setRecordState(false);
                }
            });
        }
        return recordManager;
    }


    /**
     * 精确到毫秒  不是时间戳  new Date().getTime()-new Date().getTime()的时间 比如。
     *
     * @param position
     * @return
     */
    public static String generateTime(long position) {
        if (position <= 0) {
            return "00:00";
        }
        int totalSeconds = (int) (position / 1000);

        int seconds = totalSeconds % 60;
        int minutes = (totalSeconds / 60) % 60;
        int hours = totalSeconds / 3600;

        if (hours > 0) {
            return String.format(Locale.US, "%02d:%02d:%02d", hours, minutes,
                    seconds).toString();
        } else {
            return String.format(Locale.US, "%02d:%02d", minutes, seconds)
                    .toString();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (recordManager != null) {
            if (recordManager.isRecordIng()) {
                recordManager.setOnTimeOutStopListener(null);
                recordManager.stopRecord();
            }
        }
        PlayEngine.destory();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_start:
                if (getRecordManager().isRecordIng()) {//在录制中
                    if (getRecordManager().getCurrenttime() < 5) {//小于5秒不鸟
                    } else {
                        getRecordManager().stopRecord();//否则停止
                    }
                } else {//不再录制中 开始新的录制
                    try {
                        tvTitle.setText("00:00");
                        getRecordManager().startRecordCreateFile(60);
                        setRecordState(true);
                    } catch (IOException e) {
                        getRecordManager().stopRecord();
                        e.printStackTrace();
                        Toast.makeText(this, "无法录制 " + e.toString(), Toast.LENGTH_SHORT).show();
                        setRecordState(false);
                    }
                }
                break;
            case R.id.btn_play:
                if (mAudioFile == null || !mAudioFile.exists()) {
                    Toast.makeText(this, "文件出现错误,请重新录制!", Toast.LENGTH_SHORT).show();
                    return;
                }


                PlayEngine.play(mAudioFile.getAbsolutePath(), ivVoice, new PlayEngine.PlayListener() {

                    @Override
                    public void onStart(boolean fromCache) {

                    }

                    @Override
                    public void onPause(int flag, boolean pause) {

                    }

                    @Override
                    public void onBufferingUpdate(MediaPlayer mp, int percent) {

                    }
                });
                break;
            case R.id.tv_path:
                copy(mAudioFile == null ? "" : mAudioFile.getAbsolutePath(), this);
                Toast.makeText(this, "复制路径成功 ", Toast.LENGTH_SHORT).show();
                break;
            case R.id.tv_aac:
                recordManager = null;
                mediaType = MediaType.AAC;
                break;
            case R.id.tv_wav_mp3:
                recordManager = null;
                mediaType = MediaType.WAV_TO_MP3;
                break;
            case R.id.tv_mp3:
                recordManager = null;
                mediaType = MediaType.MP3;
                break;

            case R.id.tv_wav:
                recordManager = null;
                mediaType = MediaType.WAV;
                break;
            case R.id.tv_amr:
                recordManager = null;
                mediaType = MediaType.AMR;
                break;
        }
    }

    public void setRecordState(boolean recording) {
        this.recordState = recording;
        tvStart.setText(recording ? "停止录音" : "开始录音");
        ;

    }

    /**
     * 实现文本复制功能
     * add by wangqianzhou
     *
     * @param content
     */
    public static void copy(String content, Context context) {
//   // 从API11开始android推荐使用android.content.ClipboardManager
        // 为了兼容低版本我们这里使用旧版的android.text.ClipboardManager，虽然提示deprecated，但不影响使用。
        ClipboardManager cm = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        // 将文本内容放到系统剪贴板里。
        cm.setText(content);

    }

}
