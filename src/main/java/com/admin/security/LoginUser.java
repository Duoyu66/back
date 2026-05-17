package com.admin.security;

import com.admin.entity.SysUser;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Getter
public class LoginUser extends User {

    private final SysUser sysUser;
    private final List<String> permCodes;

    public LoginUser(SysUser sysUser, List<String> roleCodes, List<String> permCodes) {
        super(sysUser.getUsername(), sysUser.getPassword(), sysUser.getStatus() == 1,
                true, true, true, buildAuthorities(roleCodes, permCodes));
        this.sysUser = sysUser;
        this.permCodes = permCodes;
    }

    private static Collection<? extends GrantedAuthority> buildAuthorities(
            List<String> roleCodes, List<String> permCodes) {
        Stream<SimpleGrantedAuthority> roles = roleCodes.stream()
                .map(r -> new SimpleGrantedAuthority("ROLE_" + r));
        Stream<SimpleGrantedAuthority> perms = permCodes.stream()
                .map(SimpleGrantedAuthority::new);
        return Stream.concat(roles, perms).collect(Collectors.toList());
    }
}
