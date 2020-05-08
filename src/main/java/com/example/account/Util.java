package com.example.account;

import com.example.account.Dao.UserDaoRepository;
import com.example.account.Model.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;


@Component
public class Util {

    private final static String Key = "axWDVrgnYJil";
    private final static String ISSUER = "ADMIN_SHOPPING";
    private final static String SUBJECT = "USER_SHOPPING";
    private static final String SESS_EMAIL = "SESSION_EMAIL";
    private static final String SESS_PASS = "SESSION_PASS";
    private static final String SESS_TYPE = "SESSION_TYPE";

    @Autowired
    public UserDaoRepository userdao;

    public String createToken(String sess_email, String sess_pass, String sess_type) {
        Map<String, Object> map = new HashMap<>();
        map.put(SESS_EMAIL, sess_email);
        map.put(SESS_PASS, sess_pass);
        map.put(SESS_TYPE, sess_type);

        SignatureAlgorithm algo = SignatureAlgorithm.HS512;
        String br = Jwts.builder().setIssuer(ISSUER).setClaims(map).setSubject(SUBJECT)
                .signWith(algo, Key).compact();
        return br;
    }

    public User checkToken(String token)
    {
        Claims claims = Jwts.parser().setSigningKey(Key).parseClaimsJws(token).getBody();
        User user = userdao.findByEmailAndPasswordAndUserPermission(claims.get(SESS_EMAIL).toString(),
                claims.get(SESS_PASS).toString(), claims.get(SESS_TYPE).toString());
        return user;
    }

}


