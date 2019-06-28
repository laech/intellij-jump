package com.gitlab.lae.intellij.jump

import com.intellij.openapi.editor.Editor
import java.awt.event.KeyAdapter
import java.awt.event.KeyEvent
import java.awt.event.KeyEvent.VK_ESCAPE
import java.awt.event.KeyListener
import javax.swing.KeyStroke.getKeyStrokeForEvent
import javax.swing.SwingUtilities.getWindowAncestor

val markerChars = "fjdkslaowierqghpnvzxcmbtyu".toCharArray()
val markerIndices = markerChars.asSequence()
    .mapIndexed { i, c -> Pair(c, i) }
    .associate { it }

class Model(editors: Iterable<Editor>) {

    private var tree = Tree.Node(listOf<Tree<EditorOffset>>())
    private val editorEscapeKeyStrokes = getSingleStrokeEditorEscapeKeys()
    private val existingKeyListeners = hashMapOf<Editor, Array<KeyListener>>()
    private val editorHighlighters =
        editors.associateTo(mutableMapOf()) { editor ->
            Pair(editor, Highlighter(editor))
        }

    private val inputListener = object : KeyAdapter() {
        override fun keyTyped(e: KeyEvent) {
            super.keyTyped(e)
            tree = editors.asSequence()
                .searchVisibleOffsets(e.keyChar, true)
                .toCompleteTree(markerChars.size)
            editors
                .map(Editor::getContentComponent)
                .forEach {
                    it.removeKeyListener(this)
                    it.addKeyListener(jumpListener)
                }
            editorHighlighters.values.forEach { it.tree = tree }
        }
    }

    private val jumpListener = object : KeyAdapter() {
        override fun keyTyped(e: KeyEvent) {
            if (isCancel(e)) {
                detach()
                return
            }

            // TODO no jump if there is no option
            // TODO jump directly if there is one option

            val index = markerIndices[e.keyChar] ?: return
            when (val node = tree.nodes[index]) {
                is Tree.Node -> {
                    tree = node
                    editorHighlighters.values.forEach { it.tree = tree }
                }
                is Tree.Leaf -> {
                    val (editor, offset) = node.value
                    getWindowAncestor(editor.contentComponent)?.toFront()
                    editor.contentComponent.requestFocusInWindow()
                    editor.caretModel.moveToOffset(offset)
                    detach()
                }
            }
        }

        override fun keyPressed(e: KeyEvent) {
            if (isCancel(e)) {
                detach()
            }
        }

        override fun keyReleased(e: KeyEvent) {
            if (isCancel(e)) {
                detach()
            }
        }

        private fun isCancel(e: KeyEvent): Boolean {
            return e.keyCode == VK_ESCAPE ||
                    editorEscapeKeyStrokes.contains(getKeyStrokeForEvent(e))
        }
    }

    fun attach() {
        editorHighlighters.forEach { (editor, highlighter) ->
            val contentComponent = editor.contentComponent
            val listeners = contentComponent.keyListeners
            existingKeyListeners[editor] = listeners
            listeners.forEach(contentComponent::removeKeyListener)
            contentComponent.addKeyListener(inputListener)

            val area = editor.scrollingModel.visibleArea
            highlighter.setBounds(area.x, area.y, area.width, area.height)
            contentComponent.add(highlighter)
            contentComponent.repaint()
        }
    }


    private fun detach() {
        editorHighlighters.forEach { (editor, highlighter) ->
            val contentComponent = editor.contentComponent
            contentComponent.removeKeyListener(inputListener)
            contentComponent.removeKeyListener(jumpListener)
            contentComponent.remove(highlighter)
            existingKeyListeners[editor]!!.forEach(contentComponent::addKeyListener)
            contentComponent.repaint()
        }
        existingKeyListeners.clear()
        editorHighlighters.clear()
    }

}
