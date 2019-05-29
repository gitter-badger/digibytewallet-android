package io.digibyte.tools.adapter;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;

import io.digibyte.DigiByte;
import io.digibyte.presenter.activities.BreadActivity;
import io.digibyte.presenter.activities.callbacks.TransactionClickCallback;
import io.digibyte.presenter.adapter.MultiTypeDataBoundAdapter;
import io.digibyte.presenter.entities.TxItem;
import io.digibyte.presenter.fragments.models.TransactionDetailsViewModel;
import io.digibyte.tools.animation.BRAnimator;
import io.digibyte.tools.database.Database;
import io.digibyte.tools.list.items.ListItemTransactionData;
import io.digibyte.tools.manager.BRSharedPrefs;
import io.digibyte.wallet.BRWalletManager;


/**
 * BreadWallet
 * <p>
 * Created by Mihail Gutan <mihail@breadwallet.com> on 7/27/15.
 * Copyright (c) 2016 breadwallet LLC
 * <p>
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * <p>
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

public class TransactionListAdapter extends MultiTypeDataBoundAdapter implements TransactionClickCallback {
    public static final String TAG = TransactionListAdapter.class.getName();

    private ArrayList<ListItemTransactionData> listItemData = new ArrayList<>();

    private BreadActivity activity;

    public TransactionListAdapter(BreadActivity activity) {
        super(null, (Object) null);
        setActionCallback(this);
        this.activity = activity;
    }

    public void updateTransactions(ArrayList<ListItemTransactionData> transactions) {
        for (ListItemTransactionData listItemTransactionData : listItemData) {
            if (listItemTransactionData == null) {
                continue;
            }
            ListItemTransactionData updatedTransaction = findTransaction(listItemTransactionData, transactions);
            if (updatedTransaction == null) {
                continue;
            }
            listItemTransactionData.update(updatedTransaction);
            int confirms = BRSharedPrefs.getLastBlockHeight(DigiByte.getContext())
                    - listItemTransactionData.getTransactionItem().getBlockHeight() + 1;
            if (confirms <= 8) {
                BRWalletManager.getInstance().refreshBalance(DigiByte.getContext());
            }
        }
    }

    @Nullable
    private ListItemTransactionData findTransaction(ListItemTransactionData listItemTransactionData,
                                                    ArrayList<ListItemTransactionData> transactions) {
        for (ListItemTransactionData checkTransaction : transactions) {
            if (checkTransaction.equals(listItemTransactionData)) {
                return checkTransaction;
            }
        }
        return null;
    }

    public void addTransactions(ArrayList<ListItemTransactionData> transactions) {
        Collections.sort(transactions,
                (o1, o2) -> new Date(o1.transactionItem.getTimeStamp()).compareTo(
                        new Date(o2.transactionItem.getTimeStamp())));
        for (ListItemTransactionData transaction : transactions) {
            saveFiatValue(transaction.transactionItem);
            listItemData.add(0, transaction);
            addItem(0, transaction);
        }
    }

    public ArrayList<ListItemTransactionData> getTransactions() {
        return listItemData;
    }

    @Override
    public int getItemCount() {
        return listItemData.size();
    }

    @Override
    public long getItemId(int position) {
        return Integer.valueOf(listItemData.get(
                position).getTransactionItem().hashCode()).longValue();
    }

    private void saveFiatValue(TxItem txItem) {
        //Save the transaction text in fiat mode, the first time the transaction is displayed in the app
        if (!Database.instance.containsTransaction(txItem.getTxHash())) {
            Database.instance.saveTransaction(txItem.getTxHash(), TransactionDetailsViewModel.getRawFiatAmount(txItem));
        }
    }

    @Override
    public void onTransactionClick(ListItemTransactionData listItemTransactionData) {
        if (listItemTransactionData.transactionItem.isAsset) {
            activity.onAssetsButtonClick(null);
        } else {
            int adapterPosition = listItemData.indexOf(listItemTransactionData);
            BRAnimator.showTransactionPager(activity, listItemData, adapterPosition);
        }
    }
}