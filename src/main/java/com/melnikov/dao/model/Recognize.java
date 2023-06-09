package com.melnikov.dao.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@Document(collection = "recognizes")
public class Recognize {
    @Id
    private Long id;
    private String faceUid;
    private String url;
    private String nameSpace;
}
