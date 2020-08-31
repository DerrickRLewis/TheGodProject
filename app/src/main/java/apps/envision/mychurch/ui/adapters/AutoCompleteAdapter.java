package apps.envision.mychurch.ui.adapters;

import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.Filterable;

import org.json.JSONArray;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import static apps.envision.mychurch.utils.Constants.GOOGLE_SEARCH.SEARCH_API;

/**
 * Created by raypower on 8/29/2017.
 */

public class AutoCompleteAdapter extends ArrayAdapter<String> implements Filterable {
    private ArrayList<String> data;
    private boolean lyricSearch = false;

    public AutoCompleteAdapter(@NonNull Context context, @LayoutRes int resource) {
        super(context, resource);
        this.data = new ArrayList<>();
    }

    public void setLyricsSearch(boolean lyricSearch){
        this.lyricSearch = lyricSearch;
    }

    @Override
    public int getCount() {
        return data.size();
    }

    @Nullable
    @Override
    public String getItem(int position) {
        return data.get(position);
    }

    @NonNull
    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                FilterResults results = new FilterResults();
                if (constraint != null) {
                    HttpURLConnection conn = null;
                    InputStream input = null;
                    try {
                        String query_url = String.format(SEARCH_API, Uri.encode(constraint.toString()));
                        if(lyricSearch){
                            query_url = String.format(SEARCH_API, Uri.encode(constraint.toString() + " lyrics"));
                        }
                        Log.e("search_url",query_url);
                        URL url = new URL(query_url + constraint.toString());
                        conn = (HttpURLConnection) url.openConnection();
                        input = conn.getInputStream();
                        InputStreamReader reader = new InputStreamReader(input, "UTF-8");
                        BufferedReader buffer = new BufferedReader(reader, 8192);
                        StringBuilder builder = new StringBuilder();
                        String line;
                        while ((line = buffer.readLine()) != null) {
                            builder.append(line);
                        }
                        JSONArray terms = new JSONArray(builder.toString());
                        //Log.e("terms",String.valueOf(terms));
                        JSONArray res_data = new JSONArray(terms.getString(1));
                        //Log.e("results",String.valueOf(res_data));
                       // Log.e("length",String.valueOf(res_data.length()));
                        ArrayList<String> suggestions = new ArrayList<>();
                        for (int ind = 0; ind < res_data.length(); ind++) {
                            String term = res_data.getString(ind);
                            //Log.e("term",term);
                            suggestions.add(term);
                        }
                        results.values = suggestions;
                        results.count = suggestions.size();
                        data = suggestions;
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    } finally {
                        if (input != null) {
                            try {
                                input.close();
                            } catch (Exception ex) {
                                ex.printStackTrace();
                            }
                        }
                        if (conn != null) conn.disconnect();
                    }
                }
                return results;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                if (results != null && results.count > 0) {
                    notifyDataSetChanged();
                } else notifyDataSetInvalidated();
            }
        };
    }
}