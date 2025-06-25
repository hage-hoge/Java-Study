package reservation.util;

import java.io.*;
import java.util.*;
import java.util.regex.*;

import reservation.model.*;
import reservation.repository.*;

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
    /* ---------- 設定 ---------- */
    private static final String PATH = "./reservation/util/error_messages.csv";   // 位置を変えたい場合はここ

    /* ---------- メッセージ格納 ---------- */
    private static final Map<String, String> MAP = new HashMap<String, String>();
    private static final Scanner sc = new Scanner(System.in);
    private static final Pattern NG_CHARS_PATTERN = Pattern.compile(
        "[" +
        "\\p{IsHiragana}" +           // ひらがな
        "\\p{IsKatakana}" +           // 全角カタカナ
        "\\uFF66-\\uFF9F" +           // 半角カタカナ
        "\\uFF10-\\uFF19" +           // 全角数字 ０–９
        "\\uFF21-\\uFF3A" +           // 全角英大 Ａ–Ｚ
        "\\uFF41-\\uFF5A" +           // 全角英小 ａ–ｚ
        "\\p{IsHan}" +               // 漢字（不要なら削る）
        "　 " +                       // 全角・半角スペース
        "]"
    );

    private static final Pattern DATE_PATTERN =
        Pattern.compile("^(\\d{4})[/-](\\d{1,2})[/-](\\d{1,2})$");

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

    private TroubleShooting() {}   // static util

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

    public static String checkString(String target, String mode) throws Exception{
        String formatter = target;
        if("StringError".equals(mode)){
            if(target == null || target.isEmpty()) throw new Exception("isEmpty");
            if(target.length()>20) throw new Exception("over 20 char");
            if(NG_CHARS_PATTERN.matcher(target).find()) throw new Exception("bad format");
        }else if("DateError".equals(mode)){
            if(target == null) throw new Exception("isEmpty");
            formatter = normalizeDate(target);
        }else{
            throw new Exception("undefined MODE");
        }
        
        return formatter;
    }

    public static boolean checkString(String target) throws Exception{
        if(target == null || target.isEmpty()) throw new Exception("isEmpty");
        if(target.length()>20) throw new Exception("over 20 char");
        if(NG_CHARS_PATTERN.matcher(target).find()) throw new Exception("bad format");
    
        return true;
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

    private static String normalizeDate(String raw) throws Exception {
        String src = toHalfWidth(raw).trim();        // 全角→半角

        Matcher m = DATE_PATTERN.matcher(src);
        if (!m.matches()) throw new Exception("bad date format");

        int y  = Integer.parseInt(m.group(1));
        int mo = Integer.parseInt(m.group(2));
        int d  = Integer.parseInt(m.group(3));

        Calendar cal = Calendar.getInstance();
        cal.setLenient(false);
        try {
            cal.set(y, mo - 1, d);
            cal.getTime();                           // 妥当性検査
        } catch (IllegalArgumentException e) {
            throw new Exception("not exist date");
        }
        return String.format("%04d/%02d/%02d", y, mo, d);
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

    public static void checkRepository(UserRepository userRepo) throws Exception{
        if(userRepo.getMap().size() <= 0) throw new Exception("null UserRepository");
    }

    public static void checkRepository(RoomRepository roomRepo) throws Exception{
        if(roomRepo.getMap().size() <= 0) throw new Exception("null RoomRepository");
    }


    public static void isExist(UserRepository userRepo, String targetName) throws Exception{
        for(User user : userRepo.getMap().values()){
            if(targetName.equals(user.getName())){ 
                throw new Exception("isExist User");
            }
        }
    }

    public static void isExist(UserRepository userRepo, int targetId) throws Exception{
        if(userRepo.getMap().get(targetId) == null) throw new Exception("not Exist User");
    }

    public static void isExist(RoomRepository roomRepo, String targetName) throws Exception{
        for(Room room : roomRepo.getMap().values()){
            if(targetName.equals(room.getName())){ 
                throw new Exception("isExist Room");
            }
        }
    }

    public static void isExist(RoomRepository roomRepo, int targetId) throws Exception{
        if(roomRepo.getMap().get(targetId) == null) throw new Exception("not Exist Room");
    }


    public static void isNotExist(UserRepository userRepo, String targetName) throws Exception{
        for(User user : userRepo.getMap().values()){
            if(targetName.equals(user.getName())){ 
                return;
            }
        }
        throw new Exception("isNotExist User");
    }

    public static boolean confirmDelete(){
        while(true){
            System.out.print("Delete?  [ y/n ] >"); String confirm = sc.nextLine();
            confirm = TroubleShooting.toHalfWidth(confirm).toLowerCase();

            if("y".equals(confirm) || "yes".equals(confirm)) return true;
            if("n".equals(confirm) || "no".equals(confirm)) return false;

            System.out.println("Y or N");
        }
    }
    /*
    public checkRepository(RoomRepository roomRepo){

    }

    public checkRepository(ReservationRepository resRepo){

    }
    */
    
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
