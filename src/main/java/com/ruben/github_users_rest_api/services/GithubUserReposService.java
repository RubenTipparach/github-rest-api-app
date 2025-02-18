package com.ruben.github_users_rest_api.services;

import com.ruben.github_users_rest_api.dto.GithubUserReposDto;
import com.ruben.github_users_rest_api.dto.Repo;
import com.ruben.github_users_rest_api.producers.GithubReposProducer;
import com.ruben.github_users_rest_api.producers.GithubUserProducer;
import lombok.val;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Arrays;

/***
 * This service combines the producers for Users and Repos. It combines the data and formats it according to specifications.
 */
@Service
public class GithubUserReposService {

    private final GithubReposProducer reposProducer;
    private final GithubUserProducer userProducer;

    public GithubUserReposService(GithubReposProducer reposProducer,
                                  GithubUserProducer userProducer) {
        this.reposProducer = reposProducer;
        this.userProducer = userProducer;
    }

    // Maps the data according to requirements
    public GithubUserReposDto getUserRepos(String username) throws IOException {
        val userReply = this.userProducer.sendAndReceive(username);
        val reposReply = this.reposProducer.sendAndReceive(username);

        // combine users and repos together.
        val response = new GithubUserReposDto();
        if (userReply.getReply() != null) {
            val user = userReply.getReply();
            response.setUsername(user.getLogin());
            response.setDisplayName(user.getName());
            response.setAvatar(user.getAvatarUrl());
            response.setGeoLocation(user.getLocation());
            response.setUrl(user.getUrl());
            response.setCreatedAt(user.getCreatedAt());
        }

        if (reposReply.getRepos() != null) {
            val repos = reposReply.getRepos();
            response.setRepos(
                    Arrays.stream(repos)
                            .map(repo -> new Repo(repo.getName(), repo.getUrl())).toList()
            );
        }

        return response;
    }
}
