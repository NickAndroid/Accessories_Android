package dev.nick.imageloader.loader.task;

public class TaskRecord {

    protected long upTime = System.currentTimeMillis();

    protected int taskId;

    public TaskRecord(int taskId) {
        this.taskId = taskId;
    }

    public int getTaskId() {
        return taskId;
    }

    public long upTime() {
        return upTime;
    }

    @Override
    public String toString() {
        return "TaskRecord{" +
                "taskId=" + taskId +
                ", upTime=" + upTime +
                '}';
    }
}
