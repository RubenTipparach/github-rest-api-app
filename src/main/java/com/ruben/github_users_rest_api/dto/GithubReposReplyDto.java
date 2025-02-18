package com.ruben.github_users_rest_api.dto;

import lombok.Data;

@Data
public class GithubReposReplyDto {
    private GithubRepoDto[] repos;
    private MetaData metaData;
}
