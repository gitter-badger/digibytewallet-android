package io.digibyte.presenter.fragments.models;

import android.databinding.BaseObservable;
import android.databinding.Bindable;
import android.databinding.BindingAdapter;
import android.support.v7.widget.RecyclerView;

import io.digibyte.R;
import io.digibyte.databinding.LetterColumnBinding;
import io.digibyte.presenter.adapter.DataBoundViewHolder;
import io.digibyte.presenter.adapter.DynamicBinding;
import io.digibyte.presenter.adapter.LayoutBinding;
import io.digibyte.presenter.adapter.MultiTypeDataBoundAdapter;

public class LetterColumnViewModel extends BaseObservable implements LayoutBinding, DynamicBinding {

    private MultiTypeDataBoundAdapter adapter;

    public LetterColumnViewModel(MultiTypeDataBoundAdapter adapter) {
        this.adapter = adapter;
    }

    @Override
    public int getLayoutId() {
        return R.layout.letter_column;
    }

    @Bindable
    public MultiTypeDataBoundAdapter getAdapter() {
        return adapter;
    }

    @BindingAdapter({"adapter"})
    public static void setAdapter(RecyclerView recyclerView, MultiTypeDataBoundAdapter adapter) {
        recyclerView.setAdapter(adapter);
    }

    @Override
    public void bind(DataBoundViewHolder holder) {
        LetterColumnBinding binding = (LetterColumnBinding) holder.binding;
        binding.bubbleScrollBar.attachToRecyclerView(binding.recycler);
        binding.bubbleScrollBar.setBubbleTextProvider(i -> ((PhraseViewModel) adapter.getItem(
                i)).getPhrase());
    }
}