package reservation.model;

public class User {
    private final int userId;
    private final String userName;
    private final String pass;
    private final String userType;

    private int failed;
    private boolean lock;

    public User(int userId, String userName, String pass, String userType){
        this.userId = userId;
        this.userName = userName;
        this.pass = pass;
        this.userType = userType;
        this.failed = 0;
        this.lock = false;
    }

    public Integer getId(){ return userId; }
    public String getName(){ return userName; }
    public String getPass(){ return pass; }
    public String getType(){ return userType; }
    public int getFailed(){ return failed; }
    public void failed() { failed++; }
    public boolean isLock(){ return lock; }

    public void Lock(){ 
        this.lock = true; 
    }
    public void unLock(){ 
        this.failed = 0;
        this.lock = false; 
    }
}
