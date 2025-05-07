package ru.he3hauka.helytraswap.storage;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface Database {
    CompletableFuture<Boolean> getToggleStatus(UUID uuid);
    CompletableFuture<Void> setToggleStatus(UUID uuid, boolean status);
    void close();
}