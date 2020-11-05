package de.baumeister.itertools;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class IteratorTools {

    public static class EnumeratedEntry<T> {
        private int index;
        private T value;

        public EnumeratedEntry(int index, T value) {
            this.index = index;
            this.value = value;
        }

        public int getIndex() {
            return index;
        }

        public T getValue() {
            return value;
        }
    }

    /**
     * Enumerates the given iterator with a start value of zero.
     * Note that the objects returned by the iterator are only valid for one iteration.
     * The objects are reused to removed performance overhead.
     * @param iterable The Iterable to be enumerated
     * @param <T> Type of the iterator
     * @return Enumerated iterator
     */
    public static <T> Iterable<EnumeratedEntry<T>> enumerate(Iterable<T> iterable) {
        return enumerate(iterable, 0);
    }

    /**
     * Enumerates the given iterator.
     * Note that the objects returned by the iterator are only valid for one iteration.
     * The objects are reused to removed performance overhead.
     * @param iterable The Iterable to be enumerated
     * @param startValue The start value of the enumeration
     * @param <T> Type of the iterator
     * @return Enumerated iterator
     */
    public static <T> Iterable<EnumeratedEntry<T>> enumerate(Iterable<T> iterable, int startValue) {
        return () -> new Iterator<>() {
            private int idx = startValue;
            private final Iterator<T> it = iterable.iterator();
            private final EnumeratedEntry<T> entry = new EnumeratedEntry<>(0, null);
            @Override
            public boolean hasNext() {
                return it.hasNext();
            }
            @Override
            public EnumeratedEntry<T> next() {
                entry.value = it.next();
                entry.index = idx++;
                return entry;
            }
        };
    }



    public static class ZipEntry<T, E> {
        private T left;
        private E right;

        public ZipEntry(T left, E right) {
            this.left = left;
            this.right = right;
        }

        public T getLeft() {
            return left;
        }

        public E getRight() {
            return right;
        }
    }

    /**
     * Zips the given Iterators. E.g. if you give it two iterators containing [1, 2, 3, 4] and [2, 3, 4, 5] it will return
     * an iterator which will give you pairs of [1, 2] [2, 3] [3, 4] [4, 5]
     * @param leftIterable The first iterator
     * @param rightIterable The second iterator
     * @param <T> Type of the first Iterator
     * @param <E> Type fof the second Iterator
     * @return Iterator zipping the given iterators
     */
    public static <T, E> Iterable<ZipEntry<T, E>> zip(Iterable<T> leftIterable, Iterable<E> rightIterable) {
        var leftIterator = leftIterable.iterator();
        var rightIterator = rightIterable.iterator();

        return () -> new Iterator<>() {
            private final ZipEntry<T,E> entry = new ZipEntry<>(null, null);
            @Override
            public boolean hasNext() {
                return leftIterator.hasNext() && rightIterator.hasNext();
            }
            @Override
            public ZipEntry<T, E> next() {
                entry.left = leftIterator.next();
                entry.right = rightIterator.next();
                return entry;
            }
        };
    }

    /**
     * Chains the given iterator together. E.g. if you give it two iterators containing [1, 2, 3] and [4, 5, 6] it will give
     * you a single iterator with [1, 2, 3, 4, 5, 6].
     * @param first The first iterator
     * @param second The second iterator
     * @param <T> Type of the Iterators
     * @return Iterator chaining the two given iterators
     */
    public static <T> Iterable<T> chain(Iterable<T> first, Iterable<T> second) {
        var firstIterator = first.iterator();
        var secondIterator = second.iterator();
        return () -> new Iterator<T>() {
            @Override
            public boolean hasNext() {
                return firstIterator.hasNext() || secondIterator.hasNext();
            }
            @Override
            public T next() {
                T val;
                if(firstIterator.hasNext())
                    val = firstIterator.next();
                else
                    val = secondIterator.next();
                return val;
            }
        };
    }

    /**
     * Creates an infinite iterable that will count values from zero to infinity.
     * @return An infinite iterable that will count values from zero
     */
    public static Iterable<Integer> count() {
        return count(0);
    }

    /**
     * Creates an infinite iterable that will count values from the given value to infinity.
     * @param startValue The value to start counting from
     * @return An infinite iterable that will count values from the given value
     */
    public static Iterable<Integer> count(Integer startValue) {
        return count(startValue, null);
    }

    /**
     * Creates a iterable of numbers between the start and end value
     * @param startValue The start value (inclusive)
     * @param endValue The end value (inclusive)
     * @return An iterable that will count values from the given start to end value
     */
    public static Iterable<Integer> count(Integer startValue, Integer endValue) {
        return () -> new Iterator<>() {
            private int val = startValue;
            @Override
            public boolean hasNext() {
                if(endValue == null)
                    return true;
                else
                    return val <= endValue;
            }
            @Override
            public Integer next() {
                return val++;
            }
        };
    }

    /**
     * Creates an infinite iterable over the given iterable. If the iterable reaches the end, it will start again.
     * @param iterable Iterable to cycle through
     * @param <T> Type of the values in the iterable
     * @return An infinite iterable with the values from the given iterable
     */
    public static <T> Iterable<T> cycle(Iterable<T> iterable) {
        return () -> new Iterator<T>() {
            private Iterator<T> iterator = iterable.iterator();
            @Override
            public boolean hasNext() {
                return true;
            }
            @Override
            public T next() {
                if(!iterator.hasNext())
                    iterator = iterable.iterator();
                return iterator.next();
            }
        };
    }

    /**
     * Transforms a <code>Stream</code> to an iterable.
     * This function is very useful if you want to use a stream in a conventional for-each loop.
     * @param stream The stream to transform
     * @param <T> Type of the values inside the stream
     * @return An iterable of the given stream
     */
    public static <T> Iterable<T> iterate(Stream<T> stream) {
        return () -> new Iterator<T>() {
            private Iterator<T> iterator = stream.iterator();
            @Override
            public boolean hasNext() {
                return iterator.hasNext();
            }
            @Override
            public T next() {
                return iterator.next();
            }
        };
    }

    public static <T, E> Map<E, List<T>> groupBy(Iterable<T> iterable, Function<T, E> keyExtractor) {
        return groupBy(iterable, keyExtractor, false);
    }

    public static <T, E> Map<E, List<T>> groupByParallel(Iterable<T> iterable, Function<T, E> keyExtractor) {
        return groupBy(iterable, keyExtractor, true);
    }

    private static <T, E> Map<E, List<T>> groupBy(Iterable<T> iterable, Function<T, E> keyExtractor, boolean parallel) {
        return StreamSupport.stream(iterable.spliterator(), parallel)
                .collect(Collectors.groupingBy(keyExtractor));
    }
}