package io.digibyte.presenter.activities.callbacks;

import io.digibyte.presenter.adapter.MultiTypeDataBoundAdapter;
import io.digibyte.presenter.entities.TxItem;
import io.digibyte.tools.list.items.ListItemTransactionData;

public interface TransactionClickCallback extends MultiTypeDataBoundAdapter.ActionCallback {

    void onTransactionClick(ListItemTransactionData listItemTransactionData);
}
