package swati4star.createpdf.adapter;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import java.util.List;

import swati4star.createpdf.R;
import swati4star.createpdf.interfaces.OnItemClickListner;
import swati4star.createpdf.model.BrushItem;

public class BrushItemAdapter extends RecyclerView.Adapter<BrushItemAdapter.BrushItemViewHolder> {

    private final Context mContext;
    private final OnItemClickListner mOnItemClickListner;
    private final List<BrushItem> mBrushItems;

    public BrushItemAdapter(Context context,
                            OnItemClickListner onItemClickListner,
                            List<BrushItem> brushItems) {
        mBrushItems = brushItems;
        mOnItemClickListner = onItemClickListner;
        mContext = context;
    }

    @NonNull
    @Override
    public BrushItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View mView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.brush_color_item, parent, false);
        return new BrushItemAdapter.BrushItemViewHolder(mView);
    }

    @Override
    public void onBindViewHolder(@NonNull BrushItemViewHolder holder, int position) {
        int color = mBrushItems.get(position).getColor();
        holder.Doodlebutton.setBackgroundColor(mContext.getResources().getColor(color));
    }

    @Override
    public int getItemCount() {
        return mBrushItems.size();
    }

    public class BrushItemViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        Button Doodlebutton;

        BrushItemViewHolder(View itemView) {
            super(itemView);
            Doodlebutton = itemView.findViewById(R.id.doodle_color);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            mOnItemClickListner.onItemClick(getAdapterPosition());
        }
    }
}