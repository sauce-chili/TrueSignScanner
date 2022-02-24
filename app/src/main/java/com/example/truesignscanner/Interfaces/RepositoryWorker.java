package com.example.truesignscanner.Interfaces;

import com.google.android.gms.tasks.Task;

import java.util.HashMap;

public interface RepositoryWorker {
    Task<String> read(String fileName);
    Task<Boolean> write(String data, String fileName);
    Task<HashMap<String,String>> getInfoAboutFile(String fileName);
}
