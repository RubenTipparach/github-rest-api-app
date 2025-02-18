package com.ruben.github_users_rest_api.producers;

import com.ruben.github_users_rest_api.dto.GithubReposReplyDto;
import com.ruben.github_users_rest_api.dto.MetaData;
import com.ruben.github_users_rest_api.AppConfiguration;
import lombok.val;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.io.IOException;

/**
 * As the proile name suggests, this producer creates a request for the consumer
 * to fetch Repo data from github.
 */
@Service
@Profile("rest")
public class GithubReposProducer {
    private Logger logger = LoggerFactory.getLogger(GithubReposProducer.class);

    private final RabbitTemplate rabbitTemplate;

    public GithubReposProducer(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public GithubReposReplyDto sendAndReceive(String username) throws IOException {
        val response = rabbitTemplate.convertSendAndReceive(
                AppConfiguration.EXCHANGE,
                AppConfiguration.ROUTING_KEY_REPOS,
                username);

        if (response != null) {
            val body = (GithubReposReplyDto) response;
            return body;
        }

        val reposDto = new GithubReposReplyDto();
        val metaData = new MetaData();
        reposDto.setMetaData(metaData);
        logger.error("no repos reply received for {}", username);
        reposDto.getMetaData().setError(true);
        return reposDto;
    }
}
