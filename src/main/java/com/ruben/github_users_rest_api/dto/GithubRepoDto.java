package com.ruben.github_users_rest_api.dto;


import lombok.Data;

import java.util.List;

@Data
public class GithubRepoDto {
    private long id;
    private String nodeId;
    private String name;
    private String fullName;
    private boolean isPrivate;
    private Owner owner;
    private String htmlUrl;
    private String description;
    private boolean fork;
    private String url;
    private String createdAt;
    private String updatedAt;
    private String pushedAt;
    private String gitUrl;
    private String sshUrl;
    private String cloneUrl;
    private String svnUrl;
    private String homepage;
    private int size;
    private int stargazersCount;
    private int watchersCount;
    private String language;
    private boolean hasIssues;
    private boolean hasProjects;
    private boolean hasDownloads;
    private boolean hasWiki;
    private boolean hasPages;
    private boolean hasDiscussions;
    private int forksCount;
    private boolean archived;
    private boolean disabled;
    private int openIssuesCount;
    private boolean allowForking;
    private boolean isTemplate;
    private boolean webCommitSignoffRequired;
    private List<String> topics;
    private String visibility;
    private int forks;
    private int openIssues;
    private int watchers;
    private String defaultBranch;
}

