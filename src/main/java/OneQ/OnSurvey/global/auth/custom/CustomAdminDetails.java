package OneQ.OnSurvey.global.auth.custom;

import OneQ.OnSurvey.domain.admin.domain.model.Admin;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.Collection;

@RequiredArgsConstructor
public class CustomAdminDetails implements UserDetails, Authenticatable {

    private final Admin admin;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        Collection<GrantedAuthority> collection = new ArrayList<>();
        collection.add(new SimpleGrantedAuthority(admin.getRole().name()));
        return collection;
    }

    @Override
    public String getPassword() {
        return admin.getPassword();
    }

    @Override
    public String getUsername() {
        return admin.getUsername();
    }

    public String getAdminId() {
        return admin.getAdminId();
    }

    public Long getMemberId() {
        return admin.getMemberId();
    }

    public Long getUserKey() {
        return admin.getUserKey();
    }
}
