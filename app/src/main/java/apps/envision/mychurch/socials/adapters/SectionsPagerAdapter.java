package apps.envision.mychurch.socials.adapters;

import android.content.Context;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import apps.envision.mychurch.socials.fragments.BlankFragment;
import apps.envision.mychurch.socials.fragments.PeopleFragment;
import apps.envision.mychurch.socials.fragments.SocialSettingsFragment;
import apps.envision.mychurch.socials.fragments.UserNotificationsFragment;
import apps.envision.mychurch.socials.fragments.UserPostsFragment;
import apps.envision.mychurch.socials.fragments.BioInfoFragment;


/**
 * A [FragmentPagerAdapter] that returns a fragment corresponding to
 * one of the sections/tabs/pages.
 */
public class SectionsPagerAdapter extends FragmentPagerAdapter {

    public SectionsPagerAdapter(Context context, FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        // getItem is called to instantiate the fragment for the given page.
        // Return a PlaceholderFragment (defined as a static inner class below).
        switch (position){
            case 0: default:
                return UserPostsFragment.newInstance();
            case 1:
                return BlankFragment.newInstance("hello","world");
            case 2:
                return PeopleFragment.newInstance();
            case 3:
                return UserNotificationsFragment.newInstance();
            case 4:
                return SocialSettingsFragment.newInstance();
        }
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        return "";
    }

    @Override
    public int getCount() {
        // Show 2 total pages.
        return 5;
    }
}