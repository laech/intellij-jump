package com.gitlab.lae.intellij.jump.tree;

import com.google.auto.value.AutoValue;

import java.util.function.BiConsumer;
import java.util.function.Function;

@AutoValue
public abstract class TreeLeaf<K, T> extends Tree<K, T> {
    TreeLeaf() {
    }

    public abstract T value();

    public static <K, T> TreeLeaf<K, T> of(T value) {
        return new AutoValue_TreeLeaf<>(value);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <R> TreeLeaf<R, T> mapKey(Function<? super K, ? extends R> mapper) {
        return (TreeLeaf<R, T>) this;
    }

    void forEach(TreePath<K> path, BiConsumer<TreePath<K>, T> action) {
        action.accept(path, value());
    }

    @Override
    public String toString() {
        return "TreeLeaf{" + value() + "}";
    }
}
