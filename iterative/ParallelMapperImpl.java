package info.kgeorgiy.ja.dobris.iterative;

import info.kgeorgiy.java.advanced.mapper.ParallelMapper;

import java.util.*;
import java.util.function.Function;

public class ParallelMapperImpl implements ParallelMapper {

    private final List<Thread> workThreads;

    private final Queue<Work> works;

    /**
     * Constructor in which we will initilisation the threads
     * @param threads count of threads on which we will count function
     */
    public ParallelMapperImpl(int threads) {
        workThreads = new ArrayList<>(Collections.nCopies(threads, null));
        works = new ArrayDeque<>();
        for (int i = 0; i < threads; i++) {
            workThreads.set(i, new Thread(() -> {
                try {
                    while (!Thread.interrupted()) {
                        Work work;
                        synchronized (works) {
                            while (works.isEmpty()) {
                                works.wait();
                            }
                            work = works.poll();
                        }
                        work.syncWork();
                    }
                } catch (InterruptedException ignored) {
                }
            }));
            workThreads.get(i).start();
        }
    }

    /**
     * Class which do the work with Users
     */
    public class Work {
        private final Runnable work;
        private final Sync sync;
        public Work(Runnable work, Sync sync) {
            this.sync = sync;
            this.work = work;
        }
        private void syncWork() {
            work.run();
            sync.syncCount();
        }
        private void addWork() {
            synchronized (works) {
                works.add(this);
                works.notifyAll();
            }
        }
    }

    /**
     *  Class which is responsible for synchronisation of threads
     */
    public static class Sync {
        private int count;
        public Sync(int count) {
            this.count = count;
        }
        private synchronized void waitingEnd() throws InterruptedException {
            while(count != 0) {
                wait();
            }
        }
        private synchronized void syncCount() {
            count--;
            if(count == 0) {
                notifyAll();
            }
        }
    }

    /**
     *
     * @param f function which we will do
     * @param items elements on which we will do function
     * @return list of result of the function
     * @param <T> parameter
     * @param <R> parameter
     * @throws InterruptedException if we have a problem in threads
     */
    @Override
    public <T, R> List<R> map(Function<? super T, ? extends R> f, List<? extends T> items) throws InterruptedException {
        List<R> result = new ArrayList<>(Collections.nCopies(items.size(), null));
        final Sync sync = new Sync(result.size());
        for (int i = 0; i < items.size(); i++) {
            int finalI = i;
            Work work = new Work(() -> result.set(finalI, f.apply(items.get(finalI))), sync);
            work.addWork();
        }
        sync.waitingEnd();
        return result;
    }

    @Override
    public void close() {
        workThreads.forEach(Thread::interrupt);
    }
}
