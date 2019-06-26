package com.gitlab.lae.intellij.jump

sealed class Tree<T> {
    data class Leaf<T>(val value: T) : Tree<T>()
    data class Node<T>(val nodes: List<Tree<T>>) : Tree<T>()
}

private fun <T> Tree<T>.depth(): Int = when (this) {
    is Tree.Leaf -> 0
    is Tree.Node ->
        if (nodes.isEmpty()) 0
        else nodes.first().depth() + 1
}

fun <T> Tree<T>.forEachPath(action: (IntArray, Int, T) -> Unit) =
    forEachPath(IntArray(depth()), 0, action)

private fun <T> Tree<T>.forEachPath(
    path: IntArray,
    length: Int,
    action: (IntArray, Int, T) -> Unit
): Unit = when (this) {
    is Tree.Leaf -> action(path, length, value)
    is Tree.Node -> nodes.indices.forEach { i ->
        path[length] = i
        nodes[i].forEachPath(path, length + 1, action)
    }
}

fun <T> Sequence<T>.toCompleteTree(degree: Int): Tree.Node<T> =
    map { Tree.Leaf(it) }.toList().join(degree)

private fun <T> List<Tree<T>>.join(degree: Int): Tree.Node<T> = when {
    size <= degree -> Tree.Node(this)
    else -> chunked(degree).map { it.join(degree) }.join(degree)
}
