package com.gitlab.lae.intellij.jump

sealed class Tree<T> {
    data class Leaf<T>(val value: T) : Tree<T>()
    data class Node<T>(val nodes: List<Tree<T>>) : Tree<T>()
}

typealias TreePath = Sequence<Int>

fun <T> Tree<T>.forEach(action: (TreePath, T) -> Unit): Unit =
    forEach(sequenceOf(), action)

private fun <T> Tree<T>.forEach(
    path: TreePath,
    action: (TreePath, T) -> Unit
): Unit =
    when (this) {
        is Tree.Leaf -> action(path, value)
        is Tree.Node -> nodes.forEachIndexed { i, value ->
            value.forEach(path + i, action)
        }
    }

fun <T> Sequence<T>.toCompleteTree(degree: Int): Tree.Node<T> =
    map { Tree.Leaf(it) }.toList().join(degree)

private fun <T> List<Tree<T>>.join(degree: Int): Tree.Node<T> = when {
    size <= degree -> Tree.Node(this)
    else -> chunked(degree).map { it.join(degree) }.join(degree)
}
