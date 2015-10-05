package com.intrbiz.accounting.model;

import java.util.UUID;

public interface AccountingEvent
{    
    /**
     * Get the unique identifier for this event type
     */
    UUID getTypeId();
    
    /**
     * When did this event happen
     */
    long getTimestamp();
}
