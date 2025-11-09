package com.igriss.ListIn.user.entity;

import com.igriss.ListIn.security.roles.Role;
import org.junit.jupiter.api.Test;

import java.util.Collection;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class UserTest {

    @Test
    void testGetAuthorities() {
        User user = User.builder()
                .role(Role.ADMIN)
                .build();

        Collection<?> authorities = user.getAuthorities();

        assertNotNull(authorities);
        assertEquals(Role.ADMIN.getAuthorities(), authorities);
    }

    @Test
    void testGetPassword() {
        String password = "secret";
        User user = User.builder()
                .password(password)
                .build();

        assertEquals(password, user.getPassword());
    }

    @Test
    void testGetUsername() {
        String email = "john@example.com";
        User user = User.builder()
                .email(email)
                .build();

        assertEquals(email, user.getUsername());
    }
}
