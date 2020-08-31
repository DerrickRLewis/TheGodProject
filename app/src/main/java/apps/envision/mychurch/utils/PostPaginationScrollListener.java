package apps.envision.mychurch.utils;

import android.graphics.Rect;
import android.util.Log;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import apps.envision.mychurch.R;
import apps.envision.mychurch.libs.localmessagemanager.LocalMessageManager;
import apps.envision.mychurch.pojo.RecyclerviewState;

public abstract class PostPaginationScrollListener extends RecyclerView.OnScrollListener {

    private LinearLayoutManager layoutManager;

    /**
     * Supporting only LinearLayoutManager for now.
     *
     * @param layoutManager
     */
    public PostPaginationScrollListener(LinearLayoutManager layoutManager) {
        this.layoutManager = layoutManager;
    }

    @Override
    public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
        super.onScrolled(recyclerView, dx, dy);
        int visibleItemCount = layoutManager.getChildCount();
        int totalItemCount = layoutManager.getItemCount();
        int firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition();

        if (!isLoading() && !isLastPage()) {
            if ((visibleItemCount + firstVisibleItemPosition) >= totalItemCount
                    && firstVisibleItemPosition >= 0) {
                loadMoreItems();
            }
        }

    }

    @Override
    public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
        super.onScrollStateChanged(recyclerView, newState);
        if (newState == RecyclerView.SCROLL_STATE_IDLE){
            int position = ((LinearLayoutManager)recyclerView.getLayoutManager()).findFirstVisibleItemPosition();
            int visible = ((LinearLayoutManager)recyclerView.getLayoutManager()).findFirstCompletelyVisibleItemPosition();
            // LocalMessageManager.getInstance().send(R.id.log_recyclerview_pos, new RecyclerviewState(position,visible));
            //Log.e("crnt position", "post item pos "+position);
            //Log.e("crnt visible", "post visible item pos "+ String.valueOf(visible));
        }

        LinearLayoutManager layoutManager = ((LinearLayoutManager) recyclerView.getLayoutManager());

        final int firstPosition = layoutManager.findFirstVisibleItemPosition();
        final int lastPosition = layoutManager.findLastVisibleItemPosition();
        int visible = ((LinearLayoutManager)recyclerView.getLayoutManager()).findFirstCompletelyVisibleItemPosition();

        Rect rvRect = new Rect();
        recyclerView.getGlobalVisibleRect(rvRect);

        for (int i = firstPosition; i <= lastPosition; i++) {
            Rect rowRect = new Rect();
            layoutManager.findViewByPosition(i).getGlobalVisibleRect(rowRect);

            int percentFirst;
            if (rowRect.bottom >= rvRect.bottom){
                int visibleHeightFirst =rvRect.bottom - rowRect.top;
                percentFirst = (visibleHeightFirst * 100) / layoutManager.findViewByPosition(i).getHeight();
            }else {
                int visibleHeightFirst = rowRect.bottom - rvRect.top;
                percentFirst = (visibleHeightFirst * 100) / layoutManager.findViewByPosition(i).getHeight();
            }

            if (percentFirst>100)
                percentFirst = 100;

            //mData.get(i).setPercentage(percentFirst);
            //mAdapter.notifyItemChanged(i);
           // if(i == getPlayerPosition()) {
                //Log.e("percentage", String.valueOf(percentFirst));
                LocalMessageManager.getInstance().send(R.id.log_recyclerview_pos, new RecyclerviewState(i,percentFirst));
          //  }
        }
    }

    protected abstract void loadMoreItems();

    public abstract boolean isLastPage();

    public abstract boolean isLoading();

    public abstract int getPlayerPosition();

}
