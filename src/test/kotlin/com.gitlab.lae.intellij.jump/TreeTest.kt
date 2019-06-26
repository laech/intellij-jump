package com.gitlab.lae.intellij.jump

import junit.framework.TestCase

class TreeTest : TestCase() {

    fun `test toCompleteTree`() {

        val expected: Tree<Int> = node(
                node(
                        node(
                                leaves(0, 1),
                                leaves(2, 3)),
                        node(
                                leaves(4, 5),
                                leaves(6, 7))),
                node(
                        node(
                                leaves(8, 9))))

        val actual: Tree<Int> =
                sequenceOf(0, 1, 2, 3, 4, 5, 6, 7, 8, 9)
                        .toCompleteTree(2)

        assertEquals(expected, actual)
    }

    private fun <T> node(vararg nodes: Tree<T>) =
            Tree.Node(nodes.toList())

    private fun <T> leaves(vararg nodes: T) =
            Tree.Node(nodes.map { Tree.Leaf(it) })

}
