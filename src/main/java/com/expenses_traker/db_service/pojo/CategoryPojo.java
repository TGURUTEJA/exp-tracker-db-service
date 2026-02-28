package com.expenses_traker.db_service.pojo;

import lombok.Data;

@Data
public class CategoryPojo {
    private Long id;
    private String userId;
    private String name;
    private String type;
    private String icon;
    private String color;
}
