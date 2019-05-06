package io.digibyte.presenter.activities.models;

public class SendAsset {
    String fee;
    Address[] from;
    To[] to;

    SendAsset(String fee, String from, String to, String assetId, int amount) {
        this.fee = fee;
        this.from = new Address[1];
        this.from[0].address = from;
        this.to = new To[1];
        this.to[0].address = to;
        this.to[0].assetId = assetId;
        this.to[0].amount = amount;
    }

    public class Address {
        String address;
    }

    public class To {
        String address;
        int amount;
        String assetId;
    }
}
