package com.alex.springSecurity.security;

import com.google.common.collect.Sets;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Set;
import java.util.stream.Collectors;

import static com.alex.springSecurity.security.ApplicationUserPermission.*;

// Definisco i ruoli e i permessi delle utenze
public enum ApplicationUserRole {
    STUDENT(Sets.newHashSet()), // Nessun permesso per questo ruolo
    ADMIN(Sets.newHashSet(STUDENT_WRITE, STUDENT_READ, COURSE_WRITE, COURSE_READ)),
    ADMINTRAINEE(Sets.newHashSet(STUDENT_READ, COURSE_READ));

    private final Set<ApplicationUserPermission> permissions;

    ApplicationUserRole(Set<ApplicationUserPermission> permissions) {
        this.permissions = permissions;
    }

    public Set<ApplicationUserPermission> getPermissions() {
        return permissions;
    }

    // aggiungo le Authorities al ruolo
    public Set<SimpleGrantedAuthority> getGrantedAuthorities() {

        // trasformo le permission in SimpleGrantedAuthority
        Set<SimpleGrantedAuthority> permissions = getPermissions().stream()
                .map(p -> new SimpleGrantedAuthority(p.getPermission()))
                .collect(Collectors.toSet());

        // aggiungo ROLE_{name} come primo oggetto del SET
        permissions.add(new SimpleGrantedAuthority("ROLE_" + this.name()));

        return permissions;
    }
}
