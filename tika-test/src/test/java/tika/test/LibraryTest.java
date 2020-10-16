/*
 * This Java source file was generated by the Gradle 'init' task.
 */
package tika.test;

import com.google.common.collect.ImmutableSet;
import com.google.common.reflect.ClassPath;
import org.hamcrest.CoreMatchers;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.Optional;

public class LibraryTest {

    private static final String[] SUSPICIOS_EXTENSIONS = {".exe", ".dll", ".com", ".bat", ".sh", ".pl", ".js", ".vb",
            ".xlm", ".msi", ".sys", ".rll"};
    private static final Logger LOGGER = LoggerFactory.getLogger(LibraryTest.class);
    private static Library classUnderTest;

    @BeforeClass
    public static void setUpClazz() {
        classUnderTest = new Library();
    }

    @AfterClass
    public static void tearDownClazz() {
        classUnderTest = null;
    }

    private static boolean isExecutableExt(final Path path) {
        final String s = path.toString();
        return Optional.of(s).filter(a -> a.length() > 4).map(a -> a.substring(a.length() - 4)).map(
                a -> Arrays.stream(SUSPICIOS_EXTENSIONS).anyMatch(e -> e.equalsIgnoreCase(a))
        ).filter(b -> b).isPresent();
    }

    private static boolean isAllowedExt(Path path) {
        final String s = path.toString();
        return Optional.of(s).filter(a -> a.length() > 4).map(a -> a.substring(a.length() - 4))
                .filter(b -> b.equalsIgnoreCase(".pdf")
                        || b.equalsIgnoreCase(".zip")
                        || b.equalsIgnoreCase(".jar"))
                .isPresent();
    }

    @Test
    public void testIsContentSuspicious() throws IOException {
        final Path rootPath = Paths.get(System.getProperty("java.home"));
        // Paths.get(System.getenv("WINDIR") + "/system32"); // ;
        final BaseTester tester = new SuspiciousTester();
        walkFiles(rootPath, tester);
        Assert.assertThat("Expected non-zero tested files", tester.getQty(), CoreMatchers.not(0L));
        Assert.assertThat(String.format("Expected 0 - no fails of %d files", tester.getFailQty()), tester.getFailQty(), CoreMatchers.equalTo(0L));
    }

    @Test
    public void testIsContentAllowed() throws IOException {
        final Path rootPath = Paths.get(System.getProperty("java.home"));
        // Paths.get(System.getenv("WINDIR") + "/system32"); // ;
        final BaseTester allowedDetector = new AllowedTester();
        walkFiles(rootPath, allowedDetector);
        Assert.assertThat("Expected non-zero tested files", allowedDetector.getQty(), CoreMatchers.not(0L));
        Assert.assertThat(String.format("Expected 0 - no fails of %d files", allowedDetector.getFailQty()), allowedDetector.getFailQty(), CoreMatchers.equalTo(0L));
    }

    @Test
    public void testIsContentAllowedWhenAllRight() throws IOException {
        final ImmutableSet<ClassPath.ResourceInfo> resources = ClassPath
                .from(getClass().getClassLoader()).getResources();
        final BaseTester allowedDetector = new AllowedTester();
        resources.stream()
                .filter(r -> r.getResourceName().startsWith("files/"))
                .forEach(r -> {
                    try (CloseableStream stream = new CloseableStream(Paths.get(r.url().toURI()),
                            r.asByteSource().openBufferedStream())) {
                        allowedDetector.accept(stream);
                    } catch (final IOException | URISyntaxException iox) {
                        LOGGER.error("FAIL DETECTING CONTENT", iox);
                    }
                });
        Assert.assertThat("Expected non-zero tested files", allowedDetector.getQty(), CoreMatchers.not(0L));
        Assert.assertThat(String.format("Expected 0 - no fails of %d files",
                allowedDetector.getFailQty()), allowedDetector.getFailQty(), CoreMatchers.equalTo(0L));
    }

    private void walkFiles(final Path rootPath, final BaseTester dtr) throws IOException {
        Files.find(rootPath, 0x10, (path, basicFileAttributes) -> {
            return Files.isRegularFile(path) && Files.isReadable(path);
        }, FileVisitOption.FOLLOW_LINKS).forEach((path) -> {
            try (CloseableStream stream = new CloseableStream(path)) {
                dtr.accept(stream);
            } catch (IOException iox) {
                LOGGER.error("FAIL DETECTING CONTENT", iox);
            }
        });
    }

    private interface DetectableResource extends AutoCloseable {
        void close();
    }

    private abstract static class BaseTester {
        protected long failQty;
        protected long qty;

        public abstract void accept(CloseableStream closeableStream) throws IOException;

        long getFailQty() {
            return failQty;
        }

        public long getQty() {
            return qty;
        }

    }

    private static class AllowedTester extends BaseTester {
        @Override
        public void accept(CloseableStream closeableStream) throws IOException {
            // deliberately bypass, because no definite mapping ext -> content-type
            var p = closeableStream.path.toString();
            if (p.endsWith(".dat") || p.endsWith(".sym")) {
                return;
            }
            final String mimeType = classUnderTest.detectMimeType(closeableStream.input);
            final boolean fAllowed = classUnderTest.isContentAllowed(mimeType);
            boolean fExtensionAllowed = isAllowedExt(closeableStream.path);

            final String msg = String.format("%s\t%s\t%s\t%s\t\t%s", closeableStream.path, mimeType, fAllowed,
                    fExtensionAllowed, (fAllowed == fExtensionAllowed) ? "OK" : "FAIL");
            ++qty;
            if (fAllowed == fExtensionAllowed) {
                LOGGER.debug(msg);
            } else {
                ++failQty;
                LOGGER.warn(msg);
            }
        }
    }

    private static class SuspiciousTester extends BaseTester {
        @Override
        public void accept(CloseableStream closeableStream) throws IOException {
            final String mimeType = classUnderTest.detectMimeType(closeableStream.input);
            final boolean fSuspicious = classUnderTest.isContentSuspicious(mimeType);
            boolean fExtensionSuspicious = isExecutableExt(closeableStream.path);

            final String msg = String.format("%s\t%s\t%s\t%s\t\t%s", closeableStream.path, mimeType, fSuspicious,
                    fExtensionSuspicious, (fSuspicious == fExtensionSuspicious) ? "OK" : "FAIL");
            ++qty;
            if (fSuspicious == fExtensionSuspicious) {
                LOGGER.debug(msg);
            } else {
                ++failQty;
                LOGGER.warn(msg);
            }
        }
    }

    private static class CloseableStream implements DetectableResource {

        private final Path path;
        private final InputStream input;

        private CloseableStream(final Path path) throws IOException {
            this.path = path;
            this.input = Files.newInputStream(path, StandardOpenOption.READ);
        }

        private CloseableStream(final Path path, final InputStream stream) throws IOException {
            this.path = path;
            this.input = stream;
        }


        public void close() {
            try {
                if (input != null) {
                    input.close();
                }
            } catch (IOException ioe) {
                LOGGER.error("FAILED closing {}", input, ioe);
            }
        }

        public Path getPath() {
            return path;
        }
    }

}
