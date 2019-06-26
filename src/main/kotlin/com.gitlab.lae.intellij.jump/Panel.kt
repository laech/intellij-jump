package com.gitlab.lae.intellij.jump

import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.IdeActions.ACTION_EDITOR_ESCAPE
import com.intellij.openapi.actionSystem.KeyboardShortcut
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.colors.EditorFontType
import java.awt.Color
import java.awt.Graphics
import java.awt.Graphics2D
import java.awt.RenderingHints
import java.awt.event.KeyEvent
import java.awt.event.KeyEvent.VK_ESCAPE
import java.awt.event.KeyListener
import javax.swing.JComponent
import javax.swing.KeyStroke
import javax.swing.KeyStroke.getKeyStrokeForEvent
import kotlin.math.roundToInt

fun addPanel(editor: Editor): Panel {
    val area = editor.scrollingModel.visibleArea
    val panel = Panel(editor)
    panel.setBounds(area.x, area.y, area.width, area.height)
    panel.attach()
    return panel
}

private const val markerArc = 6
private val markerBackground = Color(0xffde7a)
private val markerBorder = markerBackground.darker()

class Panel(private val editor: Editor) : JComponent(), KeyListener {

    private val editorEscapeKeyStrokes = getEditorEscapeKeyStrokes()
    private val contentComponent = editor.contentComponent
    private var existingKeyListeners = emptyArray<KeyListener>()
    private var attached = false
    private var listening = true
    private var tree = Tree.Node<Int>(emptyList())

    fun attach() {
        if (attached) {
            return
        }
        existingKeyListeners = contentComponent.keyListeners
        existingKeyListeners.forEach(contentComponent::removeKeyListener)
        contentComponent.addKeyListener(this)
        contentComponent.add(this)
        contentComponent.repaint()
        attached = true
        listening = true
    }

    private fun detach() {
        if (!attached) {
            return
        }
        contentComponent.removeKeyListener(this)
        existingKeyListeners.forEach(contentComponent::addKeyListener)
        contentComponent.remove(this)
        contentComponent.repaint()
        attached = true
        listening = false
    }

    private fun getEditorEscapeKeyStrokes(): Set<KeyStroke> {
        val shortcuts = ActionManager.getInstance()
            .getActionOrStub(ACTION_EDITOR_ESCAPE)
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

    override fun keyTyped(e: KeyEvent) {
        println(e)
        if (e.keyCode == VK_ESCAPE || editorEscapeKeyStrokes.contains(
                getKeyStrokeForEvent(e)
            )
        ) {
            detach()
            return
        }
        if (listening) {
            listening = false
            tree = editor
                .searchVisibleOffsets(e.keyChar, true)
                .toCompleteTree(26)
            repaint()
            return
        }

        // TODO no jump if there is no option
        // TODO jump directly if there is one option

        if (e.keyChar in 'a'..'z') {
            // TODO check index
            val node = tree.nodes[e.keyChar - 'a']
            when (node) {
                is Tree.Node -> {
                    tree = node
                    repaint()
                }
                is Tree.Leaf -> {
                    editor.caretModel.moveToOffset(node.value)
                    detach()
                }
            }
        }
    }

    override fun keyPressed(e: KeyEvent) {
        println(e)

        if (e.keyCode == VK_ESCAPE || editorEscapeKeyStrokes.contains(
                getKeyStrokeForEvent(e)
            )
        ) {
            detach()
        }
    }

    override fun keyReleased(e: KeyEvent) {
        println(e)
        if (e.keyCode == VK_ESCAPE || editorEscapeKeyStrokes.contains(
                getKeyStrokeForEvent(e)
            )
        ) {
            detach()
        }
    }

    override fun paintComponent(g: Graphics) {
        val gg = g.create()
        try {
            paintMarkers(gg)
        } finally {
            gg.dispose()
        }
    }

    private fun paintMarkers(g: Graphics) {
        val font = editor.colorsScheme.getFont(EditorFontType.PLAIN)
        val fontMetrics = contentComponent.getFontMetrics(font)
        g.font = font
        (g as Graphics2D).setRenderingHint(
            RenderingHints.KEY_ANTIALIASING,
            RenderingHints.VALUE_ANTIALIAS_ON
        )
        val lineHeight = editor.lineHeight
        tree.forEach { path, offset ->

            val label = path.map { 'a' + it }.joinToString("")
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
                (loc.y + contentComponent.y + lineMetrics.ascent + lineMetrics.leading + (lineHeight - lineMetrics.height) / 2).roundToInt()
            )

        }
    }
}