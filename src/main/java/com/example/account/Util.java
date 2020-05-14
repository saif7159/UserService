package com.example.account;

import com.example.account.Dao.UserDaoRepository;
import com.example.account.Model.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;


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

//    Creating token
    public String createToken(String sess_email, String sess_pass, String sess_type) {
        Map<String, Object> map = new HashMap<>();
        map.put(SESS_EMAIL, sess_email);
        map.put(SESS_PASS, sess_pass);
        map.put(SESS_TYPE, sess_type);

        SignatureAlgorithm algo = SignatureAlgorithm.HS512;


        return Jwts.builder().setIssuer(ISSUER).setClaims(map).setSubject(SUBJECT)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis()+ 1000*60*60*24))
                .signWith(algo, Key).compact();
    }

//    Verifying token
    public User checkToken(String token) {
        if(token!=null && token.startsWith("Bearer "))
            token = token.substring(7);
        User user = null;
        System.out.println("token: "+token);
        try {
            Claims claims = Jwts.parser().setSigningKey(Key).parseClaimsJws(token).getBody();
            user = userdao.findByEmailAndPasswordAndUserPermission(claims.get(SESS_EMAIL).toString(),
                    claims.get(SESS_PASS).toString(), claims.get(SESS_TYPE).toString());
        } catch (Exception e) {
            System.out.println("no user found");
        }
        return user;
    }

//    Check Expiration date
    public boolean isTokenExpired(String token) {
        if(token!=null && token.startsWith("Bearer "))
            token = token.substring(7);
        final Date expiration = getExpirationDateFromToken(token);
        return expiration.before(new Date());
    }

    private Date getExpirationDateFromToken(String token) {
        return getClaimFromToken(token, Claims::getExpiration);
    }

    public <T> T getClaimFromToken(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = Jwts.parser().setSigningKey(Key).parseClaimsJws(token).getBody();
        return claimsResolver.apply(claims);
    }

}


