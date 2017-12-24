package net.amay077.kustaway.listener;

import twitter4j.auth.AccessToken;

public interface RemoveAccountListener {
    void removeAccount(AccessToken accessToken);
}
