package org.elastos.videoclips.ui.account;

import android.content.Context;
import android.content.SharedPreferences;

import org.elastos.videoclips.utils.Utils;

// Demo only, need use KeyStore
public final class AccountInfo {
    private AccountInfo() {}

    public static void saveMnemonic(String mnemonic, String language) {
        synchronized (AccountInfo.class) {
            sMnemonic = mnemonic;
            sLanguage = language;
        }

        SharedPreferences sharedPrefs = getSharedPrefs();
        SharedPreferences.Editor editor = sharedPrefs.edit();
        editor.putString(PREFS_KEY_Mnemonic, mnemonic);
        editor.putString(PREFS_KEY_Language, language);
        editor.apply();
    }

    public static String loadMnemonic() {
        if (sMnemonic == null) {
            synchronized (AccountInfo.class) {
                if (sMnemonic == null) {
                    SharedPreferences sharedPrefs = getSharedPrefs();
                    sMnemonic = sharedPrefs.getString(PREFS_KEY_Mnemonic, "");
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

    private static SharedPreferences getSharedPrefs() {
        return Utils.getAppContext().getSharedPreferences("account", Context.MODE_PRIVATE);
    }

    private static String sMnemonic;
    private static String sLanguage;
    private static final String PREFS_KEY_Mnemonic = "mnemonic";
    private static final String PREFS_KEY_Language = "language";
}
