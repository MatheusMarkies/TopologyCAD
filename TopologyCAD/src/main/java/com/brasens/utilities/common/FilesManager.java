package com.brasens.utilities.common;

import com.brasens.CAD;

import java.io.*;

public class FilesManager {

    public static String ApplicationDataFolder = System.getenv("APPDATA") + "\\MSPM";
    public static String ApplicationSystemFolder = System.getenv("ProgramFiles") + "\\Brasens";
    public static String[] ImportantDirectories = new String[]{
            ApplicationDataFolder + "\\" + "data",
            ApplicationDataFolder + "\\" + "updates",
            ApplicationSystemFolder + "\\" + "MSPM",
            ApplicationSystemFolder + "\\" + "MSPM" + "\\" + "jbr",
            ApplicationSystemFolder + "\\" + "MSPM" + "\\" + "resources",
    };

    public static final String LOCAL_FILE_REMEMBER_PASSWORD = "remember.fit";

    public static void write(Object o, String filename) throws IOException {
        try {
            File selectedFile = new File(ApplicationDataFolder + "\\"+ filename);

            FileOutputStream fileOutput = new FileOutputStream(selectedFile);
            ObjectOutputStream objectStream = new ObjectOutputStream(fileOutput);

            objectStream.writeObject(o);

            objectStream.close();
            fileOutput.close();
        }catch (Exception e){
            CAD.printNicerStackTrace(e);
        }
    }

    public static Object read(File file){
        Object o = null;
       try {
           File selectedFile = file;

           FileInputStream fileInput = new FileInputStream(selectedFile);
           ObjectInputStream objectStream = new ObjectInputStream(fileInput);

           o = objectStream.readObject();

           objectStream.close();
           fileInput.close();

       }catch (Exception e){
           CAD.printNicerStackTrace(e);
       }
        return o;
    }

    public static void applicationDirCreator() {
        File mainAppDataRoot = new File(FilesManager.ApplicationDataFolder);
        File mainRoot = new File(FilesManager.ApplicationSystemFolder);

        if (!mainAppDataRoot.exists())
            mainAppDataRoot.mkdirs();

        if (!mainRoot.exists())
            mainRoot.mkdirs();

        for (String dir : FilesManager.ImportantDirectories) {
            File dirFile = new File(dir);
            if (!dirFile.exists())
                dirFile.mkdirs();
        }
    }

}
