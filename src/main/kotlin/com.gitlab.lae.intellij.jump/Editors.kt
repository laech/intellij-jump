package com.gitlab.lae.intellij.jump

import com.intellij.openapi.editor.Editor
import java.awt.Point
import java.awt.Rectangle

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
