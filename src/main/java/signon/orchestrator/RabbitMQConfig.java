// Copyright 2021-2023 FINCONS GROUP AG within the Horizon 2020
// European project SignON under grant agreement no. 101017255.

// Licensed under the Apache License, Version 2.0 (the "License"); 
// you may not use this file except in compliance with the License. 
// You may obtain a copy of the License at 

//     http://www.apache.org/licenses/LICENSE-2.0 

// Unless required by applicable law or agreed to in writing, software 
// distributed under the License is distributed on an "AS IS" BASIS, 
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
// See the License for the specific language governing permissions and 
// limitations under the License.

package signon.orchestrator;

import javax.annotation.PostConstruct;

import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    @Value("${rabbitmq.reply.timeout}")
    private int replyTimeout;

    @Value("${rabbitmq.rpc-exchange}")
    private String rpcExchange;

    @Value("${rabbitmq.rpc-queue}")
    private String rpcQueue;

    @Value("${rabbitmq.rpc-routing-key}")
    private String rpcRoutingKey;

    @Value("${rabbitmq.wp4-exchange}")
    private String wp4Exchange;

    @Value("${rabbitmq.wp4-queue}")
    private String wp4Queue;

    @Value("${rabbitmq.wp4-routing-key}")
    private String wp4RoutingKey;

    @Value("${rabbitmq.wp5-exchange}")
    private String wp5Exchange;

    @Value("${rabbitmq.wp5-queue}")
    private String wp5Queue;

    @Value("${rabbitmq.wp5-routing-key}")
    private String wp5RoutingKey;

    @Value("${rabbitmq.username}")
    private String username;

    @Value("${rabbitmq.password}")
    private String password;

    @Value("${rabbitmq.virtualhost}")
    private String virtualhost;

    @Value("${rabbitmq.hostname}")
    private String hostname;

    @Value("${rabbitmq.port}")
    private int port;

    @Bean
    public DirectExchange rpcExchange() {
        return new DirectExchange(rpcExchange, false, false);
    }

    @Bean
    public Queue rpcQueue() {
        return new Queue(rpcQueue);
    }

    @Bean
    public Binding rpcBinding() {
        return BindingBuilder.bind(rpcQueue())
            .to(rpcExchange())
            .with(rpcRoutingKey);
    }

    @Bean
    public DirectExchange wp4Exchange() {
        return new DirectExchange(wp4Exchange, false, false);
    }

    @Bean
    public Queue wp4Queue() {
        return new Queue(wp4Queue);
    }

    @Bean
    public Binding wp4Binding() {
        return BindingBuilder.bind(wp4Queue())
            .to(wp4Exchange())
            .with(wp4RoutingKey);
    }

    @Bean
    public DirectExchange wp5Exchange() {
        return new DirectExchange(wp5Exchange, false, false);
    }

    @Bean
    public Queue wp5Queue() {
        return new Queue(wp5Queue);
    }

    @Bean
    public Binding wp5Binding() {
        return BindingBuilder.bind(wp5Queue())
            .to(wp5Exchange())
            .with(wp5RoutingKey);
    }

    @Bean
    public ConnectionFactory connectionFactory() {
        CachingConnectionFactory connectionFactory = new CachingConnectionFactory();
        connectionFactory.setUsername(username);
        connectionFactory.setPassword(password);
        connectionFactory.setVirtualHost(virtualhost);
        connectionFactory.setHost(hostname);
        connectionFactory.setPort(port);

        return connectionFactory;
    }

    @Bean
    public RabbitTemplate rabbitTemplate() {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory());
        rabbitTemplate.setMessageConverter(producerJackson2MessageConverter());
        rabbitTemplate.setReplyTimeout(replyTimeout);
        return rabbitTemplate;
    }

    @Bean
    public Jackson2JsonMessageConverter producerJackson2MessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public AmqpAdmin amqpAdmin() {
        return new RabbitAdmin(connectionFactory());
    }

    @PostConstruct
    public void afterInit() {
        AmqpAdmin amqpAdmin = amqpAdmin();

        amqpAdmin.declareQueue(rpcQueue());
        amqpAdmin.declareExchange(rpcExchange());
        amqpAdmin.declareBinding(rpcBinding());

        amqpAdmin.declareQueue(wp4Queue());
        amqpAdmin.declareExchange(wp4Exchange());
        amqpAdmin.declareBinding(wp4Binding());

        amqpAdmin.declareQueue(wp5Queue());
        amqpAdmin.declareExchange(wp5Exchange());
        amqpAdmin.declareBinding(wp5Binding());
    }

}
