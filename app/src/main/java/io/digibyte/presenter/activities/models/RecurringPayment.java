package io.digibyte.presenter.activities.models;

import com.orm.SugarRecord;

import io.digibyte.R;
import io.digibyte.presenter.adapter.LayoutBinding;

public class RecurringPayment extends SugarRecord implements LayoutBinding {

    public String address;
    public String amount;
    public String recurrence;

    public RecurringPayment() {
    }

    public RecurringPayment(String address, String amount, String recurrence) {
        this.address = address;
        this.amount = amount;
        this.recurrence = recurrence;
    }

    @Override
    public int getLayoutId() {
        return R.layout.recurring_payment;
    }
}
