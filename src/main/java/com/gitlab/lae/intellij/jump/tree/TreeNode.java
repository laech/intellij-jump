package com.gitlab.lae.intellij.jump.tree;

import com.google.auto.value.AutoValue;
import com.google.common.collect.ImmutableMap;

import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;

import static java.util.Collections.emptyMap;
import static java.util.stream.Collectors.joining;

@AutoValue
public abstract class TreeNode<K, V> extends Tree<K, V> {

    private static final TreeNode<Object, Object> EMPTY = of(emptyMap());

    @SuppressWarnings("unchecked")
    public static <K, V> TreeNode<K, V> empty() {
        return (TreeNode<K, V>) EMPTY;
    }

    public abstract ImmutableMap<K, Tree<K, V>> nodes();

    public static <K, V> TreeNode<K, V> of(Map<K, ? extends Tree<K, V>> nodes) {
        return new AutoValue_TreeNode<>(ImmutableMap.copyOf(nodes));
    }

    @Override
    public <R> TreeNode<R, V> mapKey(Function<? super K, ? extends R> mapper) {
        return of(nodes().entrySet().stream()
                .map(e -> new SimpleImmutableEntry<>(
                        mapper.apply(e.getKey()),
                        e.getValue().<R>mapKey(mapper)))
                .reduce(ImmutableMap.<R, Tree<R, V>>builder(),
                        ImmutableMap.Builder::put,
                        (a, b) -> a.putAll(b.build()))
                .build());
    }

    void forEach(TreePath<K> path, BiConsumer<TreePath<K>, V> action) {
        nodes().forEach((k, v) -> v.forEach(path.append(k), action));
    }

    @Override
    public String toString() {
        return "TreeNode{\n  " +
                nodes().entrySet().stream()
                        .map(Object::toString)
                        .collect(joining("\n"))
                        .replace("\n", "\n  ") +
                "\n}";
    }
}
