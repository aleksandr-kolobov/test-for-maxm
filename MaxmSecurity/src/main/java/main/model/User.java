package main.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String email;
    private String firstname;
    private String lastname;
    private String password;
    @ManyToOne
    private Town town;
    @Enumerated(value = EnumType.STRING)
    private Role role;
    @Column(name = "refresh_token")
    private String refreshToken;
    @Column(name = "refresh_expiration")
    private Long refreshExpiration;
}
