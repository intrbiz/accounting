package com.intrbiz.accounting;

import com.intrbiz.accounting.model.AccountingEvent;

/**
 * Consume accounting events
 */
public interface AccountingConsumer
{    
    /**
     * Process an accounting event
     */
    void account(AccountingEvent event);
    
    // lifecycle
    
    void start();
    
    void stop();
}
