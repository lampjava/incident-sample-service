package org.chiwooplatform.incident.core.support;

import org.springframework.integration.transformer.Transformer;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.support.GenericMessage;
import org.springframework.messaging.support.MessageBuilder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * http://www.programcreek.com/java-api-examples/index.php?api=com.amazonaws.services.s3.model.S3Object
 * @author seonbo.shim
 *
 */
public class S3MessageTransformer
    implements Transformer {

    private final transient Logger logger = LoggerFactory.getLogger( MessageTransformer.class );

    @Override
    public Message<?> transform( Message<?> message ) {
        MessageHeaders headers = message.getHeaders();
        Object payload = message.getPayload();
        logger.debug( "message.getHeaders(): {}", message.getHeaders() );
        logger.debug( "message.getPayload(): {}", payload );
        logger.debug( "transform( Message<?> headers ): {}", headers );
        logger.debug( "transform( Message<?> message ): {}", payload.getClass().getName() );
        if ( payload != null && payload instanceof GenericMessage<?> ) {
            GenericMessage<?> gmsg = (GenericMessage<?>) payload;
            Object ipayload = gmsg.getPayload();
            logger.debug( "gmsg.getPayload(): {}", ipayload );
            if ( ipayload instanceof String ) {
                String str = (String) ipayload;
                return MessageBuilder.withPayload( str.getBytes() ).copyHeadersIfAbsent( gmsg.getHeaders() ).build();
            }
        }
        logger.debug( "transform( Message<?> payload ): {}", payload );
        return message;
    }
}
