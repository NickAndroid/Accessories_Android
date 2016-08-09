package dev.nick.imageloader.queue;

public class LIFOPriorityBlockingQueue<E> extends PriorityBlockingQueue<E> {

    @Override
    public E take() throws InterruptedException {
        return takeLast();
    }
}
