package cn.qssq666.voiceutil.record;

import java.io.File;
import java.io.IOException;

/**
 * Created by 情随事迁(qssq666@foxmail.com) on 2017/3/22.
 */

public interface RecordManagerI {
    public boolean startRecordCreateFile(int stopTime) throws IOException;

    public boolean stopRecord();

    public void setOnTimeSecondChanage(OnTimeSecondChanage onTimeSecondChanage);

    public void setSoundAmplitudeListenr(SoundAmplitudeListenr soundAmplitudeListenr);

    public void setOnTimeOutStopListener(RecordManagerI.OnTimeOutStopListener onTimeOutStopListener);

    public Object getAudioRecord();

    public boolean isRecordIng();

    public int getCurrenttime();

    public File getFile();

    public interface SoundAmplitudeListenr {
        public void amplitude(int amplitude);
    }

    public interface OnTimeOutStopListener {
        void onStop();
    }

    /**
     * 当前流逝的时间
     */
    public interface OnTimeSecondChanage {
        void onSecondChnage(int duration);
    }

}
