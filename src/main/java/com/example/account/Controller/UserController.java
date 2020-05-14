package com.example.account.Controller;


import com.example.account.Dao.UserDaoRepository;
import com.example.account.Model.User;
import com.example.account.Util;
import com.example.account.Validation.SignUpValidation;

import javassist.NotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.*;
import com.example.account.Response.serverResponse;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;


import javax.validation.Valid;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@CrossOrigin(origins = "http://localhost:4200")
@RestController
@RequestMapping("/User")
@RefreshScope
public class UserController {

    Logger logger = LoggerFactory.getLogger(UserController.class);


    @Autowired
    public UserDaoRepository userdao;

    @Autowired
    private Util jwtutil;

    @Autowired
    private SignUpValidation signUpValidation;



//    @Autowired
//    private UserDetailsService userDetailsService;


    //    Check token
    @PostMapping("/checkToken")
    public User checkToken(String token) {
        User usr = null;
        boolean time;
        if (jwtutil.checkToken(token) != null) {
            time = jwtutil.isTokenExpired(token);
            if (!time)
                usr = jwtutil.checkToken(token);
        }
        return usr;
    }




    // User Registration
    @PostMapping("/UserRegister")
    @HystrixCommand(fallbackMethod = "userRegistrationFallback")
    public ResponseEntity<serverResponse> adduser(@Valid @RequestBody User Customerincomingdata) {

        HttpStatus responsecode = null;
        serverResponse response = new serverResponse();

        try {
            Boolean checkEmail = signUpValidation.CheckEmail(Customerincomingdata);
            if (!checkEmail)
                throw new NotFoundException("");
            try {

                Customerincomingdata.setUserPermission("customer");

                Boolean EmailPresent = signUpValidation.CheckEmailInDb(Customerincomingdata);
                if (!EmailPresent)
                    throw new IllegalArgumentException();

                User usr = userdao.saveAndFlush(Customerincomingdata);

                response.setUsertype(usr.getUserPermission());
                response.setStatus(HttpStatus.OK.value());
                response.setMessage("Account Created Successfully");
                responsecode = HttpStatus.OK;

            } catch (Exception e) {
                System.out.println(e);
                response.setStatus(HttpStatus.CONFLICT.value());
                response.setMessage("Email-ID already associated with different Account");
                responsecode = HttpStatus.CONFLICT;

            }
        } catch (Exception e) {
            System.out.println(e);
            response.setStatus(HttpStatus.NOT_IMPLEMENTED.value());
            response.setMessage("Email id not valid");
            responsecode = HttpStatus.NOT_FOUND;
        }


        return new ResponseEntity<>(response, responsecode);
    }


    // Admin Registration
    @PostMapping("/AdminRegister")
    @HystrixCommand(fallbackMethod = "adminRegistrationFallback")
    public ResponseEntity<serverResponse> addadmin(@Valid @RequestBody User adminincommingdata) {

        serverResponse response = new serverResponse();
        HttpStatus responsecode = null;
        try {
            Boolean checkEmail = signUpValidation.CheckEmail(adminincommingdata);
            if (!checkEmail)
                throw new NotFoundException("");
            try {
                adminincommingdata.setUserPermission("admin");
                Boolean EmailPresent = signUpValidation.CheckEmailInDb(adminincommingdata);
                if (!EmailPresent)
                    throw new IllegalArgumentException();

                User usr = userdao.saveAndFlush(adminincommingdata);

                response.setStatus(HttpStatus.OK.value());
                response.setMessage("Admin created Successfully");
                response.setUsertype(usr.getUserPermission());
                responsecode = HttpStatus.OK;
            } catch (Exception e) {
                response.setStatus(HttpStatus.CONFLICT.value());
                response.setMessage("Email-ID already associated with different Account");
                responsecode = HttpStatus.CONFLICT;

            }
        } catch (Exception e) {
            response.setStatus(HttpStatus.NOT_IMPLEMENTED.value());
            response.setMessage("Email id not valid");
            responsecode = HttpStatus.NOT_IMPLEMENTED;
            logger.trace(String.valueOf(e));

        }

        return new ResponseEntity<>(response, responsecode);
    }


    // User Login
    @PostMapping("/UserLogin")
    @HystrixCommand(fallbackMethod = "UserLogingFallback")
    public ResponseEntity<serverResponse> verifyuser(@Valid @RequestBody Map<String, String> credential) throws Exception {


        serverResponse response = new serverResponse();

        String email = "";
        String password = "";

        if (credential.containsKey("email")) {
            email = credential.get("email");
        }
        if (credential.containsKey("password")) {
            password = credential.get("password");
        }





      User loggedUser = userdao.findByEmailAndPasswordAndUserPermission(email, password, "customer");

        if (loggedUser != null) {
            String jwtToken = jwtutil.createToken(email, password, "customer");
            response.setAuth_TOKEN(jwtToken);
            response.setMessage("SUCCESS");
            response.setStatus(HttpStatus.OK.value());
            response.setUsertype("customer");
        }
        else {
            response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
            response.setMessage("Could Not Find User ");

        }
        return new ResponseEntity<>(response, HttpStatus.OK);
    }


    // Admin Login
    @PostMapping("/AdminLogin")
    @HystrixCommand(fallbackMethod = "adminLogingFallback")
    public ResponseEntity<serverResponse> verifyadmin(@Valid @RequestBody Map<String, String> credential) {
        String email = "";
        String password = "";
        if (credential.containsKey("email")) {
            email = credential.get("email");
        }
        if (credential.containsKey("password")) {
            password = credential.get("password");
        }
        User loggedUser = userdao.findByEmailAndPasswordAndUserPermission(email, password, "admin");
        serverResponse response = new serverResponse();
        if (loggedUser != null) {
            String jwtToken = jwtutil.createToken(email, password, "admin");
            response.setAuth_TOKEN(jwtToken);
            response.setMessage("SUCCESS");
            response.setStatus(HttpStatus.OK.value());
            response.setUsertype("admin");
        } else {
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            response.setMessage("Invalid username or password");
        }
        return new ResponseEntity<>(response, HttpStatus.OK);
    }


    //  Find all user
        @GetMapping("All_User/{id}")
    public ResponseEntity<serverResponse> find_all(@RequestHeader(name = "Authentication") String token, @PathVariable int id) {
        serverResponse response = new serverResponse();

        if (jwtutil.checkToken(token) != null) {
            try {
                User usr = checkToken(token);
                System.out.println(usr.getEmail().equals("") + " : " + jwtutil.isTokenExpired(token));
                if (usr.getEmail().equals("") || jwtutil.isTokenExpired(token))
                    throw new NotFoundException("");

                if (usr.getUserid() == id && usr.getUserPermission().equals("admin")) {
                    List<User> userlst = userdao.findAll();
                    response.setMessage("All data");
                    response.setStatus(HttpStatus.OK.value());
                    response.setUseralldata(userlst);

                } else {
                    response.setMessage("Invalid Token or no valid permission ");
                    response.setStatus(HttpStatus.BAD_REQUEST.value());
                }


            } catch (Exception e) {
                response.setMessage("Token Invalid or token Expired");
                response.setStatus(HttpStatus.UNAUTHORIZED.value());

            }

        } else {
            response.setMessage("no token can't process further");
            response.setStatus(HttpStatus.UNAUTHORIZED.value());

        }


        return new ResponseEntity<>(response, HttpStatus.OK);
    }


    // find User by id
    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    @Cacheable(value = "Userdata", key = "'Userdata'+#id")
    public Optional<User> find_By_id(@PathVariable int id) {
        return userdao.findById(id);
    }


    //  Delete User by id
    @DeleteMapping("/Delete/{id}")
    public ResponseEntity<serverResponse> deleteUser(@RequestHeader(name = "Authentication") String token, @PathVariable int id) {
        serverResponse response = new serverResponse();
        if (jwtutil.checkToken(token) != null) {
            try {
                User usr = checkToken(token);
                if (usr.getEmail().equals("") || jwtutil.isTokenExpired(token))
                    throw new NotFoundException("");

                if (usr.getUserid() == id) {
                    userdao.deleteById(id);
                    response.setMessage("Deleted Successfully");
                    response.setStatus(HttpStatus.OK.value());
                } else {
                    response.setMessage("Invalid Token");
                    response.setStatus(HttpStatus.BAD_REQUEST.value());
                }

            } catch (Exception e) {
                response.setMessage("Token Expired");
                response.setStatus(HttpStatus.UNAUTHORIZED.value());
                logger.trace(String.valueOf(e));

            }

        } else {
            response.setMessage("no token can't process further");
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
        }

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    //    Fall Back Method:-

    public ResponseEntity<serverResponse> userRegistrationFallback(@Valid @RequestBody User Customerincomingdata) {
        serverResponse response = new serverResponse();
        response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
        response.setMessage("Could not Create Account  Right Now ");
        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    public ResponseEntity<serverResponse> adminRegistrationFallback(@Valid @RequestBody User adminincommingdata) {
        serverResponse response = new serverResponse();
        response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
        response.setMessage("Could not Create Account  Right Now ");
        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }


    public ResponseEntity<serverResponse> UserLogingFallback(@Valid @RequestBody Map<String, String> credential) {
        serverResponse response = new serverResponse();
        response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
        response.setMessage("Could not Login  Right Now ");
        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    public ResponseEntity<serverResponse> adminLogingFallback(@Valid @RequestBody Map<String, String> credential) {
        serverResponse response = new serverResponse();
        response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
        response.setMessage("Could not Login  Right Now ");
        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }



}
