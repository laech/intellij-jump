package com.gitlab.lae.intellij.jump.tree;

import com.google.auto.value.AutoValue;

import java.util.function.BiConsumer;

@AutoValue
public abstract class TreeLeaf<T> extends Tree<T> {
    TreeLeaf() {
    }

    public abstract T value();

    public static <T> TreeLeaf<T> of(T value) {
        return new AutoValue_TreeLeaf<>(value);
    }

    void forEach(TreePath path, BiConsumer<TreePath, T> action) {
        action.accept(path, value());
    }
}
