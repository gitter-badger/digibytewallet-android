package io.digibyte.presenter.activities.models;

public class SendAssetResponse {
    String txHex;

    public String getTxHex() {
        return txHex;
    }

    @Override
    public String toString() {
        return "SendAssetResponse{" +
                "txHex='" + txHex + '\'' +
                '}';
    }
}
