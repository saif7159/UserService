package com.example.account.Controller;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Logger;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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

@RestController
public class UserController {

	public static Logger logger = Logger.getLogger(UserController.class.getName());

	@Autowired
	public UserDaoRepository userdao;

	@Autowired
	private Util jwtutil;

	@PostMapping("/UserRegister")
	public ResponseEntity<serverResponse> adduser(@RequestBody User user) {
		serverResponse resp = new serverResponse();
		try {
			User usr = userdao.save(user);

			resp.setUsertype(usr.getUserPermission());
			resp.setStatus(HttpStatus.OK.value());
			resp.setMessage("Account Created Successfully");

		} catch (Exception e) {
			resp.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
			resp.setMessage("Failed to create account ");

		}

		return new ResponseEntity<serverResponse>(resp, HttpStatus.OK);
	}

	@PostMapping("/AdminRegister")
	public ResponseEntity<serverResponse> addadmin(@Valid @RequestBody User user) {
		serverResponse resp = new serverResponse();
		try {
			User usr = userdao.saveAndFlush(user);

			resp.setStatus(HttpStatus.OK.value());
			resp.setMessage("Admin created Successfully");
			resp.setUsertype(user.getUserPermission());

		} catch (Exception e) {
			resp.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
			resp.setMessage("Error Creating admin");
		}

		return new ResponseEntity<serverResponse>(resp, HttpStatus.OK);
	}

	@PostMapping("/UserLogin")
	public ResponseEntity<serverResponse> verifyuser(@Valid @RequestBody Map<String, String> credential) {
		String email = "";
		String password = "";
		if (credential.containsKey("email")) {
			email = credential.get("email");
		}
		if (credential.containsKey("password")) {
			password = credential.get("password");
		}
		User loggedUser = userdao.findByEmailAndPasswordAndUserPermission(email, password, "customer");
		serverResponse response = new serverResponse();
		if (loggedUser != null) {
			String jwtToken = jwtutil.createToken(email, password, "customer");
			response.setAuth_TOKEN(jwtToken);
			response.setMessage("SUCCESS");
			response.setStatus(HttpStatus.OK.value());
			response.setUsertype("customer");
		} else {
			response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
			response.setMessage("ERROR");
		}
		return new ResponseEntity<>(response, HttpStatus.OK);
	}

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

	@GetMapping("All_User")
	public List<User> find_all() {
		return userdao.findAll();
	}

	@RequestMapping(value = "/{id}", method = RequestMethod.GET)
	public Optional<User> find_By_id(@PathVariable int id) {
		return userdao.findById(id);
	}

	@DeleteMapping("/Delete")
	public ResponseEntity<serverResponse> deleteUser(@RequestHeader(name = "Authentication") String token,
			@PathVariable int id) {
		serverResponse resp = new serverResponse();
		if (jwtutil.checkToken(token) != null) {
			try {
				User usr = jwtutil.checkToken(token);
				if (usr.userPermission.equals("admin")) {
					userdao.deleteById(usr.getUserid());
					resp.setStatus(HttpStatus.OK.value());
					resp.setMessage("Deleted Successfully");
				} else {
					resp.setStatus(HttpStatus.NOT_MODIFIED.value());
					resp.setMessage("Can't Perform operation no valid permission");
				}
			} catch (Exception e) {
				resp.setStatus(HttpStatus.NOT_FOUND.value());
				resp.setMessage("Can't Perform operation");
			}
		} else {
			resp.setStatus(HttpStatus.UNAUTHORIZED.value());
			resp.setMessage("Unauthorized");
		}
		return new ResponseEntity<serverResponse>(resp, HttpStatus.OK);
	}

}
