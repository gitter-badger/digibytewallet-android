package io.digibyte.presenter.activities.introactivities;

import android.os.Bundle;
import androidx.annotation.Nullable;

import io.digibyte.presenter.activities.base.BRActivity;
import io.digibyte.tools.animation.BRAnimator;
import io.digibyte.wallet.BRWalletManager;

public class LauncherActivity extends BRActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        overridePendingTransition(0, 0);
        BRWalletManager wallet = BRWalletManager.getInstance();
        if (wallet.noWallet(this)) {
            IntroActivity.open(this);
        } else {
            BRAnimator.startBreadActivity(this, true);
        }
        finish();
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(0, 0);
    }
}
