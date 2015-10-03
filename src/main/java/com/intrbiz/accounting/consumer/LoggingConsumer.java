package com.intrbiz.accounting.consumer;

import org.apache.log4j.Logger;

import com.intrbiz.accounting.model.AccountingEvent;

public class LoggingConsumer extends AsyncConsumer
{
    private Logger logger = Logger.getRootLogger();
    
    public LoggingConsumer()
    {
        super();
    }

    public LoggingConsumer(int queueSize)
    {
        super(queueSize);
    }

    @Override
    protected void processAccountingEvent(Class<?> source, AccountingEvent event)
    {
        logger.info("Accounting [" + source.getCanonicalName() + "] [" + event.getTypeId() + "] [" + event.getTimestamp() + "] " + event.toString());
    }

}
