package com.example.truesignscanner.ViewModel;

import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.truesignscanner.Interfaces.onItemClickListener;
import com.example.truesignscanner.Interfaces.onItemLongClickListener;
import com.example.truesignscanner.R;
import com.example.truesignscanner.model.Pack;

public class PackViewHolder extends RecyclerView.ViewHolder{
    private TextView namePack;
    private RadioButton SelectedBtn;
    private ImageView icPack;

    private final static String DEBUG_TAG = "PackViewHolder";

    private onItemClickListener onItemClickListener;
    private onItemLongClickListener onItemLongClickListener;


    public void setOnItemClickListener(onItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    public void setOnItemLongClickListener(onItemLongClickListener onItemLongClickListener) {
        this.onItemLongClickListener = onItemLongClickListener;
    }

    public PackViewHolder(@NonNull View itemView) {
        super(itemView);

        namePack = itemView.findViewById(R.id.name_pack);
        SelectedBtn = itemView.findViewById(R.id.select_btn);
        icPack = itemView.findViewById(R.id.ic_code);
    }

    public void switchRadioButton(boolean mode){
        this.SelectedBtn.setChecked(mode);
    }

    public void bind(Pack pack){
        namePack.setText(pack.name);
        if (pack.isWritingPackage){
            itemView.findViewById(R.id.ic_writingPack).setVisibility(View.VISIBLE);
        }

        if(pack.isVisibleRadioButton) {
            this.SelectedBtn.setVisibility(View.VISIBLE);
            this.switchRadioButton(pack.isSelected);
        }
        else {
            this.SelectedBtn.setVisibility(View.GONE);
        }

        if (onItemClickListener != null){
            itemView.setOnClickListener(v -> onItemClickListener.onClick(this,pack));
        }
        else{
            Log.d(DEBUG_TAG, "The onItemClickListener could not be set in the module with name " + pack.name);
        }
        if (onItemLongClickListener != null)
            itemView.setOnLongClickListener(v -> onItemLongClickListener.onLongClick(this,pack));
        else{
            Log.d(DEBUG_TAG,"The onItemLongClickListener could not be set in the module with name " + pack.name);
        }

        /*if(pack.isVisibleRadioButton){
            Log.d("bind","rebinding pack: " + pack.toString());
            SelectedBtn.setVisibility(View.VISIBLE);
            itemView.setOnClickListener(null);
            itemView.setOnClickListener(v -> {

                pack.isSelected = !pack.isSelected;
                SelectedBtn.setChecked(pack.isSelected);
                clickListeners.onClickInSelectedMode(v, pack);

            });
            SelectedBtn.setChecked(pack.isSelected);
            //Log.d("Select", String.valueOf(pack.isSelected));
            itemView.setOnLongClickListener(null);
        }else{
            Log.d("bind","binding ");
            SelectedBtn.setVisibility(View.GONE);
            itemView.setOnClickListener(null);
            itemView.setOnClickListener(v -> clickListeners.onClick(v,pack));
            itemView.setOnLongClickListener(v -> {
                clickListeners.onLongClick(v,pack);
                return true;
            });
        }*/
    }
}
