
 图片演示

![演示图片地址](https://github.com/qssq/giftanim/blob/master/Pictures/1.gif)

##### 使用方法

```
 compile 'cn.qssq666:recordutil:0.1'
```

支持录制amr,wav,mp3,aac，只需要改变工厂方法即可，都是一个抽象实现，因此用户轻松切换解决boss的变动需求。

录音接口类
```
public interface RecordManagerI {
    public boolean startRecordCreateFile(int stopTime) throws IOException;

    public boolean stopRecord();

    public void setOnTimeSecondChanage(OnTimeSecondChanage onTimeSecondChanage);

    public void setSoundAmplitudeListenr(SoundAmplitudeListenr soundAmplitudeListenr);

    public void setOnTimeOutStopListener(RecordManagerI.OnTimeOutStopListener onTimeOutStopListener);

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
```

另外 需要注意的是 监听录制声音高低setSoundAmplitudeListenr改变某些格式不支持如mp3格式的直接录制。如果你很牛比你可以做这件事情，我已经封装的很好了，基于我这个基础扩展还是很简单的哈。


如果用户要自定义临时生成的文件名已经产生的路径，可以 在Application创建的时候设置一下方法即可。如果返回的文件名是空,则表示该方法不需要改动,这个方法是静态的哈。


```

 MediaDirectoryUtils.setMediaManagerProvider(new MediaDirectoryUtils.MediaManagerProvider() {
                                                         @Override
                                                         public File getTempCacheWavFileName() {
                                                             return null;
                                                         }
                                             
                                                         @Override
                                                         public File getTempAmrFileName() {
                                                             return null;
                                                         }
                                             
                                                         @Override
                                                         public File getTempMp3FileName() {
                                                             return null;
                                                         }
                                             
                                                         @Override
                                                         public File getTempAACFileName() {
                                                             return null;
                                                         }
                                             
                                                         @Override
                                                         public File getTempCachePcmFileName() {
                                                             return null;
                                                         }
                                                     });
 
```

demo中部分代码 
采用工厂模式轻松切换任意录制格式 RecordFactory类提供了5种录音姿势封装的演示


```
`    public RecordManagerI getRecordManager() {
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
```