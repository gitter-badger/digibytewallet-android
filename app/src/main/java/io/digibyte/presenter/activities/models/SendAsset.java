package io.digibyte.presenter.activities.models;

import java.util.Arrays;

public class SendAsset {
    String fee;
    String[] from;
    String[] financeAddresses;
    To[] to;

    SendAsset(String fee, String from, String[] financeAddresses, String to, String assetId) {
        this.fee = fee;
        this.from = new String[1];
        this.from[0] = from;
        this.financeAddresses = financeAddresses;
        this.to = new To[1];
        this.to[0] = new To();
        this.to[0].address = to;
        this.to[0].assetId = assetId;
    }

    public SendAsset setQuantity(int amount) {
        this.to[0].amount = amount;
        return this;
    }

    @Override
    public String toString() {
        return "SendAsset{" +
                "fee='" + fee + '\'' +
                ", from=" + Arrays.toString(from) +
                ", to=" + Arrays.toString(to) +
                '}';
    }

    public class Address {
        String address;

        @Override
        public String toString() {
            return "Address{" +
                    "address='" + address + '\'' +
                    '}';
        }
    }

    public class To {
        String address;
        int amount;
        String assetId;

        @Override
        public String toString() {
            return "To{" +
                    "address='" + address + '\'' +
                    ", amount=" + amount +
                    ", assetId='" + assetId + '\'' +
                    '}';
        }
    }
}
