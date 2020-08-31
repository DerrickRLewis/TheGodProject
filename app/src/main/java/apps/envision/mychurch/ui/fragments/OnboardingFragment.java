package apps.envision.mychurch.ui.fragments;


import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.fragment.app.Fragment;

import apps.envision.mychurch.R;
import apps.envision.mychurch.db.PreferenceSettings;
import apps.envision.mychurch.ui.activities.LoginActivity;


/**
 * A simple {@link Fragment} subclass.
 */
public class OnboardingFragment extends Fragment {

    private View view;

    public OnboardingFragment() {
        // Required empty public constructor
    }

    /**
     * @return
     */
    public static OnboardingFragment newInstance(int position) {
        OnboardingFragment onboardingFragment = new OnboardingFragment();
        Bundle args = new Bundle();
        args.putInt("position", position);
        onboardingFragment.setArguments(args);
        return onboardingFragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        Bundle args = getArguments();
        assert args != null;
        int position = args.getInt("position");
        switch (position){
            case 0: default:
                view = inflater.inflate(R.layout.wizard_item, container, false);
                return view;
            case 1:
                view = inflater.inflate(R.layout.wizard_item2, container, false);
                return view;
            case 2:
                view = inflater.inflate(R.layout.wizard_item3, container, false);
                return view;
            case 3:
                view = inflater.inflate(R.layout.wizard_item4, container, false);
                Button create_account = view.findViewById(R.id.create_account);
                if(create_account!=null){
                    create_account.setOnClickListener(v ->{
                        startLoginActivity();
                    });
                }
                return view;

        }
    }

    private void startLoginActivity(){
        PreferenceSettings.setOnboardingCompleted(true);
        Intent intent = new Intent(getActivity(), LoginActivity.class);
        intent.putExtra("onboarding", "true");
        startActivity(intent);
        if(getActivity()!=null) {
            getActivity().finish();
        }
    }

}
