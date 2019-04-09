package org.elastos.videoclips.demo.logindemo;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.elastos.sdk.wallet.BlockChainNode;
import org.elastos.sdk.wallet.Did;
import org.elastos.sdk.wallet.DidManager;
import org.elastos.sdk.wallet.HDWallet;
import org.elastos.sdk.wallet.Identity;
import org.elastos.sdk.wallet.IdentityManager;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class MainActivity extends AppCompatActivity {
    public interface onRequestPermissionsListener {
        void onRequestPermissionsResult(String[] permissions, int[] grantResults);
    }

    public interface onActivityResultListener {
        void onActivityResult(int resultCode, Intent data);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "MainActivity.onCreate() this=" + this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        EditText editTxtMessage = findViewById(R.id.edit_message);
        EditText editTxtMnemonic = findViewById(R.id.edit_mnemonic);
        EditText editTxtLanguage = findViewById(R.id.edit_language);
        Button btnByCarrier = findViewById(R.id.btn_by_carrier);
        editTxtMessage.setText("I'm Elastos user");
        editTxtMnemonic.setText(
                "hobby theme load okay\n"
              + "village inhale garlic box\n"
              + "cement draft patrol net");
        editTxtLanguage.setText("english");
        btnByCarrier.setOnClickListener(v -> {
            getAuthInfo();
            LoginByCarrier.getInstance(this).start(this);
        });

        Button btnByOther = findViewById(R.id.btn_by_other);
        btnByOther.setOnClickListener(v -> {
            Toast.makeText(this, "Not Implentment.", Toast.LENGTH_LONG).show();
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//        super.onActivityResult();
        Log.i(TAG, "MainActivity.onActivityResult() this=" + this + " requestCode=" + requestCode + " data=" + data);

        onActivityResultListener listener = mActivityResultListenerMap.remove(requestCode);
        if(listener == null) {
            return;
        }

        listener.onActivityResult(resultCode, data);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        onRequestPermissionsListener listener = mRequestPermissionsListenerMap.get(requestCode);
        mRequestPermissionsListenerMap.remove(requestCode);
        if(listener == null) {
            return;
        }

        listener.onRequestPermissionsResult(permissions, grantResults);
    }

    public void startActivityForResult(Intent intent, onActivityResultListener listener) {
        int index = mActivityResultListenerIndex.getAndIncrement();
        startActivityForResult(intent, index);
        mActivityResultListenerMap.put(index, listener);
    }

    public synchronized void requestPermissions(String[] permissions, onRequestPermissionsListener listener) {
        int index = mRequestPermissionsListenerIndex.getAndIncrement();
        ActivityCompat.requestPermissions(this, permissions, index);
        mRequestPermissionsListenerMap.put(index, listener);
    }

    public LoginByCarrier.AuthInfo getAuthInfo() {
        EditText editTxtMessage = findViewById(R.id.edit_message);
        EditText editTxtMnemonic = findViewById(R.id.edit_mnemonic);
        EditText editTxtLanguage = findViewById(R.id.edit_language);
        TextView txtDid = findViewById(R.id.edit_did);
        TextView txtAddress = findViewById(R.id.edit_address);

        LoginByCarrier.AuthInfo authInfo = new LoginByCarrier.AuthInfo();
        authInfo.message = editTxtMessage.getText().toString();
        authInfo.mnemonic = editTxtMnemonic.getText().toString();
        authInfo.language = editTxtLanguage.getText().toString();
        authInfo.mnemonic = authInfo.mnemonic.replace('\n', ' ');

        Identity identity = IdentityManager.createIdentity(this.getFilesDir().getAbsolutePath());
        String seed = IdentityManager.getSeed(authInfo.mnemonic, authInfo.language, "", "");
        DidManager didManager = identity.createDidManager(seed);
        Did did = didManager.createDid(0);
        String blockChainNodeURL = "https://api-wallet-ela-testnet.elastos.org";
        BlockChainNode node = new BlockChainNode(blockChainNodeURL);
        HDWallet wallet = identity.createSingleAddressWallet(seed, node);
        new Handler(Looper.getMainLooper()).post(() -> {
            txtDid.setText(did.getId());
            txtAddress.setText(wallet.getAddress(0, 0));
        });

        return authInfo;
    }

    public synchronized void showDialog(String title, String msg) {
        if(Looper.myLooper() != Looper.getMainLooper()) {
            Handler handler = new Handler(Looper.getMainLooper());
            handler.post(() -> {
                showDialog(title, msg);
            });
            return;
        }

        if(mAlertDialog != null) {
            mAlertDialog.dismiss();
            mAlertDialog = null;
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(title);
        builder.setMessage(msg);
        builder.setNegativeButton("OK", (dialog, which) -> {
            dialog.dismiss();
        });
        mAlertDialog = builder.create();
        mAlertDialog.show();
    }

    public void showError(String msg) {
        showDialog("Error", msg);
    }

    public static final String TAG = "elastos";

    private static ConcurrentHashMap<Integer, onRequestPermissionsListener> mRequestPermissionsListenerMap
            = new ConcurrentHashMap<>();
    private static AtomicInteger mRequestPermissionsListenerIndex
            = new AtomicInteger();

    private static ConcurrentHashMap<Integer, onActivityResultListener> mActivityResultListenerMap
            = new ConcurrentHashMap<>();
    private static AtomicInteger mActivityResultListenerIndex
            = new AtomicInteger();

    private AlertDialog mAlertDialog;
}
