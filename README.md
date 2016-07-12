# ImageLoader
Android image loader library


## Arts
![Logo](art/1.png)
![Logo](art/2.png)
![Logo](art/3.png)
![Logo](art/4.png)
![Logo](art/5.png)

## Usage

### JCenter:

[ ![Download](https://api.bintray.com/packages/nickandroid/maven/imageloader/images/download.svg) ](https://bintray.com/nickandroid/maven/imageloader/_latestVersion)


### mvn:
```
<dependency>
  <groupId>dev.nick</groupId>
  <artifactId>imageloader</artifactId>
  <version>0.7</version>
  <type>pom</type>
</dependency>
```

### gradle
```
compile 'dev.nick:imageloader:0.7@aar'
```

## Samples

### Config:
```java
public class MyApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        ImageLoader.init(getApplicationContext(), new LoaderConfig.Builder()
                        .cachePolicy(new CachePolicy.Builder()
                        .preferredLocation(CachePolicy.Location.EXTERNAL)
                                .compressFormat(Bitmap.CompressFormat.PNG)
                                .build())
                        .cachingThreads(Runtime.getRuntime().availableProcessors())
                        .loadingThreads(Runtime.getRuntime().availableProcessors() * 2)
                        .diskCacheEnabled(true)
                        .memCacheEnabled(true)
                        .debug(true)
                        .build());
    }
}
```

### Loading:
```java
ImageLoader.getInstance().displayImage(uri, holder.imageView,
                        new DisplayOption.Builder()
                                .imageQuality(DisplayOption.ImageQuality.FIT_VIEW)
                                .loadingImgRes(R.drawable.ic_cloud_download_black_24dp)
                                .defaultImgRes(R.drawable.ic_broken_image_black_24dp)
                                .bitmapProcessor(new BlackWhiteBitmapProcessor())
                                .imageAnimator(new FadeInImageAnimator())
                                .build());
```

### Pause:
```java
ImageLoader.getInstance().pause();
```

### Resume:
```java
ImageLoader.getInstance().resume();
```

### Clear tasks:
```java
ImageLoader.getInstance().clearTasks();
```

### Terminate:
```java
ImageLoader.getInstance().terminate();
```

### Clear cache:
```java
 @Override
    public void onTrimMemory(int level) {
        super.onTrimMemory(level);
        ImageLoader.getInstance().clearMemCache();
    }
```
```java
@Override
    protected void onDestroy() {
        super.onDestroy();
        ImageLoader.getInstance().clearAllCache();
    }
```

## Supported images:
```java
file://
```
```java
content://
```
```java
http(s)://
```
```java
assets://
```
```java
drawable://
```

## Contact me
```java
nick.guo.dev@icloud.com
```
```java
nick.guo.dev@gmail.com
```
