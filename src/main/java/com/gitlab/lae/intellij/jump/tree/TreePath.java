package com.gitlab.lae.intellij.jump.tree;

import com.google.auto.value.AutoValue;

import java.util.stream.Stream;

public abstract class TreePath<K> {
    private TreePath() {
    }

    private static final TreePath<Object> EMPTY = new TreePath<Object>() {
        @Override
        public Stream<Object> keys() {
            return Stream.empty();
        }

        @Override
        public String toString() {
            return "TreePath.empty()";
        }
    };

    @SuppressWarnings("unchecked")
    static <K> TreePath<K> empty() {
        return (TreePath<K>) EMPTY;
    }

    TreePath<K> append(K key) {
        return SubPath.of(this, key);
    }

    public abstract Stream<K> keys();

    @AutoValue
    static abstract class SubPath<K> extends TreePath<K> {
        SubPath() {
        }

        abstract TreePath<K> parent();

        abstract K key();

        static <K> SubPath<K> of(TreePath<K> parent, K value) {
            return new AutoValue_TreePath_SubPath<>(parent, value);
        }

        @Override
        public Stream<K> keys() {
            return Stream.concat(
                    parent().keys(),
                    Stream.of(key()));
        }
    }
}
