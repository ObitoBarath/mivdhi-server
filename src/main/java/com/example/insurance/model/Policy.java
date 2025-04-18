package com.example.insurance.model;


import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class Policy  {
    private Long id;
    private String name;
    private String type;
    private Integer premium;
    private Integer coverage;

}
