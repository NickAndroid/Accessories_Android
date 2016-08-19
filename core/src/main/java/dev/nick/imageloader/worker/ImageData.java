package dev.nick.imageloader.worker;

public class ImageData<X> {

    private ImageSource<X> type;
    private String url;

    public ImageData() {
    }

    public ImageData(ImageSource<X> type, String url) {
        this.type = type;
        this.url = url;
    }

    public ImageSource<X> getType() {
        return type;
    }

    public String getUrl() {
        return url;
    }

    public void setType(ImageSource<X> type) {
        this.type = type;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
