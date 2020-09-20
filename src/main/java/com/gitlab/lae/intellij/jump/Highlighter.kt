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

  private var entries = emptyList<Pair<String, Int>>()

  fun setTree(tree: Tree<String, EditorOffset>) {
    entries = tree.asSequence()
      .filter { (_, value) -> editor == value.editor }
      .map { (path, value) -> path.keys.joinToString("") to value.offset }
      .sortedWith(comparingInt { it.second })
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

    entries.forEach { (label, offset) ->
      val metrics = fontMetrics.getLineMetrics(label, g)
      val width = fontMetrics.stringWidth(label)
      val height = fontMetrics.height
      val point = editor.offsetToPoint2D(offset)
      val bgX = (point.x + contentComponent.x).ceilToInt()
      val bgY = (point.y + contentComponent.y +
        (editor.lineHeight - metrics.height) / 2.0).ceilToInt()

      g.color = markerBackground
      g.fillRoundRect(
        bgX,
        bgY,
        width,
        height,
        markerInnerRound,
        markerInnerRound
      )

      g.color = markerBorder
      g.drawRoundRect(
        bgX,
        bgY,
        width,
        height,
        markerBorderRound,
        markerBorderRound
      )

      g.color = markerForeground
      g.drawString(
        label,
        bgX,
        (point.y + contentComponent.y +
          metrics.ascent +
          metrics.leading +
          (editor.lineHeight - metrics.height) / 2).toInt()
      )
    }
  }
}

private fun Double.ceilToInt() = ceil(this).toInt()
