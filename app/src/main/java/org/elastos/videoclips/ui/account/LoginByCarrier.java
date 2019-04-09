package org.elastos.videoclips.ui.account;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.google.gson.Gson;

import org.elastos.carrier.AbstractCarrierHandler;
import org.elastos.carrier.Carrier;
import org.elastos.carrier.ConnectionStatus;
import org.elastos.carrier.FriendInfo;
import org.elastos.thirdparty.carrier.CarrierHelper;
import org.elastos.videoclips.R;
import org.elastos.videoclips.did.DidInfo;
import org.elastos.videoclips.ui.app.QRCodeView;
import org.elastos.videoclips.utils.Utils;

public final class LoginByCarrier {
    public static LoginByCarrier getInstance(Context context) {
        if (mLoginByCarrier == null) {
            synchronized (LoginByCarrier.class) {
                if (mLoginByCarrier == null) {
                    mLoginByCarrier = new LoginByCarrier(context);
                }
            }
        }
        return mLoginByCarrier;
    }
    private static LoginByCarrier mLoginByCarrier = null;
    private LoginByCarrier(Context context) {
        CarrierHelper.startCarrier(context);
    }

    public void showLoginView(ViewGroup contentView) {
        mContext = contentView.getContext();

        FrameLayout layout = new FrameLayout(mContext);
        contentView.addView(layout);

        String carrierAddress = CarrierHelper.getAddress();
        mQRCodeView = Utils.showQRCodeView(layout, "Carrier Address", carrierAddress);
        mQRCodeView.setStatus("Loading...");

        mLoadingView = LayoutInflater.from(mContext).inflate(R.layout.loading, null);
        layout.addView(mLoadingView);

        CarrierHelper.setEventListener(eventListener);
    }

    public void hideLoginView() {
        CarrierHelper.setEventListener(null);
        mQRCodeView = null;
        mLoadingView = null;
    }

    private AbstractCarrierHandler eventListener = new AbstractCarrierHandler() {
        @Override
        public void onConnection(Carrier carrier, ConnectionStatus status) {
            Log.i(Utils.TAG, "CarrierHandler.onConnection() status=" + status);
            if(status == ConnectionStatus.Connected) {
                mQRCodeView.setStatus("Please scan this QRCode.");
                mLoadingView.setVisibility(View.GONE);
            } else {
                mQRCodeView.setStatus("Device is offline.");
            }
        }

        public void onFriendAdded(Carrier carrier, FriendInfo info) {
            mQRCodeView.setStatus("Success to scan...");
        }

        @Override
        public void onFriendConnection(Carrier carrier, String friendId, ConnectionStatus status) {
            Log.i(Utils.TAG, "CarrierHandler.onFriendConnection() friendId=" + friendId + " status=" + status);
            if(status == ConnectionStatus.Connected) {
                mQRCodeView.setStatus("Connecting with wallet...");
            } else {
                mQRCodeView.setStatus("Wallet is offline.");
            }
        }

        @Override
        public void onFriendMessage(Carrier carrier, String from, byte[] message) {
            Log.i(Utils.TAG, "CarrierHandler.onConnection() message=" + new String(message));
            AuthInfo authInfo;
            try {
                authInfo = new Gson().fromJson(new String(message), AuthInfo.class);
            } catch (Exception e) {
                mQRCodeView.setStatus("Failed to recognize wallet.");
                return;
            }

            if(! authInfo.method.equals("account")) {
                return;
            }

            boolean validMnemonic = DidInfo.checkMnemonic(authInfo.mnemonic, authInfo.language);
            if(validMnemonic == false) {
                mQRCodeView.setStatus("Invalid mnemonic.");
                return;
            }

            AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(mContext);
            dialogBuilder.setTitle("Change Account")
                    .setMessage(authInfo.message)
                    .setPositiveButton("Allow",
                        (dialog, which) -> {
                            dialog.dismiss();

                            boolean ret = AccountInfo.save(authInfo.mnemonic, authInfo.language);
                            if(ret) {
                                mQRCodeView.setStatus("Success to change account.");
                                authInfo.message = "allowed";
                            } else {
                                mQRCodeView.setStatus("Invalid auth info.");
                                authInfo.message = "failed";
                            }
                            authInfo.mnemonic = null;
                            authInfo.language = null;
                            String msg = new Gson().toJson(authInfo);
                            CarrierHelper.sendMessage(msg);
                        }
                    ).setNegativeButton("Disallow",
                        (dialog, which) -> {
                            dialog.dismiss();

                            mQRCodeView.setStatus("Disallow to change account.");
                            authInfo.message = "disallowed";
                            authInfo.mnemonic = null;
                            authInfo.language = null;
                            String msg = new Gson().toJson(authInfo);
                            CarrierHelper.sendMessage(msg);
                        }
                    );
            new Handler(Looper.getMainLooper()).post(() -> {
                AlertDialog alertDialog = dialogBuilder.create();
                alertDialog.show();
            });
        }
    };

    private class AuthInfo {
        String method;
        String message;
        String mnemonic;
        String language;
    }

    private QRCodeView mQRCodeView;
    private View mLoadingView;
    private Context mContext;
}
