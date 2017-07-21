package cn.qssq666.voiceutiltest;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.widget.ImageView;

/**
 * Created by 情随事迁(qssq666@foxmail.com) on 2017/3/21.
 */

public class UploadVoice extends ImageView implements PlayEngine.AnimInterface {
    public UploadVoice(Context context) {
        super(context);
    }

    public UploadVoice(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public UploadVoice(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public void stopAnim() {
        this.setImageResource(R.drawable.btn_quan_recordingbox);
    }

    @Override
    public void startAnim() {
        this.setImageResource(R.drawable.btn_quan_recordingbox_check);
    }
}
