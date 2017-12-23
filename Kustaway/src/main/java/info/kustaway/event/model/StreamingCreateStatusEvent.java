package info.kustaway.event.model;

import info.kustaway.model.Row;

public class StreamingCreateStatusEvent {
    private final Row row;

    public StreamingCreateStatusEvent(Row row) {
        this.row = row;
    }

    public Row getRow() {
        return row;
    }
}
