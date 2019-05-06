package io.digibyte.presenter.fragments.models

import io.digibyte.R
import io.digibyte.presenter.adapter.LayoutBinding

class PhraseViewModel(val phrase: String) : LayoutBinding {
    override fun getLayoutId(): Int {
        return R.layout.phrase_view
    }
}
