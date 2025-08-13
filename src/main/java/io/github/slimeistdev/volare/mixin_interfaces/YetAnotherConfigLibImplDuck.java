package io.github.slimeistdev.volare.mixin_interfaces;

import dev.isxander.yacl3.config.v2.api.ConfigClassHandler;

public interface YetAnotherConfigLibImplDuck {
    void volare$setServerSided(boolean serverSided);
    boolean volare$isServerSided();

    void volare$setHandler(ConfigClassHandler<?> handler);
    ConfigClassHandler<?> volare$getHandler();
}
