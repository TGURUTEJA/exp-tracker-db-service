package com.expenses_traker.db_service.Entity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;
import org.springframework.data.relational.core.mapping.Column;

@Data
@Table("categories")
public class CategoryEntity {

    @Id
    private Long id;

    @Column("user_id")
    private String userId;

    @Column("name")
    private String name;

    @Column("type")
    private String type;

    @Column("icon")
    private String icon;

    @Column("color")
    private String color;
}