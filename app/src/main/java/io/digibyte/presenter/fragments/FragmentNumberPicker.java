package io.digibyte.presenter.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;

import io.digibyte.R;
import io.digibyte.presenter.activities.models.SendAsset;
import io.digibyte.presenter.customviews.BRKeyboard;
import io.digibyte.presenter.activities.callbacks.BRAuthCompletion;

public class FragmentNumberPicker extends FragmentPin implements View.OnClickListener {

    public static void show(AppCompatActivity activity, BRAuthCompletion.AuthType type) {
        FragmentNumberPicker fragmentNumberPicker = new FragmentNumberPicker();
        //We cannot use fragment arguments for asset AuthType as it's potentially too large
        //We override getType in this implementation of Fragment pin, and pass it directly into the
        //fragment. We also pop the back stack in onActivityCreated if there's a saved state,
        //because in such a scenario the AuthType will not be stored in the arguments, thus unavailable.
        fragmentNumberPicker.setAuthType(type);
        FragmentTransaction transaction = activity.getSupportFragmentManager().beginTransaction();
        transaction.setCustomAnimations(R.animator.from_bottom, R.animator.to_bottom,
                R.animator.from_bottom, R.animator.to_bottom);
        transaction.add(android.R.id.content, fragmentNumberPicker,
                FragmentNumberPicker.class.getName());
        transaction.addToBackStack(FragmentNumberPicker.class.getName());
        transaction.commitAllowingStateLoss();
    }

    private BRAuthCompletion.AuthType authType = null;

    private void setAuthType(BRAuthCompletion.AuthType authType) {
        this.authType = authType;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);
        view.findViewById(R.id.dialogLayout).setVisibility(View.GONE);
        view.findViewById(R.id.quantity_view).setVisibility(View.VISIBLE);
        view.findViewById(R.id.complete).setOnClickListener(this);
        ((BRKeyboard) view.findViewById(R.id.brkeyboard)).setShowDot(true);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (authType == null) {
            getFragmentManager().popBackStack();
        }
    }

    @Override
    protected boolean onlyDigits() {
        return false;
    }

    @Override
    protected void handleDigitClick(@NonNull String dig) {
        pin.append(dig);
        updateDots();
    }

    @Override
    protected void updateDots() {
        // Not updating dots for just number entry
        ((TextView) getView().findViewById(R.id.quantity_edit)).setText(pin.toString());
    }

    @Override
    public void onClick(View v) {
        fadeOutRemove(true);
    }

    @Override
    protected BRAuthCompletion.AuthType getType() {
        if (pin.length() > 0) {
            try {
                double quantity = Double.parseDouble(pin.toString());
                quantity = quantity * Math.pow(10, authType.sendAsset.divisibility);
                authType.sendAsset.setQuantity(Double.valueOf(quantity).intValue());
            } catch (NumberFormatException e) {
                authType.sendAsset.setQuantity(SendAsset.INVALID_AMOUNT);
                e.printStackTrace();
            }
        }
        return authType;
    }
}