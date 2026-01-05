package com.inguzdev.gitly.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "users")
public class User {

    @Id
    private String id;

    @Indexed(unique = true)
    private String username;

    @Indexed(unique = true)
    private String email;

    private String password;

    private String firstName;

    private String lastName;

    @Builder.Default
    private Set<Role> roles = Set.of(Role.ROLE_VIEWER);

    @Builder.Default
    private Integer urlCreationLimit = 10; // Daily limit for URL creation

    @Builder.Default
    private Integer urlCreatedCount = 0; // Total URLs created by user

    private String apiKey; // API key for programmatic access

    @Builder.Default
    private boolean premiumAccount = false; // Premium account status

    @Builder.Default
    private boolean accountLocked = false;

    // Timestamps
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    private LocalDateTime updatedAt;

    private LocalDateTime lastLoginAt;
}
