package io.digibyte.presenter.activities.callbacks

import android.view.View
import io.digibyte.presenter.activities.models.AssetModel
import io.digibyte.presenter.adapter.MultiTypeDataBoundAdapter

interface AssetClickCallback : MultiTypeDataBoundAdapter.ActionCallback {
    fun onAssetClick(v: View, assetModel: AssetModel)
}