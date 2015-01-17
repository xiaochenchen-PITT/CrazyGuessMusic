package chen.pitt.crazyguessmusic.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;

import chen.pitt.crazyguessmusic.R;
import chen.pitt.crazyguessmusic.data.Const;
import chen.pitt.crazyguessmusic.util.Util;

public class AllPassView extends Activity{
    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.all_pass_view);

        // 隐藏右上角金币
        FrameLayout view = (FrameLayout) findViewById(R.id.topbar_coin);
        view.setVisibility(View.INVISIBLE);

        handleRestart();
    }

    public void handleRestart() {
        ImageButton button = (ImageButton) findViewById(R.id.btn_restart);
        button.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                Util.saveData(AllPassView.this, "0", Const.TOTAL_COINS + "", "default_song_name", "default_song_path");
                Util.startActivity(AllPassView.this, MainActivity.class);
            }
        });
    }

}
