package cn.qssq666.voiceutil.utils;

import android.graphics.Bitmap;
import android.media.ThumbnailUtils;
import android.os.Environment;
import android.support.annotation.NonNull;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by luozheng on 16/1/27.
 * getExternalStorageDirectory
 */
public class MediaDirectoryUtils {


    public static Bitmap getVideoThumbnail(String videoPath, int width, int height, int kind) {
        Bitmap bitmap = null;
        bitmap = ThumbnailUtils.createVideoThumbnail(videoPath, kind);
        bitmap = ThumbnailUtils.extractThumbnail(bitmap, width, height, ThumbnailUtils.OPTIONS_RECYCLE_INPUT);
        return bitmap;
    }

    private static final String TAG = "MediaDirectoryUtils";

    /*
        public  static  void initVideoPlayer(MediaController mediaController, VideoView videoView, String path) {
            videoView.setVideoPath(path);
            videoView.setMediaController(mediaController);
            mediaController.setMediaPlayer(videoView);
            // 让VideoView获取焦点
            videoView.requestFocus();
            // 开始播放
            mediaController.createPlayStationAndSHow(0);
        }

    */


    /**
     * 格式为 20160128115859
     *
     * @return
     */
    public static File getTempCacheAudioFileName() {
        return new File(getCachePath(), getAmrSimpleFileName());
    }

    public static File getTempCacheWavFileName() {
        if (mediaManagerProvider != null && mediaManagerProvider.getTempCacheWavFileName() != null) {
            return mediaManagerProvider.getTempCacheWavFileName();
        }
        return getTempCacheFileName(".wav");
    }

    public static File getTempCachePcmFileName() {
        if (mediaManagerProvider != null && mediaManagerProvider.getTempCachePcmFileName() != null) {
            return mediaManagerProvider.getTempCachePcmFileName();
        }
        return getTempCacheFileName(".pcm");
    }

    public static File getTempMp3FileName() {
        if (mediaManagerProvider != null && mediaManagerProvider.getTempMp3FileName() != null) {
            return mediaManagerProvider.getTempMp3FileName();
        }
        File tempCacheFileName = getTempCacheFileName(".mp3");

        return tempCacheFileName;
    }

    public static File getTempAACFileName() {
        if (mediaManagerProvider != null && mediaManagerProvider.getTempAACFileName() != null) {
            return mediaManagerProvider.getTempAACFileName();
        }
        return getTempCacheFileName(".aac");
    }

    public static File getTempAmrFileName() {
        if (mediaManagerProvider != null && mediaManagerProvider.getTempAmrFileName() != null) {
            return mediaManagerProvider.getTempAmrFileName();
        }
        return getTempCacheFileName(".amr");
    }

    private static File getTempCacheFileName(String pex) {
        return new File(getCachePath(), productSimpleFileName(pex));
    }


    private static String getAmrSimpleFileName() {

        return productSimpleFileName(".amr");
    }

    private static String getWavSimpleFileName() {
        return productSimpleFileName(".wav");
    }


    @NonNull
    public static File getCachePath() {

        if (mediaManagerProvider != null && mediaManagerProvider.getCachePath() != null) {
            return mediaManagerProvider.getCachePath();
        }

        return getAppPath(cacheStr);
    }


    private static String productSimpleFileName(String postfix) {


        if (mediaManagerProvider != null && mediaManagerProvider.productFileName(postfix) != null) {
            return mediaManagerProvider.productFileName(postfix);
        }
        Date date = new Date(System.currentTimeMillis()); //2016-01-28 12:02:28  14位年月日
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
        return sdf.format(date) + MediaDirectoryUtils.getRandom(3) + postfix;//(2017 02 23 17 38 23 1
//        return sdf.format(date) + MediaUtils.getRandom(1) + postfix;//(2017 02 23 17 38 23 1
    }


    public static int getRandom(int n) {
        int ans = 0;
        while (Math.log10(ans) + 1 < n)
            ans = (int) (Math.random() * Math.pow(10, n));
        return ans;
    }

    public static File getAppPath(String path) {
/*        Log.d(TAG, "系统空间:" + FileUtils.readSystemAvailableBlocks() + "k," + Formatter.formatFileSize(AppContext.getInstance(), FileUtils.getAvailableInternalMemorySize()));
        Log.d(TAG, "磁盘空间:" + FileUtils.readSDCardAvailableBlocks() + "k,," + Formatter.formatFileSize(AppContext.getInstance(), FileUtils.getAvailableExternalMemorySize()));*/
        File externalStorageDirectory = Environment.getExternalStorageDirectory();
        File file = new File(externalStorageDirectory, ROOT_FOLDER);
        return new File(file, path);
    }

    public final static String ROOT_FOLDER = "qssqvoice";
    private static String cacheStr = "cache";

    public interface MediaManagerProvider {

        File getTempCacheWavFileName();

        File getTempAmrFileName();

        File getTempMp3FileName();

        File getTempAACFileName();

        File getTempCachePcmFileName();

        /**
         * 缓存根目录
         *
         * @return
         */
        File getCachePath();

        String productFileName(String postfix);
    }

    public static void setMediaManagerProvider(MediaManagerProvider mediaManagerProvider) {
        MediaDirectoryUtils.mediaManagerProvider = mediaManagerProvider;
    }

    static MediaManagerProvider mediaManagerProvider;
}
