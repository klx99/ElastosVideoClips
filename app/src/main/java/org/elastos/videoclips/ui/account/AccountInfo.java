package org.elastos.videoclips.ui.account;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

import org.elastos.sdk.wallet.Did;
import org.elastos.videoclips.did.DidInfo;
import org.elastos.videoclips.utils.Utils;

import java.util.Currency;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

// Demo only, need use KeyStore
public final class AccountInfo {
    private AccountInfo() {}

    public interface OnAccountListener {
        void onAccountChanged();
    }

    public static void setAccountListener(OnAccountListener listener) {
        sOnAccountListener = listener;
    }

    public static boolean save(String mnemonic, String language) {
        if(TextUtils.equals(sMnemonic, mnemonic)) {
            return true;
        }

        if(! DidInfo.checkMnemonic(mnemonic, language)) {
            return false;
        }

        saveLanguage(language);
        saveMnemonic(mnemonic);
        return true;
    }

    public static String loadMnemonic() {
        if (sMnemonic == null) {
            synchronized (AccountInfo.class) {
                if (sMnemonic == null) {
                    SharedPreferences sharedPrefs = getSharedPrefs();
                    sMnemonic = sharedPrefs.getString(PREFS_KEY_Mnemonic, null);
                }
                if (sMnemonic == null) {
                    String language = loadLanguage();
                    sMnemonic = DidInfo.generateMnemonic(language);
                    saveMnemonic(sMnemonic);
                }
            }
        }

        return sMnemonic;
    }

    public static String loadLanguage() {
        if (sLanguage == null) {
            synchronized (AccountInfo.class) {
                if (sLanguage == null) {
                    SharedPreferences sharedPrefs = getSharedPrefs();
                    sLanguage = sharedPrefs.getString(PREFS_KEY_Language, "english");
                }
            }
        }

        return sLanguage;
    }

    private static void saveMnemonic(String mnemonic) {
        synchronized (AccountInfo.class) {
            sMnemonic = mnemonic;
        }

        SharedPreferences sharedPrefs = getSharedPrefs();
        SharedPreferences.Editor editor = sharedPrefs.edit();
        editor.putString(PREFS_KEY_Mnemonic, mnemonic);
        editor.apply();

        if(sOnAccountListener != null) {
            sOnAccountListener.onAccountChanged();
        }
    }

    private static void saveLanguage(String language) {
        synchronized (AccountInfo.class) {
            sLanguage = language;
        }

        SharedPreferences sharedPrefs = getSharedPrefs();
        SharedPreferences.Editor editor = sharedPrefs.edit();
        editor.putString(PREFS_KEY_Language, language);
        editor.apply();
    }


    private static SharedPreferences getSharedPrefs() {
        return Utils.getAppContext().getSharedPreferences("account", Context.MODE_PRIVATE);
    }

    private static String sMnemonic;
    private static String sLanguage;
    private static OnAccountListener sOnAccountListener;
    private static final String PREFS_KEY_Mnemonic = "mnemonic";
    private static final String PREFS_KEY_Language = "language";
}
