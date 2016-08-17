package dev.nick.imageloader.loader;

public class ImageSource<X> {

    private ImageSourceType<X> type;
    private String url;

    public ImageSource() {
    }

    public ImageSource(ImageSourceType<X> type, String url) {
        this.type = type;
        this.url = url;
    }

    public ImageSourceType<X> getType() {
        return type;
    }

    public String getUrl() {
        return url;
    }

    public void setType(ImageSourceType<X> type) {
        this.type = type;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
