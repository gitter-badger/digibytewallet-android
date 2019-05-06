package io.digibyte.presenter.activities.models;

import android.graphics.BitmapFactory;
import android.text.TextUtils;
import android.util.Base64;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ImageView;

import androidx.appcompat.view.ContextThemeWrapper;
import androidx.appcompat.widget.PopupMenu;
import androidx.databinding.BaseObservable;
import androidx.databinding.Bindable;
import androidx.databinding.BindingAdapter;

import java.util.Objects;

import io.digibyte.BR;
import io.digibyte.R;
import io.digibyte.databinding.AssetBinding;
import io.digibyte.presenter.activities.util.RetrofitManager;
import io.digibyte.presenter.adapter.DataBoundViewHolder;
import io.digibyte.presenter.adapter.DynamicBinding;
import io.digibyte.presenter.adapter.LayoutBinding;

public class AssetModel extends BaseObservable implements LayoutBinding, DynamicBinding {

    private AddressAssets.Asset asset;
    private MetaModel metaModel;
    double assetAmount = 100.1;

    public AssetModel(AddressAssets.Asset asset) {
        this.asset = asset;
    }

    @Override
    public int getLayoutId() {
        return R.layout.asset;
    }

    @Bindable
    public String getAssetImage() {
        if (metaModel == null) {
            return "";
        }
        return metaModel.metadataOfIssuence.data.urls[0].url;
    }

    @Bindable
    public String getAssetName() {
        if (metaModel == null) {
            return "";
        }
        return metaModel.metadataOfIssuence.data.assetName;
    }

    @Bindable
    public String getAssetQuantity() {
        if (metaModel == null) {
            return "";
        }
        return Double.toString(asset.amount);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AssetModel that = (AssetModel) o;
        return Double.compare(that.assetAmount, assetAmount) == 0 &&
                Objects.equals(asset, that.asset) &&
                Objects.equals(metaModel, that.metaModel);
    }

    @Override
    public int hashCode() {
        return Objects.hash(asset, metaModel, assetAmount);
    }

    @Override
    public void bind(DataBoundViewHolder holder) {
        AssetBinding binding = (AssetBinding) holder.binding;
        binding.assetMenu.setOnClickListener(v -> {
            ContextThemeWrapper context = new ContextThemeWrapper(v.getContext(),
                    R.style.AssetPopup);
            PopupMenu popup = new PopupMenu(context, v);
            MenuInflater inflater = popup.getMenuInflater();
            inflater.inflate(R.menu.asset_menu, popup.getMenu());
            popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    switch (item.getItemId()) {
                        case R.id.send:
//                            SendAsset sendAsset = new SendAsset(500, );
//                            RetrofitManager.instance.sendAsset();
                            break;
                    }
                    return true;
                }
            });
            popup.show();
        });
        RetrofitManager.instance.getAssetMeta(asset.assetId, asset.originatingTxId, asset.index,
                metaModel -> {
                    AssetModel.this.metaModel = metaModel;
                    notifyPropertyChanged(BR.assetName);
                    notifyPropertyChanged(BR.assetQuantity);
                    notifyPropertyChanged(BR.assetImage);
                });
    }

    @BindingAdapter("remoteImage")
    public static void remoteImage(ImageView imageView, String imageData) {
        if (TextUtils.isEmpty(imageData)) {
            return;
        }
        byte[] image = Base64.decode(imageData.substring(imageData.indexOf(",")), Base64.DEFAULT);
        imageView.setImageBitmap(BitmapFactory.decodeByteArray(image, 0, image.length));
    }
}