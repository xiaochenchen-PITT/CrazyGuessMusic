package chen.pitt.crazyguessmusic.model;

import android.widget.Button;

public class WordButton {

    /**
     * @param word button
     * mIndex; the index of the character
     * mIsVisible; is the content visible
     * mChar; the content
     */
    private int mIndex;
    private boolean mIsVisible;
    private String mChar;
    private Button mViewButton;

    public WordButton() {
        mIsVisible = true;
        mChar = "";
    }

    public int getmIndex() {
        return mIndex;
    }

    public void setmIndex(int mIndex) {
        this.mIndex = mIndex;
    }

    public boolean ismIsVisible() {
        return mIsVisible;
    }

    public void setmIsVisible(boolean mIsVisible) {
        this.mIsVisible = mIsVisible;
    }

    public Button getmViewButton() {
        return mViewButton;
    }

    public void setmViewButton(Button mViewButton) {
        this.mViewButton = mViewButton;
    }

    public String getmChar() {
        return mChar;
    }

    public void setmChar(String mChar) {
        this.mChar = mChar;
    }


}
