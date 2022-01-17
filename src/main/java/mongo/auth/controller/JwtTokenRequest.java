package mongo.auth.controller;

import lombok.Data;


import java.io.Serializable;

@Data
public class JwtTokenRequest implements Serializable 
{

	private static final long serialVersionUID = -5616176897013108345L;

	private String username;
	private String password;

}
