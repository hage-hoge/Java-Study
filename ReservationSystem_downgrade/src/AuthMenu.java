package reservation.ui;

import reservation.model.*;
import reservation.repository.UserRepository;
import reservation.service.AuthService;

import java.util.List;
import java.util.Scanner;

/**
 * 認証 UI（ログイン + ユーザ管理）
 */
public class AuthMenu {
    private final Scanner       sc;
    private final AuthService   auth;
    private final UserRepository repo;

    public AuthMenu(Scanner sc, AuthService auth, UserRepository repo) {
        this.sc   = sc;
        this.auth = auth;
        this.repo = repo;
    }

    /* ---------- ログイン ---------- */
    public User loginLoop() throws Exception {
        while (true) {
            System.out.print("ユーザ名  : ");
            String n = sc.nextLine().trim();
            System.out.print("パスワード: ");
            String p = sc.nextLine().trim();
            User u = auth.login(n, p);
            if (u != null) {
                System.out.println("ログイン成功 (" + u.getRole() + ")");
                return u;
            }
            System.out.println("失敗。再入力してください\n");
        }
    }

    /* ---------- ユーザ登録（管理者専用） ---------- */
    public void adminAddUser(User admin) throws Exception {
        if (!admin.isAdmin()) { System.out.println("権限なし"); return; }

        System.out.print("新ユーザ名: ");
        String n = sc.nextLine().trim();

        System.out.print("パスワード: ");
        String p = sc.nextLine().trim();

        System.out.print("role (admin/user または 1=admin, 0=user): ");
        String r = sc.nextLine().trim();

        UserRole role = ("admin".equalsIgnoreCase(r) || "1".equals(r))
                        ? UserRole.ADMIN
                        : UserRole.USER;

        try {
            auth.addUser(admin, n, p, role);
            System.out.println("登録しました");
        } catch (Exception ex) {
            System.out.println("エラー: " + ex.getMessage());
        }
    }

    /* ---------- ユーザ一覧（管理者専用） ---------- */
    public void adminListUsers(User admin) {
        if (!admin.isAdmin()) { System.out.println("権限なし"); return; }

        List<User> list = repo.findAll();
        if (list.isEmpty()) { System.out.println("ユーザなし"); return; }

        System.out.println("+----+------------+--------+--------+");
        System.out.println("| ID | ユーザ名   | ロール | 状態   |");
        System.out.println("+----+------------+--------+--------+");
        int idx = 1;
        for (User u : list) {
            System.out.printf("| %-2d | %-10s | %-6s | %-6s |\n",
                    idx++, u.getName(), u.getRole(),
                    u.isLocked() ? "LOCK" : "");
        }
        System.out.println("+----+------------+--------+--------+");
    }

    /* ---------- ロック解除（管理者専用） ---------- */
    public void adminUnlockUser(User admin) {
        if (!admin.isAdmin()) { System.out.println("権限なし"); return; }

        System.out.print("解除するユーザ名: ");
        String target = sc.nextLine().trim();
        try {
            auth.unlock(admin, target);
            System.out.println("解除しました");
        } catch (Exception ex) {
            System.out.println("エラー: " + ex.getMessage());
        }
    }
}
