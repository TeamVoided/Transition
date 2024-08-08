package org.teamvoided.transition.mixin;

import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.teamvoided.transition.Transition;
import org.teamvoided.transition.mappings.MappingManager;

import java.util.Map;

@Mixin(Identifier.class)
public class IdentifierMixin {

    @Mutable
    @Final
    @Shadow
    private String path;

    @Mutable
    @Final
    @Shadow
    private String namespace;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void init(String oldNamespace, String oldPath, CallbackInfo ci) {
        if (Transition.IS_ACTIVE) {
            MappingManager.ACTIVE_MAPPINGS.forEach((currentNamespace, mapping) -> {
                for (String namespace : mapping.oldNamespaces()) {
                    if (namespace.equals(oldNamespace)) {
                        this.namespace = currentNamespace;
                        break;
                    }
                }
                if (namespace.equals(currentNamespace)) {
                    for (Map.Entry<String, String> entry : mapping.oldToNewPaths().entrySet()) {
                        if (oldPath.equals(entry.getKey())) {
                            this.path = entry.getValue();
                            break;
                        }
                    }
                }
            });
        }
    }
}
