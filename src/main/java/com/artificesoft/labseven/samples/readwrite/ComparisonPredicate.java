package com.artificesoft.labseven.samples.readwrite;

@FunctionalInterface
public interface ComparisonPredicate<E> {
    E compare(E e1, E e2);
}
