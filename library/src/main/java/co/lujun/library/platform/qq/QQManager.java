package co.lujun.library.platform.qq;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import com.tencent.connect.UserInfo;
import com.tencent.tauth.Tencent;

import co.lujun.library.TPManager;
import co.lujun.library.bean.Config;
import co.lujun.library.bean.QQShareContent;
import co.lujun.library.listener.StateListener;
import co.lujun.library.platform.qq.listener.BaseUiListener;

/**
 * Created by lujun on 2015/9/6.
 */
public class QQManager {

    private Context mContext;
    private Intent mIntent;
    private StateListener<String> mListener;
    private ReceiveBroadCast mReceiveBroadCast;

    public QQManager(Context context){
        mContext = context;
        mIntent = new Intent(mContext, AssistActivity.class);
        mIntent.putExtra(Config.KEY_OF_APPID, TPManager.getInstance().getQQAppId());
        mIntent.putExtra(Config.KEY_OF_APPSECRET, TPManager.getInstance().getQQAppSecret());
        mReceiveBroadCast = new ReceiveBroadCast();
    }

    /**
     * QQ三方登录
     */
    public void onLoginWithQQ(){
        mIntent.putExtra(Config.KEY_OF_TYPE, Config.LOGIN_TYPE);
        mContext.registerReceiver(mReceiveBroadCast, new IntentFilter(Config.KEY_OF_QQ_BCR_ACTION));
        mContext.startActivity(mIntent);
    }

    /**
     * 取消QQ授权
     */
    public void onLoginOut(){
        //TODO 取消授权

    }

    /**
     * QQ分享
     */
    public void share(QQShareContent content){
        mIntent.putExtra(Config.KEY_OF_TYPE, Config.SHARE_TYPE);
        mIntent.putExtra(Config.KEY_OF_BUNDLE, content.getBundle());
        mContext.registerReceiver(mReceiveBroadCast, new IntentFilter(Config.KEY_OF_QQ_BCR_ACTION));
        mContext.startActivity(mIntent);
    }

    /**
     * 设置登录callback
     * @param listener
     */
    public void setListener(StateListener listener){
        this.mListener = listener;
    }

    /**
     * 获取用户信息
     * @param accessToken
     * @param expires_in
     * @param openId
     */
    private void getUserInfo(String accessToken, String expires_in, String openId){
        Tencent tencent = Tencent.createInstance(TPManager.getInstance().getQQAppId(), mContext);
        tencent.setAccessToken(accessToken, expires_in);
        tencent.setOpenId(openId);
        UserInfo userInfo = new UserInfo(mContext, tencent.getQQToken());
        BaseUiListener mUserInfoListener = new BaseUiListener();
        mUserInfoListener.setListener(new StateListener<Object>() {
            @Override
            public void onComplete(Object o) {
                mListener.onComplete(o.toString());
            }

            @Override
            public void onError(String s) {
                mListener.onError(s);
            }

            @Override
            public void onCancel() {
                mListener.onCancel();
            }
        });
        userInfo.getUserInfo(mUserInfoListener);
    }

    /**
     * 接受QQ授权回调得到的用户信息
     */
    private class ReceiveBroadCast extends BroadcastReceiver{

        @Override
        public void onReceive(Context context, Intent intent) {
            if (mListener != null){
                if (intent.getIntExtra(Config.KEY_OF_TYPE, 0x1000) == Config.LOGIN_TYPE){
                    getUserInfo(
                            intent.getStringExtra(Config.KEY_OF_ACCESS_TOKEN),
                            intent.getStringExtra(Config.KEY_OF_EXPIRES_IN),
                            intent.getStringExtra(Config.KEY_OF_OPEN_ID)
                    );
                }else {
                    mListener.onComplete(intent.getStringExtra(Config.KEY_OF_QQ_BCR));
                }
            }
            mContext.unregisterReceiver(mReceiveBroadCast);
        }
    }
}
