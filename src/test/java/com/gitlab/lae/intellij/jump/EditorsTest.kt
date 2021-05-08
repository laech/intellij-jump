package com.gitlab.lae.intellij.jump

import com.intellij.openapi.fileTypes.FileTypes
import com.intellij.testFramework.fixtures.BasePlatformTestCase

class EditorsTest : BasePlatformTestCase() {

  fun `test search offsets returns offsets matching query`() {
    myFixture.configureByText(FileTypes.PLAIN_TEXT, "HelloWorld")
    val editor = myFixture.editor
    assertOrderedEquals(
      searchOffsets(editor, "o", true, 0, 10).toList(),
      EditorOffset(editor, 4),
      EditorOffset(editor, 6)
    )
  }

  fun `test search offsets skip fold regions`() {
    myFixture.configureByText(
      FileTypes.PLAIN_TEXT, """
     HelloWorld
     HelloWorld
     HelloWorld
     HelloWorld
     """.trimIndent()
    )
    val editor = myFixture.editor
    val foldingModel = editor.foldingModel
    foldingModel.runBatchFoldingOperation {
      val r1 = foldingModel.addFoldRegion(0, 20, "...")
      val r2 = foldingModel.addFoldRegion(30, 40, "...")
      assertNotNull(r1)
      assertNotNull(r2)
      r1!!.isExpanded = false
      r2!!.isExpanded = false
    }
    assertOrderedEquals(
      searchOffsets(editor, "o", true, 0, 40).toList(),
      EditorOffset(editor, 26),
      EditorOffset(editor, 28)
    )
  }

  fun `test search offsets sorts by distance from caret`() {
    val text = "ABAB          A"
    myFixture.configureByText(FileTypes.PLAIN_TEXT, text)
    val editor = myFixture.editor
    editor.caretModel.primaryCaret.moveToOffset(4)
    assertOrderedEquals(
      searchOffsets(editor, "a", true, 0, text.length).toList(),
      EditorOffset(editor, 2),
      EditorOffset(editor, 0),
      EditorOffset(editor, 14)
    )
  }
}
