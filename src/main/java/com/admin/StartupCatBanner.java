package com.admin;

import org.springframework.boot.ansi.AnsiColor;
import org.springframework.boot.ansi.AnsiOutput;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;

/**
 * 应用完全就绪后再打印小猫，避免 main 返回后仍有日志插在小猫下面。
 */
@Component
public class StartupCatBanner implements ApplicationListener<ApplicationReadyEvent>, Ordered {

    private static final String MESSAGE = "♡ 木瓜系统启动成功 ♡";
    /** 文案显示列：心(1)+空格(1)+中文8×2+空格(1)+心(1) = 20，左右各留 2 列 */
    private static final int INNER_WIDTH = 24;
    private static final int BLOCK_WIDTH = INNER_WIDTH + 2;

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        printCat();
    }

    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE;
    }

    private static void printCat() {
        AnsiOutput.setEnabled(AnsiOutput.Enabled.ALWAYS);
        String green = AnsiOutput.encode(AnsiColor.BRIGHT_GREEN);
        String reset = AnsiOutput.encode(AnsiColor.DEFAULT);

        String top = "+" + "-".repeat(INNER_WIDTH) + "+";
        String mid = "|" + padCenter(MESSAGE, INNER_WIDTH) + "|";
        String bottom = "+" + "-".repeat(INNER_WIDTH) + "+";

        String[] cat = {
                "∧,,,∧",
                "(  ≧ · ω · ≦ )♡",
        };

        System.out.println();
        for (String line : cat) {
            printlnGreen(green, reset, padCenter(line, BLOCK_WIDTH));
        }
        printlnGreen(green, reset, top);
        printlnGreen(green, reset, mid);
        printlnGreen(green, reset, bottom);
        System.out.println(reset);
    }

    private static void printlnGreen(String green, String reset, String line) {
        System.out.println(green + line + reset);
    }

    private static int displayWidth(String text) {
        int width = 0;
        for (int i = 0; i < text.length(); ) {
            int cp = text.codePointAt(i);
            width += charDisplayWidth(cp);
            i += Character.charCount(cp);
        }
        return width;
    }

    private static int charDisplayWidth(int cp) {
        if (Character.isISOControl(cp)) {
            return 0;
        }
        if (isWideCodePoint(cp)) {
            return 2;
        }
        return 1;
    }

    private static boolean isWideCodePoint(int cp) {
        if (cp < 0x2E80) {
            return false;
        }
        return (cp <= 0xA4CF)
                || (cp >= 0xAC00 && cp <= 0xD7AF)
                || (cp >= 0xF900 && cp <= 0xFAFF)
                || (cp >= 0xFE10 && cp <= 0xFE6F)
                || (cp >= 0xFF00 && cp <= 0xFF60)
                || (cp >= 0xFFE0 && cp <= 0xFFE6);
    }

    /**
     * 按显示列居中；若估算与终端不一致，再对称补空格，保证与上下边框等宽。
     */
    private static String padCenter(String text, int totalWidth) {
        int textWidth = displayWidth(text);
        int pad = Math.max(0, totalWidth - textWidth);
        int left = pad / 2;
        int right = pad - left;
        String result = " ".repeat(left) + text + " ".repeat(right);
        int gap = totalWidth - displayWidth(result);
        if (gap <= 0) {
            return result;
        }
        int extraLeft = gap / 2;
        int extraRight = gap - extraLeft;
        return " ".repeat(extraLeft) + result + " ".repeat(extraRight);
    }
}
