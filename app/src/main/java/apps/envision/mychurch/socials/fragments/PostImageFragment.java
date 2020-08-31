package apps.envision.mychurch.socials.fragments;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import apps.envision.mychurch.R;
import apps.envision.mychurch.utils.ImageLoader;
import apps.envision.mychurch.utils.TouchImageView;
import apps.envision.mychurch.utils.Utility;

/**
 * A simple {@link Fragment} subclass.
 */
public class PostImageFragment extends Fragment {

    private static final String ARG_PARAM1 = "param1";
    // TODO: Rename and change types of parameters
    private String url;
    private View view;

    public PostImageFragment() {
        // Required empty public constructor
    }

    public static PostImageFragment newInstance(String url) {
        PostImageFragment fragment = new PostImageFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, url);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            url = getArguments().getString(ARG_PARAM1);
        }
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_post_image, container, false);
        ImageView image = view.findViewById(R.id.image);
        //Log.e("url",url);
        ImageLoader.loadPostImage(image, url);

        image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //new Utility(getActivity()).showLargeCoverPhoto(url);
            }
        });
        return view;
    }
}
