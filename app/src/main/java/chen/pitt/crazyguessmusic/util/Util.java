package chen.pitt.crazyguessmusic.util;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

import chen.pitt.crazyguessmusic.R;
import chen.pitt.crazyguessmusic.data.Const;
import chen.pitt.crazyguessmusic.model.IAlertDialogButtonListener;

public class Util {

    private static AlertDialog mAlertDialog;


    public static View getView(Context context, int layoutId) {
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View layout = inflater.inflate(layoutId, null);
        return layout;
    }

    public static Drawable reSize(Context context, int resId, int newWidth,
                                  int newHeight) {

        // load the original drawable as a Bitmap
        Bitmap original = BitmapFactory.decodeResource(context.getResources(),
                resId);
        int width = original.getWidth();
        int height = original.getHeight();
        Log.d("resize image:", width + "," + height);

        // calculate the scale
        float scaleWidth = (float) newWidth / (float) width;
        float scaleHeight = (float) newHeight / (float) height;

        // create a matrix for manipulation
        Matrix matrix = new Matrix();
        // resize the Bitmap
        matrix.postScale(scaleWidth, scaleHeight);

        // if you want to rotate the Bitmap
        // matrix.postRotate(45);

        // recreate the new Bitmap
        Bitmap resizedBitmap = Bitmap.createBitmap(original, 0, 0, width,
                height, matrix, true);
        Log.d("resize image:",
                resizedBitmap.getWidth() + "," + resizedBitmap.getHeight());

        // make a Drawable from Bitmap to allow to set the Bitmap
        // to the ImageView, ImageButton or what ever
        return new BitmapDrawable(context.getResources(), resizedBitmap);
    }

    /**
     * 显示自定义对话框
     *
     * @param context
     * @param message
     * @param listener
     */
    public static void showDialog(final Context context, String message,
                                  final IAlertDialogButtonListener listener) {

        // 自定义的view
        View dialogView = null;
        AlertDialog.Builder builder = null;

        // 初始化
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            builder = new AlertDialog.Builder(context);
        } else {
            builder = new AlertDialog.Builder(context,
                    R.style.Theme_Transparent);
        }
        dialogView = getView(context, R.layout.dialog_view);

        ImageButton btnOkView = (ImageButton) dialogView
                .findViewById(R.id.btn_dialog_ok);
        ImageButton btnCancelView = (ImageButton) dialogView
                .findViewById(R.id.btn_dialog_cancel);
        TextView txtMessageView = (TextView) dialogView
                .findViewById(R.id.text_dialog_message);

        txtMessageView.setText(message);

        btnOkView.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // 关闭对话框
                if (mAlertDialog != null) {
                    mAlertDialog.cancel();
                }
                // 事件回调
                if (listener != null) {
                    listener.onDialogButtonClick();
                }

            }
        });

        btnCancelView.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // 关闭对话框
                if (mAlertDialog != null) {
                    mAlertDialog.cancel();
                }
            }
        });

        // 为dialog设置view setView
        builder.setView(dialogView);

        // 创建 显示该对话框
        mAlertDialog = builder.create();
        mAlertDialog.show();
    }


    /**
     * 数据保存
     *
     * @param context
     * @param stageIndex
     * @param coins
     */
    public static void saveData(Context context, String stageIndex, String coins, String currentSongName, String currentSongPath) {

        FileOutputStream fos = null;
        try {
            fos = context.openFileOutput(Const.FILE_NAME_SAVE_DATA, Context.MODE_PRIVATE);
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(fos, "GB2312");
            outputStreamWriter.write(stageIndex + "\n");
            outputStreamWriter.write(coins + "\n");
            outputStreamWriter.write(currentSongName + "\n");
            outputStreamWriter.write(currentSongPath + "\n");
            outputStreamWriter.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (fos != null) {
                    fos.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 数据读取
     *
     * @param context
     * @return [stage, coins, currentSongName, currentSongPath]
     */
    public static String[] readData(Context context) {
        FileInputStream fis = null;
        String[] datas = {String.valueOf(0), String.valueOf(Const.TOTAL_COINS), "default_song_name", "default_song_path"};

        try {
            fis = context.openFileInput(Const.FILE_NAME_SAVE_DATA);
            if (fis != null) {
                InputStreamReader inputStreamReader = new InputStreamReader(fis, "GB2312");
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                datas[Const.INDEX_LOAD_DATA_STAGE] = bufferedReader.readLine();
                datas[Const.INDEX_LOAD_DATA_COINS] = bufferedReader.readLine();
                datas[Const.INDEX_LOAD_DATA_SONGNAME] = bufferedReader.readLine();
                datas[Const.INDEX_LOAD_DATA_SONGPATH] = bufferedReader.readLine();
                fis.close();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (fis != null) {
                    fis.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return datas;
    }

    public static void startActivity(Context context, Class desti) {
        Intent intent = new Intent();
        intent.setClass(context, desti);
        context.startActivity(intent);

        //关闭当前的activity
        ((Activity)context).finish();
    }


}
