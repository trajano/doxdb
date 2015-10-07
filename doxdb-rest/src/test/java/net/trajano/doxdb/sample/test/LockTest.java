package net.trajano.doxdb.sample.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.StringReader;

import javax.json.Json;
import javax.persistence.PersistenceException;

import org.junit.Test;

import net.trajano.doxdb.DoxID;
import net.trajano.doxdb.DoxMeta;

public class LockTest extends AbstractBeanTest {

    @Test
    public void testCreateLockUpdateUnlock() throws Exception {

        final DoxID doxId;
        {
            tx.begin();
            final String inputJson = "{\"email\":\"abc@foryou.com\",\"encodedPassword\":\"abc@foryou.com\",\"name\":\"abc@foryou.com\",\"roles\":[]}";
            final DoxMeta meta = bean.create("user", Json.createReader(new StringReader(inputJson)).readObject());
            doxId = meta.getDoxId();
            assertEquals("abc@foryou.com", bean.read("user", meta.getDoxId()).getContent().getString("name"));

            tx.commit();
        }
        final int lockId;
        {
            tx.begin();
            lockId = bean.lock("user", doxId);
            tx.commit();
        }
        {
            tx.begin();
            assertTrue(bean.isLocked("user", doxId));
            tx.commit();
        }
        {
            tx.begin();
            final String inputJson = "{\"email\":\"abc@foryou.com\",\"encodedPassword\":\"abc@foryou.com\",\"name\":\"abc@foryou.com\",\"roles\":[]}";
            bean.update("user", doxId, Json.createReader(new StringReader(inputJson)).readObject(), 1, lockId);
            tx.commit();
        }
        {
            tx.begin();
            assertTrue(bean.isLocked("user", doxId));
            tx.commit();
        }
        {
            tx.begin();
            bean.unlock("user", doxId, lockId);
            tx.commit();
        }
    }

    @Test(expected = PersistenceException.class)
    public void testFailCreateLockWhenNotLockable() throws Exception {

        final DoxID doxId;
        {
            tx.begin();
            final String inputJson = "{\"name\":\"abc\",\"fei\":\"abc\"}";
            final DoxMeta meta = bean.create("horse", Json.createReader(new StringReader(inputJson)).readObject());

            doxId = meta.getDoxId();
            assertEquals("abc", bean.read("horse", doxId).getContent().getString("name"));
            tx.commit();
        }
        {
            tx.begin();
            bean.lock("horse", doxId);
            tx.commit();
        }
    }

    /**
     * Fail non-locking update.
     *
     * @throws Exception
     */
    @Test(expected = PersistenceException.class)
    public void testFailCreateNoLockUpdate() throws Exception {

        final DoxID doxId;
        {
            tx.begin();
            final String inputJson = "{\"email\":\"abc@foryou.com\",\"encodedPassword\":\"abc@foryou.com\",\"name\":\"abc@foryou.com\",\"roles\":[]}";
            final DoxMeta meta = bean.create("user", Json.createReader(new StringReader(inputJson)).readObject());
            doxId = meta.getDoxId();
            assertEquals("abc@foryou.com", bean.read("user", meta.getDoxId()).getContent().getString("name"));

            tx.commit();
        }
        final int lockId;
        {
            tx.begin();
            lockId = bean.lock("user", doxId);
            tx.commit();
        }
        {
            tx.begin();
            assertTrue(bean.isLocked("user", doxId));
            tx.commit();
        }
        {
            tx.begin();
            final String inputJson = "{\"email\":\"abc@foryou.com\",\"encodedPassword\":\"abc@foryou.com\",\"name\":\"abc@foryou.com\",\"roles\":[]}";
            bean.update("user", doxId, Json.createReader(new StringReader(inputJson)).readObject(), 1);
            tx.commit();
        }
        {
            tx.begin();
            assertTrue(bean.isLocked("user", doxId));
            tx.commit();
        }
        {
            tx.begin();
            bean.unlock("user", doxId, lockId);
            tx.commit();
        }
    }

    @Test(expected = PersistenceException.class)
    public void testFailIsLockedWhenCollectionIsNotLockable() throws Exception {

        final DoxID doxId;
        {
            tx.begin();
            final String inputJson = "{\"name\":\"abc\"}";
            final DoxMeta meta = bean.create("horse", Json.createReader(new StringReader(inputJson)).readObject());

            doxId = meta.getDoxId();
            assertEquals("abc", bean.read("horse", meta.getDoxId()).getContent().getString("name"));

            tx.commit();
        }
        {
            tx.begin();
            bean.lock("horse", doxId);
            tx.commit();
        }
    }
}
