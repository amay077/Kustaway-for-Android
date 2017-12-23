package info.kustaway.event.model;

import info.kustaway.model.Row;

public class NotificationEvent {

    private final Row mRow;

    public NotificationEvent(final Row row) {
        mRow = row;
    }

    public Row getRow() {
        return mRow;
    }
}
