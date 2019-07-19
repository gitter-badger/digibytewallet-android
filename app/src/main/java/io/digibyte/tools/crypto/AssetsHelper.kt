package io.digibyte.tools.crypto

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import io.digibyte.R
import io.digibyte.presenter.activities.models.AssetModel
import io.digibyte.presenter.activities.models.FinanceUTXO
import io.digibyte.presenter.activities.models.SendAsset
import io.digibyte.presenter.activities.utils.RetrofitManager
import io.digibyte.presenter.fragments.FragmentNumberPicker
import io.digibyte.presenter.activities.callbacks.BRAuthCompletion

class AssetsHelper {

    private external fun getNeededUTXOTxid(amount: Int): FinanceUTXO
    @kotlin.jvm.JvmField
    var pendingAssetTx: AssetTx? = null

    companion object {
        val instance: AssetsHelper = AssetsHelper()
    }

    data class AssetTx(var changeAddress: String, var destinationAddress: CharSequence, val utxoTxids: Array<String>, val assetQuantity: Int, val assetId: String, val divisibility: Int, val assetModel: AssetModel)

    fun sendPendingAssetTx(context: Context) {
        pendingAssetTx?.let {
            processAssetTx(context, it)
            pendingAssetTx = null
        }
    }

    fun processAssetTx(context: Context, assetTx: AssetTx) {
        try {
            Log.d(AssetModel::class.java.simpleName, "Clipped Address: ${assetTx.destinationAddress}")

            val financeUTXO = getNeededUTXOTxid(1200)
            if (financeUTXO.txid.isNullOrEmpty()) {
                Toast.makeText(context, R.string.not_enough_digi, Toast.LENGTH_SHORT).show()
                return
            }
            RetrofitManager.instance.clearCache(assetTx.changeAddress)

            val sendAsset = SendAsset(
                    Integer.toString(1200),
                    assetTx.changeAddress,
                    assetTx.utxoTxids,
                    financeUTXO.vout,
                    financeUTXO.txid,
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
}