package com.example.truesignscanner.ViewModel;

import android.graphics.Color;
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

import de.hdodenhof.circleimageview.CircleImageView;

public class PackViewHolder extends RecyclerView.ViewHolder{
    private TextView namePack;
    private RadioButton SelectedMark;
    private CircleImageView icPack;
    private TextView numberRecords;
    private TextView memorySize;

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
        SelectedMark = itemView.findViewById(R.id.select_mark);
        icPack = itemView.findViewById(R.id.ic_code);
        numberRecords = itemView.findViewById(R.id.numRecords);
        memorySize = itemView.findViewById(R.id.sizeMemory);
    }

    public void switchRadioButton(boolean mode){
        this.SelectedMark.setChecked(mode);
    }

    public void bind(Pack pack){
        namePack.setText(pack.name);
        if (pack.isWritingPackage){
//            itemView.findViewById(R.id.ic_code).setCircleBackgroundColor(Color.parseColor("#6EE10D"));
            icPack.setCircleBackgroundColor(Color.parseColor("#6EE10D"));
        }else{
            icPack.setCircleBackgroundColor(Color.parseColor("#FFEB3B"));
//            itemView.findViewById(R.id.ic_code).setBackgroundColor(Color.parseColor("#FFEB3B"));
        }

        numberRecords.setText(pack.numbersRecords);
        memorySize.setText(pack.memorySize);

        if(pack.isVisibleRadioButton) {
            this.SelectedMark.setVisibility(View.VISIBLE);
            this.switchRadioButton(pack.isSelected);
        } else {
            this.SelectedMark.setVisibility(View.INVISIBLE);
        }

        if (onItemClickListener != null){
            itemView.setOnClickListener(v -> onItemClickListener.onClick(this,pack));
        } else{
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
