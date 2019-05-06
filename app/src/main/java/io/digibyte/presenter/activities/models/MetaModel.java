package io.digibyte.presenter.activities.models;

public class MetaModel {
    String assetId;
    String issuanceTxid;
    int firstBlock;
    String someUtxo;
    int divisibility;
    String aggregationPolicy;
    boolean lockStatus;
    int numOfIssuance;
    int numOfTransfers;
    int totalSupply;
    int numOfHolders;
    String issueAddress;
    IsuanceModel metadataOfIssuence;

    public class IsuanceModel {
        String sha2Issue;
        IsuanceData data;
    }

    public class IsuanceData {
        String assetName;
        String description;
        Urls[] urls;
    }

    public class Urls {
        String name;
        String mimeType;
        String url;
    }
}
