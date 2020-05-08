package com.example.account.Validation;

import com.example.account.Model.User;

public class SignUpValiadtion {

    public Boolean Check(User usr)
    {
        if (usr.getEmail()==null || usr.getEmail().equals(""))
        {
            return false;
        }
        return true;
    }
}
