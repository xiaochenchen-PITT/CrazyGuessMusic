package chen.pitt.crazyguessmusic.ui;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.os.PowerManager;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;
import android.widget.Toast;

import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.SendMessageToWX;
import com.tencent.mm.sdk.openapi.WXAPIFactory;
import com.tencent.mm.sdk.openapi.WXMediaMessage;
import com.tencent.mm.sdk.openapi.WXTextObject;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import chen.pitt.crazyguessmusic.R;
import chen.pitt.crazyguessmusic.data.Const;
import chen.pitt.crazyguessmusic.model.IAlertDialogButtonListener;
import chen.pitt.crazyguessmusic.model.IWordButtonClickListener;
import chen.pitt.crazyguessmusic.model.Song;
import chen.pitt.crazyguessmusic.model.WordButton;
import chen.pitt.crazyguessmusic.myui.MyGridView;
import chen.pitt.crazyguessmusic.util.MyPlayer;
import chen.pitt.crazyguessmusic.util.Util;

public class MainActivity extends Activity implements IWordButtonClickListener {

    // 答案状态
    private final static int STATUS_ANSWER_RIGHT = 1;
    private final static int STATUS_ANSWER_WRONG = 2;
    private final static int STATUS_ANSWER_INCOMPLETE = 3;

    // 闪烁次数
    public static final int SPARK_TIMES = 6;

    // 过关界面view
    private View mPassView;

    // 当前关索引View
    private TextView mCurrentStagePassView;
    private TextView mCurrentStageView;

    // 当前歌曲名称
    private TextView mCurrentSongNamePassView;

    // Disc and stick animation related
    private Animation mDiscAnim;

    private LinearInterpolator mDiscLin;
    private Animation mStickInAnim;

    private LinearInterpolator mStickInLin;
    private Animation mStickOutAnim;

    private LinearInterpolator mStickOutLin;
    // Play button
    private ImageButton mPlayButton;

    // ImageView of Disc and Sick
    public static ImageView mViewDisc;

    private ImageView mViewStick;
    // are the whole set of animations still running
    private boolean mIsRunning = false;

    // Word target container
    private ArrayList<WordButton> mAllWords; // 待选文字

    // Word selected container
    private ArrayList<WordButton> mSelectedWords; // 已选文字
    private MyGridView mMyGridView;
    // Word selected UI component
    private LinearLayout mViewWordsContainer;


    // Sd卡音乐文件 <FileName, FilePath>
    public HashMap<String, String> mSongInfo = new HashMap<String, String>();
    public ArrayList<String> mCandidates = new ArrayList<String>();

    // 当前的歌曲
    public Song mCurrentSong;

    // 当前关的索引
    private int mCurrentStageIndex = 0;

    // 当前金币数量
    private int mCurrentCoins = Const.TOTAL_COINS;
    // 金币View
    private TextView mViewCurrentCoins;

    // 自定义AlertDialog事件响应
    // 删除错误答案
    private IAlertDialogButtonListener mBtnOkDeleteWordListener = new IAlertDialogButtonListener() {

        @Override
        public void onDialogButtonClick() {
            deleteOneWord();
        }
    };

    // 提示答案
    private IAlertDialogButtonListener mBtnOkTipAnswerListener = new IAlertDialogButtonListener() {

        @Override
        public void onDialogButtonClick() {
            tipAnswer();
        }
    };

    // 金币不足
    private IAlertDialogButtonListener mBtnOkLackCoinsListener = new IAlertDialogButtonListener() {

        @Override
        public void onDialogButtonClick() {
            return;
        }
    };

    // 微信分享
    private IAlertDialogButtonListener mBtnOkShareListener = new IAlertDialogButtonListener() {

        @Override
        public void onDialogButtonClick() {
            wxShare();
        }
    };

    // 微信加金币
    private IAlertDialogButtonListener mBtnOkAddCoinListener = new IAlertDialogButtonListener() {

        @Override
        public void onDialogButtonClick() {
            wxAddCoin();
        }
    };

    // AlertDialog 事件名称
    public static final int ID_DIALOG_DELETE_WORD = 1;
    public static final int ID_DIALOG_TIP_ANSWER = 2;
    public static final int ID_DIALOG_LACK_COINS = 3;
    public static final int ID_DIALOG_WX_ADD_COIN = 4;

    //获得系统POWER_SERVICE对象
    public PowerManager mPowerManager;
    //通过newWakeLock()方法创建WakeLock实例
    public PowerManager.WakeLock mWakeLock;

    // IWXAPI 是第三方app和微信通信的openapi接口
    private IWXAPI mApi;

    //发送对话(false) 或者 朋友圈(true)
    private boolean mIsTimeline = false;
    private boolean mIsReturnFromWXShare = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialization of WakeLock object
        mPowerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
        mWakeLock = mPowerManager.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, "PowerManagerTAG");

        //数据读取
        String[] datas = Util.readData(MainActivity.this);
//        Log.d("datas - stage", datas[Const.INDEX_LOAD_DATA_STAGE]);
//        Log.d("datas - coin", datas[Const.INDEX_LOAD_DATA_COINS]);
//        Log.d("datas - name", datas[Const.INDEX_LOAD_DATA_SONGNAME]);
//        Log.d("datas - path", datas[Const.INDEX_LOAD_DATA_SONGPATH]);
        mCurrentStageIndex = Integer.parseInt(datas[Const.INDEX_LOAD_DATA_STAGE]);
        mCurrentCoins = Integer.parseInt(datas[Const.INDEX_LOAD_DATA_COINS]);
        mCurrentSong = new Song();
        mCurrentSong.setSongName(datas[Const.INDEX_LOAD_DATA_SONGNAME]);
        mCurrentSong.setSongFileName(datas[Const.INDEX_LOAD_DATA_SONGPATH]);
        Util.saveData(MainActivity.this, mCurrentStageIndex+"", mCurrentCoins+"", mCurrentSong.getSongName(), mCurrentSong.getSongFileName());
//        Log.d("data load", "" + mCurrentStageIndex + "," + mCurrentCoins);
//        Log.d("current song", mCurrentSong.getSongName());
//        Log.d("current song", mCurrentSong.getSongFileName());


        // Initialization of MyGridView object
        mMyGridView = (MyGridView) findViewById(R.id.gridview);

        // 注册监听
        mMyGridView.registernWordButtonClick(this);

        // Initialization of coin view
        mViewCurrentCoins = (TextView) findViewById(R.id.coin_amount);
        mViewCurrentCoins.setText(mCurrentCoins + "");

        // Initialization of Word Selected UI component object
        mViewWordsContainer = (LinearLayout) findViewById(R.id.word_selector_container);

        // Initialization of the Views
        mViewDisc = (ImageView) findViewById(R.id.gameDisc);
        mViewStick = (ImageView) findViewById(R.id.discStick);

        // Initialization of the Disc object
        mDiscAnim = AnimationUtils.loadAnimation(this, R.anim.rotate_disc);
        mDiscLin = new LinearInterpolator();
        mDiscAnim.setInterpolator(mDiscLin);
        mDiscAnim.setAnimationListener(new AnimationListener() {

            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                mViewStick.startAnimation(mStickOutAnim);

            }
        });

        // Initialization of the Stick object
        mStickInAnim = AnimationUtils.loadAnimation(this,
                R.anim.rotate_stick_in);
        mStickInLin = new LinearInterpolator();
        mStickInAnim.setInterpolator(mStickInLin);
        mStickInAnim.setFillAfter(true); // keep the current position when
        // animation finish
        mStickInAnim.setAnimationListener(new AnimationListener() {

            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                mViewDisc.startAnimation(mDiscAnim);

            }
        });

        mStickOutAnim = AnimationUtils.loadAnimation(this,
                R.anim.rotate_stick_out);
        mStickOutLin = new LinearInterpolator();
        mStickOutAnim.setInterpolator(mStickOutLin);
        mStickOutAnim.setFillAfter(true);
        mStickOutAnim.setAnimationListener(new AnimationListener() {

            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                mIsRunning = false;
                mPlayButton.setVisibility(View.VISIBLE);
            }
        });

        // 过关界面
        mPassView = (LinearLayout) findViewById(R.id.pass_view);

        // Initialization of play button
        mPlayButton = (ImageButton) findViewById(R.id.play);
        mPlayButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                handlePlayButton();
            }
        });

        // 注册微信
        regToWx(MainActivity.this);
        // 扫描sd卡音乐文件
        initSdMusic();
        // 初始化游戏数据
        initCurrentStageData();
        // 初始化delete word onclick事件
        handleDeleteWord();
        // 初始化give a tip onclick事件
        handleTipAnswer();
        // 初始化微信add Coin onclick事件
        handleWxAddCoin();

    }

    @Override
    protected void onResume() {
        super.onResume();

        // 屏幕常亮 -－ 开始
        mWakeLock.acquire();

        // 重新播放歌曲
        if (!mIsReturnFromWXShare) {
            handlePlayButton();
        }
        mIsReturnFromWXShare = false;
    }

    @Override
    protected void onPause() {
        super.onPause();

        // 屏幕常亮 -－ 结束
        mWakeLock.release();

        //停止动画
        mViewDisc.clearAnimation();
        mViewStick.clearAnimation();

        //停止音乐
        MyPlayer.stopTheSong(MainActivity.this);
        mIsRunning = false;

        //实现数据存储
//        Log.d("Storage", mCurrentStageIndex - 1 + "");
        Util.saveData(MainActivity.this, (mCurrentStageIndex - 1) + "", mCurrentCoins + "", mCurrentSong.getSongName(), mCurrentSong.getSongFileName());

    }

    public void regToWx(Context context) {
        // 通过WXAPIFactory, 获取IWXAPI实例
        mApi = WXAPIFactory.createWXAPI(context, Const.APP_ID, true);
        //将应用的app id注册到微信
        mApi.registerApp(Const.APP_ID);
    }

    public void initSdMusic() {
        if (Const.SD_CARD_DIR.exists()) {
            checkFile(Const.SD_CARD_DIR);
        } else {
            Toast.makeText(MainActivity.this, "No SD Card Found!", Toast.LENGTH_LONG).show();
            Log.d("SONG_INFO", "NO Music Found!!!");
        }
        Log.d("SONG_INFO", mSongInfo + "");

        //生成待选文字pool
        for (String key : mSongInfo.keySet()) {
            for (int i = 0; i < key.length(); i++) {
                mCandidates.add(key.charAt(i) + "");
            }
        }
        Log.d("pool", mCandidates + "");
    }

    private void checkFile(File file) {
        if (file.getName().startsWith(".")) {
            return;
        }

        if (file.isDirectory()) {
            File[] files = file.listFiles();
            if (files != null) {
                for (int i = 0; i < files.length; i++) {
                    File f = files[i];
                    checkFile(f);
                }
            }
        } else if (file.isFile()) {
            int dot = file.getName().lastIndexOf(".");
            if (dot > -1 && dot < file.getName().length()) {
                String extName = file.getName().substring(dot, file.getName().length());
                for (int i = 0; i < Const.EXT_NAMES.length; i++) {
                    if (extName.equals(Const.EXT_NAMES[i])) {
                        String fileName = file.getName().substring(0, dot);
                        if (fileName.length() < 7 && !fileName.startsWith(".") && !fileName.startsWith("_")) {
                            mSongInfo.put(file.getName().substring(0, dot), file.getAbsolutePath());
                        }
                        break;
                    }
                }
            }
        }
    }

    public Song loadStageSongInfo() {
        Song song = new Song();

        Random random = new Random();
        String[] keys = mSongInfo.keySet().toArray(new String[mSongInfo.size()]);

        int randomIndex = random.nextInt(mSongInfo.size());
        String randomKey = keys[randomIndex];
        String randomValue = mSongInfo.get(randomKey);

        song.setSongFileName(randomValue);
        song.setSongName(randomKey);

//        Log.d("SongFileName", randomValue);
//        Log.d("SongName", randomKey);

        return song;

    }

    /**
     * 加载当前关的数据
     */
    public void initCurrentStageData() {

        // 清空上一关答案
        mViewWordsContainer.removeAllViews();

        // 读取当前关的歌曲信息
        if (mCurrentSong == null || mCurrentSong.getSongName().equals("default_song_name")) {
            mCurrentSong = loadStageSongInfo();
        }
        Util.saveData(MainActivity.this, mCurrentStageIndex + "", mCurrentCoins + "", mCurrentSong.getSongName(), mCurrentSong.getSongFileName());

        // 初始化已选择框
        mSelectedWords = initWordSelect();

        // 新的答案框
        LayoutParams params = new LayoutParams(80, 80);
        for (int i = 0; i < mSelectedWords.size(); i++) {
            mViewWordsContainer.addView(mSelectedWords.get(i).getmViewButton(),
                    params);
        }

        // 显示当前关的索引
        mCurrentStageView = (TextView) findViewById(R.id.musicTitle);
        if (mCurrentStageView != null) {
            mCurrentStageView.setText((mCurrentStageIndex + 1) + "");
        }

        // get data
        mAllWords = initAllWord();
        // update data--MyGridView
        mMyGridView.updateData(mAllWords);

        //立刻开始播放音乐
        handlePlayButton();

        // 即将进入下一关
        mCurrentStageIndex += 1;
    }

    /*
     *  初始化待选文字框
     */
    private ArrayList<WordButton> initAllWord() {
        ArrayList<WordButton> data = new ArrayList<WordButton>();
        // 获得所有待选文字
        String[] words = generateWords();

        for (int i = 0; i < MyGridView.COUNTS_WORD; i++) {
            WordButton button = new WordButton();
            button.setmChar(words[i]);
            data.add(button);
        }
        return data;
    }

    /*
     * 初始化已选文字框
     */
    private ArrayList<WordButton> initWordSelect() {
        ArrayList<WordButton> data = new ArrayList<WordButton>();

        for (int i = 0; i < mCurrentSong.getNameLength(); i++) {
            View view = Util.getView(this, R.layout.self_ui_gridview_item);
            final WordButton holder = new WordButton();
            holder.setmViewButton((Button) view
                    .findViewById(R.id.item_wordButton));
            holder.getmViewButton().setTextColor(Color.WHITE);
            holder.getmViewButton().setText("");
            holder.setmIsVisible(true);

            holder.getmViewButton().setBackgroundResource(
                    R.drawable.game_wordblank);
            holder.getmViewButton().setOnClickListener(
                    new View.OnClickListener() {

                        @Override
                        public void onClick(View v) {
                            clearAnswer(holder);
                        }
                    });

            data.add(holder);

        }
        return data;
    }

    /*
     * 生成所有待选文字
     */
    private String[] generateWords() {
        String[] words = new String[MyGridView.COUNTS_WORD];
        Random random = new Random();

        // 存入歌曲名字
        for (int i = 0; i < mCurrentSong.getNameLength(); i++) {
            words[i] = mCurrentSong.getNameChars()[i] + "";
        }
        // 获取其他的随机文字
        for (int i = mCurrentSong.getNameLength(); i < MyGridView.COUNTS_WORD; i++) {
            String wordChar = getRandomChar();
            while (Arrays.asList(words).contains(wordChar)) {
                wordChar = getRandomChar();
            }
            words[i] = wordChar;
        }

        // 打乱顺序 shuffle － Fisher–Yates shuffle
        for (int i = 0; i < MyGridView.COUNTS_WORD; i++) {
            int index = random.nextInt(i + 1);

            String t = words[i];
            words[i] = words[index];
            words[index] = t;
        }

        return words;
    }

    private String getRandomChar() {
        Random random = new Random();
        String randomNameChar = mCandidates.get(random.nextInt(mCandidates.size()));
        return randomNameChar;
    }

    /*
     * onClick handler, play the music
     */
    public void handlePlayButton() {
        if (mPlayButton != null) {
            if (!mIsRunning) {
                mIsRunning = true;

                //开始拨杆进入动画
                mViewStick.startAnimation(mStickInAnim);
                mPlayButton.setVisibility(View.GONE);

                //播放音乐
                MyPlayer.playSong(MainActivity.this, mCurrentSong.getSongFileName());
            }
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * chen.pitt.crazyguessmusic.model.IWordButtonClickListener#onWordButtonClick
     * (chen.pitt.crazyguessmusic.model.WordButton)
     */
    @Override
    public void onWordButtonClick(WordButton wordButton) {
        setSelectedWord(wordButton);

        // 检查答案
        int checkResult = checkAnswer();

        switch (checkResult) {
            case STATUS_ANSWER_RIGHT:
                handlePassEvent();
                break;
            case STATUS_ANSWER_WRONG:
                // 闪烁文字 提示用户
                sparkTheWord();
                break;
            case STATUS_ANSWER_INCOMPLETE:
                // 将文字颜色设置回白色(Normal)
                for (int i = 0; i < mSelectedWords.size(); i++) {
                    mSelectedWords.get(i).getmViewButton()
                            .setTextColor(Color.WHITE);
                }
                break;
            default:
                break;
        }
    }

    /**
     * 处理过关界面及事件
     */
    public void handlePassEvent() {
        // 停止未完成的动画
        mViewDisc.clearAnimation();
        mViewStick.clearAnimation();

        //停止未完成的音乐
        MyPlayer.stopTheSong(MainActivity.this);
        mIsRunning = false;

        // 加金币50
        handleCoins(Const.PASS_COIN);

        // 存储数据
//        Log.d("pass event", mCurrentStageIndex - 1 + "");
        Util.saveData(MainActivity.this, (mCurrentStageIndex - 1) + "", mCurrentCoins + "", mCurrentSong.getSongName(), mCurrentSong.getSongFileName());

        // 显示过关界面
        mPassView.setVisibility(View.VISIBLE);

        //播放音效
        MyPlayer.playTone(MainActivity.this, MyPlayer.INDEX_TONE_COIN);

        // 显示当前关的索引
        mCurrentStagePassView = (TextView) findViewById(R.id.text_current_stage_pass);
        if (mCurrentStagePassView != null) {
            mCurrentStagePassView.setText((mCurrentStageIndex) + "");
        }

        // 显示歌曲名称
        mCurrentSongNamePassView = (TextView) findViewById(R.id.text_current_song_name_pass);
        if (mCurrentSongNamePassView != null) {
            mCurrentSongNamePassView.setText(mCurrentSong.getSongName());
        }

        // 下一关 按键处理
        ImageButton btnPass = (ImageButton) findViewById(R.id.btn_next);
        btnPass.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (judgeAppPassed()) {
                    // 进入通关界面
                    Util.startActivity(MainActivity.this, AllPassView.class);
                } else {
                    // 开始下一关
                    mPassView.setVisibility(View.GONE);
                    // 加载关卡数据
                    mCurrentSong = loadStageSongInfo();
                    initCurrentStageData();
                }
            }
        });

        // 分享到微信 按键处理
        ImageButton btnShare = (ImageButton) findViewById(R.id.btn_share);
        btnShare.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                wxShare();
            }
        });
    }

    /**
     * 判断是否通关
     */
    private boolean judgeAppPassed() {
        return (mCurrentStageIndex == mSongInfo.keySet().size() - 1);
    }

    private void clearAnswer(WordButton wordButton) {
        // 设置已选框可见性
        wordButton.getmViewButton().setText("");
        wordButton.setmChar("");
        wordButton.setmIsVisible(false);
        // 已选框设置回白色
        for (int i = 0; i < mSelectedWords.size(); i++) {
            mSelectedWords.get(i).getmViewButton().setTextColor(Color.WHITE);
        }

        // 设置待选框可见性
        setButtonVisible(mAllWords.get(wordButton.getmIndex()), View.VISIBLE);

    }

    /**
     * 设置答案
     *
     * @param wordButton
     */
    private void setSelectedWord(WordButton wordButton) {
        for (int i = 0; i < mSelectedWords.size(); i++) {
            if (mSelectedWords.get(i).getmChar().length() == 0) {
                // 设置已选框内容和可见性
                mSelectedWords.get(i).getmViewButton()
                        .setText(wordButton.getmChar());
                mSelectedWords.get(i).setmIsVisible(true);
                mSelectedWords.get(i).setmChar(wordButton.getmChar());

                // 设置 记录索引
                mSelectedWords.get(i).setmIndex(wordButton.getmIndex());

                // 设置待选框可见性
                setButtonVisible(wordButton, View.INVISIBLE);

                break;

            }

        }
    }

    /**
     * 设置待选文字框可见性
     *
     * @param wordButton
     * @param visibility
     */
    private void setButtonVisible(WordButton wordButton, int visibility) {
        wordButton.getmViewButton().setVisibility(visibility);
        wordButton.setmIsVisible(visibility == View.VISIBLE);
    }

    /**
     * 检查答案
     *
     * @return
     */
    private int checkAnswer() {
        // 先检查长度
        for (int i = 0; i < mSelectedWords.size(); i++) {
            if (mSelectedWords.get(i).getmChar().length() == 0) { // 如果有空的
                // 则是答案不完全
                // 返回3
                return STATUS_ANSWER_INCOMPLETE;
            }
        }
        // 答案完整 再检查答案正确
        StringBuffer buf = new StringBuffer();
        for (int i = 0; i < mSelectedWords.size(); i++) {
            buf.append(mSelectedWords.get(i).getmChar());
        }
        return (buf.toString().equals(mCurrentSong.getSongName())) ? STATUS_ANSWER_RIGHT
                : STATUS_ANSWER_WRONG;
    }

    /**
     * 错误答案 闪烁文字
     */
    private void sparkTheWord() {
        TimerTask task = new TimerTask() { // 定时器任务框架
            boolean mChange = false;
            int mSparkTime = 0;

            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    public void run() {
                        // 显示闪烁次数
                        if (++mSparkTime > SPARK_TIMES) {
                            return;
                        } else { // 交替显示红 白色
                            for (int i = 0; i < mSelectedWords.size(); i++) {
                                mSelectedWords.get(i).getmViewButton().setTextColor(mChange ? Color.RED : Color.WHITE);
                            }
                            mChange = !mChange;
                        }
                    }
                });
            }
        };

        Timer timer = new Timer();
        timer.schedule(task, 1, 150);
    }

    /**
     * 删除文字逻辑
     */
    public void deleteOneWord() {
        if (mCurrentCoins - getDeleteWordCoins() >= 0) {
            handleCoins(-getDeleteWordCoins()); // 减少金币
            MyPlayer.playTone(MainActivity.this, MyPlayer.INDEX_TONE_COIN); //播放音效
            Random random = new Random();
            WordButton button = new WordButton();
            int i = 0;
            while (i++ < MyGridView.COUNTS_WORD) { // 防止死循环
                button = mAllWords.get(random.nextInt(MyGridView.COUNTS_WORD));
                if (!mCurrentSong.getSongName().contains(button.getmChar())
                        && button.ismIsVisible()) { // 非答案字 并且是可见的
                    break;
                }
            }
            setButtonVisible(button, View.INVISIBLE);

        } else {
            // 金币不够
            showConfirmDialog(ID_DIALOG_LACK_COINS);
            return;
        }
    }

    /**
     * 提示逻辑, 随机提示一个答案字
     */
    public void tipAnswer() {
        // 如果已选文字框是满的 则直接return
        for (int i = 0; i < mSelectedWords.size(); i++) {
            if (mSelectedWords.get(i).getmChar().length() == 0) {
                break;
            }
            if (i == mSelectedWords.size() - 1
                    && mSelectedWords.get(i).getmChar().length() != 0) {
                sparkTheWord();
                return;
            }
        }
        if (mCurrentCoins - getTipCoins() >= 0) {
            handleCoins(-getTipCoins()); // 减少金币
            MyPlayer.playTone(MainActivity.this, MyPlayer.INDEX_TONE_COIN); //播放音效

            Random random = new Random();
            int index = random.nextInt(mCurrentSong.getSongName().length());
            while (mSelectedWords.get(index).getmChar().length() != 0) {
                index = random.nextInt(mCurrentSong.getSongName().length());
            }
            String target = mCurrentSong.getSongName().charAt(index) + "";
            for (int i = 0; i < MyGridView.COUNTS_WORD; i++) {

                if (target.equals(mAllWords.get(i).getmChar())) {
                    // onWordButtonClick(mAllWords.get(i));

                    mSelectedWords.get(index).getmViewButton()
                            .setText(mAllWords.get(i).getmChar());
                    mSelectedWords.get(index).setmIsVisible(true);
                    mSelectedWords.get(index).setmChar(
                            mAllWords.get(i).getmChar());

                    // 设置 记录索引
                    mSelectedWords.get(index).setmIndex(
                            mAllWords.get(i).getmIndex());

                    // 设置待选框可见性
                    setButtonVisible(mAllWords.get(i), View.INVISIBLE);

                    // 检查答案
                    int checkResult = checkAnswer();

                    switch (checkResult) {
                        case STATUS_ANSWER_RIGHT:
                            handlePassEvent();
                            break;
                        case STATUS_ANSWER_WRONG:
                            // 闪烁文字 提示用户
                            sparkTheWord();
                            break;
                        case STATUS_ANSWER_INCOMPLETE:
                            // 将文字颜色设置回白色(Normal)
                            for (int j = 0; j < mSelectedWords.size(); j++) {
                                mSelectedWords.get(j).getmViewButton()
                                        .setTextColor(Color.WHITE);
                            }
                            break;
                        default:
                            break;
                    }
                    break;
                }
            }
        } else {
            // 金币不够
            showConfirmDialog(ID_DIALOG_LACK_COINS);
            return;
        }
    }

    public void wxCommunication(String text) {
        // WX媒体对象 初始化
        WXTextObject textObj = new WXTextObject();
        textObj.text = text;

        // WX消息对象 初始化及装载媒体对象
        WXMediaMessage msg = new WXMediaMessage();
        msg.mediaObject = textObj;
        msg.description = text;

        // 请求对象初始化
        SendMessageToWX.Req req = new SendMessageToWX.Req();
        req.transaction = String.valueOf(System.currentTimeMillis());
        req.message = msg;
        req.scene = mIsTimeline ? SendMessageToWX.Req.WXSceneTimeline : SendMessageToWX.Req.WXSceneSession;

        // send 请求
        mApi.sendReq(req);
    }

    /**
     * 实现微信分享逻辑
     */
    public void wxShare() {
        String text = getString(R.string.wx_share_default) + mCurrentSong.getSongName();
        wxCommunication(text);
        mIsReturnFromWXShare = true;
    }

    /**
     * 实现微信加金币逻辑
     */
    public void wxAddCoin() {
        String text = getString(R.string.wx_add_coin_default);
        wxCommunication(text);
        handleCoins(getAddCoin());
        MyPlayer.playTone(MainActivity.this, MyPlayer.INDEX_TONE_COIN);
    }


    /**
     * 增加amount的金币
     *
     * @param amount
     * @return true 增加/减少成功, false 失败
     */
    private boolean handleCoins(int amount) {
        if (mCurrentCoins + amount >= 0) {
            mCurrentCoins += amount;
            mViewCurrentCoins.setText(mCurrentCoins + "");
            return true;
        } else {
            return false;
        }
    }

    /**
     * 从配置文件中读取删除操作所需金币
     *
     * @return int 所需金币
     */
    private int getDeleteWordCoins() {
        return getResources().getInteger(R.integer.delete_cost);
    }

    /**
     * 从配置文件中读取提示操作所需金币
     *
     * @return int 所需金币
     */
    private int getTipCoins() {
        return getResources().getInteger(R.integer.tip_cost);
    }

    /**
     * 从配置文件中读取提示操作所需金币
     *
     * @return int 所需金币
     */
    private int getAddCoin() {
        return getResources().getInteger(R.integer.add_coin);
    }

    /**
     * 处理删除待选文字事件
     */
    public void handleDeleteWord() {
        ImageButton button = (ImageButton) findViewById(R.id.btn_delete_word);
        button.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // deleteOneWord();
                showConfirmDialog(ID_DIALOG_DELETE_WORD);
            }
        });
    }

    /**
     * 处理提示按键事件
     */
    public void handleTipAnswer() {
        ImageButton button = (ImageButton) findViewById(R.id.btn_tip_answer);
        button.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // tipAnswer();
                showConfirmDialog(ID_DIALOG_TIP_ANSWER);
            }
        });
    }


    public void handleWxAddCoin() {

        // 发微信加金币 按键处理
        ImageButton btnShare = (ImageButton) findViewById(R.id.add_coin);
        btnShare.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                showConfirmDialog(ID_DIALOG_WX_ADD_COIN);
            }
        });
    }

    private void showConfirmDialog(int id) {
        switch (id) {
            case ID_DIALOG_DELETE_WORD:
                Util.showDialog(MainActivity.this, "确认花掉" + getDeleteWordCoins()
                        + "个金币删除一个错误答案？", mBtnOkDeleteWordListener);
                break;
            case ID_DIALOG_TIP_ANSWER:
                Util.showDialog(MainActivity.this, "确认花掉" + getTipCoins()
                        + "个金币获得一个正确提示？", mBtnOkTipAnswerListener);
                break;
            case ID_DIALOG_LACK_COINS:
                Util.showDialog(MainActivity.this, "金币不足！", mBtnOkLackCoinsListener);
                break;
            case ID_DIALOG_WX_ADD_COIN:
                Util.showDialog(MainActivity.this, "厚着脸皮发给小胖子请求加" + getAddCoin()
                        + "个金币？", mBtnOkAddCoinListener);
            default:
                break;
        }
    }

}
