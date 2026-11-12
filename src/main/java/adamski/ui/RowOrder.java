package adamski.ui;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Where each row sits, remembered between results so rows do not move about. Rows still there keep
 * their places. A row created by a pick goes straight after the row picked on; any other new row
 * goes to its family's place.
 */
final class RowOrder {
    private List<Integer> rootItemIds = new ArrayList<>();

    /**
     * @param incoming           every row's root, least mature first as the domain gives them, which
     *                           sets the order within a family
     * @param pickedOnRootItemId the row whose pick produced this result, or null
     */
    List<Integer> arrange(List<Integer> incoming, Integer pickedOnRootItemId) {
        final Map<Integer, Integer> incomingIndex = new HashMap<>();
        for (int i = 0; i < incoming.size(); i++) {
            incomingIndex.put(incoming.get(i), i);
        }

        final Comparator<Integer> familyOrder = Comparator
                .comparingInt(RowFamilies::familyRankOf)
                .thenComparingInt(incomingIndex::get);

        final List<Integer> arranged = new ArrayList<>(rootItemIds);
        arranged.retainAll(incomingIndex.keySet());

        final List<Integer> added = new ArrayList<>(incoming);
        added.removeAll(rootItemIds);

        if (pickedOnRootItemId != null && arranged.contains(pickedOnRootItemId)) {
            arranged.addAll(arranged.indexOf(pickedOnRootItemId) + 1, added);
        } else {
            for (int rootItemId : added) {
                arranged.add(placeFor(rootItemId, arranged, familyOrder), rootItemId);
            }
        }

        rootItemIds = arranged;

        return List.copyOf(arranged);
    }

    /**
     * Scans from the bottom rather than searching, because rows placed by a pick break family order,
     * and must not pull a new row up ahead of the families it really follows.
     */
    private static int placeFor(int rootItemId, List<Integer> arranged, Comparator<Integer> familyOrder) {
        for (int i = arranged.size() - 1; i >= 0; i--) {
            if (familyOrder.compare(arranged.get(i), rootItemId) < 0) return i + 1;
        }

        return 0;
    }
}
