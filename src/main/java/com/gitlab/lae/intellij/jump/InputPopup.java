package com.gitlab.lae.intellij.jump;

import com.gitlab.lae.intellij.jump.editor.EditorOffset;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.EditorFactory;
import com.intellij.openapi.ui.popup.JBPopup;
import com.intellij.openapi.ui.popup.JBPopupFactory;

import javax.annotation.Nullable;
import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.List;

import static com.gitlab.lae.intellij.jump.editor.Editors.getSingleStrokeEditorEscapeKeys;
import static com.gitlab.lae.intellij.jump.editor.Editors.searchVisibleOffsets;
import static java.util.Arrays.stream;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;
import static javax.swing.BorderFactory.createEmptyBorder;
import static javax.swing.JComponent.WHEN_IN_FOCUSED_WINDOW;
import static javax.swing.SwingUtilities.invokeLater;

final class InputPopup extends KeyAdapter {

    private final ActionManager actionManager;
    private final JBPopup popup;
    private final JTextField field;
    private final Jumper jumper;

    @Nullable
    private final Editor activeEditor;

    InputPopup(
            Jumper jumper,
            ActionManager actionManager,
            @Nullable Editor activeEditor
    ) {
        this.jumper = requireNonNull(jumper);
        this.actionManager = requireNonNull(actionManager);
        this.field = createTextField(this);
        this.popup = createPopup(createContainer(field), field);
        this.activeEditor = activeEditor;
        registerCancel(actionManager, popup);
    }

    private static void registerCancel(
            ActionManager actionManager,
            JBPopup popup
    ) {
        getSingleStrokeEditorEscapeKeys(actionManager)
                .forEach(key -> popup.getContent().registerKeyboardAction(
                        __ -> popup.cancel(), key, WHEN_IN_FOCUSED_WINDOW));
    }

    private static JTextField createTextField(KeyListener listener) {
        JTextField field = new JTextField(1);
        field.setBorder(createEmptyBorder());
        field.addKeyListener(listener);
        return field;
    }

    private static JPanel createContainer(JTextField field) {
        JPanel container = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        container.setBorder(createEmptyBorder(2, 2, 1, 0));
        container.add(new JLabel("Jump to char: "));
        container.add(field);
        container.setBackground(field.getBackground());
        return container;
    }

    private static JBPopup createPopup(JPanel container, JTextField field) {
        return JBPopupFactory.getInstance()
                .createComponentPopupBuilder(container, field)
                .setFocusable(true)
                .setRequestFocus(true)
                .createPopup();
    }

    void show(DataContext context) {
        popup.showInBestPositionFor(context);
    }

    @Override
    public void keyTyped(KeyEvent e) {
        field.removeKeyListener(this);

        // We jump at the end of the event queue, because there may be
        // other key type events coming in the queue still to fill the
        // text field, this is the case for using input methods to
        // enter international unicode characters, such as pinyin.
        invokeLater(() -> {
            popup.setFinalRunnable(this::jump);
            popup.closeOk(e);
        });
    }

    private void jump() {
        String query = field.getText();
        if (query.isEmpty()) {
            return;
        }

        List<Editor> editors = stream(EditorFactory
                .getInstance()
                .getAllEditors())
                .filter(editor -> editor.getContentComponent().isShowing())
                .collect(toList());

        List<EditorOffset> offsets =
                searchVisibleOffsets(activeEditor, editors, query, true)
                        .collect(toList());

        jumper.attach(editors, offsets, actionManager);
    }
}
