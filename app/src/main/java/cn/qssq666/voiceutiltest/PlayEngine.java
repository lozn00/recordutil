package cn.qssq666.voiceutiltest;

import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import java.io.IOException;

/**
 * Created by luozheng on 2016/7/6.  qssq.space
 * see MediaPlayerDemo_Audio.java by every
 */
public class PlayEngine {

    private static final int MSG_VIEW_PLAY_ANIM = 1;
    private static final int MSG_VIEW_STOP_ANIM = 2;
    private static final int MSG_ERROR_TEXT = 3;
    private static MediaPlayer mMediaPlayer;

    public static PlayListener getPlayListener() {
        return mPlayListener;
    }

    private static PlayListener mPlayListener;

    public static void setBindAnimView(AnimInterface mBindAnimView) {
        PlayEngine.mBindAnimView = mBindAnimView;
    }

    public static AnimInterface getBindAnimView() {
        return mBindAnimView;
    }

    public static String getPlayurl() {
        return playurl;
    }


    private static String playurl;
    private static AnimInterface mBindAnimView;
    private static String TAG = "PlayEngine";

    /**
     * 无需暂停 还是需要暂停? 但是界面销毁还是需要暂停的
     *
     * @param url
     */
    public static boolean play(String url) {
        return play(url, null);
    }

    public static boolean play(String url, AnimInterface bindView) {
        return play(url, bindView, null);
    }

    /**
     * @param url
     * @param bindView     绑定的动画view
     * @param playListener 停止的回调 true表示 是 正常停止的
     * @return
     */
    public static boolean play(String url, AnimInterface bindView, PlayListener playListener) {
        PlayEngine.mPlayListener = playListener;
        if (mBindAnimView != null) {
            mBindAnimView.stopAnim();
        }
        mBindAnimView = bindView;
        if (playurl != null && playurl.equals(url) && mMediaPlayer != null && mMediaPlayer.getDuration() > 0) {//说明已经加载完毕了呗

            boolean b = continuePlay();
            if (mPlayListener != null) {

                mPlayListener.onStart(true);
            }
            if (!b) {
                if (mPlayListener != null) {
                    mPlayListener.onPause(FLAG_HANDLE_PLAY_END, true);
                }
                pausePlay();
            } else {
            }
            return true;
        }
        destoryMedia();//管它咋的先销毁 绑定的view当然也一样

        mMediaPlayer = new MediaPlayer();
        mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mMediaPlayer.setLooping(false);
        try {

            mMediaPlayer.setDataSource(url);
            playurl = url;
        } catch (IOException e) {

            Log.e(TAG, "播放失败,设置数据源错误" + e.toString() + ",播放地址:" + playurl);
            toastMsg("播放地址有错");
            e.printStackTrace();
            if (mPlayListener != null) {
                mPlayListener.onPause(FLAG_HANDLE_PLAY_END, false);

            }
            return false;
        }
        mMediaPlayer.setOnBufferingUpdateListener(new MediaPlayer.OnBufferingUpdateListener() {
            @Override
            public void onBufferingUpdate(MediaPlayer mp, int percent) {
                Log.i(TAG, "缓存进度:" + percent);
                if (mPlayListener != null) {
                    mPlayListener.onBufferingUpdate(mp, percent);
                }
            }
        });
        mMediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                startAnim();
//                ActionEngine.requestAudioFocus();
                mMediaPlayer.start();
                if (mPlayListener != null) {
                    mPlayListener.onStart(false);
                }
            }
        });
        mMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                stopAnim();
                if (mPlayListener != null) {
                    mPlayListener.onPause(FLAG_PLAY_END, true);
                }
            }
        });
        mMediaPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(MediaPlayer mp, int what, int extra) {
                if (mPlayListener != null) {
                    mPlayListener.onPause(FLAG_ERROR_END, false);
                }
                switch (extra) {
                    case MediaPlayer.MEDIA_ERROR_IO:
                        Log.d(TAG, "文件流错误" + what + "," + extra);
                        toastMsg("文件流错误");
                        break;
                    case MediaPlayer.MEDIA_ERROR_MALFORMED:
                        toastMsg("MEDIA_ERROR_MALFORMED");
                        Log.d(TAG, "MEDIA_ERROR_MALFORMED" + what + "," + extra);
                        break;
                    case MediaPlayer.MEDIA_ERROR_UNSUPPORTED:
                        toastMsg("MEDIA_ERROR_UNSUPPORTED");
                        Log.d(TAG, "MEDIA_ERROR_UNSUPPORTED" + what + "," + extra);
                        break;
                    case MediaPlayer.MEDIA_ERROR_TIMED_OUT:
                        Log.d(TAG, "MEDIA_ERROR_TIMED_OUT");
                        toastMsg("播放时间超出");
                        break;
                    default:
                        Log.d(TAG, "未知错误 " + what + "," + extra);
                        toastMsg("未知错误 waht:" + what + ",extra:" + extra);
                        break;
                }
                stopAnim();
                return false;
            }
        });
        mMediaPlayer.prepareAsync();
        return true;
    }


    public static boolean pausePlay(boolean needCallBack, int flag) {
        if (mBindAnimView != null) {
            mBindAnimView.stopAnim();
        }

        if (mMediaPlayer != null && mMediaPlayer.isPlaying()) {
            mMediaPlayer.pause();
//            ActionEngine.abandonAudioFocus();
            if (needCallBack && mPlayListener != null) {
                mPlayListener.onPause(flag, true);
            }
            return true;
        } else {
            return false;
        }
    }

    public static boolean pausePlay() {
        return pausePlay(false, FLAG_HANDLE_PLAY_END);
    }


    static Handler handler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_VIEW_PLAY_ANIM:

                    if (mBindAnimView != null) {
                        mBindAnimView.startAnim();
                    }
                    break;
                case MSG_VIEW_STOP_ANIM:
                    if (mBindAnimView != null) {
                        mBindAnimView.stopAnim();
                        mBindAnimView = null;
                    }
                    break;
                case MSG_ERROR_TEXT:
//                    ToastUtils.showToast("" + msg.obj.toString());
                    Log.e(TAG, "PLAYERROR:" + msg.obj.toString());
                    break;
            }
        }
    };

    public static void startAnim() {
        handler.sendEmptyMessage(MSG_VIEW_PLAY_ANIM);
    }

    public static void stopAnim() {
        handler.sendEmptyMessage(MSG_VIEW_STOP_ANIM);
    }

    public static void toastMsg(String msg) {
        handler.obtainMessage(MSG_ERROR_TEXT, msg).sendToTarget();
    }

    public static boolean continuePlay() {
        return continuePlay(false);
    }

    /**
     * @param needCallBack 是否回调
     * @return
     */
    public static boolean continuePlay(boolean needCallBack) {

        if (mMediaPlayer != null && !mMediaPlayer.isPlaying()) {
//            ActionEngine.requestAudioFocus();
            mMediaPlayer.start();
            if (needCallBack && mPlayListener != null) {
                mPlayListener.onStart(true);
            }
            startAnim();
            return true;
        }
        return false;
    }

    /**
     * 不会有任何回调  如果要修改 状态 请先调用 pause方法
     *
     * @return
     */
    public static boolean destory() {
        stopAnim();
        PlayEngine.mPlayListener = null;
        return destoryMedia();

    }

    public static boolean isStop() {
        return mMediaPlayer == null || !mMediaPlayer.isPlaying();
    }

    public static boolean isDestory() {
        return mMediaPlayer == null;
    }

    public static boolean destoryMedia() {
        playurl = null;
        if (mMediaPlayer != null) {
//            mMediaPlayer.reset();
            if (mMediaPlayer.isPlaying()) {
                mMediaPlayer.stop();
//                ActionEngine.abandonAudioFocus();
            }
            mMediaPlayer.release();
            mMediaPlayer = null;
            return true;
        }
        return false;
    }

    public interface PlayListener {
        /**
         * 说明已经开始缓存了,就绪完毕了
         */
        void onStart(boolean fromCache);

        /**
         * @param flag  @see {FLAG_ERROR_END,FLAG_HANDLE_PLAY_END,FLAG_PLAY_END} 错我的停止 了 手动的停止了 ，还是一首歌曲的正常结束
         * @param pause
         */
        void onPause(int flag, boolean pause);

        void onBufferingUpdate(MediaPlayer mp, int percent);

    }

    public interface AnimInterface {
        void stopAnim();

        void startAnim();
    }

    public static abstract class SimplePlayListener implements PlayListener {
        public void setPosition(int position) {
            this.position = position;
        }

        public int position;

        public SimplePlayListener() {
        }

        public SimplePlayListener(int position) {
            this.position = position;
        }


        @Override
        public void onPause(int flag, boolean pause) {
            if (flag == FLAG_ERROR_END) {
                //非正常涨停
                onError();
            }
        }

        public abstract void onError();

        public void onBufferingUpdate(MediaPlayer mp, int percent) {

        }
    }

    public static final int FLAG_PLAY_END = 1;//一曲结束了
    public static final int FLAG_HANDLE_PLAY_END = 2;//手动的点击播放结束了 这个不会 关闭 播放控制台
    public static final int FLAG_HANDLE_FOREVER_PLAY_END = 3;//手动的点击播放结束了  永久关闭 就需要关闭控制台了
    public static final int FLAG_ERROR_END = 4;//错误的结束了
}
