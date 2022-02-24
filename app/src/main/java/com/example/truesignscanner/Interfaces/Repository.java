package com.example.truesignscanner.Interfaces;

import com.google.android.gms.tasks.Task;

import java.io.File;
import java.util.ArrayList;

public interface Repository {
    Task<Boolean> create(String pathToFile);
    Task<Boolean> delete(ArrayList<String> pathsToFiles);
    String getPathToFile(String fileName);
    File getRepository();
}
