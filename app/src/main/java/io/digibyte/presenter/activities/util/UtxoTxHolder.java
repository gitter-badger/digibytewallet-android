package io.digibyte.presenter.activities.util;

import java.util.HashSet;
import java.util.LinkedList;

public class UtxoTxHolder {
    public static UtxoTxHolder instance = new UtxoTxHolder();
    private HashSet<String> allAssetUtxoTxIds = new HashSet<>();
    private HashSet<String> assetUtxoTxIds = new HashSet<>();

    private UtxoTxHolder() {
    }

    public void addUtxoTxId(String txId) {
        allAssetUtxoTxIds.add(txId);
    }

    public void addAssetUtxoTxId(String txId) {
        assetUtxoTxIds.add(txId);
    }

    public String[] removeAssetUtxoTxIds(String[] unspenUtxoTxIds) {
        LinkedList<String> availableNonAssetUTXO = new LinkedList<>();
        for (String utxo : unspenUtxoTxIds) {
            if (!this.assetUtxoTxIds.contains(utxo)) {
                availableNonAssetUTXO.add(utxo);
            }
        }
        String[] utxo = new String[availableNonAssetUTXO.size()];
        return availableNonAssetUTXO.toArray(utxo);
    }
}
