package com.gitlab.lae.intellij.jump.tree;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.stream.Stream;

import static com.google.common.collect.Lists.partition;
import static java.util.stream.Collectors.toList;

public abstract class Tree<T> {
    Tree() {
    }

    public void forEach(BiConsumer<TreePath, T> action) {
        forEach(TreePath.empty(), action);
    }

    abstract void forEach(TreePath path, BiConsumer<TreePath, T> action);

    public static <T> TreeNode<T> of(Stream<T> items, int nodeSize) {
        return join(items.map(TreeLeaf::of).collect(toList()), nodeSize);
    }

    private static <T> TreeNode<T> join(List<Tree<T>> nodes, int degree) {
        if (nodes.size() <= degree) {
            return TreeNode.of(nodes);
        }
        List<Tree<T>> subtrees = partition(nodes, degree).stream()
                .map(list -> join(list, degree))
                .collect(toList());
        return join(subtrees, degree);
    }
}
