package dev.nick.imageloader.queue;

public class LIFOPriorityBlockingQueue<E> extends PriorityBlockingQueue<E> {

    @Override
    public boolean offer(E e) {
        return super.offerFirst(e);
    }
}
