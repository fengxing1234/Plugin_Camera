package com.picc.plugin_camera.camera;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.picc.plugin_camera.R;

import java.util.List;

public class SelectPictureTypeAdapter extends RecyclerView.Adapter<SelectPictureTypeAdapter.SelectPictureTypeHolder> {
    private static final String TAG = SelectPictureTypeAdapter.class.getSimpleName();
    private List<SelectPictureTypeData> mData;

    public SelectPictureTypeAdapter(List<SelectPictureTypeData> list) {
        this.mData = list;
    }

    @NonNull
    @Override
    public SelectPictureTypeHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_select_picture_type, parent, false);
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
        return new SelectPictureTypeHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SelectPictureTypeHolder holder, final int position) {
        SelectPictureTypeData item = mData.get(position);
        Log.d(TAG, "onBindViewHolder: " + item.isSelected + "  position = " + position);
        if (position == mData.size() - 1) {
            holder.divider.setVisibility(View.GONE);
        } else {
            holder.divider.setVisibility(View.VISIBLE);
        }

        holder.tvIsMust.setVisibility(item.isMust ? View.VISIBLE : View.INVISIBLE);

        holder.tvPictureType.setText(item.pictureType);

        holder.ivSelectStatus.setChecked(item.isSelected);

        holder.ivSelectStatus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for (SelectPictureTypeData data : mData) {
                    data.isSelected = false;
                }
                mData.get(position).isSelected = true;
                notifyDataSetChanged();

            }
        });
    }


    @Override
    public int getItemCount() {
        return mData.size();
    }

    public static class SelectPictureTypeHolder extends RecyclerView.ViewHolder {

        private final View divider;
        private final CheckBox ivSelectStatus;
        private final TextView tvPictureType;
        private final TextView tvIsMust;

        public SelectPictureTypeHolder(@NonNull View itemView) {
            super(itemView);
            divider = itemView.findViewById(R.id.divider);
            ivSelectStatus = itemView.findViewById(R.id.iv_select_status);
            tvPictureType = itemView.findViewById(R.id.tv_picture_type);
            tvIsMust = itemView.findViewById(R.id.tv_is_must);


        }
    }
}
