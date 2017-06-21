package org.chiwooplatform.incident.core.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.core.serializer.support.DeserializingConverter;
import org.springframework.integration.aws.inbound.kinesis.KinesisMessageDrivenChannelAdapter;
import org.springframework.integration.aws.inbound.kinesis.KinesisShardOffset;
import org.springframework.integration.aws.inbound.kinesis.ListenerMode;
import org.springframework.integration.dsl.channel.MessageChannels;
import org.springframework.integration.metadata.MetadataStore;
import org.springframework.integration.metadata.SimpleMetadataStore;
import org.springframework.integration.scheduling.PollerMetadata;
import org.springframework.messaging.MessageChannel;
import org.springframework.scheduling.support.PeriodicTrigger;

import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.kinesis.AmazonKinesisAsync;
import com.amazonaws.services.kinesis.AmazonKinesisAsyncClientBuilder;

@Configuration
public class KinesisConfiguration {

    @Value("${aws.kinesis.streamName}")
    private String streamName;

    @Autowired
    private Regions region;

    @Autowired
    public AWSCredentialsProvider credentialsProvider;

    @Bean
    public AmazonKinesisAsync amazonKinesis() {
        AmazonKinesisAsyncClientBuilder builder = AmazonKinesisAsyncClientBuilder.standard();
        builder.setRegion( region.getName() );
        builder.setCredentials( credentialsProvider );
        //    builder.setEndpointConfiguration( endpointConfiguration );
        //    builder.setExecutorFactory( executorFactory );
        //    builder.setRequestHandlers( handlers );
        AmazonKinesisAsync amazonKinesis = builder.build();
        //    amazonKinesis.describeStream( streamName );
        //    amazonKinesis.describeStreamAsync( streamName );
        return amazonKinesis;
    }

    @Bean
    public Converter<byte[], Object> converter() {
        return new Converter<byte[], Object>() {

            private DeserializingConverter converter = new DeserializingConverter();

            @Override
            public Object convert( byte[] source ) {
                return converter.convert( source );
            }
        };
    }

    private static final String KINESIS_CHANNEL = "kinesisChannel";

    @Bean(name = PollerMetadata.DEFAULT_POLLER)
    public PollerMetadata defaultPoller() {
        PollerMetadata pollerMetadata = new PollerMetadata();
        pollerMetadata.setTrigger( new PeriodicTrigger( 10 ) );
        return pollerMetadata;
    }

    @Bean
    public MessageChannel kinesisChannel() {
        return MessageChannels.queue( KINESIS_CHANNEL ).get();
        // return MessageChannels.publishSubscribe( KINESIS_CHANNEL ).get();
    }

    // @Bean
    public MetadataStore checkpointStore() {
        final String checkPointKey = "SpringIntegration" + ":" + streamName + ":" + "1";
        SimpleMetadataStore metadataStore = new SimpleMetadataStore();
        metadataStore.put( checkPointKey, "1" );
        return metadataStore;
    }

    @Bean
    public KinesisMessageDrivenChannelAdapter kinesisChannelAdapter() {
        KinesisMessageDrivenChannelAdapter adapter = new KinesisMessageDrivenChannelAdapter( amazonKinesis(),
                                                                                             streamName );
        adapter.setOutputChannel( kinesisChannel() );
        //         adapter.setStreamInitialSequence( KinesisShardOffset.trimHorizon() ); /*가장 오래된 stream 부터 차례대로*/ 
        adapter.setStreamInitialSequence( KinesisShardOffset.latest() );/* 과거 이벤트는 무시 */
        adapter.setListenerMode( ListenerMode.record );
        // adapter.setCheckpointMode( CheckpointMode.record );
        // adapter.setCheckpointStore( checkpointStore() );
        adapter.setStartTimeout( 10000 );
        adapter.setDescribeStreamRetries( 1 );
        adapter.setConcurrency( 10 );
        adapter.setConverter( converter() );
        //        adapter.setAutoStartup( false );
        //        adapter.setRecordsLimit( 25 );
        //        adapter.setCheckpointStore( checkpointStore );
        //        DirectFieldAccessor dfa = new DirectFieldAccessor( adapter );
        //        dfa.setPropertyValue( "describeStreamBackoff", 10 );
        //        dfa.setPropertyValue( "consumerBackoff", 10 );
        //// dfa.setPropertyValue( "idleBetweenPolls", 1 );
        return adapter;
    }
}
