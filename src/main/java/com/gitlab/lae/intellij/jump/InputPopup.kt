package com.gitlab.lae.intellij.jump

import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.EditorFactory
import com.intellij.openapi.ui.popup.JBPopup
import com.intellij.openapi.ui.popup.JBPopupFactory
import java.awt.FlowLayout
import java.awt.FlowLayout.LEFT
import java.awt.event.KeyAdapter
import java.awt.event.KeyEvent
import java.awt.event.KeyListener
import javax.swing.BorderFactory
import javax.swing.JComponent.WHEN_IN_FOCUSED_WINDOW
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.JTextField
import javax.swing.SwingUtilities.invokeLater

class InputPopup(
  private val jumper: Jumper,
  private val actionManager: ActionManager,
  private val activeEditor: Editor?
) : KeyAdapter() {

  private val field = createTextField(this)
  private val popup = createPopup(createContainer(field), field)

  init {
    registerCancel(actionManager, popup)
  }

  private fun registerCancel(
    actionManager: ActionManager,
    popup: JBPopup
  ) {
    getSingleStrokeEditorEscapeKeys(actionManager).forEach {
      popup.content.registerKeyboardAction(
        { popup.cancel() }, it, WHEN_IN_FOCUSED_WINDOW
      )
    }
  }

  private fun createTextField(listener: KeyListener): JTextField {
    val field = JTextField(1)
    field.border = BorderFactory.createEmptyBorder()
    field.addKeyListener(listener)
    return field
  }

  private fun createContainer(field: JTextField): JPanel {
    val container = JPanel(FlowLayout(LEFT, 0, 0))
    container.border = BorderFactory.createEmptyBorder(2, 2, 1, 0)
    container.add(JLabel("Jump to char: "))
    container.add(field)
    container.background = field.background
    return container
  }

  private fun createPopup(container: JPanel, field: JTextField): JBPopup =
    JBPopupFactory.getInstance()
      .createComponentPopupBuilder(container, field)
      .setFocusable(true)
      .setRequestFocus(true)
      .createPopup()

  fun show(context: DataContext) {
    popup.showInBestPositionFor(context)
  }

  override fun keyTyped(e: KeyEvent) {
    field.removeKeyListener(this)

    // We jump at the end of the event queue, because there may be
    // other key type events coming in the queue still to fill the
    // text field, this is the case for using input methods to
    // enter international unicode characters, such as pinyin.
    invokeLater {
      popup.setFinalRunnable(::jump)
      popup.closeOk(e)
    }
  }

  private fun jump() {
    val query = field.text
    if (query.isEmpty()) {
      return
    }

    val editors = EditorFactory.getInstance().allEditors
      .filter { it.contentComponent.isShowing }

    val offsets =
      searchVisibleOffsets(activeEditor, editors, query, true).toList()

    jumper.attach(editors, offsets, actionManager)
  }
}
