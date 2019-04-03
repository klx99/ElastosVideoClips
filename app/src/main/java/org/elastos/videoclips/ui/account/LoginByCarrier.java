package org.elastos.videoclips.ui.account;

import android.app.AlertDialog;
import android.content.Context;
import android.view.ViewGroup;

import com.google.gson.Gson;

import org.elastos.carrier.AbstractCarrierHandler;
import org.elastos.carrier.Carrier;
import org.elastos.carrier.ConnectionStatus;
import org.elastos.thirdparty.carrier.CarrierHelper;
import org.elastos.videoclips.did.DidInfo;
import org.elastos.videoclips.ui.app.QRCodeView;
import org.elastos.videoclips.utils.Utils;

public final class LoginByCarrier {
    private LoginByCarrier() {}

    public static void showLoginView(ViewGroup contentView) {
        mContext = contentView.getContext();

        CarrierHelper.setEventListener(eventListener);
        String carrierAddress = CarrierHelper.getAddress();
        mQRCodeView = Utils.showQRCodeView(contentView, "Carrier Address", carrierAddress);
        mQRCodeView.setStatus("Loading...");
    }

    public static void hideLoginView() {
        CarrierHelper.setEventListener(null);
        mQRCodeView = null;
    }

    private static AbstractCarrierHandler eventListener = new AbstractCarrierHandler() {
        @Override
        public void onConnection(Carrier carrier, ConnectionStatus status) {
            if(status == ConnectionStatus.Connected) {
                mQRCodeView.setStatus("Please scan this QRCode.");
            } else {
                mQRCodeView.setStatus("Device is offline.");
            }
        }

        @Override
        public void onFriendConnection(Carrier carrier, String friendId, ConnectionStatus status) {
            if(status == ConnectionStatus.Connected) {
                mQRCodeView.setStatus("Connecting with wallet...");
            } else {
                mQRCodeView.setStatus("Wallet is offline.");
            }
        }

        @Override
        public void onFriendMessage(Carrier carrier, String from, byte[] message) {
            AuthInfo authInfo;
            try {
                authInfo = new Gson().fromJson(new String(message), AuthInfo.class);
            } catch (Exception e) {
                mQRCodeView.setStatus("Failed to recognize wallet.");
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
                            AccountInfo.saveMnemonic(authInfo.mnemonic, authInfo.language);
                            dialog.dismiss();
                        }
                    ).setNegativeButton("Disallow",
                        (dialog, which) -> {
                            dialog.dismiss();
                        }
                    );

        }
    };

    private class AuthInfo {
        String message;
        String mnemonic;
        String language;
    }

    private static QRCodeView mQRCodeView;
    private static Context mContext;
}
