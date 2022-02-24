package com.example.truesignscanner;

import android.content.Context;
import android.util.Log;

import com.example.truesignscanner.Interfaces.Publisher;
import com.example.truesignscanner.Interfaces.RepositoryWorker;
import com.example.truesignscanner.Interfaces.Subscriber;
import com.example.truesignscanner.model.Pack;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.util.ArrayList;

public class UpdaterReviewActivity extends Thread implements Publisher<ArrayList<Pack>> {


    private final static String TAG = "UpdaterReviewActivity";

    private ArrayList<Subscriber<ArrayList<Pack>>> subs;
    private ArrayList<Pack> data;
    private File observedDirectory;
    private RepositoryWorker observedRepositoryWorker;
    private Context context;



    UpdaterReviewActivity(Context cnt, String observedDirPath, RepositoryWorker dirWorker) {
        this.context = cnt;
        this.observedDirectory = new File(observedDirPath);
        this.observedRepositoryWorker = dirWorker;
        subs = new ArrayList<Subscriber<ArrayList<Pack>>>();
        data = new ArrayList<>();
        this.start();
    }

    public String getWritingPackageName() {
        return context.getSharedPreferences(context.getResources().getString(R.string.SHARED_NAME), context.MODE_PRIVATE).
                getString(context.getResources().getString(R.string.KEY_CURRENT_WRITING_PACK_NAME), "");
    }

    @Override
    public void subscribe(Subscriber<ArrayList<Pack>> sub) {
        if (subs != null) {
//            Log.d("update","new subscriber: "  + sub.toString());
//            Log.d("update","subscriber data: " + sub.getData().toString());
//            Log.d("update","Publisher data: " + data.toString());

//                Log.d("update","publisher data and sub date isn't equal.New sub data : " + data.toString());
            sub.handleChanges(new ArrayList<>(this.data));
        }
        assert this.subs != null;
        this.subs.add(sub);
//            Log.d("update","new sub list:" + subs.toString());
    }


    @Override
    public void unsubscribe(Subscriber<ArrayList<Pack>> sub) {
        if (subs != null)
            this.subs.remove(sub);
    }

    @Override
    public void notifyDataChange() {
        if (subs == null || subs.isEmpty()) {
            return;
        }

        for (Subscriber<ArrayList<Pack>> sub : subs) {
            Log.d(TAG,"notify sub :" + sub.toString());
            sub.handleChanges(data);
        }
    }

    @Override
    public synchronized void start() {
        super.start();
    }

    private String getFileSize(float size){
        String[] amount = new String[]{"Kb","Mb","Gb"};
        int cursor = 0;
        size /= 1024f;
        while (size / 1024f > 1){
            size /= 1024f;
            cursor += 1;
        }
        if (cursor > 2) return "дохера";
        return (size > 100 ?
                String.valueOf(Math.round(size)) :
                String.format( size < 10? "%.2f" : "%.1f" ,size).replace(",",".")
                        + amount[cursor]);
    }

    // TODO реализовать работу с текущей data, без копирования
    @Override
    public void run() {
        // хранилище для имеющиеся в данный момент файлов;
        ArrayList<Pack> currentPackages = new ArrayList<>();
        for (; ; ) {
            try {
                //Log.d(TAG, Arrays.toString(Objects.requireNonNull(dir.listFiles())));
                // имеющиеся в данный момент файлы;
                for (File file : observedDirectory.listFiles()) {

                    if (!file.isFile()) {
                        continue;
                    }

                    File f = new File(file.getAbsolutePath());
                    // имя csv файла
                    String namePack = file.getName().split("\\.")[0];
                    String memory = this.getFileSize(f.length());
                    String records = "0";

                    try (FileReader fr = new FileReader(file)) {
                        records =  String.valueOf(new LineNumberReader(fr).lines().count());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }


                    /**
                     * TODO сделать получение информации о файле, для её отоюражения и сортировки по ней
                     **/

                    Pack p = new Pack(namePack,
                            namePack.equals(getWritingPackageName()),
                            records,
                            memory);

                    if (p.isWritingPackage) {
                        currentPackages.add(0, p);
                    } else {
                        currentPackages.add(p);
                    }
                }


//                Log.d("update", "Data: " + this.getData().toString());
//                Log.d("update", "Current: " + currentPackages.toString());
                if (!this.data.equals(currentPackages)) {
                    Log.d("update", "current data after valid:" + currentPackages.toString());
                    this.data = new ArrayList<Pack>(currentPackages);
                    this.notifyDataChange();
                }
                currentPackages.clear();

            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                Thread.sleep(300);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
