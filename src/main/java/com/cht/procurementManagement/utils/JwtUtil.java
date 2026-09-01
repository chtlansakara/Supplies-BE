package com.cht.procurementManagement.utils;

import com.cht.procurementManagement.entities.User;
import com.cht.procurementManagement.repositories.UserRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

@Component
public class JwtUtil {

//To find the logged in user ------------------------------------------------------------
    //inject User Repository
    private UserRepository userRepository;
    public JwtUtil(UserRepository userRepository) {
        this.userRepository = userRepository;
    }
    //JWT related code ------------------------------------------------------------

    //secret string from environment
    @Value("${jwt.secret}")
    private String jwtSecret;

    //public method
    public String generateToken(UserDetails userDetails){
        return generateToken(new HashMap<>(), userDetails);
    }
    //private method
    //expires the token after 10 days
    private String generateToken(Map<String,Object> extraClaims, UserDetails userDetails){
        return Jwts.builder().setClaims(extraClaims).setSubject(userDetails.getUsername())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis()+1000*60*60*24*10))
                .signWith(getSigningKey(),SignatureAlgorithm.HS256)
                .compact();
    }

    //other required methods
    private Key getSigningKey(){
        byte[] keyBytes = Decoders.BASE64.decode(this.jwtSecret);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    //checking validity
    public boolean isTokenValid(String token, UserDetails userDetails){
        final String userName =  extractUserName(token);
        return (userName.equals(userDetails.getUsername())) && !isTokenExpired(token);
    }

    //other required methods
    public String extractUserName(String token){
        return extractClaim(token, Claims::getSubject);
    }

    private boolean isTokenExpired(String token){
        return extractExpiration(token).before(new Date());
    }

    private Date extractExpiration(String token){
        return extractClaim(token, Claims::getExpiration);
    }
    private <T> T extractClaim(String token, Function<Claims,T> claimsResolvers){
        final Claims claims = extractAllClaims(token);
        return claimsResolvers.apply(claims);
    }

    private Claims extractAllClaims(String token){
        return Jwts.parserBuilder().setSigningKey(getSigningKey()).build().parseClaimsJws(token).getBody();
    }

    //To find the logged in user -----------------------------------------------------------------------
    public User getLoggedInUser(){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if(authentication != null && authentication.isAuthenticated()){
            User user = (User) authentication.getPrincipal();
            //finding who is the user
            Optional<User> optionalUser = userRepository.findById(user.getId());
            //return the User object or null
            return optionalUser.orElse(null);
        }
        //if no user is logged in
        return null;
    }

}
