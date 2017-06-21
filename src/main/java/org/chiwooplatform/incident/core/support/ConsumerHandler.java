package org.chiwooplatform.incident.core.support;

import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHandler;
import org.springframework.messaging.MessagingException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConsumerHandler
    implements MessageHandler {

    private final transient Logger logger = LoggerFactory.getLogger( this.getClass() );

    @Override
    public void handleMessage( Message<?> message )
        throws MessagingException {
        logger.debug( "{}", message );
        logger.info( "---------- PROCESSING ALARM DATA and INSERT TO RDS. ----------" );
    }
}
