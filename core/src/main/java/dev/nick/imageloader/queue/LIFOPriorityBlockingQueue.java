package dev.nick.imageloader.queue;

public class LIFOPriorityBlockingQueue<E> extends PriorityBlockingQueue<E> {

    @Override
    public E take() throws InterruptedException {
        return takeFirst();
    }

    @Override
    public boolean offer(E e) {
        return offerFirst(e);
    }

    @Override
    public E remove() {
        return removeFirst();
    }
}
