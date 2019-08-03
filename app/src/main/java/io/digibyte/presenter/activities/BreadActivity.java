package io.digibyte.presenter.activities;

import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.animation.LayoutTransition;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.view.GravityCompat;
import androidx.databinding.DataBindingUtil;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.ToxicBakery.viewpager.transforms.CubeOutTransformer;
import com.appolica.flubber.Flubber;
import com.crashlytics.android.Crashlytics;
import com.google.android.material.appbar.AppBarLayout;
import com.google.common.collect.Ordering;
import com.google.common.io.BaseEncoding;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.gson.Gson;

import org.apache.commons.codec.binary.Hex;

import java.math.RoundingMode;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnLongClick;
import butterknife.Unbinder;
import io.digibyte.DigiByte;
import io.digibyte.R;
import io.digibyte.databinding.ActivityBreadBinding;
import io.digibyte.presenter.activities.adapters.TxAdapter;
import io.digibyte.presenter.activities.models.AddressInfo;
import io.digibyte.presenter.activities.models.AssetModel;
import io.digibyte.presenter.activities.models.MetaModel;
import io.digibyte.presenter.activities.models.SendAsset;
import io.digibyte.presenter.activities.models.SendAssetResponse;
import io.digibyte.presenter.activities.settings.SecurityCenterActivity;
import io.digibyte.presenter.activities.settings.SettingsActivity;
import io.digibyte.presenter.activities.settings.SyncBlockchainActivity;
import io.digibyte.presenter.activities.utils.ActivityUtils;
import io.digibyte.presenter.activities.base.BRActivity;
import io.digibyte.presenter.activities.utils.RetrofitManager;
import io.digibyte.presenter.activities.utils.TransactionUtils;
import io.digibyte.presenter.adapter.MultiTypeDataBoundAdapter;
import io.digibyte.presenter.entities.TxItem;
import io.digibyte.presenter.activities.callbacks.BRAuthCompletion;
import io.digibyte.tools.animation.BRAnimator;
import io.digibyte.tools.database.Database;
import io.digibyte.tools.list.items.ListItemTransactionData;
import io.digibyte.tools.manager.BRApiManager;
import io.digibyte.tools.manager.BRSharedPrefs;
import io.digibyte.tools.manager.JobsHelper;
import io.digibyte.tools.manager.SyncManager;
import io.digibyte.tools.manager.TxManager;
import io.digibyte.tools.manager.TxManager.onStatusListener;
import io.digibyte.tools.security.AuthManager;
import io.digibyte.tools.security.BRKeyStore;
import io.digibyte.tools.sqlite.TransactionDataSource;
import io.digibyte.tools.threads.BRExecutor;
import io.digibyte.tools.util.BRConstants;
import io.digibyte.tools.util.TypesConverter;
import io.digibyte.tools.util.ViewUtils;
import io.digibyte.wallet.BRPeerManager;
import io.digibyte.wallet.BRWalletManager;

/**
 * <p/>
 * Created by Noah Seidman <noah@noahseidman.com> on 4/14/18.
 * <p/>
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * <p/>
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * <p/>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

public class BreadActivity extends BRActivity implements BRWalletManager.OnBalanceChanged,
        BRPeerManager.OnTxStatusUpdate, BRSharedPrefs.OnIsoChangedListener,
        TransactionDataSource.OnTxAddedListener, SyncManager.onStatusListener, onStatusListener, SwipeRefreshLayout.OnRefreshListener {

    ActivityBreadBinding bindings;
    private Unbinder unbinder;
    private Handler handler = new Handler(Looper.getMainLooper());
    public TxAdapter adapter;
    @BindView(R.id.assets_recycler)
    RecyclerView assetRecycler;
    private MultiTypeDataBoundAdapter assetAdapter;
    private Executor txDataExecutor = Executors.newSingleThreadExecutor();
    private FirebaseAnalytics firebaseAnalytics;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        firebaseAnalytics = FirebaseAnalytics.getInstance(this);
        bindings = DataBindingUtil.setContentView(this, R.layout.activity_bread);
        bindings.assetRefresh.setOnRefreshListener(this);
        bindings.digiSymbolBackground.
                setBackground(AppCompatResources.getDrawable(DigiByte.getContext(),
                        R.drawable.nav_drawer_header));
        bindings.balanceVisibility.setImageResource(
                BRSharedPrefs.getBalanceVisibility(this) ? R.drawable.show_balance : R.drawable.hide_balance);
        bindings.setPagerAdapter(adapter = new TxAdapter(this));
        bindings.txPager.setOffscreenPageLimit(2);
        bindings.txPager.setPageTransformer(true, new CubeOutTransformer());
        bindings.tabLayout.setupWithViewPager(bindings.txPager);
        bindings.contentContainer.getLayoutTransition()
                .enableTransitionType(LayoutTransition.CHANGING);
        ViewUtils.increaceClickableArea(bindings.qrButton);
        ViewUtils.increaceClickableArea(bindings.navDrawer);
        ViewUtils.increaceClickableArea(bindings.digiidButton);
        unbinder = ButterKnife.bind(this);
        Animator animator = AnimatorInflater.loadAnimator(this, R.animator.from_bottom);
        animator.setTarget(bindings.bottomNavigationLayout);
        animator.setDuration(1000);
        animator.setInterpolator(new DecelerateInterpolator());
        animator.start();
        animator = AnimatorInflater.loadAnimator(this, R.animator.from_top);
        animator.setTarget(bindings.tabLayout);
        animator.setDuration(1000);
        animator.setInterpolator(new DecelerateInterpolator());
        animator.start();
        assetAdapter = new MultiTypeDataBoundAdapter(null, (Object[]) null);
        assetRecycler.setLayoutManager(new LinearLayoutManager(this));
        assetRecycler.setAdapter(assetAdapter);
        bindings.drawerLayout.addDrawerListener(new DrawerLayout.SimpleDrawerListener() {
            @Override
            public void onDrawerClosed(View drawerView) {
                bindings.assetRefresh.post(() -> bindings.assetRefresh.setRefreshing(false));
            }
        });
    }

    private Runnable nodeConnectionCheck = new Runnable() {
        @Override
        public void run() {
            txDataExecutor.execute(() -> {
                int connectionStatus = BRPeerManager.connectionStatus();
                handler.post(() -> {
                    switch (connectionStatus) {
                        case 1:
                            if (!bindings.nodeConnectionStatus.isAnimating()) {
                                bindings.nodeConnectionStatus.setMaxFrame(90);
                                bindings.nodeConnectionStatus.playAnimation();
                            }
                            break;
                        case 2:
                            bindings.nodeConnectionStatus.cancelAnimation();
                            bindings.nodeConnectionStatus.setFrame(50);
                            break;
                        default:
                            bindings.nodeConnectionStatus.cancelAnimation();
                            bindings.nodeConnectionStatus.setFrame(150);
                            break;
                    }
                    handler.postDelayed(nodeConnectionCheck, 1000);
                });
            });
        }
    };

    private Runnable showSyncButtonRunnable = new Runnable() {
        @Override
        public void run() {
            bindings.syncButton.setVisibility(View.VISIBLE);
            Flubber.with()
                    .animation(Flubber.AnimationPreset.FLIP_Y)
                    .interpolator(Flubber.Curve.BZR_EASE_IN_OUT_QUAD)
                    .duration(1000)
                    .autoStart(true)
                    .createFor(findViewById(R.id.sync_button));
        }
    };

    @Override
    public void onSyncManagerStarted() {
        handler.postDelayed(showSyncButtonRunnable, 10000);
        CoordinatorLayout.LayoutParams coordinatorLayoutParams =
                (CoordinatorLayout.LayoutParams) bindings.contentContainer.getLayoutParams();
        coordinatorLayoutParams.setBehavior(null);
        bindings.syncContainer.setVisibility(View.VISIBLE);
        bindings.toolbarLayout.setVisibility(View.GONE);
        bindings.animationView.playAnimation();
        updateSyncText();
    }

    @Override
    public void onSyncManagerUpdate() {
        handler.removeCallbacks(showSyncButtonRunnable);
        bindings.syncButton.setVisibility(View.GONE);
        updateSyncText();
    }

    @Override
    public void onSyncManagerFinished() {
        CoordinatorLayout.LayoutParams coordinatorLayoutParams =
                (CoordinatorLayout.LayoutParams) bindings.contentContainer.getLayoutParams();
        coordinatorLayoutParams.setBehavior(new AppBarLayout.ScrollingViewBehavior());
        handler.removeCallbacks(showSyncButtonRunnable);
        bindings.syncButton.setVisibility(View.GONE);
        bindings.syncContainer.setVisibility(View.GONE);
        bindings.toolbarLayout.setVisibility(View.VISIBLE);
        bindings.animationView.cancelAnimation();
    }

    @Override
    public void onSyncFailed() {
        //Do not clear visual sync state when sync fails
        //Gives the wrong impression that sync is complete
        /*CoordinatorLayout.LayoutParams coordinatorLayoutParams =
                (CoordinatorLayout.LayoutParams) bindings.contentContainer.getLayoutParams();
        coordinatorLayoutParams.setBehavior(new AppBarLayout.ScrollingViewBehavior());
        handler.removeCallbacks(showSyncButtonRunnable);
        bindings.syncButton.setVisibility(View.GONE);
        bindings.syncContainer.setVisibility(View.GONE);
        bindings.toolbarLayout.setVisibility(View.VISIBLE);
        bindings.animationView.cancelAnimation();*/
    }

    private void updateSyncText() {
        Locale current = getResources().getConfiguration().locale;
        Date time = new Date(SyncManager.getInstance().getLastBlockTimestamp() * 1000);

        DecimalFormat df = new DecimalFormat("#.##");
        df.setRoundingMode(RoundingMode.CEILING);

        bindings.syncText.setText(SyncManager.getInstance().getLastBlockTimestamp() == 0
                ? DigiByte.getContext().getString(R.string.NodeSelector_statusLabel) + ": "
                + DigiByte.getContext().getString(R.string.SyncingView_connecting)
                : df.format(Double.valueOf(SyncManager.getInstance().getProgress() * 100d)) + "%"
                + " - " + DateFormat.getDateInstance(DateFormat.SHORT, current).format(time)
                + ", " + DateFormat.getTimeInstance(DateFormat.SHORT, current).format(
                time));
    }

    @Override
    public void onTxManagerUpdate(TxItem[] newTxItems) {
        txDataExecutor.execute(() -> {
            BRWalletManager.getInstance().refreshBalance(DigiByte.getContext());
            if (newTxItems == null || newTxItems.length == 0) {
                return;
            }
            ArrayList<ListItemTransactionData> newTransactions =
                    TransactionUtils.getNewTransactionsData(newTxItems);
            ArrayList<ListItemTransactionData> transactionsToAdd = removeAllExistingEntries(
                    newTransactions);
            if (transactionsToAdd.size() > 0) {
                handler.post(() -> {
                    adapter.getAllAdapter().addTransactions(transactionsToAdd);
                    adapter.getSentAdapter().addTransactions(
                            TransactionUtils.
                                    convertNewTransactionsForAdapter(
                                            TransactionUtils.Adapter.SENT,
                                            transactionsToAdd
                                    ));
                    adapter.getReceivedAdapter().addTransactions(
                            TransactionUtils.
                                    convertNewTransactionsForAdapter(
                                            TransactionUtils.Adapter.RECEIVED,
                                            transactionsToAdd
                                    ));
                    adapter.getAllRecycler().smoothScrollToPosition(0);
                    adapter.getSentRecycler().smoothScrollToPosition(0);
                    adapter.getReceivedRecycler().smoothScrollToPosition(0);
                });
            } else {
                handler.post(() -> {
                    adapter.getAllAdapter().updateTransactions(newTransactions);
                    adapter.getSentAdapter().updateTransactions(newTransactions);
                    adapter.getReceivedAdapter().updateTransactions(newTransactions);
                });
            }
            if (isPossibleNewAssetSend(transactionsToAdd)) {
                handler.post(() -> {
                    assetAdapter.clear();
                    assetAdapter.notifyDataSetChanged();
                    bindings.assetRefresh.setRefreshing(true);
                });
                RetrofitManager.instance.clearCache(transactionsToAdd.get(0).transactionItem.getTo());
                processTxAssets(new CopyOnWriteArrayList<>(
                                adapter.getAllAdapter().getTransactions()),
                        true
                );
            } else if (transactionsToAdd.size() > 0) {
                processTxAssets(new CopyOnWriteArrayList<>(transactionsToAdd), false);
            }
        });
    }

    @Override
    public void onRefresh() {
        RetrofitManager.instance.clearCache();
        assetAdapter.clear();
        assetAdapter.notifyDataSetChanged();
        processTxAssets(new CopyOnWriteArrayList<>(
                        adapter.getAllAdapter().getTransactions()),
                true
        );
    }

    private boolean isPossibleNewAssetSend(ArrayList<ListItemTransactionData> newTransactions) {
        return newTransactions.size() == 1;
    }

    private void processTxAssets(List<ListItemTransactionData> transactions, boolean forceAssetMeta) {
        HashSet<AddressTxSet> addresses = new HashSet<>();
        for (ListItemTransactionData transaction : transactions) {
            if (!transaction.transactionItem.isAsset) {
                continue;
            }
            for (String address : transaction.transactionItem.getTo()) {
                if (!TextUtils.isEmpty(address)) {
                    addresses.add(new AddressTxSet(address, transaction));
                }
            }
            for (String address : transaction.transactionItem.getFrom()) {
                if (!TextUtils.isEmpty(address)) {
                    addresses.add(new AddressTxSet(address, transaction));
                }
            }
        }
        LinkedList<AddressTxSet> addressesList = new LinkedList<>(addresses);
        for (int i = 0; i < addressesList.size(); i++) {
            AddressTxSet addressTxSet = addressesList.get(i);
            processIncomingAssets(addressTxSet.address, addressTxSet.listItemTransactionData,
                    i == addressesList.size() - 1, forceAssetMeta);
        }
    }

    private class AddressTxSet {
        String address;
        ListItemTransactionData listItemTransactionData;

        AddressTxSet(String address, ListItemTransactionData listItemTransactionData) {
            this.address = address;
            this.listItemTransactionData = listItemTransactionData;
        }

        @Override
        public boolean equals(@Nullable Object obj) {
            AddressTxSet addressTxSet = (AddressTxSet) obj;
            return address.equals(addressTxSet.address);
        }
    }

    private void processIncomingAssets(@NonNull final String address,
                                       @NonNull final ListItemTransactionData listItemTransactionData,
                                       boolean lastAddress, boolean forceAssetMeta) {
        if (lastAddress) {
            bindings.assetRefresh.post(() -> bindings.assetRefresh.setRefreshing(false));
        }
        if (!BRWalletManager.addressContainedInWallet(address)) {
            return;
        }
        RetrofitManager.instance.getAssets(address, addressInfo -> {
            if (addressInfo == null) {
                return;
            }
            for (final AddressInfo.Asset asset : addressInfo.getAssets()) {
                if (forceAssetMeta) {
                    RetrofitManager.instance.clearMetaCache(asset.assetId);
                }
                RetrofitManager.instance.getAssetMeta(
                        asset.assetId,
                        asset.txid,
                        String.valueOf(asset.getIndex()),
                        new RetrofitManager.MetaCallback() {
                            @Override
                            public void metaRetrieved(MetaModel metalModel) {
                                if (asset.txid.equals(listItemTransactionData.transactionItem.txReversed)) {
                                    Database.instance.saveAssetName(metalModel.metadataOfIssuence.data.assetName,
                                            listItemTransactionData);
                                    AssetModel assetModel = new AssetModel(asset, metalModel);
                                    if (!assetAdapter.containsItem(assetModel)) {
                                        assetAdapter.addItem(assetModel);
                                    }
                                    if (assetModel.isAggregable()) {
                                        addAssetToModel(assetModel, asset);
                                    } else {
                                        assetAdapter.addItem(assetModel);
                                    }
                                    bindings.noAssetsSwitcher.setDisplayedChild(1);
                                    sortAssets();
                                }
                            }

                            @Override
                            public void failure() {
                                Bundle bundle = new Bundle();
                                bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "meta failure");
                                bundle.putString("asset_id", asset.assetId);
                                bundle.putString("txid", asset.txid);
                                bundle.putString("index", String.valueOf(asset.getIndex()));
                                firebaseAnalytics.logEvent("meta_failure", bundle);
                                Toast.makeText(BreadActivity.this, R.string.failure_asset_meta, Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        });
    }

    private void addAssetToModel(final AssetModel assetModel, final AddressInfo.Asset asset) {
        AssetModel existingAssetModel =
                (AssetModel) assetAdapter.getItem(assetModel);
        if (existingAssetModel != null) {
            existingAssetModel.addAsset(asset);
        }
    }

    private ArrayList<ListItemTransactionData> removeAllExistingEntries(
            ArrayList<ListItemTransactionData> newTransactions) {
        return new ArrayList<ListItemTransactionData>(newTransactions) {{
            removeAll(adapter.getAllAdapter().getTransactions());
        }};
    }

    private void updateAmounts() {
        handler.post(() -> ActivityUtils.updateDigibyteDollarValues(
                BreadActivity.this,
                bindings.primaryPrice,
                bindings.secondaryPrice
        ));
    }

    @Override
    public void onStatusUpdate() {
        TxManager.getInstance().updateTxList();
    }

    @Override
    public void onIsoChanged(String iso) {
        updateAmounts();
    }

    @Override
    public void onTxAdded() {
        TxManager.getInstance().updateTxList();
    }

    @Override
    public void onBalanceChanged(final long balance) {
        updateAmounts();
    }

    @Override
    public void showSendConfirmDialog(final String message, final int error, byte[] txHash) {
        BRExecutor.getInstance().forMainThreadTasks().execute(() -> {
            BRAnimator.showBreadSignal(BreadActivity.this,
                    error == 0 ? getString(R.string.Alerts_sendSuccess)
                            : getString(R.string.Alert_error),
                    error == 0 ? getString(R.string.Alerts_sendSuccessSubheader)
                            : message, error == 0 ? R.raw.success_check
                            : R.raw.error_check, () -> {
                        try {
                            getSupportFragmentManager().popBackStack();
                        } catch (IllegalStateException e) {
                        }
                    });
        });
    }

    @OnClick(R.id.balance_visibility)
    void onBalanceVisibilityToggle(View view) {
        BRSharedPrefs.setBalanceVisibility(
                this,
                !BRSharedPrefs.getBalanceVisibility(this)
        );
        bindings.balanceVisibility.setImageResource(
                BRSharedPrefs.getBalanceVisibility(this) ? R.drawable.show_balance : R.drawable.hide_balance);
        updateAmounts();
        notifyDataSetChangeForAll();
    }

    @OnClick(R.id.nav_drawer)
    void onNavButtonClick(View view) {
        try {
            bindings.drawerLayout.openDrawer(GravityCompat.START);
        } catch (IllegalArgumentException e) {
            //Race condition inflating the hierarchy?
        }
    }

    @OnClick(R.id.assets_action)
    public void onAssetsButtonClick(View view) {
        bindings.drawerLayout.openDrawer(GravityCompat.END);
    }

    @OnClick(R.id.main_action)
    void onMenuButtonClick(View view) {
        BRAnimator.showMenuFragment(BreadActivity.this);
    }

    @OnClick(R.id.digiid_button)
    void onDigiIDButtonClick(View view) {
        BRAnimator.openScanner(this);
    }

    @OnClick(R.id.primary_price)
    void onPrimaryPriceClick(View view) {
        BRSharedPrefs.putPreferredBTC(BreadActivity.this, true);
        notifyDataSetChangeForAll();
    }

    @OnClick(R.id.secondary_price)
    void onSecondaryPriceClick(View view) {
        BRSharedPrefs.putPreferredBTC(BreadActivity.this, false);
        notifyDataSetChangeForAll();
    }

    @OnClick(R.id.sync_button)
    void onSyncButtonClick(View view) {
        startActivity(new Intent(BreadActivity.this,
                SyncBlockchainActivity.class));
    }

    @OnClick(R.id.node_connection_status)
    void onNodeConnectionStatusClick(View view) {
        int connectionStatus = BRPeerManager.connectionStatus();
        switch (connectionStatus) {
            case 1:
                Toast.makeText(this, R.string.node_connecting, Toast.LENGTH_SHORT).show();
                break;
            case 2:
                Toast.makeText(this, R.string.node_connected, Toast.LENGTH_SHORT).show();
                break;
            default:
                Toast.makeText(this, R.string.node_disconnected, Toast.LENGTH_SHORT).show();
                break;
        }
    }

    private void notifyDataSetChangeForAll() {
        adapter.getAllAdapter().notifyDataSetChanged();
        adapter.getSentAdapter().notifyDataSetChanged();
        adapter.getReceivedAdapter().notifyDataSetChanged();
    }

    @OnClick(R.id.security_center)
    void onSecurityCenterClick(View view) {
        startActivity(new Intent(this, SecurityCenterActivity.class));
    }

    @OnClick(R.id.settings)
    void onSettingsClick(View view) {
        startActivity(new Intent(this, SettingsActivity.class));
    }


    @OnClick(R.id.lock)
    void onLockClick(View view) {
        BRAnimator.startBreadActivity(this, true);
    }

    @OnClick(R.id.qr_button)
    void onQRClick(View view) {
        BRAnimator.openScanner(this);
    }

    @OnLongClick(R.id.qr_button)
    boolean onQRLongClick() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"),
                BRActivity.QR_IMAGE_PROCESS);
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateAmounts();
        BRWalletManager.getInstance().addBalanceChangedListener(this);
        BRPeerManager.getInstance().addStatusUpdateListener(this);
        BRSharedPrefs.addIsoChangedListener(this);
        TxManager.getInstance().addListener(this);
        SyncManager.getInstance().addListener(this);
        BRWalletManager.getInstance().refreshBalance(this);
        JobsHelper.SyncBlockchainJob.scheduleJob();
        JobsHelper.updateRecurringPaymentJobs();
        TxManager.getInstance().updateTxList();
        BRApiManager.getInstance().asyncUpdateCurrencyData(this);
        SyncManager.getInstance().startSyncingProgressThread();
        handler.postDelayed(nodeConnectionCheck, 1000);
        bindings.nodeConnectionStatus.setFrame(150);
    }

    @Override
    protected void onPause() {
        super.onPause();
        BRWalletManager.getInstance().removeListener(this);
        BRPeerManager.getInstance().removeListener(this);
        BRSharedPrefs.removeListener(this);
        TxManager.getInstance().removeListener(this);
        SyncManager.getInstance().removeListener(this);
        SyncManager.getInstance().stopSyncingProgressThread();
        handler.removeCallbacks(nodeConnectionCheck);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbinder.unbind();
    }

    @Override
    public void onBackPressed() {
        if (bindings.drawerLayout.isDrawerOpen(GravityCompat.START)) {
            handler.post(() -> bindings.drawerLayout.closeDrawer(GravityCompat.START));
        } else if (bindings.drawerLayout.isDrawerOpen(GravityCompat.END)) {
            handler.post(() -> bindings.drawerLayout.closeDrawer(GravityCompat.END));
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void onComplete(AuthType authType) {
        super.onComplete(authType);
        switch (authType.type) {
            case SEND_ASSET:
                if (!authType.sendAsset.isValidAmount()) {
                    Log.d(BreadActivity.class.getSimpleName(), "invalid amount");
                    return;
                }
                Gson gson = new Gson();
                final String payload = gson.toJson(authType.sendAsset);
                Log.d(BRActivity.class.getSimpleName(), payload);
                RetrofitManager.instance.sendAsset(payload, new RetrofitManager.SendAssetCallback() {
                    @Override
                    public void success(SendAssetResponse sendAssetResponse) {
                        Log.d(BreadActivity.class.getSimpleName(), sendAssetResponse.toString());
                        AuthManager.getInstance().authPrompt(BreadActivity.this, null,
                                BreadActivity.this.getString(R.string.VerifyPin_continueBody),
                                new BRAuthCompletion.AuthType(sendAssetResponse, authType.sendAsset));
                    }

                    @Override
                    public void error(String message, Throwable throwable) {
                        Bundle bundle = new Bundle();
                        bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "send asset api");
                        bundle.putString("payload", payload);
                        firebaseAnalytics.logEvent("send_asset_api", bundle);

                        throwable.printStackTrace();
                        Crashlytics.logException(throwable);
                        showSendConfirmDialog(1, TextUtils.isEmpty(message) ? getString(R.string.Alerts_sendFailure) : message);
                    }
                });
                break;
            case ASSET_BROADCAST:
                broadcast(authType.sendAssetResponse, authType.sendAsset);
                break;
        }
    }

    private void broadcast(SendAssetResponse sendAssetResponse, SendAsset sendAsset) {
        try {
            byte[] sendAddressHex = Hex.decodeHex(sendAssetResponse.getTxHex().toCharArray());
            byte[] rawSeed = BRKeyStore.getPhrase(DigiByte.getContext(), BRConstants.ASSETS_REQUEST_CODE);
            byte[] seed = TypesConverter.getNullTerminatedPhrase(rawSeed);
            byte[] transaction = BRWalletManager.parseSignSerialize(sendAddressHex, seed);
            final String txHex = BaseEncoding.base16().encode(transaction);
            Log.d(BRActivity.class.getSimpleName(), "Broadcast Payload: " + txHex);
            RetrofitManager.instance.broadcast(txHex, new RetrofitManager.BroadcastTransaction() {
                @Override
                public void success(String txId) {
                    //TODO need to come back here to ensure the dialog has proper/accurate contextual info
                    if (sendAsset.isCompleteSpend()) {
                        assetAdapter.removeItem(sendAsset.assetModel);
                    }
                    showSendConfirmDialog(0, "");
                    Log.d(BRActivity.class.getSimpleName(), "Broadcast Response: " + txId);
                }

                @Override
                public void onError(String errorMessage) {
                    Bundle bundle = new Bundle();
                    bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "asset tx hex");
                    bundle.putString("tx_hex", txHex);
                    firebaseAnalytics.logEvent("broadcast_asset_failed", bundle);

                    Crashlytics.logException(new Exception(errorMessage));
                    showSendConfirmDialog(1, TextUtils.isEmpty(errorMessage) ? getString(R.string.Alerts_sendFailure) : errorMessage);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showSendConfirmDialog(int error, String message) {
        BRExecutor.getInstance().forMainThreadTasks().execute(() -> {
            BRAnimator.showBreadSignal(BreadActivity.this,
                    error == 0 ? getString(R.string.Alerts_sendSuccess)
                            : getString(R.string.Alert_error),
                    error == 0 ? getString(R.string.Alerts_assetsSendSuccessSubheader)
                            : message, error == 0 ? R.raw.success_check
                            : R.raw.error_check, () -> {
                        try {
                            getSupportFragmentManager().popBackStack();
                        } catch (IllegalStateException e) {
                            e.printStackTrace();
                        }
                    });
        });
    }

    public void sortAssets() {
        List<Object> oldAssets = new LinkedList<>(assetAdapter.getItems());
        Collections.sort(assetAdapter.getItems(), Ordering.usingToString());
        notifyAssetsChange(oldAssets);
    }

    private void notifyAssetsChange(List<Object> oldAssets) {
        DiffUtil.DiffResult result = DiffUtil.calculateDiff(new DiffUtil.Callback() {
            @Override
            public int getOldListSize() {
                return oldAssets.size();
            }

            @Override
            public int getNewListSize() {
                return assetAdapter.getItemCount();
            }

            @Override
            public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
                return oldAssets.get(oldItemPosition).equals(assetAdapter.getItem(newItemPosition));
            }

            @Override
            public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
                AssetModel oldModel = (AssetModel) oldAssets.get(oldItemPosition);
                AssetModel newModel = (AssetModel) assetAdapter.getItem(oldItemPosition);
                return oldModel.getAssetQuantity().equals(newModel.getAssetQuantity());
            }
        }, true);
        result.dispatchUpdatesTo(assetAdapter);
    }
}