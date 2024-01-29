package main.model;

import lombok.Data;

@Data
public class RegistrationRequest {
    private String email = "";
    private String firstname = "";
    private String lastname = "";
    private Long townId = 0L;
    private String password = "";
}
