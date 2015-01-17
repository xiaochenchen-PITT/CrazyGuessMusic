package chen.pitt.crazyguessmusic.util;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.media.MediaPlayer;

import java.io.IOException;

import chen.pitt.crazyguessmusic.ui.MainActivity;

/**
 * 音乐播放类
 * Created by chen on 1/4/15.
 */
public class MyPlayer {
    //音效
    public static final int INDEX_TONE_ENTER = 0;
    public static final int INDEX_TONE_CANCEL = 1;
    public static final int INDEX_TONE_COIN = 2;
    private static final String[] TONE_NAMES = {"enter.mp3", "cancel.mp3", "coin.mp3"}; //音效名
    private static MediaPlayer[] mToneMediaPlayer = new MediaPlayer[TONE_NAMES.length];

    // 歌曲
    private static MediaPlayer mMusicMediaPlayer; //单例

    /**
     * 播放歌曲
     *
     * @param context,  上下文
     * @param fileName, 音乐文件名称
     */
    public static void playSong(final Context context, String fileName) {

        if (mMusicMediaPlayer == null) {
            mMusicMediaPlayer = new MediaPlayer();
        } else {
            //强制重置状态 非第一次播放
            mMusicMediaPlayer.reset();
        }

        try {
            mMusicMediaPlayer.setDataSource(fileName);
            mMusicMediaPlayer.prepare();
            mMusicMediaPlayer.start();
        } catch (IOException e) {
            e.printStackTrace();
        }

        //设置歌曲Completion监听
        mMusicMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                MainActivity.mViewDisc.clearAnimation();
            }
        });
    }

    public static void stopTheSong(Context context) {
        if (mMusicMediaPlayer != null) {
            mMusicMediaPlayer.stop();
        }
    }

    /**
     * 播放音效
     *
     * @param context 上下文
     * @param index   TONE_NAMES的tone索引
     */
    public static void playTone(Context context, int index) {
        //加载声音
        AssetManager assetManager = context.getAssets();

        if (mToneMediaPlayer[index] == null) {
            mToneMediaPlayer[index] = new MediaPlayer();
            try {
                AssetFileDescriptor assetFileDescriptor = assetManager.openFd(TONE_NAMES[index]);
                mToneMediaPlayer[index].setDataSource(assetFileDescriptor.getFileDescriptor(), assetFileDescriptor.getStartOffset(), assetFileDescriptor.getLength());
                mToneMediaPlayer[index].prepare();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        mToneMediaPlayer[index].start();

    }
}
