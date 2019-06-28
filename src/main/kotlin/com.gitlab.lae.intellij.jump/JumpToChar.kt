package com.gitlab.lae.intellij.jump

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.editor.EditorFactory
import com.intellij.openapi.project.DumbAware

class JumpToChar : AnAction(), DumbAware {

    override fun actionPerformed(e: AnActionEvent) {
        Model(EditorFactory.getInstance().allEditors
            .filter { it.contentComponent.isShowing })
            .attach()
    }

}
