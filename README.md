# ZImageLoader
Android image loader library

[ ![Download](https://api.bintray.com/packages/nickandroid/maven/imageloader/images/download.svg) ](https://bintray.com/nickandroid/maven/imageloader/_latestVersion)

![Logo](art/screen1.png)

### Usage
mvn:
```
<dependency>
  <groupId>dev.nick</groupId>
  <artifactId>imageloader</artifactId>
  <version>0.1</version>
  <type>pom</type>
</dependency>
```

gradle
```
compile 'dev.nick:imageloader:0.1'
```

### Samples

Config:
```java
public class MyApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        ZImageLoader.init(this, new ZImageLoader.Config()
                .setDebug(true)
                .setPreferExternalStorageCache(true)
                .setCacheThreads(2)
                .setLoadingThreads(2)
                .setEnableFileCache(true)
                .setEnableMemCache(true));
    }
}
```

Easy useage:
```java
 ZImageLoader.getInstance().displayImage(uri, holder.imageView,
                        new DisplayOption(R.drawable.ic_broken_image_black_24dp,
                                R.drawable.ic_cloud_download_black_24dp));
```

Supported content:
```java
file://
content://
http://
assets://
drawable://
```