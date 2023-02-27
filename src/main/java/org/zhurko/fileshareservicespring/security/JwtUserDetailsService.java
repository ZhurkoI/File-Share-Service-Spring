package org.zhurko.fileshareservicespring.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.zhurko.fileshareservicespring.entity.User;
import org.zhurko.fileshareservicespring.security.jwt.JwtUserFactory;
import org.zhurko.fileshareservicespring.service.UserService;

@Service
public class JwtUserDetailsService implements UserDetailsService {

    private final UserService userService;

    @Autowired
    public JwtUserDetailsService(@Lazy UserService userService) {
        this.userService = userService;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        User user = userService.findByUsername(username);

        if (user == null) {
            throw new UsernameNotFoundException("User with '" + username + "' not found");
        }

        return JwtUserFactory.create(user);
    }
}
