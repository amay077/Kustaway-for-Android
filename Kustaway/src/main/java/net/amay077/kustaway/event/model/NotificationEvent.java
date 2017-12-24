package net.amay077.kustaway.event.model;

import net.amay077.kustaway.model.Row;

public class NotificationEvent {

    private final Row mRow;

    public NotificationEvent(final Row row) {
        mRow = row;
    }

    public Row getRow() {
        return mRow;
    }
}
