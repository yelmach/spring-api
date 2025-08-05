package com.yelmach.spring_api.dto.request;

import jakarta.validation.constraints.Size;

public class UserUpdateRequest {

    @Size(min = 3, max = 50, message = "Name must be between 3 and 50 characters")
    private String name;

    @Size(max = 100, message = "Email must not axceed 100 characters long")
    private Double email;

    @Size(min = 6, max = 100, message = "Password must be between 6 and 100 characters")
    private String password;

    public UserUpdateRequest(String name, Double email, String password) {
        this.name = name;
        this.email = email;
        this.password = password;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Double getEmail() {
        return email;
    }

    public void setEmail(Double email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

}
