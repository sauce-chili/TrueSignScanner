package com.example.truesignscanner;

import android.util.Log;

import com.example.truesignscanner.Interfaces.Repository;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.io.File;
import java.util.ArrayList;


public class DMRepository implements Repository {

    final static String TAG = "DMRepository";
    private File repository;
    private String suffix;
    private final Executor mExecutor;

    public DMRepository(File appDir, String nameRep, String fileSuffix){
        String path = appDir + File.separator + nameRep;
        initRepository(path);
        this.suffix = fileSuffix;
        this.mExecutor = Executors.newSingleThreadExecutor();
    }


    private void initRepository(String path) {
        repository = new File(path);
        if (!repository.exists()) {
            Log.d(TAG, "Dir is create");
            repository.mkdir();
        } else {
            Log.d(TAG, "Dir " + repository.toString() + " is also exists");
        }
    }

    @Override
    public File getRepository() {
        return repository;
    }

    @Override
    public String getPathToFile(String fileName) {
        return repository.getAbsolutePath() + File.separator + fileName + suffix;
    }

    @Override
    public Task<Boolean> create(String pathToFile) {
        return Tasks.call(mExecutor, () -> {
            try {
                new File(pathToFile).createNewFile();
                Log.d(TAG, "Successful create new file :" + pathToFile);
                return true;
            } catch (Exception e) {
                Log.d(TAG,"Unsuccessful file create :" + pathToFile);
                Log.d(TAG, e.getMessage());
                return false;
            }
        });
    }

    @Override
    public Task<Boolean> delete(ArrayList<String> pathsToFiles) {
        return Tasks.call(mExecutor, () -> {
            try {
                for (String path : pathsToFiles) {
                    new File(path).delete();
                    Log.d(TAG, "successful delete file :" + path);
                }
                return true;
            } catch (Exception e) {
                Log.d(TAG, "Unsuccessful delete file");
                Log.d(TAG, e.getMessage());
                return false;
            }
        });
    }

}
