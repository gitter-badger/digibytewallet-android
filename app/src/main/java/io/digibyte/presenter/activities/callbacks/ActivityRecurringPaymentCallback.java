package io.digibyte.presenter.activities.callbacks;

import io.digibyte.presenter.activities.models.RecurringPayment;
import io.digibyte.presenter.adapter.MultiTypeDataBoundAdapter;

public interface ActivityRecurringPaymentCallback extends MultiTypeDataBoundAdapter.ActionCallback {
    void onSetTimeClick();

    void onAddClick();

    void onRecurringPaymentClick(RecurringPayment recurringPayment);
}
