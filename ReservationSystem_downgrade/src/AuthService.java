package reservation.service;

import reservation.model.*;
import reservation.repository.UserRepository;
import reservation.util.ErrorCatalog;

import java.security.MessageDigest;

/**
 * 認証・ユーザ管理サービス
 * ・5 回連続失敗でロック
 * ・管理者がロック解除可能
 */
public class AuthService {

    private final UserRepository repo;

    public AuthService(UserRepository repo) {
        this.repo = repo;
    }

    /* ---------- ログイン ---------- */
    public User login(String name, String pass) throws Exception {
        User u = repo.find(name);
        if (u == null) return null;

        if (u.isLocked()) {
            System.out.println("アカウントがロックされています");
            return null;
        }

        if (sha(pass).equals(u.getHash())) {
            u.resetFail();
            repo.flush();
            return u;
        } else {
            u.incFail();
            repo.flush();
            if (u.isLocked())
                System.out.println("5 回失敗したためアカウントをロックしました");
            return null;
        }
    }

    /* ---------- 新規ユーザ追加（管理者専用） ---------- */
    public void addUser(User admin, String name, String pass, UserRole role)
            throws Exception {
        if (!admin.isAdmin()) throw new IllegalAccessException(ErrorCatalog.msg("E005"));
        /* 文字数チェック */
        if (name.length() < 4 || name.length() > 20)
            throw new IllegalArgumentException(ErrorCatalog.msg("E011"));
        
        if (pass.length() < 4 || pass.length() > 20)
            throw new IllegalArgumentException(ErrorCatalog.msg("E011"));
            
        if (repo.find(name) != null)
            throw new IllegalArgumentException(ErrorCatalog.msg("E009", name)); // ★重複
        repo.add(new User(name, sha(pass), role));
        repo.flush();
    }

    /* ---------- ロック解除（管理者専用） ---------- */
    public void unlock(User admin, String target) throws Exception {
        if (!admin.isAdmin()) throw new IllegalAccessException("権限不足");
        User u = repo.find(target);
        if (u == null) throw new IllegalArgumentException("ユーザが存在しません");
        u.unlock();
        repo.flush();
    }

    /* ---------- ユーティリティ ---------- */
    private static String sha(String s) throws Exception {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        byte[] h = md.digest(s.getBytes("UTF-8"));
        StringBuilder sb = new StringBuilder();
        for (byte b : h) sb.append(String.format("%02x", b));
        return sb.toString();
    }
}
