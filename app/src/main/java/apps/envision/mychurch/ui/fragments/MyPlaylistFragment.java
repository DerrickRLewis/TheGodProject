package apps.envision.mychurch.ui.fragments;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;

import apps.envision.mychurch.App;
import apps.envision.mychurch.R;


/**
 * A simple {@link Fragment} subclass.
 */
public class MyPlaylistFragment extends Fragment{


    private View view;

    /**
     * @return
     */
    public static MyPlaylistFragment newInstance() {
        return new MyPlaylistFragment();
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        // your stuff or nothing
    }

    /**
     * @param inflater
     * @param container
     * @param savedInstanceState
     * @return
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.my_playlist_fragment, container, false);
        init_views();
        return view;
    }

    private void init_views(){
        ViewPager pager=(ViewPager) view.findViewById(R.id.pager);
        pager.setAdapter(new FragmentAdapter(getChildFragmentManager()));
        TabLayout tabLayout = (TabLayout) view.findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(pager);
        pager.setOffscreenPageLimit(2);
    }

    public class FragmentAdapter extends FragmentPagerAdapter {

        public FragmentAdapter(@NonNull FragmentManager fm) {
            super(fm);
        }

        @Override
        public int getCount() {
            return 2;
        }

        @Override
        public Fragment getItem(int position) {
            String media_type = position == 0 ? App.getContext().getString(R.string.audio) : App.getContext().getString(R.string.video);
            return (PlaylistFragment.newInstance(media_type));
        }

        @Override
        public String getPageTitle(int position) {
            return position == 0 ? App.getContext().getString(R.string.audios) : App.getContext().getString(R.string.videos);
        }
    }
}

