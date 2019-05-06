package io.digibyte.presenter.fragments.interfaces;

import io.digibyte.presenter.adapter.MultiTypeDataBoundAdapter;

public interface PhraseCallback extends MultiTypeDataBoundAdapter.ActionCallback {
    void onClick(String string);
}
