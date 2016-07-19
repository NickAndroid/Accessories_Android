package dev.nick.twenty;

/**
 * Created by guohao4 on 2016/7/19.
 */
public class Test {
    Class clz;
    String title;
    String description;

    public Test(Class clz, String title, String description) {
        this.clz = clz;
        this.title = title;
        this.description = description;
    }

    public Test(Class clz, String description) {
        this.clz = clz;
        this.description = description;
    }

    public Test(Class clz) {
        this.clz = clz;
        this.title = clz.getSimpleName();
        this.description = "No description found for this test";
    }
}
