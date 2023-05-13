package com.melnikov.dao.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;
@Data
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@Document(collection = "names")
public class Name {
    @Id
    private String id;
    private String name;
    private List<String> birthDates;
    private Boolean isUsed;
}
