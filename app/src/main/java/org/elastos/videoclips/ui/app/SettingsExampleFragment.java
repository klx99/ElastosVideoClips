/*
 * Copyright (C) 2015 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package org.elastos.videoclips.ui.app;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.support.v14.preference.PreferenceFragment;

import org.elastos.videoclips.R;
import org.elastos.videoclips.did.DidInfo;
import org.elastos.videoclips.ui.account.LoginByCarrier;
import org.elastos.videoclips.utils.Utils;

import android.support.v17.preference.LeanbackPreferenceFragment;
import android.support.v17.preference.LeanbackSettingsFragment;
import android.support.v7.preference.DialogPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceManager;
import android.support.v7.preference.PreferenceScreen;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.Toast;

import java.util.Arrays;
import java.util.Stack;

public class SettingsExampleFragment extends LeanbackSettingsFragment implements DialogPreference.TargetFragment {

    private final Stack<PreferenceFragment> fragments = new Stack<>();
    private FrameLayout mContentView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final ViewGroup root = (ViewGroup) super.onCreateView(inflater, container, savedInstanceState);

        mContentView = new FrameLayout(this.getActivity());
        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(
                this.getResources().getDisplayMetrics().widthPixels - this.getResources().getDimensionPixelSize(R.dimen.lb_settings_pane_width),
                FrameLayout.LayoutParams.MATCH_PARENT
        );
//        mContentView.setBackgroundColor(Color.BLUE);
        root.addView(mContentView, 0, layoutParams);

        return root;
    }

    @Override
    public void onPreferenceStartInitialScreen() {
        startPreferenceFragment(buildPreferenceFragment(R.xml.setting_prefs, null));
    }

    @Override
    public boolean onPreferenceStartFragment(PreferenceFragment preferenceFragment,
                                             Preference preference) {
        return false;
    }

    @Override
    public boolean onPreferenceStartScreen(PreferenceFragment preferenceFragment,
                                           PreferenceScreen preferenceScreen) {
        PreferenceFragment frag = buildPreferenceFragment(R.xml.setting_prefs, preferenceScreen.getKey());
        startPreferenceFragment(frag);
        return true;
    }

    @Override
    public Preference findPreference(CharSequence prefKey) {
        return ((PreferenceFragment) fragments.peek()).findPreference(prefKey);
    }

    private PreferenceFragment buildPreferenceFragment(int preferenceResId, String root) {
        PreferenceFragment fragment = new PrefFragment();
        Bundle args = new Bundle();
        args.putInt("preferenceResource", preferenceResId);
        args.putString("root", root);
        fragment.setArguments(args);
        return fragment;
    }

    private class PrefFragment extends LeanbackPreferenceFragment {
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            final ViewGroup root = (ViewGroup) super.onCreateView(inflater, container, savedInstanceState);
            root.getViewTreeObserver().addOnGlobalFocusChangeListener((oldFocus, newFocus) -> {
                LoginByCarrier.getInstance(this.getActivity()).hideLoginView();
                mContentView.removeAllViews();
            });

            return root;
        }

        @Override
        public void onCreatePreferences(Bundle bundle, String s) {
            String root = getArguments().getString("root", null);
            int prefResId = getArguments().getInt("preferenceResource");
            if (root == null) {
                addPreferencesFromResource(prefResId);
            } else {
                setPreferencesFromResource(prefResId, root);
            }

            updateChangedValues(this.getPreferenceManager());

            DidInfo.getInstance().setDidListener(() -> {
                for(PreferenceFragment prefFragment: fragments) {
                    PreferenceManager rootPrefMgr = prefFragment.getPreferenceManager();
                    updateChangedValues(rootPrefMgr);
                }
            });
        }

        @Override
        public boolean onPreferenceTreeClick(Preference preference) {
            final String[] keys = {"prefs_wifi_connect_wps", "prefs_date", "prefs_time",
                    "prefs_date_time_use_timezone", "app_banner_sample_app", "pref_force_stop",
                    "pref_uninstall", "pref_more_info"};
            if (Arrays.asList(keys).contains(preference.getKey())) {
                Toast.makeText(getActivity(), "Implement your own action handler.", Toast.LENGTH_SHORT).show();
                return true;
            }
            switch (preference.getKey()) {
                case "prefs_key_did_name": {
                    DidInfo didInfo = DidInfo.getInstance();
                    String didName = didInfo.getDidName();
                    Utils.showQRCodeView(mContentView, "DID", didName);
                    break;
                }
                case "prefs_key_did_wallet_address": {
                    DidInfo didInfo = DidInfo.getInstance();
                    String walletAddress = didInfo.getWalletAddress(DidInfo.CoinType.ELA);
                    Utils.showQRCodeView(mContentView, "Wallet Address", walletAddress);
                    break;
                }
                case "prefs_key_did_account_by_carrier": {
                    LoginByCarrier.getInstance(this.getActivity()).showLoginView(mContentView);
                    break;
                }

                default:
                    break;
            }

            return super.onPreferenceTreeClick(preference);
        }

        @Override
        public void onAttach(Activity activity) {
            fragments.push(this);
            super.onAttach(activity);
        }

        @Override
        public void onDetach() {
            fragments.pop();
            super.onDetach();
        }

        private void updateChangedValues(PreferenceManager prefMgr) {
            DidInfo didInfo = DidInfo.getInstance();

            Preference prefDidName = prefMgr.findPreference("prefs_key_did_name");
            if(prefDidName != null) {
                prefDidName.setSummary(didInfo.getDidName());
            }

            Preference prefWalletAddr = getPreferenceManager().findPreference("prefs_key_did_wallet_address");
            if(prefWalletAddr != null) {
                DidInfo.CoinType coinType = DidInfo.CoinType.ELA;
                if(prefWalletAddr.getParent().getKey().equals("prefs_key_did_didwallet")) {
                    coinType = DidInfo.CoinType.DID;
                }
                prefWalletAddr.setSummary(didInfo.getWalletAddress(coinType));
//                prefWalletAddr.setSummary(didInfo.getDidName());

                Preference prefWalletBalance = getPreferenceManager().findPreference("prefs_key_did_wallet_balance");
                prefWalletBalance.setSummary(didInfo.getWalletBalance(coinType));
            }
        }
    }
}
