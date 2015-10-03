package com.intrbiz.accounting;

import com.intrbiz.accounting.model.AccountingEvent;

public class Accounting
{
    private final Class<?> source;
    
    private final AccountingCategory category;
    
    private Accounting(Class<?> source)
    {
        this.source = source;
        this.category = AccountingManager.getInstance().getCategory(source);
    }
    
    Class<?> getSource()
    {
        return this.source;
    }
    
    AccountingCategory getCategory()
    {
        return this.category;
    }
    
    /**
     * Account for something
     */
    public void account(AccountingEvent event)
    {
        this.category.account(this.source, event);
    }
    
    /**
     * Create an accounting information source which can push accounting events into the 
     * accounting system
     */
    public static Accounting create(Class<?> source)
    {
        return new Accounting(source);
    }
}
