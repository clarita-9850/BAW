package com.example.kafkaeventdrivenapp.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserAccess {
    private String userId;
    private String userRole;
    private String assignedCounty;
    private List<String> accessibleCounties;
    private boolean hasFullAccess = false;
}
