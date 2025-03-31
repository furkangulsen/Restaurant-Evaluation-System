package com.example.demo.cli.shell;

import org.jline.utils.AttributedStyle;

/**
 * Terminal renkleri için enum.
 * JLine AttributedStyle ile uyumlu renk kodlarını tanımlar.
 */
public enum PromptColor {
    BLACK(AttributedStyle.BLACK),
    RED(AttributedStyle.RED),
    GREEN(AttributedStyle.GREEN),
    YELLOW(AttributedStyle.YELLOW),
    BLUE(AttributedStyle.BLUE),
    MAGENTA(AttributedStyle.MAGENTA),
    CYAN(AttributedStyle.CYAN),
    WHITE(AttributedStyle.WHITE),
    BRIGHT(AttributedStyle.BRIGHT);

    private final int value;

    PromptColor(int value) {
        this.value = value;
    }

    public int toJLineAttributedStyle() {
        return this.value;
    }
}
