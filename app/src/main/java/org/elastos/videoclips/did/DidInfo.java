package org.elastos.videoclips.did;

import org.elastos.sdk.wallet.BlockChainNode;
import org.elastos.sdk.wallet.Did;
import org.elastos.sdk.wallet.DidManager;
import org.elastos.sdk.wallet.HDWallet;
import org.elastos.sdk.wallet.Identity;
import org.elastos.sdk.wallet.IdentityManager;
import org.elastos.videoclips.utils.Utils;

public class DidInfo {
    public enum CoinType {
        ELA, DID
    }

    public static DidInfo getInstance() {
        if (mDidInfo == null) {
            synchronized (DidInfo.class) {
                if (mDidInfo == null) {
                    mDidInfo = new DidInfo();
                }
            }
        }
        return mDidInfo;
    }
    private static DidInfo mDidInfo = null;
    private DidInfo() {
        mIdentity = IdentityManager.createIdentity(Utils.getAppContext().getFilesDir().getAbsolutePath());

        mSyncThread = new Thread(() -> {
            while (true) {
                if (mDid != null) {
                    mDid.syncInfo();
                }


                for(CoinType coinType: CoinType.values()) {
                    if (mWalletArray[coinType.ordinal()] != null) {
                        mWalletArray[coinType.ordinal()].syncHistory();
                    }
                }

                try {
                    Thread.sleep(15 * 1000);
                } catch (InterruptedException e) {
                }
            }
        });
        mSyncThread.start();
    }

    public static boolean checkMnemonic(String mnemonic, String language) {
        try {
            String seed = IdentityManager.getSeed(mnemonic, language, "", "");
            if(seed != null && seed.isEmpty() == false) {
                return true;
            }
        } catch (Exception e) {
        }

        return false;
    }

    public synchronized String getDidName() {
        if(mDid == null) {
            String seed = getSeed();
            DidManager didManager = mIdentity.createDidManager(seed);
            mDid = didManager.createDid(0);
        }

        return mDid.getId();
    }

    public String getWalletAddress(CoinType coinType) {
        HDWallet hdWallet = getWallet(coinType);
        return hdWallet.getAddress(0, 0);
    }

    public synchronized String getWalletBalance(CoinType coinType) {
        final String coinUnits[] = {
                "ELA", "Points"
        };

        HDWallet hdWallet = getWallet(coinType);
        long balance = hdWallet.getBalance();

        double elaBaleance = ((double)balance / OneELA);

        String unit = coinUnits[coinType.ordinal()];
        if(balance < 1) {
            unit = unit.replaceAll("s$", "");
        }

        return String.format("%.5f %s", elaBaleance, unit);
    }

    private synchronized HDWallet getWallet(CoinType coinType) {
        int walletIdx = coinType.ordinal();
        if(mWalletArray[walletIdx] == null) {
            String seed = getSeed();
            BlockChainNode node = new BlockChainNode(BlockChainNodeURL[walletIdx]);
            mWalletArray[walletIdx] = mIdentity.createSingleAddressWallet(seed, node);
        }

        return mWalletArray[walletIdx];
    }

    private synchronized String getSeed() {
        final String language = "english";
        if(mMnemonic == null) {
            mMnemonic = IdentityManager.getMnemonic(language, "");
        }

        String seed = IdentityManager.getSeed(mMnemonic, language, "", "");

        return seed;
    }

    private Thread mSyncThread;
    private String mMnemonic;
    private Identity mIdentity;
    private Did mDid;
    private HDWallet[] mWalletArray = new HDWallet[CoinType.values().length];

    private final long OneELA = 100000000; // 1 ELA = 100000000 SELA
    private final String[] BlockChainNodeURL = new String[] {
            "https://api-wallet-ela-testnet.elastos.org",
            "https://api-wallet-did-testnet.elastos.org"
    };
}
