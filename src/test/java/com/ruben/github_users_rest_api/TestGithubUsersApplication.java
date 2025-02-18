package com.ruben.github_users_rest_api;

import org.springframework.boot.SpringApplication;

public class TestGithubUsersApplication {
	public static void main(String[] args) {
		SpringApplication.from(GithubUsersApplication::main).with(TestcontainersConfiguration.class).run(args);
	}
}
