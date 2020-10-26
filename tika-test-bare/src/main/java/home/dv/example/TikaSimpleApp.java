package home.dv.example;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class TikaSimpleApp {
    private int inQty;
    private int okQty;
    private List<String> allowed;

    public static void main(String[] args) throws IOException {
        new TikaSimpleApp().testIsContentAllowedWhenAllRight();
    }

    private void testIsContentAllowedWhenAllRight() throws IOException {
        allowed = new ArrayList<>(8);
        final TikaWrapper tikaWrapper = new TikaWrapper();

        // newer do so: refer to resources directly
        final Path rootPath = Paths.get("./src/main/resources");
        Files.find(rootPath, 0x10, (path, basicFileAttributes) -> {
            return Files.isRegularFile(path) && Files.isReadable(path);
        }, FileVisitOption.FOLLOW_LINKS).forEach((path) -> {
            ++inQty;
            try (InputStream stream = Files.newInputStream(path)) {
                final String mType = tikaWrapper.detectMimeType(stream);
                final boolean contentAllowed = tikaWrapper.isContentAllowed(mType);
                if (contentAllowed) {
                    allowed.add(path.toString());
                }
                ++okQty;
                System.out.printf("%s\t\t%s %n", path, contentAllowed ? "ALLOWED" : "REJECTED");
            } catch (IOException iox) {
                iox.printStackTrace();
            }
        });

        System.out.printf("result:\t%s%n input:\t%d files%n succeeded:\t%d files%n",
                (inQty == okQty) ? "OK" : "FAIL", inQty, okQty);

        final boolean test = allowed.stream().allMatch(a -> a.endsWith(".pdf") || a.endsWith(".zip"));
        System.out.printf("FILTERED %s%n", test ? "OK" : "FAIL");
    }
}
