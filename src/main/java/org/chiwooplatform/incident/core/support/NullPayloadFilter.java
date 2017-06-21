package org.chiwooplatform.incident.core.support;

import org.springframework.integration.core.GenericSelector;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NullPayloadFilter
    implements GenericSelector<Message<?>> {

    private final transient Logger logger = LoggerFactory.getLogger( this.getClass() );

    @Override
    public boolean accept( Message<?> message ) {
        if ( message.getPayload() == null ) {
            logger.warn( "Payload can not be null." );
            return false;
        }
        MessageHeaders headers = message.getHeaders();
        if ( logger.isDebugEnabled() ) {
            logger.debug( "headers: {}", headers );
        }
        return true;
    }
}
