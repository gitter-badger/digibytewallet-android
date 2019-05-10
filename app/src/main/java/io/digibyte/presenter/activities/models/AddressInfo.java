package io.digibyte.presenter.activities.models;

import android.util.Log;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

public class AddressInfo {
    String address;
    UTXO[] utxos;

    public class UTXO {
        String address;
        int index;
        boolean used;
        String txid;
        Asset[] assets;

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            UTXO utxo = (UTXO) o;
            return index == utxo.index &&
                    used == utxo.used &&
                    Objects.equals(address, utxo.address) &&
                    Objects.equals(txid, utxo.txid) &&
                    Arrays.equals(assets, utxo.assets);
        }

        @Override
        public int hashCode() {
            int result = Objects.hash(address, index, used, txid);
            result = 31 * result + Arrays.hashCode(assets);
            return result;
        }
    }

    public class Asset {
        String assetId;
        int amount;
        String issueTxid;
        int divisibility;
        boolean lockStatus;
        String aggregationPolicy;
        String utxoAddress;
        public String assetUtxoTxId;
        String index;

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Asset asset = (Asset) o;
            return amount == asset.amount &&
                    divisibility == asset.divisibility &&
                    lockStatus == asset.lockStatus &&
                    Objects.equals(assetId, asset.assetId) &&
                    Objects.equals(issueTxid, asset.issueTxid) &&
                    Objects.equals(aggregationPolicy, asset.aggregationPolicy);
        }

        @Override
        public int hashCode() {
            return Objects.hash(assetId, amount, issueTxid, divisibility, lockStatus,
                    aggregationPolicy);
        }
    }

    public List<Asset> getAssets() {
        List<Asset> assets = new LinkedList<>();
        for (UTXO utxo : utxos) {
            for (Asset asset : utxo.assets) {
                asset.utxoAddress = utxo.address;
                asset.assetUtxoTxId = utxo.txid;
                asset.index = Integer.toString(utxo.index);
                Log.d(AddressInfo.class.getSimpleName(), "TX ID: " + asset.assetUtxoTxId);
                assets.add(asset);
            }
        }
        return assets;
    }

    public List<String> getAssetsUtxo() {
        List<String> assetsUtxo = new LinkedList<>();
        for (UTXO utxo : utxos) {
            for (Asset asset : utxo.assets) {
                assetsUtxo.add(utxo.txid);
            }
        }
        return assetsUtxo;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AddressInfo that = (AddressInfo) o;
        return Objects.equals(address, that.address) &&
                Arrays.equals(utxos, that.utxos);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(address);
        result = 31 * result + Arrays.hashCode(utxos);
        return result;
    }
}