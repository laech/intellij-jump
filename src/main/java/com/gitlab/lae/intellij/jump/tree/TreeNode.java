package com.gitlab.lae.intellij.jump.tree;

import com.google.auto.value.AutoValue;
import com.google.common.collect.ImmutableList;

import java.util.List;
import java.util.function.BiConsumer;

import static java.util.Collections.emptyList;
import static java.util.stream.IntStream.range;

@AutoValue
public abstract class TreeNode<T> extends Tree<T> {

    private static final TreeNode<Object> EMPTY = of(emptyList());

    @SuppressWarnings("unchecked")
    public static <T> TreeNode<T> empty() {
        return (TreeNode<T>) EMPTY;
    }

    public abstract ImmutableList<Tree<T>> nodes();

    public static <T> TreeNode<T> of(List<? extends Tree<T>> nodes) {
        return new AutoValue_TreeNode<>(ImmutableList.copyOf(nodes));
    }

    void forEach(TreePath path, BiConsumer<TreePath, T> action) {
        range(0, nodes().size()).forEach(i ->
                nodes().get(i).forEach(path.append(i), action));
    }
}
