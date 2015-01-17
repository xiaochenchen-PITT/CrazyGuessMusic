package chen.pitt.crazyguessmusic.wxapi;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.tencent.mm.sdk.openapi.BaseReq;
import com.tencent.mm.sdk.openapi.BaseResp;
import com.tencent.mm.sdk.openapi.ConstantsAPI;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.IWXAPIEventHandler;
import com.tencent.mm.sdk.openapi.WXAPIFactory;

import chen.pitt.crazyguessmusic.data.Const;
import chen.pitt.crazyguessmusic.ui.MainActivity;

public class WXEntryActivity extends Activity implements IWXAPIEventHandler {
    private IWXAPI mApi;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("微信", "微信响应页面");
        mApi = WXAPIFactory.createWXAPI(this, Const.APP_ID);
        mApi.registerApp(Const.APP_ID);
        mApi.handleIntent(getIntent(), this);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        setIntent(intent);
        mApi.handleIntent(intent, this);
    }

    public void onReq(BaseReq arg0) {
        Log.d("微信", "BaseReq:" + arg0.getType());
        switch (arg0.getType()) {
            case ConstantsAPI.COMMAND_GETMESSAGE_FROM_WX:
                Log.d("微信", "ConstantsAPI.COMMAND_GETMESSAGE_FROM_WX");
                break;
            case ConstantsAPI.COMMAND_SHOWMESSAGE_FROM_WX:
                Log.d("微信", "ConstantsAPI.COMMAND_SHOWMESSAGE_FROM_WX");
                break;
            default:
                break;
        }
        Toast.makeText(this, "onReq", Toast.LENGTH_LONG).show();
        finish();
    }

    public void onResp(BaseResp arg0) {
        Log.d("微信", "BaseResp:" + arg0.errCode);
        String result = "";
        switch (arg0.errCode) {
            case BaseResp.ErrCode.ERR_OK:
                result = "亲，分享成功了";
                break;
            case BaseResp.ErrCode.ERR_USER_CANCEL:
                result = "取消分享";
                break;
            case BaseResp.ErrCode.ERR_AUTH_DENIED:
                result = "认证失败";
                break;
            default:
                result = "errcode_unknown";
                break;
        }

        Toast.makeText(this, result, Toast.LENGTH_LONG).show();
        finish();
    }

}