package io.digibyte.presenter.activities;

import android.animation.ObjectAnimator;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityOptionsCompat;
import androidx.databinding.DataBindingUtil;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.load.resource.bitmap.DownsampleStrategy;
import com.bumptech.glide.load.resource.gif.GifDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.DrawableImageViewTarget;
import com.bumptech.glide.request.target.ImageViewTarget;
import com.bumptech.glide.request.target.Target;

import io.digibyte.R;
import io.digibyte.databinding.ActivityAssetImageBinding;
import io.digibyte.databinding.ActivityQrCodeBinding;
import io.digibyte.presenter.activities.base.BRActivity;
import io.digibyte.presenter.activities.callbacks.ActivityAssetImageCallback;
import io.digibyte.presenter.activities.callbacks.ActivityQRCodeCallback;
import io.digibyte.tools.animation.BRAnimator;
import io.digibyte.tools.qrcode.QRUtils;

public class AssetImageActivity extends BRActivity {

    private static final String ASSET_IMAGE_URL = "AssetImageActivity:AssetImageUrl";
    private ActivityAssetImageBinding binding;

    private ActivityAssetImageCallback callback = this::remove;

    public static void show(AppCompatActivity activity, View view, String assetImageUrl) {
        Intent intent = new Intent(activity, AssetImageActivity.class);
        intent.putExtra(ASSET_IMAGE_URL, assetImageUrl);
        ActivityOptionsCompat options = ActivityOptionsCompat.
                makeSceneTransitionAnimation(activity, view, "asset_image");
        //Shared object behaves strangely with the gif unfortunately
        activity.startActivity(intent/*, options.toBundle()*/);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        supportPostponeEnterTransition();
        binding = DataBindingUtil.setContentView(this, R.layout.activity_asset_image);
        binding.setCallback(callback);
        String assetImageUrl = getIntent().getStringExtra(ASSET_IMAGE_URL);
        Glide.with(this).load(assetImageUrl).diskCacheStrategy(DiskCacheStrategy.ALL).listener(new RequestListener<Drawable>() {
            @Override
            public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                finish();
                return false;
            }

            @Override
            public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                supportStartPostponedEnterTransition();
                new Handler().postDelayed(() -> {
                    ObjectAnimator colorFade = BRAnimator.animateBackgroundDim(binding.background, false, null);
                    colorFade.setStartDelay(350);
                    colorFade.setDuration(500);
                    colorFade.start();
                }, 250);
                return false;
            }
        }).into(binding.image);
    }

    @Override
    public void onBackPressed() {
        remove();
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.enter_from_left, R.anim.exit_to_left);
    }

    private void remove() {
        BRAnimator.animateBackgroundDim(binding.background, true, this::finish).start();
    }
}
