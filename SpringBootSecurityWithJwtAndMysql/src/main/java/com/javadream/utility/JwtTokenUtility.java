package com.javadream.utility;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.userdetails.UserDetails;

import com.javadream.constant.JwtConstatnt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

@Configuration
public class JwtTokenUtility implements Serializable{

	private static final long serialVersionUID = -9172337479697036292L;
	private static final Logger logger = LoggerFactory.getLogger(JwtTokenUtility.class);
	
	private static final String JWT_SECRET = JwtConstatnt.JWT_SECRET;
	private static final long JWT_TOKEN_VALIDITY = JwtConstatnt.JWT_TOKEN_VALIDITY;
	
	     //get username from jwt token
		public String getUsernameFromToken(String token) {
			return getClaimFromToken(token, Claims::getSubject);
		}

		public <T> T getClaimFromToken(String token, Function<Claims, T> claimsResolver) {
			final Claims claims = getAllClaimsFromToken(token);
			return claimsResolver.apply(claims);
		}
		
		//for get any information from token we will need the secret key
		private Claims getAllClaimsFromToken(String token) {
			return Jwts.parser().setSigningKey(JWT_SECRET).parseClaimsJws(token).getBody();
		}
		
		//check if the token has expired
		private Boolean isTokenExpired(String token) {
			final Date expiration = getExpirationDateFromToken(token);
			return expiration.before(new Date());
		}
		
		//retrieve expiration date from jwt token
		public Date getExpirationDateFromToken(String token) {
			return getClaimFromToken(token, Claims::getExpiration);
		}
		
		//generate token for user
		public String generateToken(UserDetails userDetails) {
			Map<String, Object> claims = new HashMap<>();
			return doGenerateToken(claims, userDetails.getUsername());
		}
		

		private String doGenerateToken(Map<String, Object> claims, String subject) {
			return Jwts.builder().setClaims(claims).setSubject(subject).setIssuedAt(new Date(System.currentTimeMillis()))
					.setExpiration(new Date(System.currentTimeMillis() + JWT_TOKEN_VALIDITY * 1000))
					.signWith(SignatureAlgorithm.HS512, JWT_SECRET).compact();
		}
		
		//validate token
		public Boolean validateToken(String token, UserDetails userDetails) {
			final String username = getUsernameFromToken(token);
			return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
		}
}
