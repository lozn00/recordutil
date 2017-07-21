package cn.qssq666.voiceutil.record;

import android.media.MediaRecorder;
import android.os.Handler;

import java.io.File;
import java.io.IOException;

import cn.qssq666.voiceutil.utils.MediaDirectoryUtils;

public class AmrRecorderManager implements RecordManagerI {

    private static final int SAMPLE_RATE = 1600;
    private short[] mBuffer;


    private File file;//录音文件

    public MediaRecorder getMediaRecorder() {
        return mediaRecorder;
    }

    public boolean isRecordIng() {
        return record_state == RECORD_STATE.RECORDING;
    }

    private MediaRecorder mediaRecorder;//android 媒体录音类
    private RecordManagerI.SoundAmplitudeListenr soundAmplitudeListenr;//声波振幅监听器
    private final Handler mHandler = new Handler();
    private Runnable mUpdateMicStatusTimer = new Runnable() {
        /**
         * 分贝的计算公式K=20lg(Vo/Vi) Vo当前的振幅值,Vi基准值为600
         */
        private int BASE = 500;
        private int RATIO = 5;
        private int postDelayed = 200;

        @Override
        public void run() {
            if (soundAmplitudeListenr != null) {
                soundAmplitudeListenr.amplitude(mediaRecorder.getMaxAmplitude());
                mHandler.postDelayed(mUpdateMicStatusTimer, postDelayed);
            }
        }
    };

    /**
     * 暂停后不会清空 只有在重新开始才会重置
     *
     * @return
     */
    public int getCurrenttime() {
        return currenttime;
    }

    public File getFile() {
        return file;
    }

    private int currenttime;
    private int maxRecordTime;
    private Runnable mAddTimeRunnnable = new Runnable() {
        @Override
        public void run() {
            if (currenttime < maxRecordTime) {
                currenttime++;
                mHandler.postDelayed(this, 1000);//1秒一次
                if (onTimeSecondChanage != null) {
                    onTimeSecondChanage.onSecondChnage(currenttime);
                }
            } else {
                stopRecord();

            }
        }
    };


    protected void setMediaOutFormatAndEncoder(MediaRecorder encoder) {
        encoder.setAudioSource(MediaRecorder.AudioSource.MIC);
        encoder.setOutputFormat(MediaRecorder.OutputFormat.AMR_NB);
        encoder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
    }

    /**
     * @throws IOException
     */
    protected File getNewFile() {
        File temp = MediaDirectoryUtils.getTempAmrFileName();
        return temp;
    }

    public boolean startRecordCreateFile(int stopTime) throws IOException {
//        ActionEngine.requestAudioFocus();
        file = getNewFile();
        maxRecordTime = stopTime;
        currenttime = 0;//从新清空
     /*   if (!Environment.getExternalStorageState().equals(
                Environment.MEDIA_MOUNTED)) {
            return false;
        }*/

//		file=new File(Environment.getExternalStorageDirectory()+File.separator+"1"+File.separator+
//				new DateFormat().format("yyyyMMdd_HHmmss", Calendar.getInstance(Locale.CHINA))+".amr");
       /*
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.DEFAULT);//从麦克风源进行录音
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.AMR_NB);//设置输出格式
        mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);//设置编码格式
*/
        mediaRecorder = new MediaRecorder();//创建录音对象
        setMediaOutFormatAndEncoder(mediaRecorder);

/*
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.AMR_NB);//设置输出格式
        mediaRecorder.setAudioEncoder(MediaRecorder.AudioEnoder.AMR_NB);//设置编码格式



*/
/*
    int bufferSize = AudioRecord.getMinBufferSize(SAMPLE_RATE, AudioFormat.CHANNEL_IN_MONO,
				AudioFormat.ENCODING_PCM_16BIT);
		mBuffer = new short[bufferSize];
		mRecorder = new AudioRecord(MediaRecorder.AudioSource.MIC, SAMPLE_RATE, AudioFormat.CHANNEL_IN_MONO,
				AudioFormat.ENCODING_PCM_16BIT, bufferSize);
 */
        mediaRecorder.setMaxDuration(1000 * stopTime);
        mediaRecorder.setOutputFile(file.getAbsolutePath());

        //创建文件
        if (!file.getParentFile().exists()) {
            boolean mkdirs = file.getParentFile().mkdirs();
            if (!mkdirs) {
                throw new IOException("无法创建文件夹:" + file.getParentFile().getAbsolutePath() + "");
            }
        }
        boolean newFile = file.createNewFile();
        if (!newFile) {
            throw new IOException("无法创建文件:" + file.getAbsolutePath() + ",");
        }
        mediaRecorder.prepare();
        mediaRecorder.start();//这里也会崩溃?
        record_state = RECORD_STATE.RECORDING;
        mHandler.post(mUpdateMicStatusTimer);
        mHandler.postDelayed(mAddTimeRunnnable, 1000);
        return true;
    }


    public boolean stopRecord() {
//        ActionEngine.abandonAudioFocus();
        record_state = RECORD_STATE.STOP;
        boolean result;
        if (mediaRecorder != null) {
            try {
                mHandler.removeCallbacks(mUpdateMicStatusTimer);
                mHandler.removeCallbacks(mAddTimeRunnnable);

                mediaRecorder.stop();
                mediaRecorder.release();
                mediaRecorder = null;
                result = true;
            } catch (Exception e) {
//                MobclickAgent.reportError(AppContext.getInstance(), e);
                result = true;
            }
        } else {
            result = false;
        }

        if (onTimeOutStopListener != null) {
            onTimeOutStopListener.onStop();
        }
        return result;

    }

    public void setSoundAmplitudeListenr(RecordManagerI.SoundAmplitudeListenr soundAmplitudeListenr) {
        this.soundAmplitudeListenr = soundAmplitudeListenr;
    }


    public void setOnTimeOutStopListener(RecordManagerI.OnTimeOutStopListener onTimeOutStopListener) {
        this.onTimeOutStopListener = onTimeOutStopListener;
    }

    RecordManagerI.OnTimeOutStopListener onTimeOutStopListener;

    public void setOnTimeSecondChanage(OnTimeSecondChanage onTimeSecondChanage) {
        this.onTimeSecondChanage = onTimeSecondChanage;
    }

    OnTimeSecondChanage onTimeSecondChanage;

    public interface RECORD_STATE {
        int STOP = 0;
        int RECORDING = 1;
    }

    public int getRecordState() {
        return record_state;
    }

    int record_state = RECORD_STATE.STOP;


}
















