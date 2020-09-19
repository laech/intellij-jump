package com.gitlab.lae.intellij.jump

import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.editor.Editor
import java.awt.event.KeyAdapter
import java.awt.event.KeyEvent
import java.awt.event.KeyEvent.VK_ESCAPE
import java.awt.event.KeyListener
import java.util.*
import javax.swing.KeyStroke
import javax.swing.KeyStroke.getKeyStrokeForEvent
import javax.swing.SwingUtilities.getWindowAncestor

private val markers = "fjdkslaowierqghpnvzxcmbtyu".map(Char::toString)

class Jumper : KeyAdapter() {

  private var tree = emptyTree as Tree<String, EditorOffset>
  private val editorEscapeKeyStrokes = HashSet<KeyStroke>()
  private val existingKeyListeners = HashMap<Editor, Array<KeyListener>>()
  private val highlighters = HashMap<Editor, Highlighter>()

  override fun keyTyped(e: KeyEvent) {
    if (isCancel(e)) {
      detach()
      return
    }

    val node = tree[e.keyChar.toString()] ?: return
    when (node) {
      is TreeLeaf -> jump(node.value)
      is TreeNode -> {
        tree = node
        highlighters.values.forEach { it.setTree(tree) }
      }
    }
  }

  private fun jump(editorOffset: EditorOffset) {
    val editor = editorOffset.editor
    val offset = editorOffset.offset
    getWindowAncestor(editor.contentComponent)?.toFront()
    editor.contentComponent.requestFocusInWindow()
    editor.caretModel.moveToOffset(offset)
    detach()
  }

  override fun keyPressed(e: KeyEvent) {
    if (isCancel(e)) {
      detach()
    }
  }

  private fun isCancel(e: KeyEvent) =
    e.keyCode == VK_ESCAPE ||
      editorEscapeKeyStrokes.contains(getKeyStrokeForEvent(e))

  fun attach(
    editors: Collection<Editor>,
    offsets: Collection<EditorOffset>,
    actionManager: ActionManager
  ) {
    detach()

    if (editors.isEmpty() || offsets.isEmpty()) {
      return
    }

    if (offsets.size == 1) {
      jump(offsets.iterator().next())
      return
    }

    // offsets may not contain all editors in the editors collection,
    // we want to put highlighters on all editors for listening key inputs
    tree = treeOf(offsets, markers.size).mapKey { markers[it] }
    for (editor in editors) {
      val highlighter = Highlighter(editor)
      highlighter.setTree(tree)
      highlighters[editor] = highlighter

      val contentComponent = editor.contentComponent
      val listeners = contentComponent.keyListeners
      existingKeyListeners[editor] = listeners
      listeners.forEach(contentComponent::removeKeyListener)
      contentComponent.addKeyListener(this)

      val area = editor.scrollingModel.visibleArea
      highlighter.setBounds(area.x, area.y, area.width, area.height)
      contentComponent.add(highlighter)
      contentComponent.repaint()
    }
    editorEscapeKeyStrokes.addAll(
      getSingleStrokeEditorEscapeKeys(actionManager)
    )
  }

  private fun detach() {
    highlighters.forEach { (editor, highlighter) ->
      val contentComponent = editor.contentComponent
      contentComponent.removeKeyListener(this)
      contentComponent.remove(highlighter)
      existingKeyListeners[editor]?.forEach(contentComponent::addKeyListener)
      contentComponent.repaint()
    }
    highlighters.clear()
    existingKeyListeners.clear()
    editorEscapeKeyStrokes.clear()
    tree = emptyTree
  }
}
