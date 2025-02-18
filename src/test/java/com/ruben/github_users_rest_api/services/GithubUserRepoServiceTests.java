package com.ruben.github_users_rest_api.services;

import com.ruben.github_users_rest_api.dto.*;
import com.ruben.github_users_rest_api.producers.GithubReposProducer;
import com.ruben.github_users_rest_api.producers.GithubUserProducer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.IOException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

public class GithubUserRepoServiceTests {

    GithubUserReposService githubUserReposService;
    @Mock
    GithubReposProducer reposProducer;
    @Mock
    GithubUserProducer userProducer;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
        githubUserReposService = new GithubUserReposService(reposProducer, userProducer);
    }

    @Test
    public void TestBadReplyFromConsumers() throws IOException {
        var user = new GithubUserReplyDto();
        var repos = new GithubReposReplyDto();

        when(userProducer.sendAndReceive(any())).thenReturn(user);
        when(reposProducer.sendAndReceive(any())).thenReturn(repos);

        var reply = githubUserReposService.getUserRepos("user");
        assert reply != null;
        assert reply.getRepos() == null;
        assert reply.getUsername() == null;
    }

    @Test
    public void GoodReplyFromConsumers() throws IOException {
        var user = new GithubUserReplyDto();
        var userDto = new GithubUserDto();
        userDto.setLogin("octocatName");
        user.setReply(userDto);
        user.setMetaData(new MetaData());
        var repos = new GithubReposReplyDto();
        var repo = new GithubRepoDto();
        repo.setName("octocat repo");
        repos.setRepos(new GithubRepoDto[]{repo});
        repos.setMetaData(new MetaData());
        when(userProducer.sendAndReceive(any())).thenReturn(user);
        when(reposProducer.sendAndReceive(any())).thenReturn(repos);

        var reply = githubUserReposService.getUserRepos("user");
        assert reply != null;
        assert reply.getRepos() != null;
        assert reply.getUsername() != null;
        assert reply.getRepoMetaData() != null;
        assert reply.getUserMetaData() != null;

        assert reply.getUsername().equals("octocatName");
        assert reply.getRepos().getFirst().getName().equals("octocat repo");
    }
}
