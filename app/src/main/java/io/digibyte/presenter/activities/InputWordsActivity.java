package io.digibyte.presenter.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import com.noahseidman.digiid.PhraseRestore;

import io.digibyte.R;
import io.digibyte.databinding.ActivityInputWordsBinding;
import io.digibyte.presenter.activities.intro.IntroActivity;
import io.digibyte.presenter.activities.util.BRActivity;
import io.digibyte.tools.animation.BRDialog;
import io.digibyte.tools.manager.BRSharedPrefs;
import io.digibyte.tools.security.AuthManager;
import io.digibyte.tools.security.PostAuth;
import io.digibyte.tools.security.SmartValidator;
import io.digibyte.tools.util.Utils;
import io.digibyte.wallet.BRWalletManager;

public class InputWordsActivity extends BRActivity {
    private static final String INPUT_WORDS_TYPE = "InputWordsActivity:Type";

    public enum Type {
        WIPE, RESET_PIN, RESTORE
    }

    public static void open(AppCompatActivity activity, Type type) {
        Intent intent = new Intent(activity, InputWordsActivity.class);
        intent.putExtra(INPUT_WORDS_TYPE, type);
        activity.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityInputWordsBinding binding = DataBindingUtil.setContentView(this,
                R.layout.activity_input_words);
        setupToolbar();
        switch(getType()) {
            case WIPE:
                setToolbarTitle(R.string.MenuViewController_recoverButton);
                binding.setDescription(getString(R.string.WipeWallet_instruction));
                break;
            case RESTORE:
                setToolbarTitle(R.string.RecoverWallet_header);
                binding.setDescription(getString(R.string.RecoverWallet_subheader));
                break;
            case RESET_PIN:
                setToolbarTitle(R.string.RecoverWallet_header_reset_pin);
                binding.setDescription(getString(R.string.RecoverWallet_subheader_reset_pin));
                break;
        }
        new Handler(Looper.getMainLooper()).postDelayed(
                () -> PhraseRestore.Companion.show(
                        InputWordsActivity.this,
                        this::processPhrase,
                        InputWordsActivity.this::finish),
                1000
        );
    }

    private Type getType() {
        return (Type) getIntent().getSerializableExtra(INPUT_WORDS_TYPE);
    }

    private void processPhrase(String phraseToCheck) {
        String cleanPhrase = SmartValidator.cleanPaperKey(this, phraseToCheck);
        if (SmartValidator.isPaperKeyValid(this, cleanPhrase)) {
            Utils.hideKeyboard(this);
            switch (getType()) {
                case WIPE:
                    BRDialog.showCustomDialog(InputWordsActivity.this,
                            getString(R.string.WipeWallet_alertTitle),
                            getString(R.string.WipeWallet_alertMessage),
                            getString(R.string.WipeWallet_wipe), getString(R.string.Button_cancel),
                            brDialogView -> {
                                brDialogView.dismissWithAnimation();
                                BRWalletManager m = BRWalletManager.getInstance();
                                m.wipeWalletButKeystore(this);
                                m.wipeKeyStore(this);
                                Intent intent = new Intent(this, IntroActivity.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK
                                        | Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
                            }, brDialogView -> brDialogView.dismissWithAnimation(), null, 0);
                    break;
                case RESET_PIN:
                    if (SmartValidator.isPaperKeyCorrect(cleanPhrase, this)) {
                        AuthManager.getInstance().setPinCode("", InputWordsActivity.this);
                        UpdatePinActivity.open(InputWordsActivity.this,
                                UpdatePinActivity.Mode.ENTER_NEW_PIN);
                    } else {
                        BRDialog.showCustomDialog(this, "",
                                getString(R.string.RecoverWallet_invalid),
                                getString(R.string.AccessibilityLabels_close), null,
                                brDialogView -> brDialogView.dismissWithAnimation(), null, null, 0);
                    }
                    break;
                case RESTORE:
                    BRWalletManager m = BRWalletManager.getInstance();
                    m.wipeWalletButKeystore(this);
                    m.wipeKeyStore(this);
                    PostAuth.instance.setPhraseForKeyStore(cleanPhrase);
                    BRSharedPrefs.putAllowSpend(this, false);
                    //if this screen is shown then we did not upgrade to the new app, we
                    // installed it
                    BRSharedPrefs.putGreetingsShown(this, true);
                    PostAuth.instance.onRecoverWalletAuth(this, false);
                    break;
            }
        } else {
            BRDialog.showCustomDialog(this, "",
                    getResources().getString(R.string.RecoverWallet_invalid),
                    getString(R.string.AccessibilityLabels_close), null,
                    brDialogView -> brDialogView.dismissWithAnimation(), null, null, 0);
        }
    }
}