package com.gitlab.lae.intellij.jump

import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.IdeActions
import com.intellij.openapi.actionSystem.KeyboardShortcut
import com.intellij.openapi.editor.Editor
import java.awt.Point
import java.awt.Rectangle
import javax.swing.KeyStroke

data class EditorOffset(val editor: Editor, val offset: Int)

fun Sequence<Editor>.searchVisibleOffsets(
    query: Char,
    ignoreCase: Boolean
): Sequence<EditorOffset> =
    flatMap { editor ->
        editor.searchVisibleOffsets(query, ignoreCase)
            .map { offset -> EditorOffset(editor, offset) }
    }

fun Editor.searchVisibleOffsets(
    query: Char,
    ignoreCase: Boolean
): Sequence<Int> {
    // TODO take folding into account
    val area = scrollingModel.visibleArea
    val start = getAreaStartOffset(area)
    val end = getAreaEndOffset(area)
    return searchOffsets(query, ignoreCase, start, end)
}

fun Editor.searchOffsets(
    query: Char,
    ignoreCase: Boolean,
    start: Int,
    end: Int
): Sequence<Int> = object : AbstractIterator<Int>() {

    var i = start - 1
    val chars = document
        .immutableCharSequence
        .subSequence(0, end)

    override fun computeNext() {
        i = chars.indexOf(query, i + 1, ignoreCase)
        if (i > -1) {
            setNext(i)
        } else {
            done()
        }
    }
}.asSequence()

private fun Editor.getAreaEndOffset(area: Rectangle) =
    logicalPositionToOffset(
        xyToLogicalPosition(
            Point(
                area.x + area.width,
                area.y + area.height
            )
        )
    )

private fun Editor.getAreaStartOffset(area: Rectangle) =
    logicalPositionToOffset(
        xyToLogicalPosition(
            Point(
                area.x,
                area.y
            )
        )
    )

fun getSingleStrokeEditorEscapeKeys(
    actionManager: ActionManager = ActionManager.getInstance()
): Set<KeyStroke> {
    val shortcuts = actionManager
        .getActionOrStub(IdeActions.ACTION_EDITOR_ESCAPE)
        ?.shortcutSet
        ?.shortcuts
        ?: return emptySet()
    return shortcuts
        .asSequence()
        .filterIsInstance<KeyboardShortcut>()
        .filter { it.secondKeyStroke == null }
        .map { it.firstKeyStroke }
        .toSet()
}
