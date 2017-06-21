package org.chiwooplatform.incident.core.config;

import java.io.InputStream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.http.MediaType;
import org.springframework.integration.aws.outbound.S3MessageHandler;
import org.springframework.integration.dsl.channel.MessageChannels;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHandler;

import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.AmazonS3Encryption;
import com.amazonaws.services.s3.AmazonS3EncryptionClientBuilder;
import com.amazonaws.services.s3.model.EncryptionMaterialsProvider;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.transfer.TransferManager;
import com.amazonaws.services.s3.transfer.TransferManagerBuilder;

@Configuration
public class S3Configuration {

    @Value("${aws.s3.bucket}")
    private String bucket;

    @Autowired
    private Regions region;

    @Autowired
    public AWSCredentialsProvider credentialsProvider;

    private static final String S3_SEND_CHANNEL = "s3SendChannel";

    @Bean(name = S3_SEND_CHANNEL)
    public MessageChannel s3SendChannel() {
        return MessageChannels.direct( S3_SEND_CHANNEL ).get();
    }

    @Bean
    public AmazonS3 amazonS3() {
        AmazonS3ClientBuilder builder = AmazonS3ClientBuilder.standard();
        builder.withRegion( region.getName() );
        builder.setCredentials( credentialsProvider );
        final AmazonS3 s3 = builder.build();
        return s3;
    }

    @Bean
    public S3MessageHandler.UploadMetadataProvider uploadMetadataProvider() {
        return new S3MessageHandler.UploadMetadataProvider() {

            @Override
            public void populateMetadata( ObjectMetadata metadata, Message<?> message ) {
                if ( message.getPayload() instanceof InputStream ) {
                    metadata.setContentLength( 1 );
                    metadata.setContentType( MediaType.APPLICATION_JSON_VALUE );
                    /// metadata.setContentDisposition("test.json");
                }
            }
        };
    }

    private final ExpressionParser spelParser = new SpelExpressionParser();

    @Bean
    public S3MessageHandler s3MessageHandler( final AmazonS3 amazonS3 ) {
        final TransferManager transferManager = TransferManagerBuilder.standard().withS3Client( amazonS3 ).build();
        S3MessageHandler messageHandler = new S3MessageHandler( transferManager, bucket );
        Expression keyExpression = spelParser.parseExpression( "payload instanceof T(java.io.File) ? payload.name : headers.get('integration.message.key')" );
        messageHandler.setKeyExpression( keyExpression );
        // messageHandler.setCommandExpression( new ValueExpression<>( Command.UPLOAD )  ); // default is Command.UPLOAD.
        // messageHandler.setUploadMetadataProvider( uploadMetadataProvider() );
        // messageHandler.setCommand( Command.UPLOAD );
        // messageHandler.setOutputChannel( s3SendChannel() );
        // messageHandler.setLoggingEnabled( true );
        // messageHandler.setShouldTrack( true );
        // messageHandler.setCountsEnabled( true );
        // messageHandler.setStatsEnabled( true );
        // messageHandler.setSendTimeout( sendTimeout ); // milliseconds
        // messageHandler.setAsync( true );
        return messageHandler;
    }

    // @Bean
    public AmazonS3Encryption amazonS3Encryption( EncryptionMaterialsProvider encryptionMaterialsProvider ) {
        AmazonS3Encryption amazonS3Encryption = AmazonS3EncryptionClientBuilder.standard()
                                                                               .withRegion( region.getName() )
                                                                               .withEncryptionMaterials( encryptionMaterialsProvider )
                                                                               .build();
        return amazonS3Encryption;
    }

    @Value("${aws.s3.ssebucket:securedBucket}")
    private String sseBucket;

    // @Bean
    public MessageHandler sseS3MessageHandler( AmazonS3 amazonS3 ) {
        final TransferManager transferManager = TransferManagerBuilder.standard().withS3Client( amazonS3 ).build();
        // transferManager.upload( bucketName, key, input, objectMetadata )
        S3MessageHandler s3MessageHandler = new S3MessageHandler( transferManager, sseBucket );
        s3MessageHandler.setUploadMetadataProvider( ( metadata,
                                                      message ) -> metadata.setSSEAlgorithm( ObjectMetadata.AES_256_SERVER_SIDE_ENCRYPTION ) );
        return s3MessageHandler;
    }
    //    
    //    @Bean(name = PollerMetadata.DEFAULT_POLLER)
    //    public PollerMetadata poller() {
    //        return Pollers.fixedRate(5000).get();
    //    }
    //
    //    @Bean
    //    public MessageSource<InputStream> s3InboundStreamingMessageSource() {
    //        S3StreamingMessageSource messageSource = new S3StreamingMessageSource(new S3RemoteFileTemplate(amazonS3()));
    //        messageSource.setRemoteDirectory(bucket);
    //        messageSource.setFilter(new S3SimplePatternFileListFilter(bucketPrefix));
    //        return messageSource;
    //    }
}
