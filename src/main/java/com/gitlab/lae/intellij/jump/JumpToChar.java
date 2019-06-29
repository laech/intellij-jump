package com.gitlab.lae.intellij.jump;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.EditorFactory;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.ui.popup.JBPopup;
import com.intellij.openapi.ui.popup.JBPopupFactory;

import javax.swing.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.List;

import static com.gitlab.lae.intellij.jump.editor.Editors.searchVisibleOffsets;
import static java.util.Arrays.stream;
import static java.util.stream.Collectors.toList;
import static javax.swing.SwingUtilities.invokeLater;

public final class JumpToChar extends AnAction implements DumbAware {

    @Override
    public void actionPerformed(AnActionEvent e) {
        JTextField field = new JTextField();
        JBPopup popup = createInputPopup(field);
        field.addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent e) {
                field.removeKeyListener(this);
                // Jump at the end of the event queue, because there may be
                // other key type events coming in the queue still to fill the
                // text field, this is the case for using input methods to
                // enter international unicode characters, such as pinyin.
                invokeLater(() -> jump(field, popup));
            }
        });
        popup.showInBestPositionFor(e.getDataContext());
    }

    private JBPopup createInputPopup(JTextField field) {
        return JBPopupFactory.getInstance()
                .createComponentPopupBuilder(field, field)
                .setFocusable(true)
                .setRequestFocus(true)
                .createPopup();
    }

    private void jump(JTextField field, JBPopup popup) {
        String query = field.getText();
        popup.closeOk(null);
        if (query.isEmpty()) {
            return;
        }
        List<Editor> editors = stream(EditorFactory.getInstance().getAllEditors())
                .filter(editor -> editor.getContentComponent().isShowing())
                .collect(toList());
        new Jumper(editors, searchVisibleOffsets(editors, query, true)
                .collect(toList()))
                .attach();
    }
}
