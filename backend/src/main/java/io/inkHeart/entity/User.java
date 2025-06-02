package io.inkHeart.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;

import java.util.Objects;

@Entity
@Table(name = "`user`")
public class User {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    @NotBlank(message = "Email is required!")
    private String email;

    @Column(nullable = false)
    @NotBlank(message = "Password is required!")
    private String passwordHash;
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof User user)) return false;
        return Objects.equals(getId(), user.getId()) && Objects.equals(getEmail(), user.getEmail()) && Objects.equals(getPasswordHash(), user.getPasswordHash());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getEmail(), getPasswordHash());
    }

    @Override
    public String toString() {
        return "User{id= " + id +
                "email= " + email +
                "}";
    }
}
