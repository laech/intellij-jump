package com.gitlab.lae.intellij.jump

data class TreePath<out K>(val keys: List<K>)

operator fun <K> TreePath<K>.plus(key: K) = TreePath(keys + key)

sealed class Tree<out K, out V>

data class TreeLeaf<out V>(val value: V) : Tree<Nothing, V>() {
  override fun toString() = "TreeLeaf($value)"
}

data class TreeNode<out K, out V>(val nodes: List<Pair<K, Tree<K, V>>>) : Tree<K, V>() {

  constructor(vararg nodes: Pair<K, Tree<K, V>>) : this(nodes.toList())

  override fun toString() = "TreeNode(\n  ${nodes.joinToString(",\n").replace("\n", "\n  ")}\n)"
}

val emptyPath = TreePath<Nothing>(emptyList())
val emptyTree = TreeNode<Nothing, Nothing>(emptyList())

fun <V> treeOf(items: Collection<V>, nodeSize: Int): Tree<Int, V> =
    join(items.map(::TreeLeaf).toList(), nodeSize)

private fun <V> join(nodes: Collection<Tree<Int, V>>, nodeSize: Int): Tree<Int, V> =
    when {
      nodeSize < 1 -> throw IllegalArgumentException("nodeSize=$nodeSize")
      nodes.size <= 1 -> nodes.firstOrNull() ?: emptyTree
      nodes.size <= nodeSize ->
          nodes.withIndex().map { (index, value) -> index to value }.let(::TreeNode)
      else ->
          nodes
              .asSequence()
              .chunked(nodeSize)
              .map { join(it, nodeSize) }
              .let { join(it.toList(), nodeSize) }
    }

fun <K, V, R> Tree<K, V>.mapKey(mapper: (K) -> R): Tree<R, V> =
    when (this) {
      is TreeLeaf -> this
      is TreeNode ->
          nodes.map { (key, value) -> mapper(key) to value.mapKey(mapper) }.let(::TreeNode)
    }

fun <K, V> Tree<K, V>.asSequence(path: TreePath<K> = emptyPath): Sequence<Pair<TreePath<K>, V>> =
    when (this) {
      is TreeLeaf -> sequenceOf(path to value)
      is TreeNode -> nodes.asSequence().flatMap { (key, value) -> value.asSequence(path + key) }
    }

operator fun <K, V> Tree<K, V>.get(key: K): Tree<K, V>? =
    when (this) {
      is TreeNode -> nodes.firstOrNull { (k) -> k == key }?.second
      else -> null
    }
