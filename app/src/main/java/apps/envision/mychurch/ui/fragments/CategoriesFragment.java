package apps.envision.mychurch.ui.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import apps.envision.mychurch.App;
import apps.envision.mychurch.api.NetworkService;
import apps.envision.mychurch.api.StringApiClient;
import apps.envision.mychurch.R;
import apps.envision.mychurch.db.DataViewModel;
import apps.envision.mychurch.db.PreferenceSettings;
import apps.envision.mychurch.interfaces.CategoriesClickListener;
import apps.envision.mychurch.libs.pullrefresh.PullRefreshLayout;
import apps.envision.mychurch.pojo.Categories;
import apps.envision.mychurch.ui.activities.CategoriesActivity;
import apps.envision.mychurch.ui.adapters.CategoriesFragmentAdapter;
import apps.envision.mychurch.utils.Constants;
import apps.envision.mychurch.utils.JsonParser;
import apps.envision.mychurch.utils.NetworkUtil;
import apps.envision.mychurch.utils.TimUtil;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CategoriesFragment extends Fragment implements View.OnClickListener, CategoriesClickListener {

    private PullRefreshLayout pullRefreshLayout;
    private View view;
    private CategoriesFragmentAdapter categoriesFragmentAdapter;
    private DataViewModel dataViewModel;
    private boolean fragmentVisible = false;

    public CategoriesFragment() {
        // Required empty public constructor
    }

    public static CategoriesFragment newInstance() {
        return new CategoriesFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.categories_fragment, container, false);
        init_categories_view();

        // Get a new or existing ViewModel from the ViewModelProvider.
        dataViewModel = ViewModelProviders.of(this).get(DataViewModel.class);
        // Add an observer on the LiveData.
        // The onChanged() method fires when the observed data changes and the activity is
        // in the foreground.

        //observe categories data changes
        dataViewModel.getCategories().observe(this, categories -> {
            // Update the cached copy of the media list in the adapter.
            if(categories==null || categories.size()==0){
                categoriesFragmentAdapter.setInfo(App.getContext().getString(R.string.no_data));
            }else {
                categoriesFragmentAdapter.setAdapter(categories);
            }
        });

        init_pullrefresh();
        return view;
    }

    //init pullrefresh
    private void init_pullrefresh() {
        pullRefreshLayout = view.findViewById(R.id.pullRefreshLayout);
        int[] colorScheme = getResources().getIntArray(R.array.refresh_color_scheme);
        pullRefreshLayout.setRefreshStyle();
        pullRefreshLayout.setColorSchemeColors(colorScheme);
        // listen refresh event
        pullRefreshLayout.setOnRefreshListener(this::fetchData);

        if(TimUtil.hasElapsed(PreferenceSettings.getCategoriesFragmentLastRefreshTime(), Constants.ONE_DAY)) {
            pullRefreshLayout.setRefreshing(true);
            fetchData();
        }
    }

    //init categories recyclerview
    private void  init_categories_view(){
        RecyclerView categories = view.findViewById(R.id.categories);
        categoriesFragmentAdapter = new CategoriesFragmentAdapter(this);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(getActivity(), 2);
        gridLayoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                return categoriesFragmentAdapter.getSpanCount(position);
            }
        });
        categories.setLayoutManager(gridLayoutManager);
        categories.setItemAnimator(new DefaultItemAnimator());
        categories.setAdapter(categoriesFragmentAdapter);
    }


    @Override
    public void onClick(View view) {
        int id = view.getId();
    }

    private void fetchData() {
        if(getActivity()==null)return; //if activity have been destroyed, return command
        clear_database_data();
        if(!NetworkUtil.hasConnection(getActivity())){
            setErrorView();
            return;
        }
        NetworkService service = StringApiClient.createServiceWithToken(NetworkService.class);
        Call<String> callAsync = service.categories();

        callAsync.enqueue(new Callback<String>() {
            @Override
            public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                Log.d("response",String.valueOf(response.body()));
                pullRefreshLayout.setRefreshing(false);
                if(response.body()==null){
                    setErrorView();
                    return;
                }
                try {
                    JSONObject jsonObj = new JSONObject(response.body());
                    String status = jsonObj.getString("status");
                    if (status.equalsIgnoreCase("ok")) {
                        dataViewModel.insertAllCategories(JsonParser.getCategories(jsonObj.getJSONArray("categories")));
                        PreferenceSettings.setCategoriesFragmentLastRefreshTime(System.currentTimeMillis());
                    }
                } catch (JSONException e) {
                    Log.d("json parse Error", e.toString());
                    categoriesFragmentAdapter.setInfo(App.getContext().getString(R.string.no_data));
                }
            }

            @Override
            public void onFailure(@NonNull Call<String> call, @NonNull Throwable throwable) {
                Log.e("error",String.valueOf(throwable.getMessage()));
                setErrorView();
            }
        });
    }

    private void setErrorView(){
        if(!fragmentVisible)return;
        pullRefreshLayout.setRefreshing(false);
        categoriesFragmentAdapter.setInfo(App.getContext().getString(R.string.no_data));
    }

    private void clear_database_data(){
        dataViewModel.deleteAllCategories();
    }
    @Override
    public void OnItemClick(Categories categories) {
        Gson gson = new Gson();
        String myJson = gson.toJson(categories);
        Intent intent = new Intent(getActivity(), CategoriesActivity.class);
        intent.putExtra("category", myJson);
        startActivity(intent);
    }

    @Override
    public void onStart() {
        fragmentVisible = true;
        super.onStart();
    }

    @Override
    public void onDestroy() {
        fragmentVisible = false;
        super.onDestroy();
    }

    @Override
    public void onDetach() {
        fragmentVisible = false;
        super.onDetach();
    }
}
