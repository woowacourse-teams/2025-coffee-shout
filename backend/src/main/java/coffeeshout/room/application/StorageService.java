package coffeeshout.room.application;

public interface StorageService {

    String uploadDataAndGetUrl(String contents, byte[] data);
}
