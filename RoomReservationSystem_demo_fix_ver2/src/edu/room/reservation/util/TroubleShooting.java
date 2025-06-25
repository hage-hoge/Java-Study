package edu.room.reservation.util;

import java.io.*;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.regex.*;

import edu.room.reservation.service.UserService;
import edu.room.reservation.model.*;
import edu.room.reservation.repository.*;

public class TroubleShooting {

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

    /* ---------- 呼び出し元→番号 -------------- */
    private static final Map<String, Integer> CALLER_ID = new HashMap<>();
    static {
        // メソッドまで指定したい場合は「クラス.メソッド」
        CALLER_ID.put("StartUp.login",1);
        CALLER_ID.put("StartUp", 9);
        CALLER_ID.put("StartUp.execADMINmenu",9);
        CALLER_ID.put("StartUp.execUSERmenu",9);
        CALLER_ID.put("UserService.authorize",1);
        CALLER_ID.put("UserService.userfinder", 1);
        CALLER_ID.put("UserService.verification", 1);
        // クラス単位（メソッドが一致しなかった時のフォールバック用）
        CALLER_ID.put("UserService",        2);
        CALLER_ID.put("RoomService",        3);
        CALLER_ID.put("ReservationService", 4);
    }

    /* ---------- 設定 ---------- */
    private static final String PATH = "./edu/room/reservation/util/doc/error_messages.csv";   // 位置を変えたい場合はここ

    /* ---------- メッセージ格納 ---------- */
    private static final Map<String, String> MAP = new HashMap<String, String>();
    private static final Scanner sc = new Scanner(System.in);
    private static final Pattern NG_CHARS_PATTERN = Pattern.compile(
        "[" +
        "\\p{IsHiragana}" +           // ひらがな
        "\\p{IsKatakana}" +           // 全角カタカナ
        "\\uFF66-\\uFF9F" +           // 半角カタカナ
        "\\uFF10-\\uFF19" +           // 全角数字
        "\\uFF21-\\uFF3A" +           // 全角英大
        "\\uFF41-\\uFF5A" +           // 全角英小
        "\\p{IsHan}" +                // 漢字
        "　 \\t" +                    // 全角・半角スペース と TAB ← 追加
        "]"
    );

    private static final Pattern DATE_PATTERN =
        Pattern.compile("^(\\d{4})[/-](\\d{1,2})[/-](\\d{1,2})$");
    private static final Pattern FLEX_HHMM =
        Pattern.compile("^([01]?\\d|2[0-3]):([0-5]?\\d)$");
    /* ---------- 英数字だけを許可 (1–20 文字) ---------- */
    private static final Pattern ALNUM_ASCII = Pattern.compile("^[A-Za-z0-9]{1,20}$");
    /* ---------- 正の整数チェック（全角数字許容） ---------- */
    private static final Pattern DIGIT_ONLY = Pattern.compile("^\\d+$");

    static {
        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(new FileInputStream(PATH), "UTF-8"))) {

            StringBuilder logical = new StringBuilder();
            boolean inQuote = false;
            int c;
            while ((c = br.read()) != -1) {
                char ch = (char) c;
                logical.append(ch);
                if (ch == '"') inQuote = !inQuote;        // " 入り／抜け判定
                if (ch == '\n' && !inQuote) {             // 引用外の改行 → 完結
                    addCsvRow(logical.toString().trim());
                    logical.setLength(0);
                }
            }
            if (logical.length() > 0)                     // EOF 末尾
                addCsvRow(logical.toString().trim());
        } catch (IOException ex) {
            System.err.println("[ErrorCatalog] 読み込み失敗: " + ex);
        }
    }


    private TroubleShooting() {}   // static util

    /* ------------------------------------------------------------------
       外部公開 API
       ------------------------------------------------------------------ */
    public static String getErrorMsg(String code, Object... args) {
        String tpl = MAP.get(code);
        String body = (tpl == null)
                      ? "Unknown error (" + code + ")"
                      : (args == null || args.length == 0 ? tpl
                                                           : String.format(tpl, args));

        String origin = caller();
        return "[" + origin + "] " + body;
    }

    public static void checkString(String target) throws Exception{
            if(target == null || target.isEmpty()) throw new Exception(auto(1));
            if(target.length()>20) throw new Exception(auto(1));
            if(NG_CHARS_PATTERN.matcher(target).find()) throw new Exception(auto(1));
    }

    public static void checkString(String target, int MAX_LENGTH) throws Exception{
            if(target == null || target.isEmpty()) throw new Exception(auto(1));
            if(target.length() >= MAX_LENGTH) throw new Exception(auto(1));
            if(NG_CHARS_PATTERN.matcher(target).find()) throw new Exception(auto(1));
    }

    public static String checkDateFormat(String target) throws Exception{
        if(target == null) throw new Exception(auto(1));
        return normalizeDate(target);
    }

    public static void checkInt(int num, int MAX) throws Exception{
        if(num <= 0) throw new Exception("number Error, num > 0");
        if(num > MAX) throw new Exception("number over MAX");
    }

    public static void checkInt(int num) throws Exception{
        if(num <= 0) throw new Exception("number Error, num > 0");
    }

    public static boolean isAdmin(User user) throws Exception{
        if(!"ADMIN".equals(user.getType())) throw new Exception("not admin");
        return "ADMIN".equals(user.getType());
    }

    public static String normalizeHHmm(String timeRaw, int local) throws Exception {
        String t = toHalfWidth(timeRaw).trim();         // 全角→半角・空白除去
        Matcher m = FLEX_HHMM.matcher(t);
        if (!m.matches()) throw new Exception(auto(local));   // 402 / 403

        int hh = Integer.parseInt(m.group(1));
        int mm = Integer.parseInt(m.group(2));
        if (mm >= 60) throw new Exception(auto(local));       // 分は 0–59

        return String.format("%02d:%02d", hh, mm);
    }

    private static String normalizeDate(String raw) throws Exception {
        String src = toHalfWidth(raw).trim();        // 全角→半角

        Matcher m = DATE_PATTERN.matcher(src);
        if (!m.matches()) throw new Exception(auto(1));

        int y  = Integer.parseInt(m.group(1));
        int mo = Integer.parseInt(m.group(2));
        int d  = Integer.parseInt(m.group(3));

        Calendar cal = Calendar.getInstance();
        cal.setLenient(false);
        try {
            cal.set(y, mo - 1, d);
            cal.getTime();                           // 妥当性検査
        } catch (IllegalArgumentException e) {
            throw new Exception(auto(1));
        }
        return String.format("%04d/%02d/%02d", y, mo, d);
    }


    public static int parsePositiveInt(String raw) throws Exception {
        String s = toHalfWidth(raw).trim();            // 全角→半角
        if (!DIGIT_ONLY.matcher(s).matches())          // 数字のみ
            throw new Exception(auto(5));          // 305: 最大収容人数が不正
        try {
            int v = Integer.parseInt(s);
            if (v <= 0) throw new NumberFormatException();
            return v;
        } catch (NumberFormatException e) {
            throw new Exception(auto(5));          // 305
        }
    }

    public static int parseMenu(String raw) throws Exception {
        String s = toHalfWidth(raw).trim();             // 全角→半角
        if (!DIGIT_ONLY.matcher(s).matches())           // 0–9 以外
            throw new Exception(auto(1));               // 901 (local=1 に割当)
        int v = Integer.parseInt(s);
        return v;
    }

    /** 全角英数字・記号を半角へ ― 必要最小限だけ実装 */
    public static String toHalfWidth(String src) {
        StringBuilder sb = new StringBuilder(src.length());
        for (int i = 0; i < src.length(); i++) {
            char ch = src.charAt(i);

            // 全角英数字・記号の基本変換
            if (ch >= '！' && ch <= '～') {    // FF01〜FF5E
                ch -= 0xFEE0;
            }
            // 全角スペース
            else if (ch == '　') {
                ch = ' ';
            }
            sb.append(ch);
        }
        return sb.toString();
    }

    /*英数字だけ許可する ID/パスワードチェック*/

    public static void assertAsciiAlnum(String target) throws Exception {
        String t = toHalfWidth(target).trim();          // 全角→半角
        if (!ALNUM_ASCII.matcher(t).matches())
            throw new Exception(auto(7));           // 201 / 202 / 208 など
    }

    public static void assertAsciiAlnum(String target, int local) throws Exception {
        String t = toHalfWidth(target).trim();          // 全角→半角
        if (!ALNUM_ASCII.matcher(t).matches())
            throw new Exception(auto(local));           // 201 / 202 / 208 など
    }

    public static void checkRepository(UserRepository userRepo) throws Exception{
        if(userRepo.getMap().size() <= 0) throw new Exception(auto(2));
    }

    public static void checkRepository(UserRepository userRepo, int MAX) throws Exception{
        if(userRepo.getMap().size() >= MAX) throw new IllegalStateException(auto(4));
    }

    public static void checkRepository(RoomRepository roomRepo) throws Exception{
        if(roomRepo.getMap().size() <= 0) throw new Exception(auto(22));
    }

    public static void checkRepository(RoomRepository roomRepo, int MAX) throws Exception{
        if(roomRepo.getMap().size() >= MAX) throw new IllegalStateException(auto(3));
    }

    public static void checkRepository(ReservationRepository resRepo) throws Exception{
        if(resRepo.getMap().size() <= 0) throw new Exception(auto(22));
    }

    public static void checkRepository(ReservationRepository resRepo, int MAX) throws Exception{
        if(resRepo.getMap().size() >= MAX) throw new IllegalStateException(auto(10));
    }

    public static void checkRoom(String roomName) throws Exception{
        if(roomName == null || roomName.isEmpty()) throw new Exception(auto(4));
        if(roomName.length()>4) throw new Exception(auto(4));
        if(NG_CHARS_PATTERN.matcher(roomName).find()) throw new Exception(auto(4));
    }

    public static void isExist(UserRepository userRepo, String targetName) throws Exception{
        for(User user : userRepo.getMap().values()){
            if(targetName.equals(user.getName())){ 
                throw new Exception(auto(3));
            }
        }
    }
    public static void isExist(UserRepository userRepo, int targetId) throws Exception{
        if(userRepo.getMap().get(targetId) == null) throw new Exception(auto(5));
    }

  
    public static void isExist(RoomRepository roomRepo, String targetName) throws Exception{
        for(Room room : roomRepo.getMap().values()){
            if(targetName.equals(room.getName())){ 
                throw new Exception(auto(4));
            }
        }
    }

    public static void isExist(RoomRepository roomRepo, int targetId) throws Exception{
        if(roomRepo.getMap().get(targetId) == null) throw new Exception(auto(2));
    }

    public static int isExist(ReservationRepository resRepo, User user) throws Exception{
        for(Reservation r : resRepo.getMap().values()){
            if(r.getOwner().equals(user)) return r.getOwner().getId();
        }
        throw new Exception(auto(12));
    }

    public static int isNotExist(UserRepository userRepo, String targetName) throws Exception{
        for(User user : userRepo.getMap().values()){
            if(targetName.equals(user.getName())){ 
                return user.getId();
            }
        }
        throw new Exception(auto(2));
    }

    public static int isNotExist(RoomRepository roomRepo, String targetName) throws Exception{
        for(Room room : roomRepo.getMap().values()){
            if(targetName.equals(room.getName())){ 
                return room.getId();
            }
        }
        throw new Exception(auto(4));
    }

    public static boolean confirmDelete(){
        while(true){
            System.out.print("本当に削除しますか? [ y/n ] >"); String confirm = sc.nextLine();
            confirm = TroubleShooting.toHalfWidth(confirm).toLowerCase();

            if("y".equals(confirm) || "yes".equals(confirm)) return true;
            if("n".equals(confirm) || "no".equals(confirm)) return false;
        }
    }
    /*
    public checkRepository(RoomRepository roomRepo){

    }

    public checkRepository(ReservationRepository resRepo){

    }
    */
    /* 非公開 */
    /** 1 論理行を code,message に分割して MAP へ */
    private static void addCsvRow(String line) {
        if (line.isEmpty() || line.startsWith("code")) return; // ヘッダ or 空

        // "..." 内の , は残したまま最初のカンマで分割
        int idx = line.indexOf(',');
        if (idx < 0) return;
        String code = line.substring(0, idx).replaceAll("^\"|\"$", "").trim();
        String msg  = line.substring(idx + 1)
                        .replaceAll("^\"|\"$", "")      // 外側の " 除去
                        .replace("\"\"", "\"");         // "" → "
        MAP.put(code, msg);
    }

    /** スタックトレースから「クラス.メソッド」→番号を引く */
    private static int callerId() {
        String origin = caller();                // 例: "UserService.login"
        Integer id = CALLER_ID.get(origin);      // 完全一致（メソッド優先）

        if (id == null) {                        // クラス単位で再検索
            String cls = origin.contains(".")
                        ? origin.substring(0, origin.indexOf('.'))
                        : origin;
            id = CALLER_ID.get(cls);
        }
        return id == null ? 0 : id;              // 未登録は 0
    }
    /* ------------------------------------------------------------------
       呼び出し側クラス.メソッド を取得
       ------------------------------------------------------------------ */
    /** 呼び出し元クラス.メソッドを取得（TroubleShooting 自身をスキップ） */
    private static String caller() {
        String myName = TroubleShooting.class.getName();
        StackTraceElement[] st = Thread.currentThread().getStackTrace();

        for (int i = 2; i < st.length; i++) {          // 0:getStackTrace 1:caller
            if (!st[i].getClassName().equals(myName)) {
                String cls = st[i].getClassName();
                String simple = cls.substring(cls.lastIndexOf('.') + 1);
                return simple + "." + st[i].getMethodName();
            }
        }
        return "unknown";
    }

    /* ========== ローカル連番 → 3桁コードに変換して msg() ========== */
    public static String auto(int local, Object... args) {
        int caller = callerId();                    // 1～4
        int code   = caller * 100 + local;          // 101, 204, 301 …
        System.out.println("エラーメッセージ番号["+code+"]");
        return getErrorMsg(String.valueOf(code), args);
    }
}
