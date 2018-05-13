package entities;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class User {
    @Override
    public String toString() {
        return "User{" +
                ", user_name='" + user_name + '\'' +
                ", password_hash='" + password_hash + '\'' +
                ", milk=" + milk +
                ", bread=" + bread +
                ", butter=" + butter +
                '}';
    }

    public String user_name;
    public String password_hash;
    public boolean milk;
    public boolean bread;
    public boolean butter;

    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

    @JsonIgnore
    public String secret;

    public String getUser_name() {
        return user_name;
    }

    public void setUser_name(String user_name) {
        this.user_name = user_name;
    }

    public String getPassword_hash() {
        return password_hash;
    }

    public void setPassword_hash(String password_hash) {
        this.password_hash = password_hash;
    }

    public boolean isMilk() {
        return milk;
    }

    public void setMilk(boolean milk) {
        this.milk = milk;
    }

    public boolean isBread() {
        return bread;
    }

    public void setBread(boolean bread) {
        this.bread = bread;
    }

    public boolean isButter() {
        return butter;
    }

    public void setButter(boolean butter) {
        this.butter = butter;
    }

}
