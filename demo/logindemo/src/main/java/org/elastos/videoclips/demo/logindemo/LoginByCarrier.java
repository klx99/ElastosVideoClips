package org.elastos.videoclips.demo.logindemo;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.util.Log;

import com.blikoon.qrcodescanner.QrCodeActivity;
import com.google.gson.Gson;

import org.elastos.carrier.AbstractCarrierHandler;
import org.elastos.carrier.Carrier;
import org.elastos.carrier.ConnectionStatus;
import org.elastos.carrier.FriendInfo;
import org.elastos.thirdparty.carrier.CarrierHelper;

public class LoginByCarrier {
    public static class AuthInfo {
        String method;
        String message;
        String mnemonic;
        String language;
    }

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

    public void start(MainActivity mainActivity) {
        mainActivity.requestPermissions(new String[]{Manifest.permission.CAMERA},
                (permissions, grantResults) -> {
                    for (int idx = 0; idx < permissions.length; idx++) {
                        if(permissions[idx].equals(Manifest.permission.CAMERA) == false) {
                            continue;
                        }

                        if (grantResults[idx] == PackageManager.PERMISSION_GRANTED) {
                            startQRScanActivity(mainActivity);
                            break;
                        }
                    }
                }
        );
    }

    private void startQRScanActivity(MainActivity mainActivity) {
        Intent intent = new Intent(mainActivity, QrCodeActivity.class);
        mainActivity.startActivityForResult(intent,
                (resultCode, data) -> {
                    Log.i(MainActivity.TAG,"onActivityResultListener.onActivityResult()");
                    if(data == null) {
                        return;
                    }
                    if(resultCode != MainActivity.RESULT_OK) {
                        Log.i(MainActivity.TAG,"COULD NOT GET A GOOD RESULT.");
                        String result = data.getStringExtra("com.blikoon.qrcodescanner.error_decoding_image");
                        if(result == null) {
                            return;
                        }

                        mainActivity.showError("QR Code could not be scanned.");
                        return;
                    }

                    //Getting the passed result
                    String result = data.getStringExtra("com.blikoon.qrcodescanner.got_qr_scan_relult");
                    Log.i(MainActivity.TAG,"Carrier address scan result:"+ result);
                    connectToDevice(mainActivity, result);
                }
        );
    }

    private void connectToDevice(MainActivity mainActivity, String carrierAddress) {
        mainActivity.showDialog("Status", "Connecting...");

        AbstractCarrierHandler listener = new AbstractCarrierHandler() {
            @Override
            public void onFriendAdded(Carrier carrier, FriendInfo info) {
                mainActivity.showDialog("Status", "Waiting device online...");
            }

            public void onFriendConnection(Carrier carrier, String friendId, ConnectionStatus status) {
                if(status == ConnectionStatus.Connected) {
                  sendWalletMessage(mainActivity, friendId);
                }
            }
        };
        CarrierHelper.setEventListener(listener);

        CarrierHelper.addFriend(carrierAddress);
    }

    private void sendWalletMessage(MainActivity mainActivity, String carrierUserId) {
        mainActivity.showDialog("Status", "Login...");

        AbstractCarrierHandler listener = new AbstractCarrierHandler() {
            @Override
            public void onFriendMessage(Carrier carrier, String from, byte[] message) {
                String msg = new String(message);
                AuthInfo authInfo = new Gson().fromJson(msg, AuthInfo.class);
                if(from.equals(carrierUserId)
                && authInfo.method.equals("account")
                && authInfo.message.equalsIgnoreCase("allowed")) {
                    mainActivity.showDialog("Status", "Success to login...");
                } else {
                    mainActivity.showDialog("Status", "Failed to login...");
                }
            }
        };
        CarrierHelper.setEventListener(listener);

        AuthInfo authInfo = mainActivity.getAuthInfo();
        authInfo.method = "account";
        String msg = new Gson().toJson(authInfo);
        CarrierHelper.sendMessage(msg);
    }
}
