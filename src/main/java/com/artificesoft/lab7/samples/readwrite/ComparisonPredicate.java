package com.artificesoft.lab7.samples.readwrite;

@FunctionalInterface
public interface ComparisonPredicate<E> {
    E compare(E e1, E e2);
}
