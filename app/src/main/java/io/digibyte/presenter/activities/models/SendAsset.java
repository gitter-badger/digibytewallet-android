package io.digibyte.presenter.activities.models;

import java.util.Arrays;

public class SendAsset {
    String fee;
    String[] from;
    To[] to;
    String[] financeAddresses;
    public transient int divisibility;
    public transient AssetModel assetModel;

    private static final int TO_DESTINATION = 0;
    private static final int TO_CHANGE = 1;

    public static final int INVALID_AMOUNT = -1;

    SendAsset(String fee, String[] fromAddresses, String[] financeaddresses, String destinationAddress,
              int totalAssetQuantity, String assetId, int divisibility, AssetModel assetModel) {
        this.fee = fee;

        this.from = new String[1];
        this.from = fromAddresses;

        this.financeAddresses = financeaddresses;

        to = new To[2];
        to[TO_DESTINATION] = new To();
        to[TO_DESTINATION].address = destinationAddress;
        to[TO_DESTINATION].assetId = assetId;

        to[TO_CHANGE] = new To();
        to[TO_CHANGE].address = fromAddresses[0];
        to[TO_CHANGE].assetId = assetId;
        to[TO_CHANGE].amount = totalAssetQuantity;

        this.divisibility = divisibility;
        this.assetModel = assetModel;
    }

    public SendAsset setQuantity(int amount) {
        to[TO_DESTINATION].amount = amount;
        to[TO_CHANGE].amount -= amount;
        if (to[TO_CHANGE].amount < 0) {
            //Too much selected, invalid amount
            to[TO_DESTINATION].amount = INVALID_AMOUNT;
        } else if (to[TO_CHANGE].amount == 0) {
            //Total amount consumed, remove the change output
            To[] single = new To[1];
            single[TO_DESTINATION] = to[TO_DESTINATION];
            to = single;
        }
        return this;
    }

    public boolean isValidAmount() {
        return to[TO_DESTINATION].amount != INVALID_AMOUNT;
    }

    public boolean isCompleteSpend() {
        return to.length == 1;
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
