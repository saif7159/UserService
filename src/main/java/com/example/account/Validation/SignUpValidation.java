package com.example.account.Validation;

import com.example.account.Dao.UserDaoRepository;
import com.example.account.Model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SignUpValidation {

    @Autowired
    public UserDaoRepository userdao;

    public Boolean CheckEmail(User user) {
        return !user.getEmail().equals("");
    }

    public Boolean CheckEmailInDb(User user) {
        List<User> userdata = userdao.findAllByEmailAndUserPermission(user.getEmail(), user.getUserPermission());
        return userdata.size() == 0;
    }


}
