package com.intrbiz.accounting;

import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AccountingManager
{
    private static final AccountingManager US = new AccountingManager();
    
    public static final AccountingManager getInstance()
    {
        return US;
    }
    
    private final AccountingCategory root = new AccountingCategory("");
    
    private final ConcurrentMap<String, AccountingConsumer> consumers = new ConcurrentHashMap<String, AccountingConsumer>();
    
    private final ExecutorService executor = Executors.newCachedThreadPool();
    
    private AccountingManager()
    {
        super();
    }
    
    public AccountingCategory getRootCategory()
    {
        return this.root;
    }
    
    public AccountingCategory getCategory(String sourceName)
    {
        AccountingCategory category = this.getRootCategory();
        if (sourceName != null)
        {
            for (String part : sourceName.split("."))
            {
                category = category.getOrAddChild(part);
            }
        }
        return category;
    }
    
    public AccountingCategory getCategory(Class<?> source)
    {
        return this.getCategory(source == null ? null : source.getCanonicalName());
    }
    
    public void registerConsumer(String name, AccountingConsumer consumer)
    {
        if (this.consumers.putIfAbsent(name, consumer) == null)
        {
            // start the consumer
            consumer.start();
            if (consumer instanceof Runnable)
            {
                this.executor.execute((Runnable) consumer);
            }
        }
    }
    
    public AccountingConsumer getConsumer(String name)
    {
        return this.consumers.get(name);
    }
    
    public Set<String> getRegisteredConsumerNames()
    {
        return new TreeSet<String>(this.consumers.keySet());
    }
    
    public void unregisterConsumer(String name)
    {
        AccountingConsumer consumer = this.consumers.remove(name);
        if (consumer != null)
        {
            // walk the category tree and unbind the consumer
            this.unbindConsumer(this.root, consumer);
            // stop
            consumer.stop();
        }
    }
    
    private void unbindConsumer(AccountingCategory category, AccountingConsumer consumer)
    {
        category.removeConsumer(consumer);
        for (AccountingCategory child : category.getChildren())
        {
            this.unbindConsumer(child, consumer);
        }
    }
    
    /**
     * Bind a consumer to receive accounting events from the given source
     */
    public void bindConsumer(String sourceName, String consumerName)
    {
        AccountingConsumer consumer = this.getConsumer(consumerName);
        if (consumer == null) throw new IllegalArgumentException("No such consumer: " + consumerName);
        AccountingCategory category = this.getCategory(sourceName);
        category.addConsumer(consumer);
    }
    
    public void bindConsumer(Class<?> source, String consumerName)
    {
        this.bindConsumer(source.getCanonicalName(), consumerName);
    }
    
    public void bindRootConsumer(String consumerName)
    {
        this.bindConsumer((String) null, consumerName);
    }
    
    public void unbindConsumer(String sourceName, String consumerName)
    {
        AccountingConsumer consumer = this.getConsumer(consumerName);
        if (consumer == null) throw new IllegalArgumentException("No such consumer: " + consumerName);
        AccountingCategory category = this.getCategory(sourceName);
        category.removeConsumer(consumer);
    }
    
    public void unbindConsumer(Class<?> source, String consumerName)
    {
        this.unbindConsumer(source.getCanonicalName(), consumerName);
    }
    
    public void unbindRootConsumer(Class<?> source, String consumerName)
    {
        this.unbindConsumer((String) null, consumerName);
    }
}
