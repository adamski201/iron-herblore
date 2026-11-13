package adamski.ui;

import lombok.Value;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * The release notes in {@code patch-notes.txt}, shown at the top of the panel until closed.
 * <p>
 * One block per release, newest first, blocks separated by a blank line: the version on the first
 * line, then a note per line. Keep notes to about 35 characters - they do not wrap. A release with
 * nothing to say is its version alone. Every release adds a block, and its version must match
 * runelite-plugin.properties ({@code PatchNotesTest} fails the build otherwise).
 */
@Slf4j
public final class PatchNotes {
    static final String RESOURCE = "/adamski/patch-notes.txt";

    /**
     * Enough to read at a glance, for a player several releases behind.
     */
    static final int MAX_NOTES = 6;

    @Value
    static class Release {
        String version;
        List<String> notes;
    }

    private final List<Release> releases;

    PatchNotes(List<Release> releases) {
        this.releases = releases;
    }

    /**
     * Unreadable patch notes show nothing rather than breaking the panel.
     */
    public static PatchNotes load() {
        try (InputStream in = PatchNotes.class.getResourceAsStream(RESOURCE)) {
            if (in == null) {
                log.warn("No patch notes at {}", RESOURCE);
                return new PatchNotes(List.of());
            }

            return new PatchNotes(parse(new String(in.readAllBytes(), StandardCharsets.UTF_8)));
        } catch (IOException | RuntimeException e) {
            log.warn("Could not read the patch notes", e);
            return new PatchNotes(List.of());
        }
    }

    static List<Release> parse(String text) {
        final List<Release> releases = new ArrayList<>();

        String version = null;
        List<String> notes = new ArrayList<>();
        for (String raw : text.split("\\R")) {
            final String line = raw.trim();

            if (line.isEmpty()) {
                if (version != null) releases.add(new Release(version, notes));
                version = null;
                notes = new ArrayList<>();
            } else if (version == null) {
                version = line;
            } else {
                notes.add(line);
            }
        }

        if (version != null) releases.add(new Release(version, notes));
        return releases;
    }

    /**
     * @return the newest release, or null if there is none
     */
    public String latestVersion() {
        return releases.isEmpty() ? null : releases.get(0).getVersion();
    }

    /**
     * The releases newer than the one whose notes were last closed, newest first, holding at most
     * {@link #MAX_NOTES} notes between them, and leaving out releases with none. Nothing closed yet
     * - an existing player's first update, or a new install - is told of the newest release only.
     *
     * @param closedVersion empty if nothing has been closed
     */
    List<Release> releasesSince(String closedVersion) {
        final List<Release> unseen = closedVersion.isEmpty()
                ? releases.stream().limit(1).collect(Collectors.toList())
                : releases.stream()
                        .filter(release -> compareVersions(release.getVersion(), closedVersion) > 0)
                        .collect(Collectors.toList());

        final List<Release> shown = new ArrayList<>();
        int room = MAX_NOTES;
        for (Release release : unseen) {
            if (release.getNotes().isEmpty()) continue;
            if (room <= 0) break;

            final List<String> notes = release.getNotes().subList(0, Math.min(room, release.getNotes().size()));
            shown.add(new Release(release.getVersion(), Collections.unmodifiableList(notes)));
            room -= notes.size();
        }

        return shown;
    }

    /**
     * By number, part by part, so 1.10.0 is newer than 1.9.0. A part that is not a number counts as 0.
     */
    static int compareVersions(String a, String b) {
        final String[] as = a.split("\\.");
        final String[] bs = b.split("\\.");

        for (int i = 0; i < Math.max(as.length, bs.length); i++) {
            final int compared = Integer.compare(partOf(as, i), partOf(bs, i));
            if (compared != 0) return compared;
        }

        return 0;
    }

    private static int partOf(String[] parts, int index) {
        if (index >= parts.length) return 0;

        try {
            return Integer.parseInt(parts[index].trim());
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}
