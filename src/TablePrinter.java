package reservation.util;

import java.util.List;

public final class TablePrinter {

    private TablePrinter() {}

    public static void print(String[] headers, List<String[]> rows) {
        int cols = headers.length;
        int[] w  = new int[cols];

        /* 列幅計算（全角=2, 半角=1） */
        for (int c = 0; c < cols; c++) w[c] = visual(headers[c]);
        for (String[] r : rows)
            for (int c = 0; c < cols; c++)
                if (visual(r[c]) > w[c]) w[c] = visual(r[c]);

        String sep = buildSep(w);
        System.out.println(sep);

        /* ヘッダ */
        System.out.print("|");
        for (int c = 0; c < cols; c++)
            System.out.printf(" %-" + w[c] + "s |", headers[c]);
        System.out.println();
        System.out.println(sep);

        /* 本体 */
        for (String[] r : rows) {
            System.out.print("|");
            for (int c = 0; c < cols; c++) {
                System.out.printf(" %-" + w[c] + "s |", r[c]);
            }
            System.out.println();
        }
        System.out.println(sep);
    }

    /* ---------------- Helper ---------------- */
    private static int visual(String s) {
        int w = 0;
        for (int i = 0; i < s.length(); i++)
            w += isFull(s.charAt(i)) ? 2 : 1;
        return w;
    }
    private static boolean isFull(char ch) {
        Character.UnicodeBlock ub = Character.UnicodeBlock.of(ch);
        return ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS
            || ub == Character.UnicodeBlock.CJK_SYMBOLS_AND_PUNCTUATION
            || ub == Character.UnicodeBlock.HIRAGANA
            || ub == Character.UnicodeBlock.KATAKANA
            || ub == Character.UnicodeBlock.HALFWIDTH_AND_FULLWIDTH_FORMS;
    }
    private static String buildSep(int[] w) {
        StringBuilder sb = new StringBuilder("+");
        for (int x : w) {
            for (int i = 0; i < x + 2; i++) sb.append('-');
            sb.append('+');
        }
        return sb.toString();
    }
}
