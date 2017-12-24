package net.amay077.kustaway.event.model;

import net.amay077.kustaway.model.Row;

public class StreamingCreateFavoriteEvent {
    private final Row row;

    public StreamingCreateFavoriteEvent(Row row) {
        this.row = row;
    }

    public Row getRow() {
        return row;
    }
}
