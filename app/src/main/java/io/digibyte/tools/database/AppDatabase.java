package io.digibyte.tools.database;

import androidx.room.Database;
import androidx.room.RoomDatabase;

@Database(entities = {DigiTransaction.class, AssetName.class}, version = 16)
public abstract class AppDatabase extends RoomDatabase {
    public abstract TransactionDao transactionDao();

    public abstract AssetNameDao assetNameDao();
}