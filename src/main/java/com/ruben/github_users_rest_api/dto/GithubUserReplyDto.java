package com.ruben.github_users_rest_api.dto;

import lombok.Data;

@Data
public class GithubUserReplyDto {
    private GithubUserDto reply;
    private MetaData metaData;
}
