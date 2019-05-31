package io.digibyte.tools.database;

import androidx.annotation.NonNull;
import androidx.room.Room;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

import android.os.Handler;
import android.os.Looper;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import io.digibyte.DigiByte;
import io.digibyte.tools.list.items.ListItemTransactionData;

public class Database {
    public static Database instance = new Database();
    public TransactionDao transactionsDao;
    private Executor executor = Executors.newSingleThreadExecutor();
    private Handler handler = new Handler(Looper.getMainLooper());
    public List<DigiTransaction> transactions = new LinkedList<>();

    public interface TransacionStoreListener {
        void onTransactionsUpdate();
    }

    public Database() {
        AppDatabase database = Room.databaseBuilder(DigiByte.getContext(),
                AppDatabase.class, "transaction_database").fallbackToDestructiveMigration().build();
        transactionsDao = database.transactionDao();
        updateTransactions(null);
    }

    public void saveTransaction(byte[] txHash, String amount) {
        executor.execute(() -> {
            DigiTransaction digiTransaction = new DigiTransaction();
            digiTransaction.setTxHash(txHash);
            digiTransaction.setTxAmount(amount);
            transactionsDao.insertAll(digiTransaction);
            transactions = transactionsDao.getAll();
        });
    }

    private void updateTransactions(TransacionStoreListener transacionStoreListener) {
        executor.execute(() -> {
            transactions = transactionsDao.getAll();
            handler.post(() -> {
                if (transacionStoreListener != null) {
                    transacionStoreListener.onTransactionsUpdate();
                }
            });
        });
    }

    public boolean containsTransaction(byte[] txHash) {
        return findTransaction(txHash) != null;
    }

    public DigiTransaction findTransaction(byte[] txHash) {
        for (DigiTransaction digiTransaction : transactions) {
            if (Arrays.equals(txHash, digiTransaction.getTxHash())) {
                return digiTransaction;
            }
        }
        return null;
    }
}