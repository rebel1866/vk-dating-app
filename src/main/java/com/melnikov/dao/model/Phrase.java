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
@Document(collection = "phrases")
public class Phrase {
    @Id
    private Integer id;
    private String phraseText;
}
