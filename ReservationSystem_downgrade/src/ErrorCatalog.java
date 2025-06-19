package reservation.util;

import java.io.*;
import java.util.*;

/**
 * エラーカタログ & 呼び出し元トレーサ
 *
 *  使い方:
 *      throw new IllegalArgumentException(ErrorCatalog.msg("E002", r.getId()));
 *
 *  出力例:
 *      [ReservationService.reserve] 既存予約と重複しています (Reservation #17)
 *
 *  ❶ `error_messages.csv` にコードとメッセージを定義（暗号化可）  
 *  ❷ ErrorCatalog.msg("コード", 可変引数…) を呼ぶだけで  
 *     - メッセージの String.format 置換  
 *     - 呼び出し側クラス.メソッド を自動で前置
 */
public final class ErrorCatalog {

    /* ---------- 設定 ---------- */
    private static final String PATH = "error_messages.csv";   // 位置を変えたい場合はここ

    /* ---------- メッセージ格納 ---------- */
    private static final Map<String, String> MAP = new HashMap<String, String>();

    static {
        BufferedReader br = null;
        try {
            br = new BufferedReader(new InputStreamReader(
                    new FileInputStream(PATH), "UTF-8"));
            String line = br.readLine();            // ヘッダ行
            while ((line = br.readLine()) != null) {
                String[] p = line.split(",", 2);
                if (p.length == 2) MAP.put(p[0].trim(), p[1].trim());
            }
        } catch (Exception ex) {
            System.err.println("[ErrorCatalog] 読み込み失敗: " + ex);
        } finally { try { if (br != null) br.close(); } catch (IOException ignore) {} }
    }

    private ErrorCatalog() {}   // static util

    /* ------------------------------------------------------------------
       外部公開 API
       ------------------------------------------------------------------ */
    public static String msg(String code, Object... args) {
        String tpl = MAP.get(code);
        String body = (tpl == null)
                      ? "Unknown error (" + code + ")"
                      : (args == null || args.length == 0 ? tpl
                                                           : String.format(tpl, args));

        String origin = caller();
        return "[" + origin + "] " + body;
    }

    /* ------------------------------------------------------------------
       呼び出し側クラス.メソッド を取得
       ------------------------------------------------------------------ */
    private static String caller() {
        StackTraceElement[] st = Thread.currentThread().getStackTrace();
        /* 0:getStackTrace 1:caller() 2:msg() 3:呼び出し元 ←これを取る */
        if (st.length > 3) {
            StackTraceElement c = st[3];
            String cls = c.getClassName();
            String simple = cls.substring(cls.lastIndexOf('.') + 1);
            return simple + "." + c.getMethodName();
        }
        return "unknown";
    }
}