package com.gitlab.lae.intellij.jump.tree;

import junit.framework.TestCase;

import java.util.stream.Stream;

import static com.gitlab.lae.intellij.jump.tree.Tree.of;
import static java.util.Arrays.asList;
import static java.util.Arrays.stream;
import static java.util.stream.Collectors.toList;

public final class TreeTest extends TestCase {

    public void testToCompleteTree() {

        Tree<Integer> expected = node(
                node(
                        node(
                                leaves(0, 1),
                                leaves(2, 3)),
                        node(
                                leaves(4, 5),
                                leaves(6, 7))),
                node(
                        node(
                                leaves(8, 9))));

        Tree<Integer> actual =
                of(Stream.of(
                        0, 1, 2, 3, 4, 5, 6, 7, 8, 9), 2);

        assertEquals(expected, actual);
    }

    @SafeVarargs
    private final <T> TreeNode<T> node(Tree<T>... nodes) {
        return TreeNode.of(asList(nodes));
    }

    @SafeVarargs
    private final <T> TreeNode<T> leaves(T... nodes) {
        return TreeNode.of(stream(nodes)
                .map(TreeLeaf::of)
                .collect(toList()));
    }

}
