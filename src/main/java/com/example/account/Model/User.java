package com.example.account.Model;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.validation.constraints.Email;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Data;

@Entity
@Data
@JsonIgnoreProperties(value = "true")
public class User implements Serializable {

	private static final long serialVersionUID = -8850740904859933967L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	public int userid;
	public String name;
	@Column(unique = true)
	@Email(message = "Incorrect email")
	public String email;
	public String password;
	public String userPermission;

}
