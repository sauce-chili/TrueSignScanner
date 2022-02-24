package com.example.truesignscanner.Managers;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.example.truesignscanner.DMRepWorker;
import com.example.truesignscanner.DMRepository;
import com.example.truesignscanner.Interfaces.Repository;
import com.example.truesignscanner.Interfaces.RepositoryWorker;
import com.example.truesignscanner.R;
import com.example.truesignscanner.model.Pack;

import java.io.File;
import java.util.ArrayList;

public class PackManager {
    private final static String TAG = "PackManager";


    Repository repository;
    RepositoryWorker repWorker;
    GoogleDriveManager googleDriveManager;

    private static PackManager Instance;

    private Context context;


    private PackManager(Context cnt) {
        this.context = cnt;


        String nameRep = context.getResources().getString(R.string.NAME_DM_REP);
        String fileSuffix = context.getResources().getString(R.string.DM_FILE_SUFFIX);

        repository = new DMRepository(
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q ?
                        context.getExternalFilesDir(null) :
                        Environment.getExternalStorageDirectory(),
                nameRep,
                fileSuffix

        );
        repWorker = new DMRepWorker();

        googleDriveManager = new GoogleDriveManager(context,nameRep);
    }

    public static PackManager getInstance(Context cnt) {
        if (Instance == null) {
            Instance = new PackManager(cnt);
        }
        return Instance;
    }

    public void setCurrentContext(Context cnt) {
        this.context = cnt;
    }

    public RepositoryWorker getRepWorker() {
        return repWorker;
    }

    public String getPathToRepository() {
        return repository.getRepository().getAbsolutePath() + File.separator;
    }

    private String getDataFromPack(String fileName) {
        if (fileName.equals("")) {
            // TODO тост об некоректом имене
        }
        String pathToFile = repository.getPathToFile(fileName);
        return repWorker
                .read(pathToFile)
                .addOnFailureListener(e -> {
                    // TODO обработка ошибки
                })
                .getResult();
    }

    private void addRecordInPackage(String data,String fileName){
        if(fileName.equals("")){
            // TODO файл не выбран
        }
        String pathToFile = repository.getPathToFile(fileName);
        repWorker.write(data, pathToFile)
                .addOnFailureListener(e -> {
                   // TODO тост об ошибке
                });
    }

    public void addRecordInWritingPackage(String data) {

        String nameWritingPack = getWritingPackageName();

        if (nameWritingPack.equals("")) {
            // TODO тост о невыбранном пакете
        }

        this.addRecordInPackage(data,nameWritingPack);

    }

    public void setWritingPackageName(String name) {
        SharedPreferences.Editor editor = context
                .getSharedPreferences(context.getResources().getString(R.string.SHARED_NAME), context.MODE_PRIVATE)
                .edit();
        editor.putString(context.getResources().getString(R.string.KEY_CURRENT_WRITING_PACK_NAME), name);
        editor.apply();
        Log.d("WritingPack", "currently set name wr pack: " + name);
    }

    public String getWritingPackageName() {
        return context
                .getSharedPreferences(context.getResources().getString(R.string.SHARED_NAME), context.MODE_PRIVATE)
                .getString(context.getResources().getString(R.string.KEY_CURRENT_WRITING_PACK_NAME), "");
    }


    public void createPackage(String fileName) {
            String pathToFile = repository.getPathToFile(fileName);
//            csvFileManager.createCSVFile(pathToFile);
            repository.create(pathToFile);
    }

    public void deletePackage(@NonNull ArrayList<String> fileNames) {
        Log.d("Delete file", "File names: " + fileNames.toString());
        ArrayList<String> pathsToFiles = new ArrayList<>();
        for (String fileName : fileNames) {
            if (fileName.equals(getWritingPackageName())) {
                setWritingPackageName("");
            }

            String pathToFolder = repository.getPathToFile(fileName);

            pathsToFiles.add(pathToFolder);
        }
//        csvFileManager.deleteCSVFile(pathsToFiles);
        repository.delete(pathsToFiles);
    }

    public void uploadPacks(ArrayList<String> filesName) {

//        ProgressDialog progressDialog = new ProgressDialog(context);
//        activity.runOnUiThread(() -> {
//            progressDialog.setTitle("Обновление данных на GDrive");
//            progressDialog.setMessage("Загрузка..");
//            progressDialog.show();
//        });


        for (String name : filesName) {

            String pathToFile = repository.getPathToFile(name);

            googleDriveManager.createPack(pathToFile)

                    .addOnSuccessListener(s -> {

//                        activity.runOnUiThread(progressDialog::dismiss);
//                        Toast.makeText(activity.getApplicationContext(),
//                                "Пакет " + name + " выгружен успешно",
//                                Toast.LENGTH_SHORT).show();
//                        Log.d("GDrive upload", "Pack " + name + " was uploaded successful. " + s);

                    })
                    .addOnFailureListener(e -> {

                        e.printStackTrace();
//                        activity.runOnUiThread(progressDialog::dismiss);
//                        Toast.makeText(activity.getApplicationContext(),
//                                "Не удалось выгрузить пакет " + name,
//                                Toast.LENGTH_SHORT).show();
//                        Log.d("GDRive upload", "Pack " + name + " was uploaded unsuccessful");

                    });
        }
    }
}

