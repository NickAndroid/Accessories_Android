package dev.nick.twenty;

import android.app.Application;
import android.test.ApplicationTestCase;

import dev.nick.imageloader.ImageLoader;
import dev.nick.imageloader.LoaderConfig;

/**
 * <a href="http://d.android.com/tools/testing/testing_android.html">Testing Fundamentals</a>
 */
public class ApplicationTest extends ApplicationTestCase<Application> {
    public ApplicationTest() {
        super(Application.class);
    }

    public void testShared() {
        ImageLoader loader1 = ImageLoader.createShared(getContext());
        ImageLoader loader2 = ImageLoader.createShared(getContext(), LoaderConfig.DEFAULT_CONFIG);
        assertTrue(loader1 == loader2);
    }

    public void testCreate() {
        ImageLoader loader1 = ImageLoader.create(getContext());
        ImageLoader loader2 = ImageLoader.create(getContext());
        ImageLoader loader3 = ImageLoader.createShared(getContext());
        assertTrue(loader1 != loader2 && loader1 != loader3);
    }
}