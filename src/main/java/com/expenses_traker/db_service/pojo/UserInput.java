package com.expenses_traker.db_service.pojo;

import lombok.Data;
import java.time.LocalDate;

@Data
public class UserInput {
    private String id;
    private String firstName;
    private String lastName;
    private LocalDate dob;
}

