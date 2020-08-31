package apps.envision.mychurch.ui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.airbnb.lottie.LottieAnimationView;

import java.util.ArrayList;
import java.util.List;

import apps.envision.mychurch.App;
import apps.envision.mychurch.R;
import apps.envision.mychurch.interfaces.NotesListener;
import apps.envision.mychurch.libs.RotateLoading;
import apps.envision.mychurch.pojo.Header;
import apps.envision.mychurch.pojo.Info;
import apps.envision.mychurch.pojo.Note;
import apps.envision.mychurch.ui.viewholders.NotesViewHolder;


public class NotesAdapter extends RecyclerView.Adapter< RecyclerView.ViewHolder>{


    private List<Object> items = new ArrayList<>();
    private final int VIEW_TYPE_NOTE = 0;
    private final int VIEW_TYPE_HEADER = 1;
    private final int VIEW_TYPE_INFO = 2;

    private NotesListener notesListener;

    public NotesAdapter(NotesListener notesListener) {
        this.notesListener = notesListener;
    }

    /**
     * setadapter with media arraylist
     * @param data
     */
    public void setAdapter(List<Note> data) {
        items = new ArrayList<>();
       // items.add(new Header(""));
        items.addAll(data);
        this.notifyDataSetChanged();
    }

    public void setSearchAdapter(List<Note> data) {
        items = new ArrayList<>();
        items.addAll(data);
        this.notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return items != null ? items.size() : 0;
    }

    @Override
    public int getItemViewType(int position) {
        Object object = items.get(position);
        if(object instanceof Header){
            return VIEW_TYPE_HEADER;
        }else if(object instanceof Info){
            return VIEW_TYPE_INFO;
        }else {
            return VIEW_TYPE_NOTE;
        }
    }

    /**
     * add info item
     */
    public void setInfo(String message) {
        items = new ArrayList<>();
        items.add(new Info(message));
        this.notifyDataSetChanged();
        this.notifyItemInserted(items.size() - 1);
    }

    /**
     * create view holders
     * @param viewGroup
     * @param i
     * @return
     */
    @Override
    public  RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        RecyclerView.ViewHolder viewHolder = null;
        LayoutInflater inflater = LayoutInflater.from(viewGroup.getContext());
        switch (i) {
            case VIEW_TYPE_NOTE:
                View v = inflater.inflate(R.layout.notes_item, viewGroup, false);
                viewHolder = new NotesViewHolder(v,notesListener);
                viewHolder.itemView.setClickable(true);
                break;
            case VIEW_TYPE_INFO:
                View vI = inflater.inflate(R.layout.notes_info, viewGroup, false);
                viewHolder = new InfoViewHolder(vI);
                break;
            case VIEW_TYPE_HEADER:
                View v1 = inflater.inflate(R.layout.add_playlist, viewGroup, false);
                viewHolder = new CreateNoteHolder(v1);
                break;
        }

        return viewHolder;
    }

    /**
     * bind viewholder with data
     * @param holder
     * @param i
     */
    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int i) {
        switch (holder.getItemViewType()) {
            case VIEW_TYPE_HEADER:
                CreateNoteHolder createPlaylistHolder = (CreateNoteHolder) holder;
                createPlaylistHolder.bind();
                createPlaylistHolder.holder.setOnClickListener(view -> notesListener.add_new_note());
                break;
            case VIEW_TYPE_NOTE:
                final NotesViewHolder viewHolder = (NotesViewHolder) holder;
                final Note ci = (Note) items.get(i);
                viewHolder.bind(ci);
                break;
            case VIEW_TYPE_INFO:
                final InfoViewHolder infoViewHolder = (InfoViewHolder) holder;
                final Info info = (Info) items.get(i);
                infoViewHolder.bind(info);
                break;
        }
    }


    private static class InfoViewHolder extends RecyclerView.ViewHolder {
        private TextView message;
        private LottieAnimationView lottieAnimationView;
        private InfoViewHolder(View itemView) {
            super(itemView);
            lottieAnimationView = itemView.findViewById(R.id.thumbnail_1);
            message = itemView.findViewById(R.id.message);
        }

        private void bind(Info info){
            message.setText(info.message);
            if(info.message.equalsIgnoreCase(App.getContext().getResources().getString(R.string.search_notes_empty_hint))){
                lottieAnimationView.setVisibility(View.GONE);
            }else{
                lottieAnimationView.setVisibility(View.VISIBLE);
            }
        }

    }


    public class CreateNoteHolder extends RecyclerView.ViewHolder {
        public LinearLayout holder;
        public TextView title;

        private CreateNoteHolder(View itemView) {
            super(itemView);
            holder = itemView.findViewById(R.id.itm_holder);
            title = itemView.findViewById(R.id.title);
        }

        private void bind(){
            title.setText(App.getContext().getString(R.string.create_note));
        }
    }
}






