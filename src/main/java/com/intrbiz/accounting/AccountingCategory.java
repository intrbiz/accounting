package com.intrbiz.accounting;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.apache.log4j.Logger;

import com.intrbiz.accounting.model.AccountingEvent;

public class AccountingCategory implements Comparable<AccountingCategory>
{
    private AccountingCategory parent;
    
    private final String name;
    
    private ConcurrentMap<String, AccountingCategory> children = new ConcurrentHashMap<String, AccountingCategory>();
    
    private ConcurrentMap<AccountingConsumer, AccountingConsumer> consumers = new ConcurrentHashMap<AccountingConsumer, AccountingConsumer>();
    
    public AccountingCategory(AccountingCategory parent, String name)
    {
        Objects.requireNonNull(parent);
        Objects.requireNonNull(name);
        this.parent = parent;
        this.name = name;
    }
    
    public AccountingCategory(String name)
    {
        Objects.requireNonNull(name);
        this.name = name;
    }
    
    public String getName()
    {
        return this.name;
    }
    
    public String getPath()
    {
        return this.parent == null ? this.name : this.parent.getPath() + "." + this.name;
    }
    
    public AccountingCategory getParent()
    {
        return this.parent;
    }
    
    void setParent(AccountingCategory parent)
    {
        this.parent = parent;
    }
    
    public boolean isRoot()
    {
        return this.parent == null;
    }
    
    void addChild(AccountingCategory child)
    {
        this.children.put(child.getName(), child);
        child.setParent(this);
    }
    
    void removeChild(AccountingCategory child)
    {
        this.children.remove(child.getName());
    }
    
    public AccountingCategory getChild(String name)
    {
        return this.children.get(name);
    }
    
    public AccountingCategory getOrAddChild(String name)
    {
        this.children.computeIfAbsent(name, (k) -> new AccountingCategory(this, k));
        return this.children.get(name);
    }
    
    public Set<AccountingCategory> getChildren()
    {
        return new HashSet<AccountingCategory>(this.children.values());
    }
    
    public Set<AccountingConsumer> getConsumers()
    {
        return new HashSet<AccountingConsumer>(this.consumers.values());
    }
    
    public AccountingConsumer getConsumer(String name)
    {
        return this.consumers.get(name);
    }
    
    public void addConsumer(AccountingConsumer consumer)
    {
        this.consumers.put(consumer, consumer);
    }
    
    public void removeConsumer(AccountingConsumer consumer)
    {
        this.consumers.remove(consumer);
    }
    
    ////////////
    
    public void account(AccountingEvent event)
    {
        if (this.consumers.isEmpty())
        {
            // pass up the chain
            if (this.parent != null) this.parent.account(event);
        }
        else
        {
            // pass to our consumers
            for (AccountingConsumer consumer : this.consumers.values())
            {
                try
                {
                    consumer.account(event);
                }
                catch (Exception e)
                {
                    // log but ignore
                    Logger.getLogger(AccountingCategory.class).error("Error processing accounting [" + this.getPath() + "]", e);
                }
            }
        }
    }
    
    ////////////

    @Override
    public int compareTo(AccountingCategory o)
    {
        return this.name.compareTo(o.name);
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        AccountingCategory other = (AccountingCategory) obj;
        if (name == null)
        {
            if (other.name != null) return false;
        }
        else if (!name.equals(other.name)) return false;
        return true;
    }
}
