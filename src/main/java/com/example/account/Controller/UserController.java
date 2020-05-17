package com.example.account.Controller;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.example.account.Util;
import com.example.account.Dao.UserDaoRepository;
import com.example.account.Model.User;
import com.example.account.Response.serverResponse;
import com.example.account.Validation.SignUpValidation;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;

import javassist.NotFoundException;

@CrossOrigin(origins = "http://localhost:4200")
@RestController
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

	// Check token
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
	public ResponseEntity<serverResponse> adduser(@Valid @RequestBody User Customerincomingdata) {

		HttpStatus responsecode = null;
		serverResponse response = new serverResponse();

		try {
			Boolean checkEmail = signUpValidation.CheckEmail(Customerincomingdata);
			if (!checkEmail)
				throw new NotFoundException("");
			try {

				Customerincomingdata.setUserPermission("User");

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
	public ResponseEntity<serverResponse> addadmin(@Valid @RequestBody User adminincommingdata) {

		serverResponse response = new serverResponse();
		HttpStatus responsecode = null;
		try {
			Boolean checkEmail = signUpValidation.CheckEmail(adminincommingdata);
			if (!checkEmail)
				throw new NotFoundException("");
			try {
				adminincommingdata.setUserPermission("Admin");
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
	public ResponseEntity<serverResponse> verifyuser(@Valid @RequestBody Map<String, String> credential)
			throws Exception {

		serverResponse response = new serverResponse();

		String email = "";
		String password = "";

		if (credential.containsKey("email")) {
			email = credential.get("email");
		}
		if (credential.containsKey("password")) {
			password = credential.get("password");
		}

		User loggedUser = userdao.findByEmailAndPasswordAndUserPermission(email, password, "User");

		if (loggedUser != null) {
			String jwtToken = jwtutil.createToken(email, password, "User");
			response.setAuth_TOKEN(jwtToken);
			response.setMessage("SUCCESS");
			response.setStatus(HttpStatus.OK.value());
			response.setUsertype("User");
		} else {
			response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
			response.setMessage("Could Not Find User ");

		}
		return new ResponseEntity<>(response, HttpStatus.OK);
	}

	// Admin Login
	@PostMapping("/AdminLogin")
	public ResponseEntity<serverResponse> verifyadmin(@Valid @RequestBody Map<String, String> credential) {
		String email = "";
		String password = "";
		if (credential.containsKey("email")) {
			email = credential.get("email");
		}
		if (credential.containsKey("password")) {
			password = credential.get("password");
		}
		User loggedUser = userdao.findByEmailAndPasswordAndUserPermission(email, password, "Admin");
		serverResponse response = new serverResponse();
		if (loggedUser != null) {
			String jwtToken = jwtutil.createToken(email, password, "Admin");
			response.setAuth_TOKEN(jwtToken);
			response.setMessage("SUCCESS");
			response.setStatus(HttpStatus.OK.value());
			response.setUsertype("Admin");
		} else {
			response.setStatus(HttpStatus.UNAUTHORIZED.value());
			response.setMessage("Invalid username or password");
		}
		return new ResponseEntity<>(response, HttpStatus.OK);
	}

	// Find all user
	@GetMapping("/All_User")
	public ResponseEntity<serverResponse> find_all(@RequestHeader(name = "Authorization") String token) {
		serverResponse response = new serverResponse();

		if (jwtutil.checkToken(token) != null) {
			try {
				User usr = checkToken(token);
				System.out.println(usr.getEmail().equals("") + " : " + jwtutil.isTokenExpired(token));
				if (usr.getEmail().equals("") || jwtutil.isTokenExpired(token))
					throw new NotFoundException("");

				if (usr.getUserPermission().equals("Admin")) {
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
	@RequestMapping(value = "get/{id}", method = RequestMethod.GET)
	public Optional<User> find_By_id(@PathVariable int id) {
		return userdao.findById(id);
	}

	// Delete User by id
	@DeleteMapping("/Delete/{id}")
	public ResponseEntity<serverResponse> deleteUser(@RequestHeader(name = "Authorization") String token,
			@PathVariable int id) {
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
					response.setMessage("Invalid Token"+usr.getUserid());
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

}
