package com.gitlab.lae.intellij.jump;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.DumbAware;

public final class JumpToChar extends AnAction implements DumbAware {

    private final Jumper jumper = new Jumper();

    @Override
    public void actionPerformed(AnActionEvent e) {
        new InputPopup(jumper, e.getActionManager())
                .show(e.getDataContext());
    }
}
