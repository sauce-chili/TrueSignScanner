package com.example.truesignscanner.adapter;

import android.content.pm.ShortcutManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.truesignscanner.R;
import com.example.truesignscanner.ViewModel.PackViewHolder;
import com.example.truesignscanner.Interfaces.onItemClickListener;
import com.example.truesignscanner.Interfaces.onItemLongClickListener;
import com.example.truesignscanner.model.Pack;
import com.example.truesignscanner.Interfaces.Subscriber;

import java.util.ArrayList;

import javax.crypto.AEADBadTagException;


public class PackAdapter extends RecyclerView.Adapter<PackViewHolder>
        implements Subscriber<ArrayList<Pack>> {

    static private final String TAG = "PackAdapter";


    private ArrayList<Pack> packageList;
    private ArrayList<Pack> selectedPack;
    private onItemClickListener onItemClickListener;
    private onItemLongClickListener onItemLongClickListener;

    public PackAdapter(){
        this.packageList = new ArrayList<>();
        this.selectedPack = new ArrayList<>();
    }

    public interface OnUpdaterRecyclerView{
        void update();
    }

    private OnUpdaterRecyclerView updaterRecyclerView;

    public void setOnUpdaterRecyclerView(OnUpdaterRecyclerView onUpdateRecyclerView) {
        this.updaterRecyclerView = onUpdateRecyclerView;
    }

    public void updateUI(){
        this.updaterRecyclerView.update();
    }

    @Override
    public void setData(ArrayList<Pack> data) {
        //Log.d("update","update subscriber: " + data.toString());
        //this.packageList = null;
        this.packageList.clear();
        this.packageList = new ArrayList<>(data);
        Log.d("update","Sub date: " + packageList.toString());
        if (updaterRecyclerView != null)
            updaterRecyclerView.update();
        else
            this.notifyDataSetChanged();
    }

    @Override
    public ArrayList<Pack> getData() {
        return new ArrayList<>(this.packageList);
    }


    public ArrayList<Pack> getSelectedPacks() {
        return selectedPack;
    }

    public void setSelectedPack(ArrayList<Pack> selectedPack) {
        this.selectedPack = selectedPack;
    }

    public void setOnItemClickListener(onItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    public void setItemLongClickListener(onItemLongClickListener onItemLongClickListener) {
        this.onItemLongClickListener = onItemLongClickListener;
    }

    //private PackViewHolder.onClickListeners itemClickListener;

    @NonNull
    @Override
    public PackViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater
                .from(parent.getContext())
                .inflate(R.layout.item_rv, parent, false);

        return new PackViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PackViewHolder holder, int position) {
        // holder.setClickListeners(itemClickListener);
        holder.setOnItemClickListener(onItemClickListener);
        holder.setOnItemLongClickListener(onItemLongClickListener);
        holder.bind(packageList.get(position));
    }

    @Override
    public int getItemCount() {
        return packageList.size();
    }

}
