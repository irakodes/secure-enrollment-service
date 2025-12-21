package online.eracodes.secureenrollmentservice.entity;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String email;
    private String name;
    private String registrationCode;
    private String authToken;
    private boolean isEnabled = true;
    private boolean isRegistered;
}
