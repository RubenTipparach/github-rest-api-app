package com.ruben.github_users_rest_api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.Getter;

import java.util.List;

@Data
public class GithubUserReposDto {
    @JsonProperty("user_name")
    private String username;

    @JsonProperty("display_name")
    private String displayName;

    @JsonProperty("avatar")
    private String avatar;

    @JsonProperty("geo_location")
    private String geoLocation;

    @JsonProperty("email")
    private String email;

    @JsonProperty("url")
    private String url;

    @JsonProperty("created_at")
    private String createdAt;

    @JsonProperty("repos")
    private List<Repo> repos;

}
