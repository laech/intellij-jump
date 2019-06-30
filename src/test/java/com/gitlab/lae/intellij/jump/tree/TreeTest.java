package com.gitlab.lae.intellij.jump.tree;

import com.google.common.collect.ImmutableMap;
import junit.framework.TestCase;

import java.util.stream.Stream;

public final class TreeTest extends TestCase {

    public void testToTree() {

        Tree<Integer, Integer> actual = TreeNode.of(
                Stream.of(0, 1, 2, 3, 4, 5, 6, 7, 8, 9),
                2);

        Tree<Integer, Integer> expected = TreeNode.of(ImmutableMap.of(
                0, TreeNode.of(ImmutableMap.of(
                        0, TreeNode.of(ImmutableMap.of(
                                0, TreeNode.of(ImmutableMap.of(
                                        0, TreeLeaf.of(0),
                                        1, TreeLeaf.of(1)
                                )),
                                1, TreeNode.of(ImmutableMap.of(
                                        0, TreeLeaf.of(2),
                                        1, TreeLeaf.of(3)
                                ))
                        )),
                        1, TreeNode.of(ImmutableMap.of(
                                0, TreeNode.of(ImmutableMap.of(
                                        0, TreeLeaf.of(4),
                                        1, TreeLeaf.of(5)
                                )),
                                1, TreeNode.of(ImmutableMap.of(
                                        0, TreeLeaf.of(6),
                                        1, TreeLeaf.of(7)
                                ))
                        ))
                )),
                1, TreeNode.of(ImmutableMap.of(
                        0, TreeLeaf.of(8),
                        1, TreeLeaf.of(9)
                ))
        ));

        assertEquals(expected, actual);
    }

}
