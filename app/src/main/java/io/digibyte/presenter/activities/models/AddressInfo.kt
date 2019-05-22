package io.digibyte.presenter.activities.models

import java.util.*

class AddressInfo {
    lateinit var address: String
    lateinit var utxos: Array<UTXO>

    val assets: List<Asset>
        get() {
            val assets = LinkedList<Asset>()
            for (utxo in utxos) {
                if (utxo.used) {
                    continue;
                }
                for (asset in utxo.assets) {
                    asset.address = utxo.address
                    asset.txid = utxo.txid
                    asset.index = utxo.index
                    assets.add(asset)
                }
            }
            return assets
        }

    inner class UTXO {
        lateinit var address: String
        var index: Int = 0
        var used: Boolean = false
        lateinit var txid: String
        lateinit var assets: Array<Asset>
    }

    inner class Asset {
        lateinit var assetId: String
        var amount: Int = 0
        lateinit var issueTxid: String
        var divisibility: Int = 0
        var lockStatus: Boolean = false
        lateinit var aggregationPolicy: String
        lateinit var address: String
        lateinit var txid: String
        var index: Int = 0
    }
}