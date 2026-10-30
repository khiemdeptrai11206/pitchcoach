package com.pitchcoach.app.data;

import android.content.Context;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.pitchcoach.app.model.PresentationItem;

import java.util.ArrayList;
import java.util.List;

public class AllPresentationsViewModel extends ViewModel {
    private final MutableLiveData<List<PresentationItem>> items = new MutableLiveData<>();

    public void init(Context context) {
        if (items.getValue() == null) {
            items.setValue(SampleData.createPresentations(context));
        }
    }

    public LiveData<List<PresentationItem>> getItems() {
        return items;
    }

    public void delete(String title) {
        List<PresentationItem> current = items.getValue();
        if (current == null) {
            return;
        }
        List<PresentationItem> updated = new ArrayList<>();
        for (PresentationItem item : current) {
            if (!item.title.equals(title)) {
                updated.add(item);
            }
        }
        items.setValue(updated);
    }
}
