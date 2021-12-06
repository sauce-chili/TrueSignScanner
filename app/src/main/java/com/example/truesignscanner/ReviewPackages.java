package com.example.truesignscanner;

import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.truesignscanner.Interfaces.Event;
import com.example.truesignscanner.adapter.PackAdapter;
import com.example.truesignscanner.model.Pack;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;


public class ReviewPackages extends BasicsActivity {

    private RecyclerView rvPackages;
    // private ArrayList<Pack> selectedPack;
    private EventHandler eventHandler;
    private PackAdapter adapter;
    private FloatingActionButton addBtn;
    private static final String TAG_DEBUG = "ReviewActivity";

    // TODO юзать мапу для состояний.

    private enum EventType{
        UPlOAD,
        DELETE,
        SELECT_ALL,
        ADD_PACK,
        NON_ACTION;
    }

    private class EventHandler{

        private HashMap<EventType, Event> EventList;
        private EventType currentEventType;
        private static final String TAG_DEBUG = "EventHandler";

        @RequiresApi(api = Build.VERSION_CODES.N)
        EventHandler(){
            EventList = new HashMap<>();

            EventList.put(EventType.UPlOAD,() -> {

                Log.d(TAG_DEBUG,"Upload selected pack");
                if (PermissionManager.checkInternetPermission(ReviewPackages.this)) {
                    if (PermissionManager.checkSignInAccount(ReviewPackages.this)) {
                        Log.d("SignInAcc","User is already signed in acc");
                        packManager.uploadPack(adapter.getSelectedPacks());
                    }else{
                        PermissionManager.requestSignInGoogleAccount(ReviewPackages.this);
                    }
                }else{
                    PermissionManager.requestInternetPermission(ReviewPackages.this);
                }
                deactivateSelectionMode();

            });

            EventList.put(EventType.DELETE, () -> {

                Log.d(TAG_DEBUG,"Delete selected pack");
                ArrayList<String> filenames = new ArrayList<>();
                for(Pack pack : adapter.getSelectedPacks()){
                    filenames.add(pack.name);
                }
                packManager.deletePackage(filenames);
                adapter.notifyDataSetChanged();
                deactivateSelectionMode();

            });

            EventList.put(EventType.ADD_PACK,() -> {
                // Begin transaction to add csv file.Showing dialog window

                Log.d(TAG_DEBUG,"Add pack.Showing window dialog");
                View view = LayoutInflater
                        .from(getApplicationContext())
                        .inflate(R.layout.dialog_create_package, null);

                AlertDialog.Builder builder = new AlertDialog.Builder(ReviewPackages.this);
                builder.setView(view);
                EditText title = view.findViewById(R.id.input_title);

                builder.setPositiveButton("Ok", (dialog, which) -> {

                    Log.d("Create new file",Boolean.toString(title == null));
                    // assert title != null;
                    String namePack = title.getText().toString();
                    ArrayList<String> filesName = new ArrayList<>();
                    filesName.add(namePack);
                    packManager.createPackage(filesName);
                    Toast.makeText(getApplicationContext(),
                            ("Пакет " + namePack + " создан"), Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                    adapter.notifyItemChanged(adapter.getItemCount() + 1);
                });
                builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());
                builder.create();
                builder.show();

            });

            EventList.put(EventType.SELECT_ALL, () -> {

                ArrayList<Pack> packagesList = adapter.getData();
                ArrayList<Pack> selectedPack = adapter.getSelectedPacks();
                Log.d(TAG_DEBUG,"Click btn select all");
                boolean isSelectAll = !packagesList.stream().allMatch(p -> p.isSelected);

                Log.d(TAG_DEBUG,"isSelectAll: " + isSelectAll);

                for(Pack p : packagesList) {
                    p.isSelected = isSelectAll;
                    if (p.isSelected)
                        if (!selectedPack.contains(p))
                            selectedPack.add(p);
                }
                if(!isSelectAll)
                    selectedPack.clear();
                adapter.notifyDataSetChanged();
                Log.d("Click","Selected pack:" + selectedPack.toString());

            });

            EventList.put(EventType.NON_ACTION, () -> {

                Log.d(TAG_DEBUG,"Action isn't select");

                adapter.setOnItemClickListener(((viewHolder, pack) -> {
                    Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                    intent.addCategory(Intent.CATEGORY_OPENABLE);
                    Log.d("Click",packManager.getDir().getAbsolutePath());
                    Uri uri = Uri.parse(
                            packManager.getDir().getAbsolutePath()
                                    + File.separator + pack.name + ".csv"
                    );
                    intent.setDataAndType(uri, "*/*");
                    startActivity(intent);
                }));
                adapter.setItemLongClickListener(((viewHolder, pack) -> {
                    Log.d("Click","Long click on pack: " + pack.name);
                    if(!packManager.getWritingPackageName().equals(pack.name)){
                        packManager.setWritingPackageName(pack.name);
                    } else {
                        Toast.makeText(getApplicationContext(),
                                "Этот пакет уже выбран",
                                Toast.LENGTH_SHORT);
                    }
                    return true;
                }));
                adapter.notifyDataSetChanged();
            });
        }

        public void setCurrentEventType(EventType type){
            this.currentEventType = type;
        }

        public void handleCurrentEvent(){
            this.EventList.get(this.currentEventType).handle();
        }
    }


    private void activateSelectionMode(){

        findViewById(R.id.toolbar).setVisibility(View.GONE);
        findViewById(R.id.select_toolbar).setVisibility(View.VISIBLE);
        addBtn.setVisibility(View.INVISIBLE);

        ArrayList<Pack> packagesList = adapter.getData();
        ArrayList<Pack> selectedPack = adapter.getSelectedPacks();

        Log.d("ON selected mode","current data: " + packagesList);
        if (packagesList == null) throw new NullPointerException("packagesList is null");
        if (selectedPack == null)   throw new NullPointerException("selectedPack is null");

        selectedPack.clear();
        for(Pack p : packagesList){
            p.isVisibleRadioButton = true;
        }
        adapter.setOnItemClickListener((viewHolder, pack) -> {

            pack.isSelected = !pack.isSelected;
            viewHolder.switchRadioButton(pack.isSelected);
            Log.d("Click","Click on pack: "
                    + pack.name + "; isSelect: " + pack.isSelected);
            if (pack.isSelected){
                selectedPack.add(pack);
            }else {
                if (selectedPack.contains(pack)){
                    selectedPack.remove(pack);
                }
            }

        });
        Log.d("ON selected mode", "current selected data: " + selectedPack);
        runOnUiThread(() -> adapter.notifyDataSetChanged());
    }

    private void deactivateSelectionMode(){
        findViewById(R.id.select_toolbar).setVisibility(View.GONE);
        findViewById(R.id.toolbar).setVisibility(View.VISIBLE);
        ArrayList<Pack> packagesList = adapter.getData();
        ArrayList<Pack> selectedPack = adapter.getSelectedPacks();
        if (packagesList == null || selectedPack == null) return;
        for(Pack p : packagesList){
            p.isVisibleRadioButton = false;
            p.isSelected = false;
        }
        selectedPack.clear();

        this.eventHandler.setCurrentEventType(EventType.NON_ACTION);
        this.eventHandler.handleCurrentEvent();
        addBtn.setVisibility(View.VISIBLE);
        Log.d("OFF selected mode","current data: " + packagesList);
        runOnUiThread(() -> adapter.notifyDataSetChanged());
    }

    private void bindToolbar() {
        ImageView icUpload = findViewById(R.id.ic_upload);
        icUpload.setOnClickListener(v -> {

            this.eventHandler.setCurrentEventType(EventType.UPlOAD);
            activateSelectionMode();

        });

        ImageView icDelete = findViewById(R.id.ic_delete);
        icDelete.setOnClickListener(v -> {

            this.eventHandler.setCurrentEventType(EventType.DELETE);
            activateSelectionMode();

        });
    }

    private void bindSelectionToolbar(){
        ImageView icAgree = findViewById(R.id.ic_agree);
        icAgree.setOnClickListener(v -> {

            Log.d("Click","Click on btn agree.");
            eventHandler.handleCurrentEvent();
        });

        ImageView icCancel = findViewById(R.id.ic_cancel);
        icCancel.setOnClickListener(v -> {

            Log.d("Click","Click on btn cancel.Selected mode: " + false);
            deactivateSelectionMode();

        });

        ImageView icSelectAll = findViewById(R.id.ic_selectAll);
        icSelectAll.setOnClickListener(v -> {
            this.eventHandler.setCurrentEventType(EventType.SELECT_ALL);
            this.eventHandler.handleCurrentEvent();
        });
    }

    private void bindAddButton(){
        addBtn = findViewById(R.id.ic_add);
        addBtn.setOnClickListener(v -> {
            this.eventHandler.setCurrentEventType(EventType.ADD_PACK);
            this.eventHandler.handleCurrentEvent();
            this.eventHandler.setCurrentEventType(EventType.NON_ACTION);
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void createRecyclerView(){

        rvPackages = findViewById(R.id.rv_pck);
        rvPackages.setLayoutManager(new LinearLayoutManager(this));
        this.eventHandler = new EventHandler();
        this.adapter = new PackAdapter();
        eventHandler.setCurrentEventType(EventType.NON_ACTION);
        eventHandler.handleCurrentEvent();
        adapter.setOnUpdaterRecyclerView(() -> runOnUiThread(() -> adapter.notifyDataSetChanged()));
        packManager.subscribe(adapter);

        this.bindAddButton();
        this.bindToolbar();
        this.bindSelectionToolbar();


        DividerItemDecoration decorator =
                new DividerItemDecoration(this, DividerItemDecoration.VERTICAL);
        decorator.setDrawable(getResources().getDrawable(R.drawable.space));
        rvPackages.setLayoutManager(new LinearLayoutManager(this));
        rvPackages.setAdapter(adapter);
        rvPackages.addItemDecoration(decorator);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(packManager.isAlive()){
            packManager.setCurrentActivity(this);
        }
        setContentView(R.layout.activity_rv_package);
        createRecyclerView();
    }

    @Override
    protected void onStop() {
        super.onStop();
        packManager.unsubscribe(adapter);
        adapter.getSelectedPacks().clear();
        deactivateSelectionMode();
        // selectedPack.clear();
    }

    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            completedTask.getResult(ApiException.class);
            Log.i(TAG_DEBUG,"Successful authorization.");
        } catch (ApiException e) {
            Log.e(TAG_DEBUG, "Unsuccessful authorization.signInResult:failed code=" + e.getStatusCode());
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 102) {
            // The Task returned from this call is always completed, no need to attach
            // a listener.
            /*Log.d(TAG_DEBUG,"SignIn .Call from rvAct.Intent ref: " + data);
            try {
                GoogleSignIn.getSignedInAccountFromIntent(data)
                        .getResult(ApiException.class);
                Log.i(TAG_DEBUG,"Successful authorization.");
            } catch (ApiException e) {
                Log.e(TAG_DEBUG, "Unsuccessful authorization.signInResult:failed code=" + e.getStatusCode());
            }*/
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleSignInResult(task);
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}