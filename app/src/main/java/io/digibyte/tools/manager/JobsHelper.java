package io.digibyte.tools.manager;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.evernote.android.job.Job;
import com.evernote.android.job.JobCreator;
import com.evernote.android.job.JobManager;
import com.evernote.android.job.JobRequest;
import com.evernote.android.job.util.support.PersistableBundleCompat;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.digibyte.DigiByte;
import io.digibyte.R;
import io.digibyte.presenter.activities.LoginActivity;
import io.digibyte.presenter.activities.RecurringPaymentsActivity;
import io.digibyte.presenter.activities.models.RecurringPayment;
import io.digibyte.wallet.BRWalletManager;

public class JobsHelper {
    private static final long SYNC_PERIOD = TimeUnit.HOURS.toMillis(24);

    public static class DigiByteJobCreator implements JobCreator {

        @Override
        @Nullable
        public Job create(@NonNull String tag) {
            switch (tag) {
                case SyncBlockchainJob.TAG:
                    return new SyncBlockchainJob();
                case RecurringPaymentJob.TAG:
                    return new RecurringPaymentJob();
                default:
                    return null;
            }
        }
    }

    public static class SyncBlockchainJob extends Job {

        public static final String TAG = "sync_blockchain_job";

        @Override
        @NonNull
        protected Result onRunJob(Params params) {
            BRWalletManager.getInstance().init();
            return Result.SUCCESS;
        }

        public static void scheduleJob() {
            JobManager.instance().cancelAllForTag(SyncBlockchainJob.TAG);
            new JobRequest.Builder(SyncBlockchainJob.TAG)
                    .setPeriodic(SYNC_PERIOD).setRequiredNetworkType(
                    JobRequest.NetworkType.UNMETERED).setRequiresCharging(true)
                    .build()
                    .schedule();
        }
    }

    public static class RecurringPaymentJob extends Job {

        public static final String TAG = "recurring_payment_blockchain_job";

        @Override
        @NonNull
        protected Result onRunJob(Params params) {
            RecurringPayment recurringPayment = RecurringPayment.findById(RecurringPayment.class, params.getExtras().getLong("id", -1));
            NotificationManager notificationManager =
                    (NotificationManager) getContext().getSystemService(Context.NOTIFICATION_SERVICE);
            if (Build.VERSION.SDK_INT >= 26) {
                NotificationChannel channel = new NotificationChannel("recurring", "Recurring Payments", NotificationManager.IMPORTANCE_DEFAULT);
                channel.setDescription("Recurring Payments Notifications");
                notificationManager.createNotificationChannel(channel);
            }
            NotificationCompat.Builder builder = new NotificationCompat.Builder(getContext(), "recurring");
            builder.setSmallIcon(R.mipmap.ic_launcher);
            builder.setAutoCancel(true);
            builder.setTicker(getContext().getString(R.string.recurring_payments));
            builder.setContentTitle(getContext().getString(R.string.recurring_payments));
            builder.setContentText(recurringPayment.address + ", " + recurringPayment.amount + ", " + recurringPayment.recurrence);
            Intent intent = new Intent(getContext(), LoginActivity.class);
            Uri uri = Uri.parse("digibyte://" + recurringPayment.address + "?amount=" + recurringPayment.amount);
            intent.setData(uri);
            PendingIntent pendingIntent = PendingIntent.getActivity(getContext(), recurringPayment.getId().intValue(), intent, PendingIntent.FLAG_CANCEL_CURRENT);
            builder.setContentIntent(pendingIntent);
            notificationManager.notify(recurringPayment.getId().intValue(), builder.build());
            return Result.SUCCESS;
        }
    }

    public static void cancelRecurringPayment(RecurringPayment recurringPayment) {
        JobManager.instance().cancelAllForTag(recurringPayment.address);
    }

    public static void updateRecurringPaymentJobs() {
        List<RecurringPayment> recurringPaymentList = RecurringPayment.listAll(RecurringPayment.class);
        for (RecurringPayment recurringPayment : recurringPaymentList) {
            scheduleRecurringPayment(recurringPayment);
        }
    }

    public static void scheduleRecurringPayment(RecurringPayment recurringPayment) {
        cancelRecurringPayment(recurringPayment);
        PersistableBundleCompat persistableBundleCompat = new PersistableBundleCompat();
        persistableBundleCompat.putLong("id", recurringPayment.getId());
        Log.d(DigiByte.class.getSimpleName(), "Job ID: " + recurringPayment.getId());
        switch (RecurringPaymentsActivity.Schedule.valueOf(recurringPayment.recurrence)) {
            case DAILY: {
                Calendar calendar = Calendar.getInstance();
                calendar.add(Calendar.DAY_OF_YEAR, 1);
                Date date = calendar.getTime();

                int jobId = new JobRequest.Builder(RecurringPaymentJob.TAG)
                        .setExtras(persistableBundleCompat)
                        .setPeriodic(date.getTime() - System.currentTimeMillis(), TimeUnit.HOURS.toMillis(1))
                        .build()
                        .schedule();
                Log.d(DigiByte.class.getSimpleName(), "Job ID: " + jobId);
            }
            case WEEKLY: {
                Calendar now = Calendar.getInstance();
                int weekday = now.get(Calendar.DAY_OF_WEEK);
                if (weekday != Calendar.MONDAY) {
                    int days = (Calendar.SATURDAY - weekday + 2) % 7;
                    now.add(Calendar.DAY_OF_YEAR, days);
                }
                Date date = now.getTime();
                int jobId = new JobRequest.Builder(RecurringPaymentJob.TAG)
                        .setExtras(persistableBundleCompat)
                        .setPeriodic(date.getTime() - System.currentTimeMillis(), TimeUnit.HOURS.toMillis(1))
                        .build()
                        .schedule();
                Log.d(DigiByte.class.getSimpleName(), "Job ID: " + jobId);
            }
            case MONTHLY: {
                Calendar cal = Calendar.getInstance();
                cal.add(Calendar.MONTH, 1);
                cal.set(Calendar.DAY_OF_MONTH, 1);
                Date date = cal.getTime();
                int jobId = new JobRequest.Builder(RecurringPaymentJob.TAG)
                        .setExtras(persistableBundleCompat)
                        .setPeriodic(date.getTime() - System.currentTimeMillis(), TimeUnit.HOURS.toMillis(1))
                        .build()
                        .schedule();
                Log.d(DigiByte.class.getSimpleName(), "Job ID: " + jobId);
            }
        }
    }
}
