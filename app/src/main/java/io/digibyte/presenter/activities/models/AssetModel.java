package io.digibyte.presenter.activities.models;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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

import com.squareup.picasso.Picasso;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import io.digibyte.BR;
import io.digibyte.R;
import io.digibyte.databinding.AssetBinding;
import io.digibyte.presenter.activities.BreadActivity;
import io.digibyte.presenter.activities.util.RetrofitManager;
import io.digibyte.presenter.adapter.DataBoundViewHolder;
import io.digibyte.presenter.adapter.DynamicBinding;
import io.digibyte.presenter.adapter.LayoutBinding;
import io.digibyte.tools.animation.BRAnimator;
import io.digibyte.tools.crypto.AssetsHelper;
import io.digibyte.tools.database.Database;
import io.digibyte.tools.list.items.ListItemTransactionData;
import io.digibyte.tools.util.BRConstants;
import io.digibyte.wallet.BRWalletManager;

public class AssetModel extends BaseObservable implements LayoutBinding, DynamicBinding {

    private MetaModel metaModel;
    private List<AddressInfo.Asset> assets = new LinkedList<>();
    private List<ListItemTransactionData> listItemTransactionDatas = new LinkedList<>();
    private static transient Handler handler = new Handler(Looper.getMainLooper());
    private static transient Executor executor = Executors.newSingleThreadExecutor();

    public AssetModel(AddressInfo.Asset asset) {
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
    }

    public void addTransaction(ListItemTransactionData listItemTransactionData) {
        if (!listItemTransactionDatas.contains(listItemTransactionData)) {
            listItemTransactionDatas.add(listItemTransactionData);
            //If we have the asset name here, it's because meta has already been retrieved
            //If we don't, it'll be in the collection processed when meta is retrieved
            if (!TextUtils.isEmpty(getAssetName())) {
                Database.instance.saveAssetName(getAssetName(), listItemTransactionData);
            }
        }
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

    private int getAssetsQuantity() {
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
        binding.assetDrawable.setImageBitmap(null);
        binding.assetMenu.setOnClickListener(v -> {
            ContextThemeWrapper context = new ContextThemeWrapper(v.getContext(),
                    R.style.AssetPopup);
            showAssetMenu(context, v);
        });
        if (metaModel != null) {
            return;
        }
        RetrofitManager.instance.getAssetMeta(
                assets.get(0).assetId,
                assets.get(0).txid,
                String.valueOf(assets.get(0).getIndex()),
                metaModel -> {
                    AssetModel.this.metaModel = metaModel;
                    notifyPropertyChanged(BR.assetName);
                    notifyPropertyChanged(BR.assetQuantity);
                    notifyPropertyChanged(BR.assetImage);
                    Database.instance.saveAssetName(getAssetName(),
                            listItemTransactionDatas.toArray(new ListItemTransactionData[0]));
                    ((BreadActivity) holder.itemView.getContext()).sortAssets();
                });
    }

    private void showAssetMenu(Context context, View v) {
        PopupMenu popup = new PopupMenu(context, v);
        MenuInflater inflater = popup.getMenuInflater();
        inflater.inflate(R.menu.asset_menu, popup.getMenu());
        popup.setOnMenuItemClickListener(item -> {
            switch (item.getItemId()) {
                case R.id.send:
                    showSendMenu(context, v);
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
            AssetsHelper.AssetTx assetTx = new AssetsHelper.AssetTx(
                    "",
                    getAddresses(),
                    getAssetsQuantity(),
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
                }
            } else if (imageData.url.contains("http")) {
                handler.post(() -> Picasso.get().load(imageData.url).into(imageView));
            }
        });
    }
}