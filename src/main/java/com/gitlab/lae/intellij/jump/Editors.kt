package com.gitlab.lae.intellij.jump

import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.IdeActions.ACTION_EDITOR_ESCAPE
import com.intellij.openapi.actionSystem.KeyboardShortcut
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.FoldRegion
import java.awt.Point
import java.awt.Rectangle
import java.util.*
import java.util.Comparator.comparingInt
import javax.swing.KeyStroke
import kotlin.math.absoluteValue

data class EditorOffset(val editor: Editor, val offset: Int)

fun searchVisibleOffsets(
  activeEditor: Editor?,
  editors: Collection<Editor>,
  query: String,
  ignoreCase: Boolean
): Sequence<EditorOffset> = editors
  .asSequence()
  .flatMap { searchVisibleOffsets(it, query, ignoreCase) }
  .sortedWith(comparingInt { if (it.editor == activeEditor) 0 else 1 })

private fun searchVisibleOffsets(
  editor: Editor, query: String, ignoreCase: Boolean
): Sequence<EditorOffset> {
  val area = editor.scrollingModel.visibleArea
  val start = getAreaStartOffset(editor, area)
  val end = getAreaEndOffset(editor, area)
  return searchOffsets(
    editor,
    query,
    ignoreCase,
    start,
    end
  )
}

fun searchOffsets(
  editor: Editor,
  query: String,
  ignoreCase: Boolean,
  start: Int,
  end: Int
): Sequence<EditorOffset> =
  OffsetIterator(editor, query, ignoreCase, start, end)
    .asSequence()
    .map { EditorOffset(editor, it) }
    .sortedWith(Comparator.comparing(::distanceFromCaret))

private fun distanceFromCaret(o: EditorOffset) =
  (o.offset - o.editor.caretModel.primaryCaret.offset).absoluteValue

private fun getFoldRegionByStartOffset(editor: Editor)
  : NavigableMap<Int, FoldRegion> =
  editor.foldingModel.allFoldRegions
    .filterNot(FoldRegion::isExpanded)
    .groupBy(FoldRegion::getStartOffset)
    .mapValuesTo(TreeMap()) { it.value.maxByOrNull(FoldRegion::getEndOffset) }

private fun getAreaStartOffset(editor: Editor, area: Rectangle): Int =
  editor.logicalPositionToOffset(
    editor.xyToLogicalPosition(Point(area.x, area.y))
  )

private fun getAreaEndOffset(editor: Editor, area: Rectangle): Int =
  editor.logicalPositionToOffset(
    editor.xyToLogicalPosition(
      Point(
        area.x + area.width,
        area.y + area.height
      )
    )
  )

fun getSingleStrokeEditorEscapeKeys(actionManager: ActionManager): Set<KeyStroke> =
  (actionManager.getActionOrStub(ACTION_EDITOR_ESCAPE)?.shortcutSet?.shortcuts
    ?: emptyArray())
    .asSequence()
    .filterIsInstance<KeyboardShortcut>()
    .filter { it.secondKeyStroke == null }
    .map { it.firstKeyStroke }
    .toSet()

private class OffsetIterator(
  editor: Editor,
  private val query: String,
  private val ignoreCase: Boolean,
  start: Int,
  end: Int
) : AbstractIterator<Int>() {

  private val regionsByStartOffset = getFoldRegionByStartOffset(editor)
  private val chars = editor.document.immutableCharSequence.subSequence(0, end)
  private var currentStart = start - 1

  override fun computeNext() {
    while (true) {
      currentStart = chars.indexOf(query, currentStart + 1, ignoreCase)
      if (currentStart < 0) {
        done()
        return
      }
      val entry = regionsByStartOffset.floorEntry(currentStart)
      if (entry == null || entry.value.endOffset <= currentStart) {
        setNext(currentStart)
        return
      }
    }
  }
}

