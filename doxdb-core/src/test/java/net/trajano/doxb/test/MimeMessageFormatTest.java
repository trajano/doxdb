package net.trajano.doxb.test;

import java.io.ByteArrayOutputStream;

import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMultipart;
import javax.mail.util.ByteArrayDataSource;
import javax.ws.rs.core.MediaType;

import org.junit.Assert;
import org.junit.Test;

import com.google.common.io.Resources;

public class MimeMessageFormatTest {

    @Test
    public void testMime() throws Exception {

        // Session session = Session.getDefaultInstance(new Properties());
        // Resources.toString(Resources.getResource("sample.xml"),
        // Charsets.UTF_8);

        final MimeMultipart mimeMultipart = new MimeMultipart();
        mimeMultipart.setSubType("mixed");
        mimeMultipart.addBodyPart(new MimeBodyPart(Resources.newInputStreamSupplier(Resources.getResource("sample.xml"))
                .getInput()));
        final MimeBodyPart mimeBodyPart = new MimeBodyPart(Resources.newInputStreamSupplier(Resources.getResource("sample.bin"))
                .getInput());
        mimeBodyPart.setFileName("foo");
        mimeMultipart.addBodyPart(mimeBodyPart);
        // MimeMessage mimeMessage = new MimeMessage(session);
        // mimeMessage.addHeader("Content", "value");
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        mimeMultipart.writeTo(baos);
        baos.close();

        final ByteArrayOutputStream baos2 = new ByteArrayOutputStream();

        final MimeMultipart mimeMultipartr = new MimeMultipart(new ByteArrayDataSource(baos.toByteArray(), MediaType.MULTIPART_FORM_DATA));
        Assert.assertEquals(2, mimeMultipartr.getCount());
        mimeMultipartr.getBodyPart(0)
                .writeTo(baos2);
        Assert.assertTrue(new String(baos2.toByteArray()).startsWith("<?xml"));
    }
}
