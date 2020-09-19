package com.gitlab.lae.intellij.jump

import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.colors.EditorFontType.PLAIN
import java.awt.Color
import java.awt.Color.BLACK
import java.awt.Graphics
import java.awt.Graphics2D
import java.awt.RenderingHints.KEY_ANTIALIASING
import java.awt.RenderingHints.VALUE_ANTIALIAS_ON
import java.util.Comparator.comparingInt
import javax.swing.JComponent
import kotlin.math.ceil

private const val markerBorderRound = 6
private const val markerInnerRound = 4
private val markerForeground = BLACK
private val markerBackground = Color(0xffe9a2)
private val markerBorder = markerBackground.darker()

class Highlighter(private val editor: Editor) : JComponent() {

  private var entries = emptyList<Pair<String, EditorOffset>>()

  fun setTree(tree: Tree<String, EditorOffset>) {
    entries = tree.asSequence()
      .filter { (_, editorOffset) -> editor == editorOffset.editor }
      .map { (path, editorOffset) -> path.keys.joinToString("") to editorOffset }
      .sortedWith(comparingInt { (_, editorOffset) -> editorOffset.offset })
      .toList()

    // Draw from the smallest offset, so that if there are
    // overlaps, the head of the next offset will be drawn
    // on top, then the user knows which key to press for that.
    repaint()
  }

  override fun paintComponent(g: Graphics) {
    val gg = g.create() as Graphics2D
    try {
      paintMarkers(gg)
    } finally {
      gg.dispose()
    }
  }

  private fun paintMarkers(g: Graphics2D) {
    val font = editor.colorsScheme.getFont(PLAIN)
    val contentComponent = editor.contentComponent
    val fontMetrics = contentComponent.getFontMetrics(font)
    g.font = font
    g.setRenderingHint(KEY_ANTIALIASING, VALUE_ANTIALIAS_ON)

    entries.forEach { (label, value) ->
      val lineMetrics = fontMetrics.getLineMetrics(label, g)
      val fontWidth = fontMetrics.stringWidth(label)
      val fontHeight = fontMetrics.height
      val loc = value.editor.offsetToXY(value.offset)
      val bgX = loc.x + contentComponent.x
      val bgY = loc.y + contentComponent.y +
        ((editor.lineHeight - lineMetrics.height) / 2.0).ceilToInt()

      g.color = markerBackground
      g.fillRoundRect(
        bgX,
        bgY,
        fontWidth,
        fontHeight,
        markerInnerRound,
        markerInnerRound
      )

      g.color = markerBorder
      g.drawRoundRect(
        bgX,
        bgY,
        fontWidth,
        fontHeight,
        markerBorderRound,
        markerBorderRound
      )

      g.color = markerForeground
      g.drawString(
        label,
        bgX,
        loc.y + contentComponent.y +
          (lineMetrics.ascent +
            lineMetrics.leading +
            (editor.lineHeight - lineMetrics.height) / 2.0).ceilToInt()
      )
    }
  }
}

private fun Double.ceilToInt() = ceil(this).toInt()
