package org.teamvoided.transition.mixin;

import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.teamvoided.transition.Transition;
import org.teamvoided.transition.mappings.MappingModes;
import org.teamvoided.transition.mappings.MappingsManager;

@Mixin(ResourceLocation.class)
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
            if (Transition.CONFIG.mode != MappingModes.CONTINUOUS) {
                return;
            }

            MappingsManager.ACTIVE_MAPPINGS.forEach((currentNamespace, mapping) -> {

                if (!oldNamespace.equals(currentNamespace) && mapping.oldNamespaces().contains(oldNamespace)) {
                    this.namespace = currentNamespace;
                }

                if (namespace.equals(currentNamespace) && mapping.oldToNewPaths().containsKey(oldPath)) {
                    this.path = mapping.oldToNewPaths().get(oldPath);
                }
            });
        }
    }
}
