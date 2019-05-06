package io.digibyte.presenter.activities.callbacks;

import io.digibyte.presenter.activities.models.AssetModel;
import io.digibyte.presenter.adapter.MultiTypeDataBoundAdapter;

public interface AssetCallback extends MultiTypeDataBoundAdapter.ActionCallback {
    void onAssetClick(AssetModel assetModel);

    void onMenuClick(AssetModel assetModel);
}
