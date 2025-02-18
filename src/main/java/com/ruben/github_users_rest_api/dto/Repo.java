package com.ruben.github_users_rest_api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Repo {
    private String name;
    private String url;
}