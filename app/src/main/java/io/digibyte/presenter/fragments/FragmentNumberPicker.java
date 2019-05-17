package io.digibyte.presenter.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;

import io.digibyte.R;
import io.digibyte.presenter.interfaces.BRAuthCompletion;

public class FragmentNumberPicker extends FragmentPin implements View.OnClickListener {

    public static void show(AppCompatActivity activity, BRAuthCompletion.AuthType type) {
        FragmentNumberPicker fragmentNumberPicker = new FragmentNumberPicker();
        Bundle args = new Bundle();
        args.putSerializable(AUTH_TYPE, type);
        fragmentNumberPicker.setArguments(args);
        FragmentTransaction transaction = activity.getSupportFragmentManager().beginTransaction();
        transaction.setCustomAnimations(R.animator.from_bottom, R.animator.to_bottom,
                R.animator.from_bottom, R.animator.to_bottom);
        transaction.add(android.R.id.content, fragmentNumberPicker,
                FragmentNumberPicker.class.getName());
        transaction.addToBackStack(FragmentNumberPicker.class.getName());
        transaction.commitAllowingStateLoss();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);
        view.findViewById(R.id.dialogLayout).setVisibility(View.GONE);
        view.findViewById(R.id.quantity_view).setVisibility(View.VISIBLE);
        view.findViewById(R.id.complete).setOnClickListener(this);
        return view;
    }

    @Override
    protected void handleDigitClick(@NonNull String dig) {
        pin.append(dig);
        updateDots();
    }

    @Override
    protected void updateDots() {
        ((TextView) getView().findViewById(R.id.quantity_edit)).setText(pin.toString());
        // Not updating dots for just number entry
    }

    @Override
    public void onClick(View v) {
        fadeOutRemove(true);
    }

    @Override
    protected BRAuthCompletion.AuthType getType() {
        BRAuthCompletion.AuthType authType = super.getType();
        if (pin.length() == 0) {
            try {
                authType.sendAsset.setQuantity(Integer.parseInt(pin.toString()));
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
        }
        return authType;
    }
}