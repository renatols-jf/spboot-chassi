package io.github.renatolsjf.chassis.util;

import java.time.Duration;
import java.time.Instant;
import java.util.*;

public class RollingTimedWindowSet<E> implements Set<E> {

    private final Duration windowDuration;
    private final ArrayList<E> backingList = new ArrayList<>();
    private final ArrayList<Instant> insertionTimeList = new ArrayList<>();

    public RollingTimedWindowSet(Duration windowDuration) {
        this.windowDuration = windowDuration;
    }

    public synchronized boolean roll() {

        if (this.insertionTimeList.isEmpty()) {
            return false;
        }

        int obsoleteCount = 0;
        for (Instant insertionInstant: this.insertionTimeList) {
            if (Instant.now().isAfter(insertionInstant.plusSeconds(this.windowDuration.toSeconds()))) {
                obsoleteCount++;
            } else {
                break;
            }
        }
        if (obsoleteCount > 0) {
            for (int i = 0; i < obsoleteCount; i++) {
                this.backingList.remove(0);
                this.insertionTimeList.remove(0);
            }
            return true;
        } else {
            return false;
        }

    }

    @Override
    public int size() {
        this.roll();
        return this.backingList.size();
    }

    @Override
    public boolean isEmpty() {
        this.roll();
        return this.backingList.isEmpty();
    }

    @Override
    public boolean contains(Object o) {
        this.roll();
        return this.backingList.contains(o);
    }

    @Override
    public Iterator<E> iterator() {
        this.roll();
        List<E> backupList = new ArrayList<>();
        backupList.addAll(this.backingList);
        return backupList.iterator();
    }

    @Override
    public Object[] toArray() {
        this.roll();
        return this.backingList.toArray();
    }

    @Override
    public <T> T[] toArray(T[] a) {
        this.roll();
        return this.backingList.toArray(a);
    }

    @Override
    public boolean add(E e) {
        this.roll();
        this.insertionTimeList.add(Instant.now());
        this.backingList.add(e);
        return true;
    }

    @Override
    public boolean remove(Object o) {
        this.roll();
        int idx = this.backingList.indexOf(o);
        if (idx > -1) {
            this.backingList.remove(idx);
            this.insertionTimeList.remove(idx);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        this.roll();
        return this.backingList.containsAll(c);
    }

    @Override
    public boolean addAll(Collection<? extends E> c) {
        this.roll();
        Instant instant = Instant.now();
        for (int i = 0; i < c.size(); i++) {
            this.insertionTimeList.add(instant);
        }
        return this.backingList.addAll(c);
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        this.roll();

        List<Integer> indexesToRetain = new ArrayList<>();
        for (Object element: c) {
            int idx = this.backingList.indexOf(element);
            if (idx > -1) {
                indexesToRetain.add(idx);
            }
        }

        if (indexesToRetain.size() == this.backingList.size()) {
            return false;
        }

        for (int i = this.backingList.size() - 1; i <= 0; i--) {
            if (!indexesToRetain.contains(i)) {
                this.backingList.remove(i);
                this.insertionTimeList.remove(i);
            }
        }

        return false;
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        this.roll();

        List<Integer> indexesToRemove = new ArrayList<>();
        for (Object element: c) {
            int idx = this.backingList.indexOf(element);
            if (idx > -1) {
                indexesToRemove.add(idx);
            }
        }

        if (indexesToRemove.isEmpty()) {
            return false;
        }

        indexesToRemove.sort(Comparator.reverseOrder());
        indexesToRemove.forEach(i -> {
            this.backingList.remove(i);
            this.insertionTimeList.remove(i);
        });

        return true;
    }

    @Override
    public void clear() {
        this.insertionTimeList.clear();
        this.backingList.clear();
    }
}
