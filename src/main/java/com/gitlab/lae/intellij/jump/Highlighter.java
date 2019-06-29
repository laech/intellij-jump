package com.gitlab.lae.intellij.jump;

import com.gitlab.lae.intellij.jump.editor.EditorOffset;
import com.gitlab.lae.intellij.jump.tree.TreeNode;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.colors.EditorFontType;

import javax.swing.*;
import java.awt.*;
import java.awt.font.LineMetrics;
import java.awt.geom.Rectangle2D;

import static com.gitlab.lae.intellij.jump.Jumper.markerChars;
import static java.awt.RenderingHints.KEY_ANTIALIASING;
import static java.awt.RenderingHints.VALUE_ANTIALIAS_ON;
import static java.util.Objects.requireNonNull;

final class Highlighter extends JComponent {

    private static final int markerArc = 6;
    private static final Color markerBackground = new Color(0xffde7a);
    private static final Color markerBorder = markerBackground.darker();

    private final Editor editor;

    private TreeNode<EditorOffset> tree = TreeNode.empty();

    Highlighter(Editor editor) {
        this.editor = requireNonNull(editor);
    }

    void setTree(TreeNode<EditorOffset> value) {
        tree = requireNonNull(value);
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D gg = (Graphics2D) g.create();
        try {
            paintMarkers(gg);
        } finally {
            gg.dispose();
        }
    }

    private void paintMarkers(Graphics2D g) {
        Font font = editor.getColorsScheme().getFont(EditorFontType.PLAIN);
        JComponent contentComponent = editor.getContentComponent();
        FontMetrics fontMetrics = contentComponent.getFontMetrics(font);
        g.setFont(font);
        g.setRenderingHint(KEY_ANTIALIASING, VALUE_ANTIALIAS_ON);
        int lineHeight = editor.getLineHeight();
        tree.forEach((path, editorOffset) -> {
            Editor editor = editorOffset.editor();
            if (this.editor != editor) {
                return;
            }
            int offset = editorOffset.offset();
            String label = path.values()
                    .mapToObj(markerChars::charAt)
                    .reduce(new StringBuilder(),
                            StringBuilder::append,
                            StringBuilder::append)
                    .toString();
            LineMetrics lineMetrics = fontMetrics.getLineMetrics(label, g);
            Rectangle2D fontRect = fontMetrics.getStringBounds(label, g);
            Point loc = editor.offsetToXY(offset);

            g.setColor(markerBackground);
            g.fillRoundRect(
                    loc.x + contentComponent.getX(),
                    loc.y + contentComponent.getY(),
                    (int) fontRect.getWidth(),
                    lineHeight,
                    markerArc,
                    markerArc);

            g.setColor(markerBorder);
            g.drawRoundRect(
                    loc.x + contentComponent.getX() - 1,
                    loc.y + contentComponent.getY(),
                    (int) (fontRect.getWidth() + 1),
                    lineHeight,
                    markerArc,
                    markerArc);

            g.setColor(Color.BLACK);
            g.drawString(
                    label,
                    loc.x + contentComponent.getX(),
                    (loc.y + contentComponent.getY() +
                            lineMetrics.getAscent() + lineMetrics.getLeading() +
                            (lineHeight - lineMetrics.getHeight()) / 2));
        });
    }
}
