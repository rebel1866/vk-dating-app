package com.melnikov.dao.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@Document(collection = "closed_users")
public class ClosedUser {
    @Id
    private Long id;
    private LocalDateTime checkTime;
}
