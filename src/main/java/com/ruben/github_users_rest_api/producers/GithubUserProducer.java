package com.ruben.github_users_rest_api.producers;

import com.ruben.github_users_rest_api.AppConfiguration;
import com.ruben.github_users_rest_api.dto.GithubUserReplyDto;
import com.ruben.github_users_rest_api.dto.MetaData;
import lombok.val;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.io.IOException;

/**
 * This User producer creates a request to get User data from github.
 */
@Service
@Profile("rest")
public class GithubUserProducer {

    private Logger logger = LoggerFactory.getLogger(GithubUserProducer.class);

    private final RabbitTemplate rabbitTemplate;
    public GithubUserProducer(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    // makes the request to the consumer
    public GithubUserReplyDto sendAndReceive(String username) throws IOException {

        val response = rabbitTemplate.convertSendAndReceive(
                AppConfiguration.EXCHANGE,
                AppConfiguration.ROUTING_KEY_USER,
                username);

        if (response != null) {
            val body = (GithubUserReplyDto) response;
            return body;
        }

        val userDto = new GithubUserReplyDto();
        val metaData = new MetaData();
        userDto.setMetaData(metaData);
        logger.error("no user data reply received for {}", username);
        userDto.getMetaData().setError(true);
        return userDto;
    }
}
