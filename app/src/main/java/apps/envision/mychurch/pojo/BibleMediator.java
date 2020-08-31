package apps.envision.mychurch.pojo;

import androidx.annotation.Nullable;
import androidx.core.util.Pair;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.Observer;

public class BibleMediator extends MediatorLiveData<Pair<String, Integer>> {
    public BibleMediator(LiveData<String> code, LiveData<Integer> nbDays) {
        addSource(code, new Observer<String>() {
            public void onChanged(@Nullable String first) {
                setValue(Pair.create(first, nbDays.getValue()));
            }
        });
        addSource(nbDays, new Observer<Integer>() {
            public void onChanged(@Nullable Integer second) {
                setValue(Pair.create(code.getValue(), second));
            }
        });
    }
}