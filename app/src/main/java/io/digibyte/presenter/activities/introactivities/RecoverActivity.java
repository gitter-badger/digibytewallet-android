package io.digibyte.presenter.activities.introactivities;

import androidx.databinding.DataBindingUtil;
import android.os.Bundle;

import io.digibyte.R;
import io.digibyte.databinding.ActivityIntroRecoverBinding;
import io.digibyte.presenter.activities.InputWordsActivity;
import io.digibyte.presenter.activities.callbacks.ActivityRecoverCallback;
import io.digibyte.presenter.activities.base.BRActivity;

public class RecoverActivity extends BRActivity {

    ActivityRecoverCallback callback = () ->
            InputWordsActivity.open(RecoverActivity.this, InputWordsActivity.Type.RESTORE);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityIntroRecoverBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_intro_recover);
        binding.setCallback(callback);
        setupToolbar();
        setToolbarTitle(R.string.RecoverWallet_header);
    }
}