package io.digibyte.presenter.fragments.interfaces;

import io.digibyte.presenter.adapter.MultiTypeDataBoundAdapter;

public interface PhraseCompleteCallback extends MultiTypeDataBoundAdapter.ActionCallback {
    void phrase(String string);
}
