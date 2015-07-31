package net.trajano.doxdb.sample.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.apache.lucene.store.IOContext;
import org.apache.lucene.store.IndexInput;
import org.apache.lucene.store.IndexOutput;
import org.junit.Test;

import net.trajano.doxdb.search.lucene.JpaDirectory;

/**
 * Tests Lucene bean.
 *
 * @author Archimedes
 */
public class LuceneDirectoryTest extends AbstractBeanTest {

    @Test
    public void testDirectory() throws Exception {

        tx.begin();
        {
            final JpaDirectory dir = new JpaDirectory(em, "searchtable");
            dir.makeLock("ARCHIE");
            final IndexOutput output = dir.createOutput("foo", new IOContext());
            for (int i = 0; i < 10 * 1024 * 1024; ++i) {
                output.writeByte((byte) 32);
            }
            output.close();
            dir.close();
        }
        {
            final JpaDirectory dir = new JpaDirectory(em, "searchtable");
            dir.makeLock("ARCHIE");
            final IndexInput input = dir.openInput("foo", new IOContext());
            assertNotNull(input);
            for (int i = 0; i < 10 * 1024 * 1024; ++i) {
                assertEquals((byte) 32, input.readByte());
            }
            input.close();
            dir.close();
        }

        tx.commit();
    }

}
