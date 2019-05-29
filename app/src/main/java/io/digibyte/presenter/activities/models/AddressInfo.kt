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
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as Asset

            if (address != other.address) return false
            if (txid != other.txid) return false
            if (index != other.index) return false

            return true
        }

        override fun hashCode(): Int {
            var result = assetId.hashCode()
            result = 31 * result + amount
            result = 31 * result + issueTxid.hashCode()
            result = 31 * result + divisibility
            result = 31 * result + lockStatus.hashCode()
            result = 31 * result + aggregationPolicy.hashCode()
            result = 31 * result + address.hashCode()
            result = 31 * result + txid.hashCode()
            result = 31 * result + index
            return result
        }
    }
}