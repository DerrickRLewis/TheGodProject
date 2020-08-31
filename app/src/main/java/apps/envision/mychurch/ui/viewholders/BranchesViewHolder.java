package apps.envision.mychurch.ui.viewholders;

import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import org.joda.time.DateTime;

import apps.envision.mychurch.App;
import apps.envision.mychurch.R;
import apps.envision.mychurch.interfaces.BranchClickListener;
import apps.envision.mychurch.interfaces.EventsClickListener;
import apps.envision.mychurch.pojo.Branches;
import apps.envision.mychurch.pojo.Events;
import apps.envision.mychurch.utils.LetterTileProvider;

public class BranchesViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

    private TextView name, phone, address, email, pastor;
    private BranchClickListener branchClickListener;
    private Branches branches;

    public BranchesViewHolder(View itemView, BranchClickListener branchClickListener) {
        super(itemView);

        name = itemView.findViewById(R.id.name);
        address = itemView.findViewById(R.id.address);
        phone = itemView.findViewById(R.id.phone);
        email = itemView.findViewById(R.id.email);
        pastor = itemView.findViewById(R.id.pastor);

        this.branchClickListener = branchClickListener;
    }

    public void bind(Branches branches){
        this.branches = branches;
        name.setText(branches.getName());
        email.setText(branches.getEmail());
        phone.setText(branches.getPhone());
        address.setText(branches.getAddress());
        pastor.setText(branches.getPastor());
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.itm_holder:
                branchClickListener.OnItemClick(branches);
                break;
        }
    }
}
