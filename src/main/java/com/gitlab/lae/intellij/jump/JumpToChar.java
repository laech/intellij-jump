package com.gitlab.lae.intellij.jump;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.editor.EditorFactory;
import com.intellij.openapi.project.DumbAware;

import static java.util.Arrays.stream;
import static java.util.stream.Collectors.toList;

public final class JumpToChar extends AnAction implements DumbAware {
    @Override
    public void actionPerformed(AnActionEvent e) {
        new Model(stream(EditorFactory.getInstance().getAllEditors())
                .filter(editor -> editor.getContentComponent().isShowing())
                .collect(toList())).attach();
    }
}
