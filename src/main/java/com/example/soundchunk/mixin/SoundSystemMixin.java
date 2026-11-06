package com.example.soundchunk.mixin;

import com.example.soundchunk.SoundChunkMarkerClient;
import net.minecraft.client.sound.SoundInstance;
import net.minecraft.client.sound.SoundSystem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(SoundSystem.class)
public abstract class SoundSystemMixin {
    @Inject(
            method = "play(Lnet/minecraft/client/sound/SoundInstance;)Lnet/minecraft/client/sound/SoundSystem$PlayResult;",
            at = @At("HEAD")
    )
    private void soundchunk$onSoundPlayed(SoundInstance sound, CallbackInfoReturnable<SoundSystem.PlayResult> cir) {
        if (sound == null || sound.isRelative()) return;
        String id = sound.getId().toString();
        if (SoundChunkMarkerClient.isInterestingSound(id)) {
            SoundChunkMarkerClient.markChunk(sound.getX(), sound.getZ());
        }
    }
}
