package accounting;

import java.util.Date;
import java.util.UUID;

import com.intrbiz.accounting.Accounting;
import com.intrbiz.accounting.AccountingManager;
import com.intrbiz.accounting.consumer.AsyncConsumer;
import com.intrbiz.accounting.model.AccountingEvent;

public class AccountingExample
{
    public static class ExampleEvent implements AccountingEvent
    {
        private static final UUID TYPE_ID = UUID.fromString("eb7d4979-768a-4128-a1c2-a6f039005284");
        
        private final Date timestamp;
        
        private final UUID accountId;
        
        private final double value;
        
        public ExampleEvent(Date timestamp, UUID accountId, double value)
        {
            this.timestamp = timestamp;
            this.accountId = accountId;
            this.value = value;
        }
        
        public ExampleEvent(UUID accountId, double value)
        {
            this(new Date(), accountId, value);
        }
        
        @Override
        public UUID getTypeId()
        {
            return TYPE_ID;
        }
        
        @Override
        public Date getTimestamp()
        {
            return this.timestamp;
        }
        
        public UUID getAccountId()
        {
            return this.accountId;
        }
        
        public double getValue()
        {
            return this.value;
        }
        
        public String toString()
        {
            return this.accountId + " => " + this.value;
        }
    }
    
    public static class ExampleSource
    {
        private Accounting accounting = Accounting.create(ExampleSource.class);
        
        public void doStuff()
        {
            this.accounting.account(new ExampleEvent(UUID.randomUUID(), 1));
        }
    }
    
    public static class ExampleConsumer extends AsyncConsumer
    {
        @Override
        protected void processAccountingEvent(AccountingEvent event)
        {
            System.out.println("Account: [" + event.getTypeId() + "] [" + event.getTimestamp() + "] " + event.toString());
        }
    }
    
    public static void main(String[] args) throws Exception
    {
        // setup a consumer
        AccountingManager.getInstance().registerConsumer("example", new ExampleConsumer());
        AccountingManager.getInstance().bindConsumer(ExampleSource.class, "example");
        // sources
        ExampleSource source = new ExampleSource();
        for (int i = 0; i < 100; i++)
        {
            source.doStuff();
        }
        //
        Thread.sleep(5000);
        AccountingManager.getInstance().unregisterConsumer("example");
        System.exit(1);
    }
}
