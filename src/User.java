package reservation.model;

/**
 * ユーザ情報（パスワードは SHA-256 ハッシュで保持）
 * ・5 回連続失敗で locked = true
 * ・unlock() でロック解除
 */
public class User {
    private final String   name;
    private final String   passHash;
    private final UserRole role;

    private int     failed;   // 連続失敗回数
    private boolean locked;   // ロック状態

    /* 通常コンストラクタ（新規作成時は failed=0, locked=false） */
    public User(String name, String hash, UserRole role) {
        this(name, hash, role, 0, false);
    }

    /* ファイル復元用 */
    public User(String name, String hash, UserRole role,
                int failed, boolean locked) {
        this.name    = name;
        this.passHash = hash;
        this.role    = role;
        this.failed  = failed;
        this.locked  = locked;
    }

    /* ------------- getter ------------- */
    public String   getName()    { return name; }
    public String   getHash()    { return passHash; }
    public UserRole getRole()    { return role; }
    public boolean  isAdmin()    { return role == UserRole.ADMIN; }
    public int      getFailed()  { return failed; }
    public boolean  isLocked()   { return locked; }

    /* ------------- 状態操作 ------------- */
    /** 成功ログイン時に呼ぶ */
    public void resetFail() {
        this.failed = 0;
    }

    /** 失敗ログイン時に呼ぶ（5 回でロック） */
    public void incFail() {
        this.failed++;
        if (failed >= 5) this.locked = true;
    }

    /** 管理者がロック解除 */
    public void unlock() {
        this.failed = 0;
        this.locked = false;
    }
}
