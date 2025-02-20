package org.example.mollyapi.search.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Table(name = "search")
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Entity
public class Search {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long searchId;

    private String keyword;

    private int count;

    public void increaseCount(){
        this.count += 1;
    }
}
