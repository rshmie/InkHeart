package io.inkHeart.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.Objects;

@Entity
@Table(name = "`user`")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    @NotBlank(message = "Email is required!")
    private String email;

    @Column(nullable = false)
    private byte[] srpSalt;

    @Lob
    @Column(nullable = false)
    private byte[] srpVerifier;

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

    public byte[] getSrpSalt() {
        return srpSalt;
    }

    public void setSrpSalt(byte[] srpSalt) {
        this.srpSalt = srpSalt;
    }

    public byte[] getSrpVerifier() {
        return srpVerifier;
    }

    public void setSrpVerifier(byte[] srpVerifier) {
        this.srpVerifier = srpVerifier;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return Objects.equals(id, user.id) &&
                Objects.equals(email, user.email) &&
                Arrays.equals(srpSalt, user.srpSalt) &&
                Arrays.equals(srpVerifier, user.srpVerifier);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(id, email);
        result = 31 * result + Arrays.hashCode(srpSalt);
        result = 31 * result + Arrays.hashCode(srpVerifier);
        return result;
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", email='" + email + '\'' +
                '}';
    }
}
