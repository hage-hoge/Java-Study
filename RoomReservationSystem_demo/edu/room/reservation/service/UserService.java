package reservation.service;

import java.util.*;

import reservation.util.*;
import reservation.repository.*;
import reservation.model.*;

public class UserService {
    private static final int MAX_TRY_LOGIN = 5;
    private static final String ROLE_USER  = "USER";
    private static final String ROLE_ADMIN = "ADMIN";
    private User currentUser;
    private static final UserRepository userRepo = new UserRepository();
    private Scanner sc = new Scanner(System.in,"MS932");

    public UserService(){}

    public User authorize(){
        this.currentUser = findByName();
        
        if(verification()){
            System.out.println("ログイン成功");
            currentUser.unLock();
            return currentUser;
        }
        else{
            System.out.println("ログイン失敗");
            return null;
        }
    }

    private User findByName(){
        while(true){
            try{
                System.out.print("Login User Name> "); String userName = sc.nextLine();
                TroubleShooting.checkString(userName, "StringError");
                TroubleShooting.isNotExist(userRepo, userName);
                for(User tmpUser : userRepo.getMap().values()){
                    if(tmpUser.getName().equals(userName)) return tmpUser;
                }
            }catch(Exception e){
                //System.out.println("wait, whats?");
                System.out.println(e.getMessage());
                continue;
            }
        }
    }

    
    private boolean verification(){
        while(currentUser.getFailed() < MAX_TRY_LOGIN){
            try{
                System.out.print("Your Passward> "); String userPass = sc.nextLine();
                TroubleShooting.checkString(userPass, "StringError");
                if(!currentUser.getPass().equals(userPass)) { currentUser.failed(); }
                else return true;
            }catch(Exception e){
                System.out.println(e.getMessage());
            }
        }
        System.out.println("locked!");
        currentUser.Lock();
        return false;
    }

    public void register(){
        boolean success = false;
        while(!success){
            try{
                System.out.print("User Name> "); String userName = sc.nextLine();
                TroubleShooting.checkString(userName, "StringError");
                TroubleShooting.isExist(userRepo, userName);
                System.out.print("Passward> "); String userPass = sc.nextLine();
                TroubleShooting.checkString(userPass, "StringError");
                System.out.print("User Type (0...USER / 1...ADMIN)> "); String userType = sc.nextLine();
                TroubleShooting.checkString(userType, "StringError");
                success = true;

                userType = normalize(userType);
                userRepo.add(userName,userPass,userType);
            }catch(Exception e){
                System.out.println(e.getMessage());
            }
        }
    }

    public void delete(){
        try{
            TroubleShooting.isAdmin(currentUser);
            showList();
            System.out.print("Select delete target >"); int targetId = sc.nextInt();
            TroubleShooting.isExist(userRepo, targetId);
            if(TroubleShooting.confirmDelete()) userRepo.remove(targetId);
            else System.out.println("Canceled Delete");

            return;
        }catch(Exception e){
            System.out.println(e.getMessage());
        }
    }

    public void showList(){
        for(User user : userRepo.getMap().values()){
            System.out.println(user.getId()+","+user.getName()+","+user.getType());
        }
    }

    private static String normalize(String raw){
        String s = raw.trim();

        /* 半角/全角数字 → 0:user  1:admin */
        if (s.length() == 1) {
            int d = Character.digit(s.charAt(0), 10);   // '０'→0, '1'→1
            if (d == 0) return ROLE_USER;
            if (d == 1) return ROLE_ADMIN;
        }

        /* 英字表記を大文字に統一して比較 */
        s = s.toUpperCase();
        if ("USER".equals(s))  return ROLE_USER;
        if ("ADMIN".equals(s) || "ADMINISTRATOR".equals(s)) return ROLE_ADMIN;

        return null;
    }

}

