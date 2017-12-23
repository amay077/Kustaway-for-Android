package info.kustaway.event.model;

import info.kustaway.model.Row;

public class StreamingCreateFavoriteEvent {
    private final Row row;

    public StreamingCreateFavoriteEvent(Row row) {
        this.row = row;
    }

    public Row getRow() {
        return row;
    }
}
