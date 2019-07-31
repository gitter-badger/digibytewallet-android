package io.digibyte.presenter.activities.models;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Base64;
import android.view.MenuInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.view.ContextThemeWrapper;
import androidx.appcompat.widget.PopupMenu;
import androidx.databinding.BaseObservable;
import androidx.databinding.Bindable;
import androidx.databinding.BindingAdapter;

import com.bumptech.glide.Glide;
import com.squareup.picasso.Picasso;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import io.digibyte.BR;
import io.digibyte.DigiByte;
import io.digibyte.R;
import io.digibyte.databinding.AssetBinding;
import io.digibyte.presenter.activities.utils.RetrofitManager;
import io.digibyte.presenter.adapter.DataBoundViewHolder;
import io.digibyte.presenter.adapter.DynamicBinding;
import io.digibyte.presenter.adapter.LayoutBinding;
import io.digibyte.tools.animation.BRAnimator;
import io.digibyte.tools.crypto.AssetsHelper;
import io.digibyte.tools.manager.BRClipboardManager;
import io.digibyte.tools.manager.BRSharedPrefs;
import io.digibyte.tools.util.BRConstants;

public class AssetModel extends BaseObservable implements LayoutBinding, DynamicBinding {

    private MetaModel metaModel;
    private List<AddressInfo.Asset> assets = new LinkedList<>();
    private static transient Handler handler = new Handler(Looper.getMainLooper());
    private static transient Executor executor = Executors.newSingleThreadExecutor();

    public AssetModel(AddressInfo.Asset asset, MetaModel metaModel) {
        this.metaModel = metaModel;
        addAsset(asset);
    }

    public void addAsset(AddressInfo.Asset newAsset) {
        for (AddressInfo.Asset asset : assets) {
            if (asset.equals(newAsset)) {
                return;
            }
        }
        assets.add(newAsset);
        notifyPropertyChanged(BR.assetQuantity);
        notifyPropertyChanged(BR.assetInfo);
    }

    public boolean isAggregable() {
        return metaModel.isAggregable();
    }

    @Override
    public int getLayoutId() {
        return R.layout.asset;
    }

    @Bindable
    public MetaModel.Urls getAssetImage() {
        if (metaModel == null ||
                metaModel.metadataOfIssuence == null ||
                metaModel.metadataOfIssuence.data == null ||
                metaModel.metadataOfIssuence.data.urls == null ||
                metaModel.metadataOfIssuence.data.urls.length == 0) {
            return null;
        }
        for (MetaModel.Urls urls : metaModel.metadataOfIssuence.data.urls) {
            if ("icon".equals(urls.name)) {
                return urls;
            }
        }
        return null;
    }

    @Bindable
    public String getAssetName() {
        if (metaModel == null) {
            return "";
        }
        return metaModel.metadataOfIssuence.data.assetName;
    }

    @NonNull
    @Override
    public String toString() {
        return getAssetName().toLowerCase();
    }

    @Bindable
    public String getAssetId() {
        return String.format(DigiByte.getContext().getString(R.string.asset_id), metaModel.assetId);
    }

    @Bindable
    public String getTotalSupply() {
        return String.format(DigiByte.getContext().getString(R.string.total_supply), metaModel.totalSupply);
    }

    @Bindable
    public String getNumberOfHolders() {
        return String.format(DigiByte.getContext().getString(R.string.number_of_holders), metaModel.numOfHolders);
    }

    @Bindable
    public String getUTXOCount() {
        return String.format(DigiByte.getContext().getString(R.string.utxo_count), assets.size());
    }

    @Bindable
    public String getAssetAddress() {
        return String.format(DigiByte.getContext().getString(R.string.address), assets.get(0).address);
    }

    @Bindable
    public String getAssetDescription() {
        return metaModel.metadataOfIssuence.data.description;
    }

    @Bindable
    public String getAssetInfo() {
        return getTotalSupply() + ", " + getNumberOfHolders() + ", " + getUTXOCount();
    }

    @Bindable
    public String getAssetQuantity() {
        double quantity = 0;
        for (AddressInfo.Asset asset : assets) {
            if (asset.getDivisibility() == 0) {
                quantity += asset.getAmount();
            } else {
                quantity += (double) asset.getAmount() / (Math.pow(10, asset.getDivisibility()));
            }
        }
        return String.valueOf(quantity);
    }

    private int getAssetQuantityInt() {
        int quantity = 0;
        for (AddressInfo.Asset asset : assets) {
            quantity += asset.getAmount();
        }
        return quantity;
    }

    private String[] getAddresses() {
        Set<String> addresses = new HashSet<>();
        for (AddressInfo.Asset asset : assets) {
            addresses.add(asset.address);
        }
        return addresses.toArray(new String[]{});
    }

    private String[] getUTXOTxIds() {
        Set<String> txids = new HashSet<>();
        for (AddressInfo.Asset asset : assets) {
            txids.add(asset.txid + ":" + asset.getIndex());
        }
        return txids.toArray(new String[]{});
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AssetModel that = (AssetModel) o;
        return assets.get(0).assetId.equals(that.assets.get(0).assetId);
    }

    @Override
    public void bind(DataBoundViewHolder holder) {
        AssetBinding binding = (AssetBinding) holder.binding;
        binding.assetInfo.setSelected(true);
        binding.assetDrawable.setImageBitmap(null);
        binding.assetMenu.setOnClickListener(v -> {
            ContextThemeWrapper context = new ContextThemeWrapper(v.getContext(),
                    R.style.AssetPopup);
            showAssetMenu(context, v);
        });
        binding.assetId.setOnClickListener(v -> {
            BRClipboardManager.putClipboard(v.getContext(), metaModel.assetId);
            Toast.makeText(v.getContext(), R.string.Receive_copied, Toast.LENGTH_SHORT).show();
        });
        binding.assetAddress.setOnClickListener(v -> {
            BRClipboardManager.putClipboard(v.getContext(), assets.get(0).address);
            Toast.makeText(v.getContext(), R.string.Receive_copied, Toast.LENGTH_SHORT).show();
        });
    }

    private void showAssetMenu(Context context, View v) {
        PopupMenu popup = new PopupMenu(context, v);
        MenuInflater inflater = popup.getMenuInflater();
        inflater.inflate(R.menu.asset_menu, popup.getMenu());
        popup.setOnMenuItemClickListener(item -> {
            switch (item.getItemId()) {
                case R.id.send:
                    if (BRSharedPrefs.getCatchedBalance(context) == 0) {
                        Toast.makeText(context, R.string.not_enough_digi, Toast.LENGTH_SHORT).show();
                    } else {
                        showSendMenu(context, v);
                    }
                    break;
                case R.id.update:
                    RetrofitManager.instance.clearMetaCache(getAssetId());
                    RetrofitManager.instance.getAssetMeta(
                            assets.get(0).assetId,
                            assets.get(0).txid,
                            String.valueOf(assets.get(0).getIndex()),
                            new RetrofitManager.MetaCallback() {
                                @Override
                                public void metaRetrieved(MetaModel metalModel) {
                                    AssetModel.this.metaModel = metaModel;
                                    notifyPropertyChanged(BR.totalSupply);
                                    notifyPropertyChanged(BR.numberOfHolders);
                                    notifyPropertyChanged(BR.uTXOCount);
                                }

                                @Override
                                public void failure() {
                                    Toast.makeText(DigiByte.getContext(), R.string.failure_asset_meta, Toast.LENGTH_SHORT).show();
                                }
                            });
                    break;
            }
            return true;
        });
        popup.show();
    }

    private void showSendMenu(Context context, View v) {
        PopupMenu popup = new PopupMenu(context, v);
        MenuInflater inflater = popup.getMenuInflater();
        inflater.inflate(R.menu.send_asset_menu, popup.getMenu());
        popup.setOnMenuItemClickListener(item -> {
            String[] fromAddresses = getAddresses();
            RetrofitManager.instance.clearCache(fromAddresses);
            AssetsHelper.AssetTx assetTx = new AssetsHelper.AssetTx(
                    fromAddresses[0],
                    "",
                    getUTXOTxIds(),
                    getAssetQuantityInt(),
                    metaModel.assetId,
                    metaModel.divisibility,
                    this
            );
            switch (item.getItemId()) {
                case R.id.qr:
                    AssetsHelper.Companion.getInstance().pendingAssetTx = assetTx;
                    BRAnimator.openScanner((Activity) v.getContext(), BRConstants.ASSETS_SCANNER_REQUEST);
                    break;
                case R.id.paste:
                    ClipboardManager clipboard = (ClipboardManager) context.getSystemService(
                            Context.CLIPBOARD_SERVICE);
                    ClipData clipData = clipboard.getPrimaryClip();
                    if (clipData != null && clipData.getItemCount() > 0) {
                        CharSequence destinationAddress = clipData.getItemAt(0).getText();
                        assetTx.setDestinationAddress(destinationAddress);
                        AssetsHelper.Companion.getInstance().processAssetTx(v.getContext(), assetTx);
                    } else {
                        Toast.makeText(context, R.string.no_clip_data, Toast.LENGTH_SHORT).show();
                    }
                    break;
            }
            return true;
        });
        popup.show();
    }

    @BindingAdapter("remoteImage")
    public static void remoteImage(ImageView imageView, MetaModel.Urls imageData) {
        if (imageData == null || TextUtils.isEmpty(imageData.url)) {
            executor.execute(() -> {
                Drawable defaultImage = imageView.getContext().getResources().getDrawable(R.drawable.ic_assets);
                handler.post(() -> imageView.setImageDrawable(defaultImage));
            });
            return;
        }
        imageView.setImageBitmap(null);
        executor.execute(() -> {
            if (imageData.url.contains(",")) {
                byte[] image = null;
                try {
                    image = Base64.decode(imageData.url.substring(imageData.url.indexOf(",")),
                            Base64.DEFAULT);
                } catch (IllegalArgumentException e) {
                }
                if (image != null) {
                    Bitmap bitmap = BitmapFactory.decodeByteArray(image, 0, image.length);
                    if (bitmap != null) {
                        handler.post(() -> imageView.setImageBitmap(bitmap));
                    }
                } else {
                    Drawable defaultImage = imageView.getContext().getResources().getDrawable(R.drawable.ic_assets);
                    handler.post(() -> imageView.setImageDrawable(defaultImage));
                }
            } else if (imageData.url.contains("http")) {
                if (imageData.url.endsWith(".gif")) {
                    handler.post(() -> Glide.with(DigiByte.getContext()).load(imageData.url).into(imageView));
                } else {
                    handler.post(() -> Picasso.get().load(imageData.url).into(imageView));
                }
            } else {
                Drawable defaultImage = imageView.getContext().getResources().getDrawable(R.drawable.ic_assets);
                handler.post(() -> imageView.setImageDrawable(defaultImage));
            }
        });
    }
}