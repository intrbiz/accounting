package com.intrbiz.accounting.consumer;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

import com.intrbiz.accounting.AccountingConsumer;
import com.intrbiz.accounting.model.AccountingEvent;

public abstract class AsyncConsumer implements Runnable, AccountingConsumer
{
    private final BlockingQueue<QueuedAccountingEvent> queue;
    
    private volatile boolean run = false;
    
    public AsyncConsumer(int queueSize)
    {
        this.queue = new ArrayBlockingQueue<QueuedAccountingEvent>(queueSize);
    }
    
    public AsyncConsumer()
    {
        this(8192);
    }

    @Override
    public void account(Class<?> source, AccountingEvent event)
    {
        try
        {
            this.queue.put(new QueuedAccountingEvent(source, event));
        }
        catch (InterruptedException e)
        {
        }
    }
    
    @Override
    public void start()
    {
        this.run = true;
    }

    @Override
    public void run()
    {
        QueuedAccountingEvent event;
        while (this.run)
        {
            try
            {
                event = this.queue.poll(5, TimeUnit.SECONDS);
                if (event != null)
                {
                    try
                    {
                        this.processAccountingEvent(event.getSource(), event.getEvent());
                    }
                    catch (Exception e)
                    {
                        Logger.getLogger(this.getClass()).error("Error processing accounting event, requeuing");
                        this.queue.put(event);
                    }
                }
            }
            catch (InterruptedException e)
            {
                // ignore
            }
        }
    }
    
    public void stop()
    {
        this.run = false;
    }
    
    protected abstract void processAccountingEvent(Class<?> source, AccountingEvent event);
    
    /**
     * A queue event with additional metadata
     */
    public static final class QueuedAccountingEvent
    {
        private final Class<?> source;
        
        private final AccountingEvent event;
        
        public QueuedAccountingEvent(Class<?> source, AccountingEvent event)
        {
            this.source = source;
            this.event = event;
        }

        public Class<?> getSource()
        {
            return source;
        }

        public AccountingEvent getEvent()
        {
            return event;
        }
    }
}
