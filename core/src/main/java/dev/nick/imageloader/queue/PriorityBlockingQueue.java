package dev.nick.imageloader.queue;

import android.annotation.TargetApi;
import android.os.Build;
import android.support.annotation.NonNull;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Spliterator;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class PriorityBlockingQueue<E> implements BlockingQueue<E> {

    LinkedBlockingDeque<E> mBase;

    public void addFirst(E e) {
        mBase.addFirst(e);
    }

    public boolean removeLastOccurrence(Object o) {
        return mBase.removeLastOccurrence(o);
    }

    public void push(E e) {
        mBase.push(e);
    }

    public E getLast() {
        return mBase.getLast();
    }

    public E pollLast() {
        return mBase.pollLast();
    }

    public boolean offerLast(E e) {
        return mBase.offerLast(e);
    }

    public E takeLast() throws InterruptedException {
        return mBase.takeLast();
    }

    public boolean offerLast(E e, long timeout, TimeUnit unit) throws InterruptedException {
        return mBase.offerLast(e, timeout, unit);
    }

    public void putLast(E e) throws InterruptedException {
        mBase.putLast(e);
    }

    public void putFirst(E e) throws InterruptedException {
        mBase.putFirst(e);
    }

    public E peekFirst() {
        return mBase.peekFirst();
    }

    public Iterator<E> descendingIterator() {
        return mBase.descendingIterator();
    }

    public E pop() {
        return mBase.pop();
    }

    public E peekLast() {
        return mBase.peekLast();
    }

    public boolean removeFirstOccurrence(Object o) {
        return mBase.removeFirstOccurrence(o);
    }

    public E getFirst() {
        return mBase.getFirst();
    }

    public void addLast(E e) {
        mBase.addLast(e);
    }

    public E removeLast() {
        return mBase.removeLast();
    }

    public E pollFirst() {
        return mBase.pollFirst();
    }

    public E removeFirst() {
        return mBase.removeFirst();
    }

    public E pollFirst(long timeout, TimeUnit unit) throws InterruptedException {
        return mBase.pollFirst(timeout, unit);
    }

    public boolean offerFirst(E e) {
        return mBase.offerFirst(e);
    }

    public E pollLast(long timeout, TimeUnit unit) throws InterruptedException {
        return mBase.pollLast(timeout, unit);
    }

    public boolean offerFirst(E e, long timeout, TimeUnit unit) throws InterruptedException {
        return mBase.offerFirst(e, timeout, unit);
    }

    public E takeFirst() throws InterruptedException {
        return mBase.takeFirst();
    }

    @Override
    public boolean add(E e) {
        return mBase.add(e);
    }

    @Override
    public boolean offer(@NonNull E e) {
        return mBase.offer(e);
    }

    @Override
    public void put(E e) throws InterruptedException {
        mBase.put(e);
    }

    @Override
    public boolean offer(E e, long timeout, @NonNull TimeUnit unit) throws InterruptedException {
        return mBase.offer(e, timeout, unit);
    }

    @Override
    public E remove() {
        return mBase.remove();
    }

    @Override
    public E poll() {
        return mBase.poll();
    }

    @Override
    public E take() throws InterruptedException {
        return mBase.take();
    }

    @Override
    public E poll(long timeout, TimeUnit unit) throws InterruptedException {
        return mBase.poll(timeout, unit);
    }

    @Override
    public E element() {
        return mBase.element();
    }

    @Override
    public E peek() {
        return mBase.peek();
    }

    @Override
    public int remainingCapacity() {
        return mBase.remainingCapacity();
    }

    @Override
    public int drainTo(Collection<? super E> c) {
        return mBase.drainTo(c);
    }

    @Override
    public int drainTo(Collection<? super E> c, int maxElements) {
        return mBase.drainTo(c, maxElements);
    }

    @Override
    public boolean remove(Object o) {
        return mBase.remove(o);
    }

    @Override
    public int size() {
        return mBase.size();
    }

    @Override
    public boolean contains(Object o) {
        return mBase.contains(o);
    }

    @NonNull
    @Override
    public Object[] toArray() {
        return mBase.toArray();
    }

    @NonNull
    @Override
    public <T> T[] toArray(T[] a) {
        return mBase.toArray(a);
    }

    @Override
    public String toString() {
        return mBase.toString();
    }

    @Override
    public void clear() {
        mBase.clear();
    }

    @NonNull
    @Override
    public Iterator<E> iterator() {
        return mBase.iterator();
    }

    @TargetApi(Build.VERSION_CODES.N)
    @Override
    public Spliterator<E> spliterator() {
        return mBase.spliterator();
    }

    @Override
    public boolean addAll(Collection<? extends E> c) {
        return mBase.addAll(c);
    }

    @Override
    public boolean isEmpty() {
        return mBase.isEmpty();
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        return mBase.containsAll(c);
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        return mBase.removeAll(c);
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        return mBase.retainAll(c);
    }

    @TargetApi(Build.VERSION_CODES.N)
    @Override
    public boolean removeIf(Predicate<? super E> filter) {
        return mBase.removeIf(filter);
    }

    @Override
    public boolean equals(Object o) {
        return mBase.equals(o);
    }

    @Override
    public int hashCode() {
        return mBase.hashCode();
    }

    @TargetApi(Build.VERSION_CODES.N)
    @Override
    public Stream<E> stream() {
        return mBase.stream();
    }

    @TargetApi(Build.VERSION_CODES.N)
    @Override
    public Stream<E> parallelStream() {
        return mBase.parallelStream();
    }

    @TargetApi(Build.VERSION_CODES.N)
    @Override
    public void forEach(Consumer<? super E> action) {
        mBase.forEach(action);
    }
}
