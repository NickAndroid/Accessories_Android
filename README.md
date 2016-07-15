# ImageLoader
Android image loader library


## Arts
![Video 1](art/files.gif)
![Video 2](art/nets.gif)
![Video 3](art/animations.gif)

## Usage

### JCenter:

[ ![Download](https://api.bintray.com/packages/nickandroid/maven/imageloader/images/download.svg) ](https://bintray.com/nickandroid/maven/imageloader/_latestVersion)


### mvn:
```
<dependency>
  <groupId>dev.nick</groupId>
  <artifactId>imageloader</artifactId>
  <version>0.8</version>
  <type>pom</type>
</dependency>
```

### gradle
```
compile 'dev.nick:imageloader:0.8@aar'
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
                        .enableMemCache()
                        .enableDiskCache()
                        .cachingThreads(Runtime.getRuntime().availableProcessors())
                        .cacheDirName("tests")
                        .preferredLocation(CachePolicy.Location.EXTERNAL)
                        .compressFormat(Bitmap.CompressFormat.PNG)
                        .build())
                .debugLevel(Log.VERBOSE)
                .loadingThreads(Runtime.getRuntime().availableProcessors() * 2)
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
**Or**
```java
ImageLoader.getInstance().displayImage(uri, holder.imageView);
```

### Listening:
```java
ImageLoader.getInstance().displayImage(uri, holder.imageView, new LoaderListener(){...});
```

### CustomView support:
```java
class CustomView implements ImageSettable {}
```
```java
ImageLoader.getInstance().displayImage(uri, customView);
```

### Cancel tasks:
```java
ImageLoader.getInstance().cancel(ImageView/ImageSettable/Url);
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

## Supported url:
- [x] http://
- [x] https://
- [x] file://
- [x] content://
- [x] drawable://
- [x] assets://

## Supported media format:
- [x] All image formats android supported.
- [ ] Video
- [ ] Gif


## Contact me
**nick.guo.dev@icloud.com** :email:
