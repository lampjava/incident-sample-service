package org.chiwooplatform.incident.core.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.aws.inbound.kinesis.KinesisMessageDrivenChannelAdapter;
import org.springframework.integration.aws.outbound.S3MessageHandler;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.integration.handler.LoggingHandler;

import org.chiwooplatform.incident.core.support.ConsumerHandler;
import org.chiwooplatform.incident.core.support.MessageTransformer;
import org.chiwooplatform.incident.core.support.NullPayloadFilter;
import org.chiwooplatform.incident.core.support.S3MessageTransformer;

@Configuration
public class IntegrationConfiguration {

    @Autowired
    private KinesisMessageDrivenChannelAdapter kinesisChannelAdapter;

    @Autowired
    private S3MessageHandler s3MessageHandler;

    @Bean
    public NullPayloadFilter nullPayloadFilter() {
        return new NullPayloadFilter();
    }

    @Bean
    public ConsumerHandler consumerHandler() {
        return new ConsumerHandler();
    }

    @Bean
    public IntegrationFlow inboundFlow() {
        return IntegrationFlows.from( kinesisChannelAdapter ).filter( nullPayloadFilter() )
                               .publishSubscribeChannel( s -> s.subscribe( f -> f.transform( new MessageTransformer() )
                                                                                 .handle( consumerHandler() ) )
                                                               .subscribe( f -> f.transform( new S3MessageTransformer() )
                                                                                 .handle( s3MessageHandler ) ) )
                               .log( LoggingHandler.Level.INFO, "org.chiwooplatform.incident" ).get();
    }
}
