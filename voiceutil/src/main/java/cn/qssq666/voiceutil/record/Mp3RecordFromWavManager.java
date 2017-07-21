package cn.qssq666.voiceutil.record;


import java.io.File;

import cn.qssq666.voiceutil.utils.MediaDirectoryUtils;
import cn.qssq666.audio.AudioManager;


/**
 * Created by 情随事迁(qssq666@foxmail.com) on 2017/3/31.
 */

public class Mp3RecordFromWavManager extends WavRecordManager {
    @Override
    public boolean stopRecord() {
        AudioManager audioManager = new AudioManager();
        File temp = MediaDirectoryUtils.getTempMp3FileName();
        File wavFile = getFile();
        setFile(temp);
        audioManager.convertmp3(wavFile.getAbsolutePath(), temp.getAbsolutePath());
        return super.stopRecord();
    }
}
