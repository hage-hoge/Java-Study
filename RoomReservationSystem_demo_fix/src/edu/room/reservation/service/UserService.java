package edu.room.reservation.service;

import java.util.*;

import edu.room.reservation.util.*;
import edu.room.reservation.repository.*;
import edu.room.reservation.model.*;

public class UserService {
    private static final int MAX_TRY_LOGIN = 5;
    private static final int MAX_USER = 5;
    private static final String ROLE_USER  = "USER";
    private static final String ROLE_ADMIN = "ADMIN";
    private User currentUser;
    private UserRepository userRepo;
    private Scanner sc = new Scanner(System.in,"MS932");

    public UserService(UserRepository userRepo){
        this.userRepo = userRepo;
    }

    public User authorize() throws Exception{
        try{
            this.currentUser = userfinder();
            if(currentUser.isLock()) throw new IllegalAccessException(TroubleShooting.auto(4));
            if(verification()){
                System.out.println("ログイン成功");
                currentUser.unLock();
                return currentUser;
            }
            else{
                System.out.println("ログイン失敗");
                throw new Exception(TroubleShooting.auto(3));
            }
        }catch(Exception e){
            throw e;
        }
    }

    private User userfinder(){
        int local = 1;
        while(true){
            try{
                System.out.print("ユーザ名> "); String userName = sc.nextLine();
                TroubleShooting.checkString(userName);
                TroubleShooting.assertAsciiAlnum(userName, local);
                TroubleShooting.isNotExist(userRepo, userName);
                for(User tmpUser : userRepo.getMap().values()){
                    if(tmpUser.getName().equals(userName)) return tmpUser;
                }
            }catch(Exception e){
                System.out.println(e.getMessage());
                continue;
            }
        }
    }

    
    private boolean verification() throws Exception{
        int local = 1;
        while(currentUser.getFailed() < MAX_TRY_LOGIN){
            try{
                System.out.print("パスワード> "); String userPass = sc.nextLine();
                TroubleShooting.checkString(userPass);
                TroubleShooting.assertAsciiAlnum(userPass, local);
                if(!currentUser.getPass().equals(userPass)) { currentUser.failed(); }
                else return true;
            }catch(Exception e){
                throw e;
            }
        }
        currentUser.Lock();
        return false;
    }

    public void unLocker() throws Exception{
        User lockedUser = userfinder();
        if(currentUser.getType().equals(ROLE_ADMIN)) lockedUser.unLock();
        else throw new Exception(TroubleShooting.auto(20));
    }

    public void register(){
        while(true){
            try{
                TroubleShooting.checkRepository(userRepo, MAX_USER);
                System.out.print("ユーザ名> "); String userName = sc.nextLine();
                TroubleShooting.checkString(userName);
                TroubleShooting.assertAsciiAlnum(userName);
                TroubleShooting.isExist(userRepo, userName);
                System.out.print("パスワード> "); String userPass = sc.nextLine();
                TroubleShooting.checkString(userPass);
                TroubleShooting.assertAsciiAlnum(userPass);
                System.out.print("ユーザ種別 (0...一般利用者 / 1...管理者)> "); String userType = sc.nextLine();
                TroubleShooting.assertAsciiAlnum(userType);
                userType = normalize(userType);
                userRepo.add(userName,userPass,userType);
                return;
            }catch(IllegalStateException e){
                System.out.println(e.getMessage());
                return;
            }catch(Exception e){
                System.out.println(e.getMessage());
            }
        }
    }

    public void delete(){
        try{
            TroubleShooting.isAdmin(currentUser);
            showList();
            System.out.print("ユーザ名>"); String targetName = sc.nextLine();
            int targetId = TroubleShooting.isNotExist(userRepo, targetName);
            if(TroubleShooting.confirmDelete()) userRepo.remove(targetId);
            /*else System.out.println("")*/;

            return;
        }catch(Exception e){
            System.out.println(e.getMessage());
        }
    }

    public void showList(){
        System.out.println("ユーザ名, ユーザ種別");
        System.out.println("======================================");
        for(User user : userRepo.getMap().values()){
            System.out.println(user.getName()+","+user.getType());
        }
        System.out.println("======================================");
    }

    private static String normalize(String raw) throws Exception {
        // 1) 全角→半角、全角スペースを半角化
        String s = TroubleShooting.toHalfWidth(raw).trim();
        
        // 2) □ 数字 0/1
        if (s.length() == 1) {
            int d = Character.digit(s.charAt(0), 10);   // '0' / '1'
            if (d == 0) return ROLE_USER;
            if (d == 1) return ROLE_ADMIN;
        }

        // 3) □ 英字 USER / ADMIN
        s = s.toUpperCase(Locale.ROOT);
        if ("USER".equals(s))  return ROLE_USER;
        if ("ADMIN".equals(s) || "ADMINISTRATOR".equals(s)) return ROLE_ADMIN;

        // 4) □ それ以外はエラー (local=3 → 203)
        throw new Exception(TroubleShooting.auto(2));
    }


}

