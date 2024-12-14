package bgu.spl.net.impl.tftp;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.io.IOException;
import  java.util.concurrent.ArrayBlockingQueue;
public class FileRepresent {
    private boolean isUpload;
    private boolean isDownload;
    private String name ;
    public int connectionId;
    private String directoryPath = "Flies" + File.separator;

    public FileRepresent(int connectionId , String fileName ){
        this.connectionId =connectionId;
        this.name = fileName ;
    }
    public FileRepresent(int connectionId , String fileName , byte[] data ){
        this.isUpload = false;
        this.isDownload =false;
        this.connectionId =connectionId;
        this.name = fileName ;
        Path fullPath = Paths.get(directoryPath, fileName);
        createFile(fullPath , data);
    }
    public void createFile(Path fullPath , byte[] data){
        try {
            Files.write(fullPath, data);
            System.out.println("File was saved successfully at: " + fullPath);
        } catch (IOException e) {
            System.err.println("An error occurred while writing the file.");
            e.printStackTrace();
        }
    }
    public byte[] getFileData() {
        Path fullPath = Paths.get(directoryPath, name);
        try {
            return Files.readAllBytes(fullPath);
        } catch (IOException e) {
            System.err.println("An error occurred while reading the file.");
            e.printStackTrace();
            return null;
        }
    }
    public static boolean isFileExistsInDirectory(String fileName) {
        String directoryPath = "Flies" + File.separator;
        Path filePath = Paths.get(directoryPath, fileName);
        return Files.exists(filePath);
    }

    public String getFileName(){
        return this.name;
    }
    public static void deleteFile(String name){
        String directoryPath = "Flies" + File.separator;
        Path fileToDeletePath = Paths.get(directoryPath, name);
        try {
            // Delete the file if it exists
            Files.delete(fileToDeletePath);
            System.out.println("File deleted successfully: " + fileToDeletePath);
        } catch (IOException e) {
            // Handle the situation that the file cannot be deleted
            System.err.println("Unable to delete the file: " + fileToDeletePath);
            e.printStackTrace();
        }

    }


    public int getConnectioId() {
        return connectionId;
    }
    public void setConnectioId(int connectioID) {
        this.connectionId = connectioID;
    }

    public boolean isUpload() {
        return isUpload;
    }

    public void setIsUpload(boolean isUpload) {
        this.isUpload = isUpload;
    }

    public boolean isDownload() {
        return isDownload;
    }

    public void setIsDownload(boolean isDownload) {
        this.isDownload = isDownload;
    }
    public Path getPath(){
        return Paths.get(directoryPath, this.name);
    }
}




