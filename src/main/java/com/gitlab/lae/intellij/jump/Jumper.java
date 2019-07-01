package com.gitlab.lae.intellij.jump;

import com.gitlab.lae.intellij.jump.editor.EditorOffset;
import com.gitlab.lae.intellij.jump.tree.Tree;
import com.gitlab.lae.intellij.jump.tree.TreeLeaf;
import com.gitlab.lae.intellij.jump.tree.TreeNode;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.editor.Editor;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.List;
import java.util.*;

import static com.gitlab.lae.intellij.jump.editor.Editors.getSingleStrokeEditorEscapeKeys;
import static java.awt.event.KeyEvent.VK_ESCAPE;
import static java.util.stream.Collectors.toList;
import static javax.swing.KeyStroke.getKeyStrokeForEvent;
import static javax.swing.SwingUtilities.getWindowAncestor;

final class Jumper extends KeyAdapter {

    private static final List<String> markers = "fjdkslaowierqghpnvzxcmbtyu"
            .codePoints()
            .mapToObj(i -> new String(new int[]{i}, 0, 1))
            .collect(toList());

    private TreeNode<String, EditorOffset> tree = TreeNode.empty();
    private final Set<KeyStroke> editorEscapeKeyStrokes = new HashSet<>();
    private final Map<Editor, KeyListener[]> existingKeyListeners = new HashMap<>();
    private final Map<Editor, Highlighter> highlighters = new HashMap<>();

    @Override
    public void keyTyped(KeyEvent e) {
        if (isCancel(e)) {
            detach();
            return;
        }

        Tree<String, EditorOffset> node = tree.nodes().get(String.valueOf(e.getKeyChar()));
        if (node == null) {
            return;
        }
        if (node instanceof TreeNode<?, ?>) {
            tree = (TreeNode<String, EditorOffset>) node;
            highlighters.values().forEach(it -> it.setTree(tree));
        } else {
            jump(((TreeLeaf<?, EditorOffset>) node).value());
        }
    }

    private void jump(EditorOffset editorOffset) {
        Editor editor = editorOffset.editor();
        int offset = editorOffset.offset();
        Window window = getWindowAncestor(editor.getContentComponent());
        if (window != null) {
            window.toFront();
        }
        editor.getContentComponent().requestFocusInWindow();
        editor.getCaretModel().moveToOffset(offset);
        detach();
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (isCancel(e)) {
            detach();
        }
    }

    private boolean isCancel(KeyEvent e) {
        return e.getKeyCode() == VK_ESCAPE ||
                editorEscapeKeyStrokes.contains(
                        getKeyStrokeForEvent(e));
    }

    void attach(
            Collection<Editor> editors,
            Collection<EditorOffset> offsets,
            ActionManager actionManager
    ) {
        detach();

        if (editors.isEmpty() || offsets.isEmpty()) {
            return;
        }

        if (offsets.size() == 1) {
            jump(offsets.iterator().next());
            return;
        }

        // offsets may not contain all editors in the editors collection,
        // we want to put highlighters on all editors for listening key inputs
        Tree<Integer, EditorOffset> t = Tree.of(offsets.stream(), markers.size());
        if (t instanceof TreeLeaf<?, ?>) {
            jump(((TreeLeaf<Integer, EditorOffset>) t).value());
            return;
        }

        tree = (TreeNode<String, EditorOffset>) t.mapKey(markers::get);

        for (Editor editor : editors) {
            Highlighter highlighter = new Highlighter(editor);
            highlighter.setTree(tree);
            highlighters.put(editor, highlighter);

            JComponent contentComponent = editor.getContentComponent();
            KeyListener[] listeners = contentComponent.getKeyListeners();
            existingKeyListeners.put(editor, listeners);
            for (KeyListener listener : listeners) {
                contentComponent.removeKeyListener(listener);
            }
            contentComponent.addKeyListener(this);

            Rectangle area = editor.getScrollingModel().getVisibleArea();
            highlighter.setBounds(area.x, area.y, area.width, area.height);
            contentComponent.add(highlighter);
            contentComponent.repaint();
        }
        editorEscapeKeyStrokes.addAll(
                getSingleStrokeEditorEscapeKeys(actionManager));
    }

    private void detach() {
        highlighters.forEach((editor, highlighter) -> {
            JComponent contentComponent = editor.getContentComponent();
            contentComponent.removeKeyListener(this);
            contentComponent.remove(highlighter);
            for (KeyListener listener : existingKeyListeners.get(editor)) {
                contentComponent.addKeyListener(listener);
            }
            contentComponent.repaint();
        });
        highlighters.clear();
        existingKeyListeners.clear();
        editorEscapeKeyStrokes.clear();
        tree = TreeNode.empty();
    }

}
