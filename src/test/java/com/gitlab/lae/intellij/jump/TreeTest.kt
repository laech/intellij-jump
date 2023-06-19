package com.gitlab.lae.intellij.jump

class TreeTest : junit.framework.TestCase() {

  fun `test to tree`() {
    val actual = treeOf(listOf(0, 1, 2, 3, 4, 5, 6, 7, 8, 9), 2)
    val expected =
        TreeNode(
            0 to
                TreeNode(
                    0 to
                        TreeNode(
                            0 to TreeNode(0 to TreeLeaf(0), 1 to TreeLeaf(1)),
                            1 to TreeNode(0 to TreeLeaf(2), 1 to TreeLeaf(3))),
                    1 to
                        TreeNode(
                            0 to TreeNode(0 to TreeLeaf(4), 1 to TreeLeaf(5)),
                            1 to TreeNode(0 to TreeLeaf(6), 1 to TreeLeaf(7)))),
            1 to TreeNode(listOf(0 to TreeLeaf(8), 1 to TreeLeaf(9))))
    assertEquals(expected, actual)
  }
}
