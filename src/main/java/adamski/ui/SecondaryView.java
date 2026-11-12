package adamski.ui;

import lombok.Value;

import java.util.List;

@Value
class SecondaryView {
    int itemId;

    String name;

    /**
     * Negative is a shortfall, positive is spare.
     */
    double net;

    /**
     * Precursors sharing a name are one line - every kind of bird nest is called "Bird nest".
     */
    List<RowView.Line> breakdown;
}
