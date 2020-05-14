package com.example.account.Response;

import com.example.account.Model.User;

import java.util.List;

public class serverResponse {

    private int status;
    private String message;
    private String usertype;
    private String Auth_TOKEN;
    private Object object;
    private List<User> useralldata;

    public List<User> getUseralldata() {
        return useralldata;
    }

    public void setUseralldata(List<User> useralldata) {
        this.useralldata = useralldata;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getAuth_TOKEN() {
        return Auth_TOKEN;
    }

    public void setAuth_TOKEN(String auth_TOKEN) {
        Auth_TOKEN = auth_TOKEN;
    }

    public Object getObject() {
        return object;
    }

    public void setObject(Object object) {
        this.object = object;
    }

    public String getUsertype() {
        return usertype;
    }

    public void setUsertype(String usertype) {
        this.usertype = usertype;
    }
}
