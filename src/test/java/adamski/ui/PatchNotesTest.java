package adamski.ui;

import adamski.ui.PatchNotes.Release;
import org.junit.Test;

import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class PatchNotesTest {
    private static final PatchNotes THREE_RELEASES = new PatchNotes(PatchNotes.parse(
            "1.2.0\nThird\n\n1.1.1\n\n1.1.0\nFirst\nSecond\n"));

    /**
     * Otherwise a release goes out showing the last one's notes, or players who closed them see
     * them again.
     */
    @Test
    public void theNewestReleaseIsTheOneBeingReleased() throws IOException {
        final Properties properties = new Properties();
        try (Reader reader = new FileReader("runelite-plugin.properties")) {
            properties.load(reader);
        }

        assertEquals(properties.getProperty("version"), PatchNotes.load().latestVersion());
    }

    @Test
    public void theRealPatchNotesRead() {
        assertTrue(PatchNotes.load().latestVersion() != null);
    }

    @Test
    public void blocksAreReleasesWithTheirNotes() {
        assertEquals(List.of(
                        new Release("1.2.0", List.of("Third")),
                        new Release("1.1.1", List.of()),
                        new Release("1.1.0", List.of("First", "Second"))),
                PatchNotes.parse("1.2.0\nThird\n\n1.1.1\n\n1.1.0\nFirst\nSecond\n"));
    }

    @Test
    public void windowsLineEndingsAndExtraBlankLinesChangeNothing() {
        assertEquals(List.of(new Release("1.2.0", List.of("Third")), new Release("1.1.0", List.of("First"))),
                PatchNotes.parse("\r\n1.2.0\r\nThird\r\n\r\n\r\n1.1.0\r\nFirst"));
    }

    @Test
    public void everyReleaseSinceTheClosedOneIsShownNewestFirst() {
        assertEquals(List.of("1.2.0", "1.1.0"), versionsOf(THREE_RELEASES.releasesSince("1.0.0")));
    }

    @Test
    public void aReleaseWithNoNotesShowsNothing() {
        assertEquals(List.of("1.2.0"), versionsOf(THREE_RELEASES.releasesSince("1.1.0")));
        assertTrue(new PatchNotes(PatchNotes.parse("1.1.1\n")).releasesSince("1.1.0").isEmpty());
    }

    @Test
    public void nothingShowsOnceTheNewestIsClosed() {
        assertTrue(THREE_RELEASES.releasesSince("1.2.0").isEmpty());
    }

    @Test
    public void nothingClosedYetShowsTheNewestReleaseOnly() {
        assertEquals(List.of("1.2.0"), versionsOf(THREE_RELEASES.releasesSince("")));
    }

    @Test
    public void notesStopAtTheCap() {
        final PatchNotes patchNotes = new PatchNotes(PatchNotes.parse("2.0.0\na\nb\nc\nd\n\n1.9.0\ne\nf\ng\nh\n"));

        final List<Release> shown = patchNotes.releasesSince("1.0.0");

        assertEquals(PatchNotes.MAX_NOTES, shown.stream().mapToInt(release -> release.getNotes().size()).sum());
        assertEquals(List.of("e", "f"), shown.get(1).getNotes());
    }

    @Test
    public void versionsCompareByNumber() {
        assertTrue(PatchNotes.compareVersions("1.10.0", "1.9.0") > 0);
        assertTrue(PatchNotes.compareVersions("1.1", "1.1.0") == 0);
        assertTrue(PatchNotes.compareVersions("1.1.1", "1.1.0") > 0);
    }

    @Test
    public void unreadablePatchNotesShowNothing() {
        assertTrue(new PatchNotes(List.of()).releasesSince("").isEmpty());
        assertEquals(null, new PatchNotes(List.of()).latestVersion());
    }

    private static List<String> versionsOf(List<Release> releases) {
        return releases.stream().map(Release::getVersion).collect(Collectors.toList());
    }
}
