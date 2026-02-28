package com.expenses_traker.db_service.Entity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;
import java.time.LocalDate;


@Data
@Table("userDetails")  // Make sure this matches your DB table exactly
public class UserDetailsEntity {
    @Id
    private String id;
    private String firstName;
    private String lastName;
    @Column("date_of_birth")
    private LocalDate dob;
}
