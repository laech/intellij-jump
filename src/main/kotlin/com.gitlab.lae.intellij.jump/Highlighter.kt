package com.gitlab.lae.intellij.jump

import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.colors.EditorFontType
import java.awt.Color
import java.awt.Graphics
import java.awt.Graphics2D
import java.awt.RenderingHints.KEY_ANTIALIASING
import java.awt.RenderingHints.VALUE_ANTIALIAS_ON
import javax.swing.JComponent
import kotlin.math.roundToInt

private const val markerArc = 6
private val markerBackground = Color(0xffde7a)
private val markerBorder = markerBackground.darker()

class Highlighter(private val editor: Editor) : JComponent() {

    var tree = Tree.Node<EditorOffset>(emptyList())
        set(value) {
            field = value
            repaint()
        }

    override fun paintComponent(g: Graphics) {
        val gg = g.create()
        try {
            paintMarkers(gg as Graphics2D)
        } finally {
            gg.dispose()
        }
    }

    private fun paintMarkers(g: Graphics2D) {
        val font = editor.colorsScheme.getFont(EditorFontType.PLAIN)
        val contentComponent = editor.contentComponent
        val fontMetrics = contentComponent.getFontMetrics(font)
        g.font = font
        g.setRenderingHint(KEY_ANTIALIASING, VALUE_ANTIALIAS_ON)
        val lineHeight = editor.lineHeight
        tree.forEach { path, (editor, offset) ->
            if (this.editor != editor) {
                return@forEach
            }

            val label = path.map(markerChars::get).joinToString("")
            val lineMetrics = fontMetrics.getLineMetrics(label, g)
            val fontRect = fontMetrics.getStringBounds(label, g)
            val loc = editor.offsetToXY(offset)

            g.color = markerBackground
            g.fillRoundRect(
                loc.x + contentComponent.x,
                loc.y + contentComponent.y,
                fontRect.width.roundToInt(),
                lineHeight,
                markerArc,
                markerArc
            )
            g.color = markerBorder
            g.drawRoundRect(
                loc.x + contentComponent.x - 1,
                loc.y + contentComponent.y,
                fontRect.width.roundToInt() + 1,
                lineHeight,
                markerArc,
                markerArc
            )

            g.color = Color.BLACK
            g.drawString(
                label,
                loc.x + contentComponent.x,
                (loc.y + contentComponent.y +
                        lineMetrics.ascent + lineMetrics.leading +
                        (lineHeight - lineMetrics.height) / 2).roundToInt()
            )

        }
    }
}
