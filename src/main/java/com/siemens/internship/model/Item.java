package com.siemens.internship.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Item {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private String name;
    private String description;
    private String status;

    @Pattern(
            regexp = "^[\\w!#$%&*+=.-]+@[\\w.-]+\\.[a-zA-Z]{2,4}$",
            message = "Invalid email"
    ) // [letter|number|character]@[letter|number|underscore|.|-].[letters]
    // we can also use @Email
    private String email;
}