/*
 * This Java source file was generated by the Gradle 'init' task.
 */
package tika.test;

import org.apache.tika.Tika;
import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.Parser;
import org.apache.tika.sax.BodyContentHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

public class Library {
    private static final String[] EXECUTABLE_TYPES = {"application/x-msdownload",
            "application/vnd.microsoft.portable-executable", "application/x-ms-installer",
            "application/x-elf", "application/x-sh", "text/x-perl", "text/x-python"};
    private static final Logger LOGGER = LoggerFactory.getLogger(Library.class);
    private final Tika tika = new Tika();

    public boolean isContentSuspicious(String mimeType) throws IOException {
//		boolean flag = (mimeType.startsWith("application/")
//				|| Arrays.stream(EXPLICIT_SCRIPT_TYPES).anyMatch((e) -> mimeType.equals(e)));

        return Arrays.stream(EXECUTABLE_TYPES).anyMatch((e) -> mimeType.startsWith(e));
    }

    public boolean isStreamSuspicious(InputStream is) throws IOException {
        return isContentSuspicious(tika.detect(is));
    }

    public String detectMimeType(InputStream is) throws IOException {
        return tika.detect(is);
    }

    /**
     * Expected PDF or zip
     *
     * @param mimeType
     * @return
     * @throws IOException
     */
    public boolean isContentAllowed(String mimeType) throws IOException {
        return mimeType.equals("application/zip") || mimeType.equals("application/pdf");
    }

    public Metadata extractMetadatatUsingFacade(InputStream stream) throws IOException {
        final Metadata metadata = new Metadata();

        tika.parse(stream, metadata);
        return metadata;
    }

	public static Metadata extractMetadataUsingParser(InputStream stream) throws IOException, SAXException, TikaException {
		Parser parser = new AutoDetectParser();
		ContentHandler handler = new BodyContentHandler();
		Metadata metadata = new Metadata();
		ParseContext context = new ParseContext();

		parser.parse(stream, handler, metadata, context);
		return metadata;
	}

}
