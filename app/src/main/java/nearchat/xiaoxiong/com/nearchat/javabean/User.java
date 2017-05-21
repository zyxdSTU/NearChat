package nearchat.xiaoxiong.com.nearchat.javabean;

import org.litepal.crud.DataSupport;

/**
 * Created by Administrator on 2017/5/18.
 */

public class User extends DataSupport{
    private String phoneNumber;
    private String nickName;
    private String password;
    private String school;
    private String sex;
    private String personality;
    private int age;

    public User(String phoneNumber, String nickName, String password, String school, String sex, String personality, int age) {
        this.phoneNumber = phoneNumber;
        this.nickName = nickName;
        this.password = password;
        this.school = school;
        this.sex = sex;
        this.personality = personality;
        this.age = age;
    }

    public User() {}

    public String getPhoneNumber() {return phoneNumber;}
    public String getNickName() {return nickName;}
    public String getPassword() {return password;}
    public String getSchool() {return school;}
    public String getSex() {return sex;}
    public String getPersonality() {return personality;}
    public int getAge() {return age;}


    public void setPhoneNumber(String phoneNumber) {this.phoneNumber = phoneNumber;}
    public void  setNickName(String nickName) {this.nickName = nickName;}
    public void setPassword(String password) {this.password = password;}
    public void setSchool(String school) {this.school = school;}
    public void setSex(String sex) {this.sex = sex;}
    public void setPersonality(String personality) {this.personality = personality;}
    public void setAge(int age) {this.age = age;}
}
