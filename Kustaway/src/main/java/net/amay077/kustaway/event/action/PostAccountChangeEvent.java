package net.amay077.kustaway.event.action;

public class PostAccountChangeEvent {
    private final long mTabId;

    public PostAccountChangeEvent(long tabId) {
        mTabId = tabId;
    }

    public long getTabId() {
        return mTabId;
    }
}
