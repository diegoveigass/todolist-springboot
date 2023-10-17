package com.diegoveiga.todolist.filter;

import java.io.IOException;
import java.util.Base64;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.diegoveiga.todolist.user.IUserRepository;

import at.favre.lib.crypto.bcrypt.BCrypt;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class FilterTaskAuth extends OncePerRequestFilter {

  @Autowired
  private IUserRepository userRepository;

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {

        var servletPath = request.getServletPath();

        if (servletPath.startsWith("/tasks/")) {
          var authorization = request.getHeader("Authorization");

          var authEnconded = authorization.substring("Basic".length()).trim();

          byte[] authDecoded = Base64.getDecoder().decode(authEnconded);

          var authString = new String(authDecoded);

          String[] credentials = authString.split(":");
          String username = credentials[0];
          String password = credentials[1];

          // validate user
          var user = userRepository.findByUsername(username);

          if (user == null) {
            response.sendError(401, "User without authorization");
          } else {

            // validate password
            var passwordVerify = BCrypt.verifyer().verify(password.toCharArray(), user.getPassword());

            if (passwordVerify.verified) {
              request.setAttribute("userId", user.getId());
              filterChain.doFilter(request, response);
            } else {
              response.sendError(401, "User without authorization");
            }
          }
        } else {
          filterChain.doFilter(request, response);
        }


        
  }

 
  
}