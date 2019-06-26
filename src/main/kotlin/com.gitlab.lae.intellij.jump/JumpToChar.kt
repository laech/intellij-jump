package com.gitlab.lae.intellij.jump

import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.editor.Caret
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.actionSystem.EditorActionHandler
import com.intellij.openapi.editor.actions.TextComponentEditorAction

class JumpToChar
    : TextComponentEditorAction(object : EditorActionHandler(false) {

    override fun doExecute(editor: Editor, caret: Caret?, ctx: DataContext?) {
        addPanel(editor)
    }
})
