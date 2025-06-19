package reservation.service;

import reservation.model.User;

import java.util.Scanner;

/**
 * 1ユーザ分のセッションを管理するだけの薄いクラス
 *  - loginLoop(...)  : 成功するまで AuthService.login() を呼び続ける
 *  - logout()       : current を null に戻す
 *  - isLoggedIn()   : ログイン済みか判定
 *  - getUser()      : 現在ユーザを返す（null 可）
 */
public class SessionService {

    private final AuthService auth;
    private User current;          // null = 未ログイン

    public SessionService(AuthService auth) {
        this.auth = auth;
    }

    /** 失敗するとループ、成功すれば current にセットして返す */
    public User loginLoop(Scanner sc) throws Exception {
        while (true) {
            System.out.print("ユーザ名  : ");
            String n = sc.nextLine().trim();
            System.out.print("パスワード: ");
            String p = sc.nextLine().trim();

            User u = auth.login(n, p);
            if (u != null) {
                current = u;
                System.out.println("ログイン成功 (" + u.getRole() + ")");
                return u;
            }
            System.out.println("失敗。再入力してください\n");
        }
    }

    /** ログアウト */
    public void logout() {
        current = null;
        System.out.println("ログアウトしました");
    }

    public boolean isLoggedIn()     { return current != null; }
    public boolean isAdmin()        { return isLoggedIn() && current.isAdmin(); }
    public User     getUser()       { return current; }
}
