package com.example.truesignscanner;

import android.icu.text.CaseMap;
import android.os.Build;
import android.telephony.mbms.FileInfo;
import android.util.Log;

import androidx.annotation.RequiresApi;

import com.example.truesignscanner.Interfaces.RepositoryWorker;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.LineNumberReader;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;


public class DMRepWorker implements RepositoryWorker {

    private final static String TAG = "DMRepWorker";

    private final Executor mExecutor;

    public DMRepWorker() {
        this.mExecutor = Executors.newSingleThreadExecutor();
    }


    // TODO убрать Tasks.call, заменить на TaskCompletionSource

    @Override
    public Task<String> read(String pathToFile) {



        return Tasks.call(mExecutor, () -> {
            if (pathToFile.equals("")) {
                return "";
            }
            StringBuilder data = new StringBuilder();

            try (FileReader fr = new FileReader(pathToFile)) {
                Scanner sc = new Scanner(fr);

                while (sc.hasNextLine()) {
                    data.append(sc.nextLine()).append("\n");
                }

                fr.close();
            } catch (IOException e) {
                e.printStackTrace();
            }


            Log.d("MatrixR", data.toString());
            return data.toString();
        });
    }

    private boolean checkSpecialChar(String data) {
        return ((byte) data.charAt(0) == 29);
    }

    @Override
    public Task<Boolean> write(String data, String pathToFile) {


        return Tasks.call(mExecutor, () -> {

            Log.d("MatrixWR", data);
            if (!checkSpecialChar(data)) {
                throw new Exception("Invalid DataMatrix code");
            }

            try (FileWriter fw = new FileWriter(pathToFile, true)) {
                String dataWritingPack = read(pathToFile).getResult();

                List<String> d = Arrays.asList(dataWritingPack.split("\n"));

                if (!d.contains(data)) {

                    fw.write(data + "\n");
                    fw.flush();
                    Log.d(TAG, "Data was recorded in: " + pathToFile);
                    return true;
                } else {
                    Log.d(TAG, "Such data also contains");
                    fw.flush();
                    throw new Exception("Such DataMatrix code also contains in this package");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return false;
        });
    }

    private String getSize(float size){
        String[] amount = new String[]{"Kb","Mb","Gb"};
        int cursor = 0;
        size /= 1024f;
        while (size / 1024f > 1){
            size /= 1024f;
            cursor += 1;
        }
        if (cursor > 2) return "дохуя";
        return (size > 100 ?
                String.valueOf(Math.round(size)) :
                String.format( size < 10? "%.2f" : "%.1f" ,size).replace(",",".")
                        + amount[cursor]);
    }

    @Override
    public Task<HashMap<String, String>> getInfoAboutFile(String pathToFile) {
        return Tasks.call(mExecutor, () -> {
            HashMap<String, String> info = new HashMap<>();



            return info;
        });
    }
}
