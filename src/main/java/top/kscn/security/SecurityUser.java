package top.kscn.security;

import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import top.kscn.entity.User;

import java.util.Collection;
import java.util.List;

@Getter
public class SecurityUser implements UserDetails {

    private final User user;
    private final List<GrantedAuthority> authorities;

    public SecurityUser(User user) {
        this.user = user;
        String roleName = user.getRole() == 0 ? "ROLE_ADMIN" : "ROLE_USER";
        this.authorities = List.of(new SimpleGrantedAuthority(roleName));
    }

    public Long getUserId() {
        return user.getUserId();
    }

    public Integer getRole() {
        return user.getRole();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return user.getPassword();
    }

    @Override
    public String getUsername() {
        return user.getUsername();
    }

    @Override
    public boolean isEnabled() {
        return user.getStatus() == 1 && user.getIsDeleted() == 0;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }
}