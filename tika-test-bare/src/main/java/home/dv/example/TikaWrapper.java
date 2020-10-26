package home.dv.example;

import org.apache.tika.Tika;

import java.io.IOException;
import java.io.InputStream;

public class TikaWrapper {

    private final Tika tika;

    public TikaWrapper() {
        tika = new Tika();
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

    /**
     * Try detect content type
     *
     * @param is
     * @return
     * @throws IOException
     */
    public String detectMimeType(InputStream is) throws IOException {
        return tika.detect(is);
    }
}
