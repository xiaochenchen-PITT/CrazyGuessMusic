package chen.pitt.crazyguessmusic.myui;

import java.util.ArrayList;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;

import chen.pitt.crazyguessmusic.model.IWordButtonClickListener;
import chen.pitt.crazyguessmusic.model.WordButton;
import chen.pitt.crazyguessmusic.R;
import chen.pitt.crazyguessmusic.util.Util;

public class MyGridView extends GridView {
    public static final int COUNTS_WORD = 24;
    private ArrayList<WordButton> mWords = new ArrayList<WordButton>();
    private MyGridAdapter mAdapter;
    private Context mContext;
    private Animation mScaleAnimation;
    private IWordButtonClickListener mWordButtonListener;

    public MyGridView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mAdapter = new MyGridAdapter();
        mContext = context;
        setAdapter(mAdapter);
    }

    public void updateData(ArrayList<WordButton> arr) {
        mWords = arr;
        // set the new data
        setAdapter(mAdapter);
    }

    public class MyGridAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return mWords.size();
        }

        @Override
        public Object getItem(int pos) {
            return mWords.get(pos);
        }

        @Override
        public long getItemId(int pos) {
            return pos;
        }

        @Override
        public View getView(int pos, View v, ViewGroup p) {
            final WordButton holder;

            if (v == null) {
                v = Util.getView(mContext, R.layout.self_ui_gridview_item);
                holder = mWords.get(pos);

                // 加载动画
                mScaleAnimation = AnimationUtils.loadAnimation(mContext,
                        R.anim.scale);
                // 为动画设置延迟时间
                mScaleAnimation.setStartOffset(pos * 100);

                holder.setmIndex(pos);
                if (holder.getmViewButton() == null) {
                    holder.setmViewButton((Button) v
                            .findViewById(R.id.item_wordButton));
                    holder.getmViewButton().setOnClickListener(
                            new OnClickListener() {

                                @Override
                                public void onClick(View v) {
                                    mWordButtonListener
                                            .onWordButtonClick(holder);

                                }
                            });
                }

                setTag(holder); // ???
            } else {
                holder = (WordButton) getTag();
            }
            holder.getmViewButton().setText(holder.getmChar());

            // 播放动画
            v.startAnimation(mScaleAnimation);
            return v;
        }

    }

    /*
     * 注册监听器接口
     */
    public void registernWordButtonClick(IWordButtonClickListener listener) {
        mWordButtonListener = listener;
    }

}
