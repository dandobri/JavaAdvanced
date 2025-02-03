package info.kgeorgiy.ja.dobris.iterative;

import info.kgeorgiy.java.advanced.iterative.NewScalarIP;
import info.kgeorgiy.java.advanced.iterative.ScalarIP;
import info.kgeorgiy.java.advanced.mapper.ParallelMapper;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * Class witch does operation with array
 * max in array
 * min in array
 * all in array
 * any in array
 * count in array
 */
public class IterativeParallelism implements NewScalarIP {

    private final ParallelMapper mapper;

    public IterativeParallelism() {
        mapper = null;
    }
    public IterativeParallelism(ParallelMapper mapper) {
        this.mapper = mapper;
    }
    public <T, E> E operation(int threads, List<? extends T> values,
                              Function<? super Stream<? extends T>, ? extends E> f1,
                              Function<Stream<? extends E>, E> result,
                              int step) throws InterruptedException {
        step = Math.min(values.size(), step);
        int size = Math.ceilDiv(values.size(), step);
        threads = Math.min(threads, size);
        Thread[] t = new Thread[threads];
        int countInGroup = size / threads;
        int countInLastGroup = size % threads;
        List<Stream<? extends T>> listSubLists = new ArrayList<>(Collections.nCopies(threads, null));
        int finalStep = step;
        List<? extends T> nthElement = Stream.iterate(0, i -> i + finalStep)
                .limit(values.size() / step + Math.min(values.size() % step, 1)).map(values::get).toList();
        int right = 0;
        for (int i = 0; i < threads; i++) {
            int left = right;
            right = (i + 1) * countInGroup + Math.min(i + 1, countInLastGroup);
            listSubLists.set(i,
                    nthElement.subList(left, right).stream());
        }
        List<E> resultArr;
        if(mapper != null) {
            resultArr = mapper.map(f1, listSubLists);
        } else {
            resultArr = new ArrayList<>(Collections.nCopies(threads, null));
            for (int i = 0; i < threads; i++) {
                final int finalI = i;
                t[finalI] = new Thread(() -> resultArr.set(finalI, f1.apply(listSubLists.get(finalI))));
                t[finalI].start();
            }
            // :NOTE: use stream or at least enhanced for
            for (Thread thread: t) {
                thread.join();
            }
        }
        return result.apply(resultArr.stream());
    }

    /**
     *
     * @param threads number of concurrent threads.
     * @param values values to get maximum of.
     * @param comparator value comparator.
     * @param step step size.
     * @return max element in array by step
     * @param <T>
     * @throws InterruptedException
     */
    // :NOTE: inheritDocs
    @Override
    public <T> T maximum(int threads, List<? extends T> values, Comparator<? super T> comparator, int step)
            throws InterruptedException {
        return operation(threads, values, a -> a.max(comparator).orElseThrow(), a -> a.max(comparator).orElseThrow(), step);
    }

    /**
     *
     * @param threads number of concurrent threads.
     * @param values values to get minimum of.
     * @param comparator value comparator.
     * @param step step size.
     * @return min element in array by step
     * @param <T>
     * @throws InterruptedException
     */
    @Override
    public <T> T minimum(int threads, List<? extends T> values, Comparator<? super T> comparator, int step)
            throws InterruptedException {
        return maximum(threads, values, comparator.reversed(), step);
    }

    /**
     *
     * @param threads number of concurrent threads.
     * @param values values to test.
     * @param predicate test predicate.
     * @param step step size.
     * @return if all elements in array equals predicate by step
     * @param <T>
     * @throws InterruptedException
     */
    @Override
    public <T> boolean all(int threads, List<? extends T> values, Predicate<? super T> predicate, int step)
            throws InterruptedException {
        return !any(threads, values, predicate.negate(), step);
    }
    // :NOTE: min via max all via any

    /**
     *
     * @param threads number of concurrent threads.
     * @param values values to test.
     * @param predicate test predicate.
     * @param step step size.
     * @return if one element in array equals predicate by step
     * @param <T>
     * @throws InterruptedException
     */
    @Override
    public <T> boolean any(int threads, List<? extends T> values, Predicate<? super T> predicate, int step)
            throws InterruptedException {
        return operation(threads, values, a -> a.anyMatch(predicate), a -> a.anyMatch(Boolean::booleanValue), step);
    }

    /**
     *
     * @param threads number of concurrent threads.
     * @param values values to test.
     * @param predicate test predicate.
    * @param step step size.
     * @return count element equals predicate by step
     * @param <T>
     * @throws InterruptedException
     */
    @Override
    public <T> int count(int threads, List<? extends T> values, Predicate<? super T> predicate, int step)
            throws InterruptedException {
        return operation(threads, values, a -> (int) a.filter(predicate).count(), a -> a.mapToInt(Integer::intValue).sum(), step);
    }
}
