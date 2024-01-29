package main.model;

import lombok.Data;

import javax.persistence.*;

@Data
@Entity
@Table(name = "towns")
public class Town {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
}
