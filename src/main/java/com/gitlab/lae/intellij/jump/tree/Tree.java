package com.gitlab.lae.intellij.jump.tree;

import com.google.common.collect.ImmutableMap;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static com.google.common.collect.Lists.partition;
import static java.util.stream.Collectors.toList;

public abstract class Tree<K, V> {
    Tree() {
    }

    public void forEach(BiConsumer<TreePath<K>, V> action) {
        forEach(TreePath.empty(), action);
    }

    public abstract <R> Tree<R, V> mapKey(Function<? super K, ? extends R> mapper);

    abstract void forEach(TreePath<K> path, BiConsumer<TreePath<K>, V> action);

    public static <V> Tree<Integer, V> of(Stream<V> items, int nodeSize) {
        return join(items
                .map(TreeLeaf::<Integer, V>of)
                .collect(toList()), nodeSize);
    }

    private static <V> Tree<Integer, V> join(
            List<? extends Tree<Integer, V>> nodes,
            int nodeSize
    ) {
        if (nodeSize < 1) {
            throw new IllegalArgumentException("nodeSize=" + nodeSize);
        }
        if (nodes.isEmpty()) {
            return TreeNode.empty();
        }
        if (nodes.size() == 1) {
            return nodes.get(0);
        }
        if (nodes.size() <= nodeSize) {
            return TreeNode.of(IntStream
                    .range(0, nodes.size())
                    .collect(ImmutableMap::<Integer, Tree<Integer, V>>builder,
                            (result, i) -> result.put(i, nodes.get(i)),
                            (a, b) -> a.putAll(b.build()))
                    .build());
        }
        List<Tree<Integer, V>> subtrees = partition(nodes, nodeSize)
                .stream()
                .map(list -> join(list, nodeSize))
                .collect(toList());
        return join(subtrees, nodeSize);
    }
}
