package info.kgeorgiy.ja.dobris.arrayset;

import java.util.*;

public class ArraySet<E> extends AbstractSet<E> implements SortedSet<E> {
    private final List<E> sortedSet;
    private final Comparator<? super E> comparator;
    public ArraySet(){
        this.sortedSet = new ArrayList<>(Collections.emptyList());
        this.comparator = null;
    }
    public ArraySet(Collection<? extends E> collection) {
        this.sortedSet = new ArrayList<>(new TreeSet<>(collection));
        this.comparator = null;
    }
    public ArraySet(Collection<? extends E> collection, Comparator<? super E> comparator) {
        TreeSet<E> comparatorSet = new TreeSet<>(comparator);
        comparatorSet.addAll(collection);
        this.sortedSet = new ArrayList<>(new TreeSet<>(comparatorSet));
        this.comparator = comparator;
    }
    public ArraySet(Comparator<? super E> comparator) {
        this.sortedSet = new ArrayList<>(Collections.emptyList());
        this.comparator = comparator;
    }

    // :NOTE: ArraySet(Comparator)

    @Override
    public Comparator<? super E> comparator() {
        return comparator;
    }

    public ArrayList<E> subList(int fromElenemt, int toElement, int offset) {
        return new ArrayList<>(sortedSet.subList(fromElenemt, toElement + offset));
    }
    @SuppressWarnings("unchecked")
    // :NOTE: unchecked cast
    @Override
    public SortedSet<E> subSet(E fromElement, E toElement) {
        if(comparator != null) {
            if(comparator.compare(fromElement, toElement) > 0) {
                throw new IllegalArgumentException("fromElement > toElement");
            }
        } else {
            if(((Comparable<E>) fromElement).compareTo(toElement) > 0) {
                throw new IllegalArgumentException("fromElement > toElement");
            }
        }
        return new ArraySet<>(subList(index(fromElement), index(toElement), 0));
    }
    @Override
    public SortedSet<E> headSet(E toElement) {
        int indexTo = index(toElement);
        if(indexTo <= 0) {
            return new ArraySet<>(comparator);
        }
        return new ArraySet<>(subList(index(first()), indexTo, 0), comparator);
    }

    @Override
    public SortedSet<E> tailSet(E fromElement) {
        int indexFrom = index(fromElement);
        if(indexFrom >= size()) {
            return new ArraySet<>(comparator);
        }
        return new ArraySet<>(subList(indexFrom, index(last()), 1), comparator);
    }
    public int index(E o) {
        int index = Collections.binarySearch(sortedSet, o, comparator);
        if(index < 0) {
            index = -index - 1;
        }
        return index;
    }

    // :NOTE: unnecessary implementation

    @Override
    public E first() {
        if(size() == 0) {
            throw new NoSuchElementException();
        } else return sortedSet.get(0);
    }

    @Override
    public E last() {
        if(size() == 0) {
            throw new NoSuchElementException();
        } else return sortedSet.get(size() - 1);
    }

    @Override
    public int size() {
        return sortedSet.size();
    }

    @Override
    public boolean isEmpty() {
        return sortedSet.isEmpty();
    }
    @SuppressWarnings("unchecked")
    @Override
    public boolean contains(Object o) {
        return Collections.binarySearch(sortedSet, (E)o, comparator) >= 0;
    }

    @Override
    public Iterator<E> iterator() {
        return sortedSet.iterator();
    }

}
