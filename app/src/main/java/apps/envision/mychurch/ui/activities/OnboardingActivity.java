package apps.envision.mychurch.ui.activities;


import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;
import apps.envision.mychurch.R;
import apps.envision.mychurch.db.PreferenceSettings;
import apps.envision.mychurch.libs.PagerIndicator;
import apps.envision.mychurch.libs.ViewPagerScroller;
import apps.envision.mychurch.libs.sharedelement.DefaultSePageTransformer;
import apps.envision.mychurch.libs.sharedelement.SePageTransformer;
import apps.envision.mychurch.libs.viewpagertransformers.AccordionTransformer;
import apps.envision.mychurch.libs.viewpagertransformers.StackTransformer;
import apps.envision.mychurch.pojo.Items;
import apps.envision.mychurch.ui.fragments.OnboardingFragment;
import apps.envision.mychurch.utils.Utility;

public class OnboardingActivity extends AppCompatActivity {

    private ViewPager viewPager;
    public final OnboardingFragment onboarding_1 = OnboardingFragment.newInstance(0);
    public final OnboardingFragment onboarding_2 = OnboardingFragment.newInstance(1);
    public final OnboardingFragment onboarding_3 = OnboardingFragment.newInstance(2);
    public final OnboardingFragment onboarding_4 = OnboardingFragment.newInstance(3);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_onboarder);
        init();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    private void init(){
        viewPager = (ViewPager) findViewById(R.id.viewpager);
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                FloatingActionButton fab = findViewById(R.id.fab);
                if(position==3){
                    fab.hide();
                }else{
                    fab.show();
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
        adapter.addFragment(0, onboarding_1);
        adapter.addFragment(1, onboarding_2);
        adapter.addFragment(2, onboarding_3);
        adapter.addFragment(3, onboarding_4);
        viewPager.setAdapter(adapter);

        PagerIndicator dots_indicator = findViewById(R.id.dots_indicator);
        dots_indicator.setViewPager(viewPager);
    }

    @Override
    protected void onStart() {
        super.onStart();

        ArrayList<Fragment> fragments = new ArrayList<>();
        fragments.add(onboarding_1);
        fragments.add(onboarding_2);
        fragments.add(onboarding_3);
        fragments.add(onboarding_4);

        SePageTransformer transformer = new DefaultSePageTransformer(this,  fragments, viewPager);
        // Uncomment this to try experimental page transformer
        // SePageTransformer transformer = new AuxiliarySePageTransformer(this,  fragments, viewPager, findViewById(R.id.main_root));

        transformer.addTransition(R.id.thumbnail_1, R.id.thumbnail_2);
        transformer.addTransition(R.id.thumbnail_2, R.id.thumbnail_3);
        transformer.addTransition(R.id.thumbnail_3, R.id.thumbnail_4);

        transformer.addTransition(R.id.title_1, R.id.title_2);
        transformer.addTransition(R.id.title_2, R.id.title_3);
        transformer.addTransition(R.id.title_3, R.id.title_4);

        transformer.addTransition(R.id.hint_1, R.id.hint_2);
        transformer.addTransition(R.id.hint_2, R.id.hint_3);
        transformer.addTransition(R.id.hint_3, R.id.hint_4);

        viewPager.setPageTransformer(false, transformer);
        viewPager.addOnPageChangeListener(transformer);
    }

    public void navigate(View view){
        int pos = viewPager.getCurrentItem();
        if (pos == 3) {
            startLoginActivity();
        }else{
            viewPager.setCurrentItem(pos + 1);
        }
    }

    public void skip(View view){
        startLoginActivity();
    }

    private void startLoginActivity(){
        //PreferenceSettings.setOnboardingCompleted(true_);
        Intent intent = new Intent(OnboardingActivity.this,LoginActivity.class);
        intent.putExtra("onboarding", "true_");
        startActivity(intent);
        finish();
    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        startLoginActivity();
    }

    public class ViewPagerAdapter extends FragmentPagerAdapter {
        private final List<Fragment> fragments = new ArrayList<>();
        private final Map itemList = new HashMap<>();
        private int size;

        public ViewPagerAdapter(FragmentManager manager) {
            super(manager);
        }
        @Override
        public Fragment getItem(int position) {
            return fragments.get(position);
        }

        @Override
        public int getCount() {
            return fragments.size();
        }

        public void addFragment(Fragment fragment) {
            fragments.add(fragment);
        }

        public void addFragment(int index, Fragment fragment) {
            fragments.add(index, fragment);
        }

    }
}
