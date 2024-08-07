package org.teamvoided.transition.api;

import net.minecraft.util.Identifier;
import org.teamvoided.transition.impl.RemappingRegistryImpl;

public interface RemappingRegistry {

    RemappingRegistryImpl INSTANCE = new RemappingRegistryImpl();

    void register(String oldNamespace, String oldPath, Identifier newId);
}
