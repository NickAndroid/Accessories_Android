package dev.nick.imageloader.loader.task;

public class ImageTaskRecord extends TaskRecord {

    private int settableId;

    public ImageTaskRecord(int settableId, int taskId) {
        super(taskId);
        this.settableId = settableId;
    }

    public int getSettableId() {
        return settableId;
    }

    @Override
    public String toString() {
        return "ImageTaskRecord{" +
                "settableId=" + settableId +
                "} " + super.toString();
    }
}
