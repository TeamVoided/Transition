package org.teamvoided.transition.impl;

import net.minecraft.util.Identifier;
import org.jetbrains.annotations.ApiStatus;
import org.teamvoided.transition.api.RemappingRegistry;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ApiStatus.Internal
public class RemappingRegistryImpl implements RemappingRegistry {

    private static final Map<String, Map<String, Identifier>> REGISTERED = new HashMap<>();

    @Override
    public void register(String oldNamespace, String oldPath, Identifier newId) {
        Map<String, Identifier> list = new HashMap<>();
        list.put(oldPath, newId);
        if (REGISTERED.put(oldNamespace, new HashMap<>()) != null) {
            throw new IllegalStateException("Duplicate IDs registered for remapping: " + oldNamespace + ":" + oldPath);
        }
    }

    public static Identifier remap(String namespace, String path) {
        if (REGISTERED.containsKey(namespace)) {
            Map<String, Identifier> map = REGISTERED.get(namespace);
            if (map.containsKey(path)) {
                return;
            }
        }
    }
}
