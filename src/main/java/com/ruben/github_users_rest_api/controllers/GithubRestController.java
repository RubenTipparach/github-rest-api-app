package com.ruben.github_users_rest_api.controllers;

import com.ruben.github_users_rest_api.dto.GithubReposReplyDto;
import com.ruben.github_users_rest_api.dto.GithubUserReplyDto;
import com.ruben.github_users_rest_api.dto.GithubUserReposDto;
import com.ruben.github_users_rest_api.producers.GithubReposProducer;
import com.ruben.github_users_rest_api.producers.GithubUserProducer;
import com.ruben.github_users_rest_api.services.GithubUserReposService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.val;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.io.IOException;

/**
 * The rest api controller.
 * Includes standalone endpoints for getting github data
 * and an endpoint that merges the two calls.
 */
@RestController
@RequestMapping("/github")
public class GithubRestController {

    private final GithubUserProducer userRequestService;
    private final GithubReposProducer reposRequestService;
    private final GithubUserReposService userReposService;

    public GithubRestController(
            GithubUserProducer userRequestService,
            GithubReposProducer reposRequestService,
            GithubUserReposService userReposService) {
        this.userRequestService = userRequestService;
        this.reposRequestService = reposRequestService;
        this.userReposService = userReposService;
    }

    @Operation(summary = "Get user info from GitHub.", description = "Returns the user data from GitHub.")
    @ApiResponse(responseCode = "200", description = "User data available.")
    @GetMapping("/user/{username}")
    public Mono<GithubUserReplyDto> getUserRepositories(@PathVariable String username) throws IOException {
        val reply = this.userRequestService.sendAndReceive(username);
        return Mono.just(reply);
    }

    @Operation(summary = "Get repositories for this user.", description = "Returns repositories owned by the user.")
    @ApiResponse(responseCode = "200", description = "Successfully found repositories for user.")
    @GetMapping("/repo/{username}")
    public Mono<GithubReposReplyDto> getRepoDetails(@PathVariable String username) throws IOException {
        val reply = this.reposRequestService.sendAndReceive(username);
        return Mono.just(reply);
    }

    @Operation(summary = "Get repositories for this user along with the user info.", description = "Returns repositories owned by the user and info about the user.")
    @ApiResponse(responseCode = "200", description = "Successfully found the user and their repositories.")
    @GetMapping("/user-repos/{username}")
    public Mono<GithubUserReposDto> getUserRepoDetails(@PathVariable String username) throws IOException {
        val reply = this.userReposService.getUserRepos(username);
        return Mono.just(reply);
    }
}