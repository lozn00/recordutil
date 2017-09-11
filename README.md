
 图片演示

![演示图片地址](https://github.com/qssq/recordutil/blob/master/Pictures/1.gif)

##### 使用方法

gradle
```
 compile 'cn.qssq666:recordutil:0.1'//回调duration由秒改成毫秒


```

maven

```
<dependency>
  <groupId>cn.qssq666</groupId>
  <artifactId>recordutil</artifactId>
  <version>0.2</version>
  <type>pom</type>
</dependency>
```


支持录制amr,wav,mp3,aac，只需要改变工厂方法即可，都是一个抽象实现，因此用户轻松切换解决boss的变动需求。
提供了如下管理器
AACMediaRecorderManager

AmrRecorderManager

MP3RecordManager

Mp3RecordFromWavManager
WavRecordManager


建议使用工厂的方式，这样切换录音格式只需要改一句工厂代码就行。
RecordFactory

如果要拿到具体的MediaRecorder 类 比如Amr的， 可以直接强转AmrRecorderManager 然后 getMediaRecorder()

工厂返回的录音接口类 接口类已被上面4个音频管理器类实现，可以直接进行强转拿如果要拿到具体的MediaRecorder 也可以直接从工厂的getInternAudioRecord()返回的Object对象强转具体的AudioRecord/MediaRecorder对象。


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
 
             @Override
             public File getCachePath() {
                 return null;//存储目录可以自定义逻辑，我这里是磁盘的某个文件夹
             }
 
             @Override
             public String productFileName(String postfix) {
                 return null;//这里是控制文件名生成格式 某些服务器端比较变态让你们这边修改
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

其它注意事项
======

混淆问题
---
m3p录制使用了jni 因此要保持这个不能被混淆

```-keep class cn.qssq666.audio.AudioManager{*;}```

如果完全不需要mp3录制
android节点加上
```
packagingOptions {
        exclude "lib/arm64-v8a/libmp3lame.so"
        exclude "lib/armeabi-v7a/libmp3lame.so"
        exclude "lib/x86/libmp3lame.so"
        exclude "lib/x86_64/libmp3lame.so"
        exclude "lib/mips/libmp3lame.so"
        exclude "lib/mips64/libmp3lame.so"
    }
    
```



```
<!-- -libraryjars ../XXX(此处为library名称)/src/main/jniLibs/x86/xxxxx.so -->
```

关于平台so优化问题.
--------

建议设置为一个so,这样可以节省不少体积
```
  ndk {

            //APP的build.gradle设置支持的SO库架构

            abiFilters 'armeabi-v7a'
        }
```
