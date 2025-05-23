package master.master.web.rest.dto;

import master.master.domain.RoleType;

public class UserResponseDto {
    private final Integer id;
    private final String firstName;
    private final String lastName;
    private final String email;
    private final RoleType role;

    public UserResponseDto(Integer id, String firstName, String lastName, String email, RoleType role) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.role = role;
    }

    public Integer getId() {
        return id;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getEmail() {
        return email;
    }

    public RoleType getRole() {
        return role;
    }
}
