package com.geekbrains.gwt.common.dtos;

import java.util.Set;

public class UserDTO {
    private Long id;
    private String username;
    private Set<RoleDTO> roles;

    public UserDTO(Long id, String username, Set<RoleDTO> roles) {
        this.id = id;
        this.username = username;
        this.roles = roles;
    }

    public UserDTO() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Set<RoleDTO> getRoles() {
        return roles;
    }

    public void setRoles(Set<RoleDTO> roles) {
        this.roles = roles;
    }
}