package com.gitlab.lae.intellij.jump

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys.EDITOR
import com.intellij.openapi.project.DumbAware

class JumpToChar : AnAction(), DumbAware {

  private val jumper = Jumper()

  override fun actionPerformed(e: AnActionEvent) {
    InputPopup(jumper, e.actionManager, e.getData(EDITOR)).show(e.dataContext)
  }
}
