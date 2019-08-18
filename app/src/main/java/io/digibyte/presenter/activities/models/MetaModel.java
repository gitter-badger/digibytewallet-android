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
    float totalSupply;
    int numOfHolders;
    String issueAddress;
    public IsuanceModel metadataOfIssuence;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MetaModel metaModel = (MetaModel) o;
        return assetId.equals(metaModel.assetId);
    }

    @Override
    public int hashCode() {
        return assetId.hashCode();
    }

    public class IsuanceModel {
        String sha2Issue;
        public IsuanceData data;
        public String description;
    }

    public class IsuanceData {
        public String assetName;
        String description;
        Urls[] urls;
    }

    public class Urls {
        String name;
        String mimeType;
        String url;
    }

    public boolean isAggregable() {
        return "aggregatable".equals(aggregationPolicy) || "hybrid".equals(aggregationPolicy);
    }
}
