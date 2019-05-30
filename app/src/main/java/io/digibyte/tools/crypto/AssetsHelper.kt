package io.digibyte.tools.crypto

import android.content.Context
import android.text.TextUtils
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import io.digibyte.presenter.activities.models.AssetModel
import io.digibyte.presenter.activities.models.SendAsset
import io.digibyte.presenter.fragments.FragmentNumberPicker
import io.digibyte.presenter.interfaces.BRAuthCompletion
import java.util.*

class AssetsHelper {

    private external fun getNeededUTXO(amount: Int): Array<String>
    @kotlin.jvm.JvmField
    var pendingAssetTx: AssetTx? = null

    companion object {
        val instance: AssetsHelper = AssetsHelper()
    }

    class AssetTx(var destinationAddress: CharSequence, val addresses: Array<String>, val assetQuantity: Int, val assetId: String, val divisibility: Int, val assetModel: AssetModel)

    fun sendPendingAssetTx(context: Context) {
        pendingAssetTx?.let {
            processAssetTx(context, it)
            pendingAssetTx = null
        }
    }

    fun processAssetTx(context: Context, assetTx: AssetTx) {
        try {
            Log.d(AssetModel::class.java.simpleName, "Clipped Address: $assetTx.destinationAddress")
            val sendAsset = SendAsset(
                    Integer.toString(1200),
                    assetTx.addresses,
                    trimNullEmpty(getNeededUTXO(1200)),
                    assetTx.destinationAddress.toString(),
                    assetTx.assetQuantity,
                    assetTx.assetId,
                    assetTx.divisibility,
                    assetTx.assetModel
            )
            FragmentNumberPicker.show(
                    context as AppCompatActivity,
                    BRAuthCompletion.AuthType(sendAsset)
            )
        } catch (t: Throwable) {
            t.printStackTrace()
        }

    }

    private fun trimNullEmpty(values: Array<String>): Array<String> {
        val newValues = LinkedList<String>()
        for (value in values) {
            if (!TextUtils.isEmpty(value) && value.toLowerCase() != "null") {
                newValues.add(value)
            }
        }
        return newValues.toTypedArray()
    }
}