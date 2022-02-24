package com.example.truesignscanner.Managers;

import android.app.Activity;
import android.content.Context;
import android.util.Log;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.http.FileContent;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.FileList;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class GoogleDriveManager{
    private final Executor mExecutor;
    private final String DRIVER_DIR_NAME;
    private final String MEDIA_TYPE = "text/csv";
    private final Context context;
    private final String TAG = "GDrive";


    private Drive getGoogleDriveService() throws NullPointerException{
        GoogleAccountCredential credential = GoogleAccountCredential
                .usingOAuth2(context,
                        Collections.singleton(DriveScopes.DRIVE));

        GoogleSignInAccount signInAccount = GoogleSignIn.getLastSignedInAccount(context);

        if (signInAccount == null)
            throw new NullPointerException("GoogleSignIn.getLastSignedInAccount() return is null." +
                    "No authorized accounts");

        credential.setSelectedAccount(signInAccount.getAccount());


        return new Drive.Builder(
                AndroidHttp.newCompatibleTransport(),
                new GsonFactory(),
                credential)
                .setApplicationName(DRIVER_DIR_NAME)
                .build();
    }

    private String getIdDriveDir(Drive GDriveService){

        String id = null;
        String pageToken = null;
        do{
            try {
                // запрос для получения id файла,явяляюзегося рабочей директорией
                String query =
                        "name = '" + DRIVER_DIR_NAME + "' " +                      // имя директории
                        "and mimeType = 'application/vnd.google-apps.folder' " +   // файл должен быть директорией
                        "and trashed = false";                                     // файл не должен находится в корзине
                FileList result = GDriveService.files().list()
                        .setQ(query)
                        .setSpaces("drive")
                        .setFields("nextPageToken, files(id, name)")
                        .setPageToken(pageToken)
                        .execute();
                List<File> fileList = result.getFiles();


                // если директория отсутствует
                if (fileList.size() == 0) {
                    Log.d(TAG,"GDrive dir is not exists");
                    return null;
                }

                id = fileList.get(0).getId();
                pageToken = result.getNextPageToken();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }while (pageToken != null);

        return id;
    }

    private String createDriveDir(Drive GDriveService) throws IOException {
        Log.d(TAG,"create GDrive dir");
        File PathMetadata = new File()
                .setName(DRIVER_DIR_NAME)
                .setFolderColorRgb("#FFEB3B")
                .setMimeType("application/vnd.google-apps.folder");
        File GPath = GDriveService.files()
                .create(PathMetadata)
                .setFields("id")
                .execute();

        return GPath.getId();
    }

    public GoogleDriveManager(Context cnt,String workingDirName){
        this.context = cnt;
        this.mExecutor = Executors.newSingleThreadExecutor();
        this.DRIVER_DIR_NAME = workingDirName;
    }

    public List<File> getFileList(String fileName){
        Drive mDriveService = this.getGoogleDriveService();
        List<File> result = null;
        String parentId = this.getIdDriveDir(mDriveService);
        String query =
                "'" + parentId + "' in parents " +
                "and name = '" + fileName + "' " +
                "and mimeType != 'application/vnd.google-apps.folder' " +
                "and trashed = false";
        String pageToken = null;
        do{
            try {
                FileList files = mDriveService.files().list()
                        .setQ(query)
                        .setSpaces("drive")
                        .setFields("nextPageToken, files(id, name)")
                        .setPageToken(pageToken)
                        .execute();

                if (!files.getFiles().isEmpty())
                    result  = files.getFiles();
                pageToken = files.getNextPageToken();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }while (pageToken != null);

        return result;
    }

    public boolean isFileExists(String fileName){
        return this.getFileList(fileName) != null;
    }

    public Task<String> createPack(String pathToFile){
        Drive mDriveService = this.getGoogleDriveService();

        java.io.File mFile = new java.io.File(pathToFile);

        String fileName = mFile.getName().split("\\.")[0];

        return Tasks.call(mExecutor,() -> {

            // id директории на Gdisk,куда выгружаются данные
            String idDirGoogleDrive = this.getIdDriveDir(mDriveService);
            //Log.d("GDrive",idDirGoogleDrive);
            // Если папка отсутсвует на google disk'e,то создаём её
            if (idDirGoogleDrive == null) {
                idDirGoogleDrive = this.createDriveDir(mDriveService);
            }
            if(this.isFileExists(fileName)){
                Log.d(TAG,"file with such name already exists");
                for(File file : this.getFileList(fileName)){
                    this.deleteFileById(file.getId());
                    Log.d(TAG,"file was delete");
                }
            }
            // создание метаданных для GDrive
            File GFileMetadata = new File()
                    .setName(fileName)
                    .setParents(Collections.singletonList(idDirGoogleDrive));

            FileContent mediaContent = new FileContent(MEDIA_TYPE, mFile);
            File Gfile = mDriveService.files()
                    .create(GFileMetadata, mediaContent)
                    .setFields("id, parents")
                    .execute();

            Log.d(TAG,"Create file on GDrive");

            return Gfile.getId();
        });
    }


    public void deleteFileById(String id){
        Drive mDriveService = this.getGoogleDriveService();

        Tasks.call(mExecutor, () -> {
            try {
                mDriveService.files()
                        .delete(id)
                        .execute();
                return true;
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
        });
    }
}
