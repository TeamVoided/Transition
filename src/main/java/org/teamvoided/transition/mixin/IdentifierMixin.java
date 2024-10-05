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

import static org.teamvoided.transition.Transition.LOGGER;
import static org.teamvoided.transition.Transition.MINECRAFT;

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
        if (Transition.IS_ACTIVE && !oldNamespace.equals(MINECRAFT)) {
            if (Transition.CONFIG.mode != MappingModes.CONTINUOUS) {
                return;
            }

            var mappings = MappingsManager.ACTIVE_MAPPINGS.get(oldNamespace);
            var newNamespace = mappings.getFirst().getMetadata().getId();
            var newPath = mappings.getSecond().mappings().get(oldNamespace).get(oldPath);
            this.namespace = newNamespace;
            if (newPath != null) this.path = newPath;

            LOGGER.info("Changed id to {}:{}", newNamespace, newPath);
        }
    }
}
