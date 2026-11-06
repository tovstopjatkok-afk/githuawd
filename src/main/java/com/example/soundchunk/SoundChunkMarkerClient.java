package com.example.soundchunk;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexRendering;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.ChunkPos;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class SoundChunkMarkerClient implements ClientModInitializer {
    private static final long MARK_LIFETIME_MS = 12_000L;
    private static final Map<Long, Long> MARKED_CHUNKS = new ConcurrentHashMap<>();

    @Override
    public void onInitializeClient() {
        WorldRenderEvents.AFTER_ENTITIES.register(context -> {
            if (context.world() == null || context.matrixStack() == null || context.consumers() == null) {
                return;
            }

            long now = System.currentTimeMillis();
            MARKED_CHUNKS.entrySet().removeIf(entry -> entry.getValue() < now);
            if (MARKED_CHUNKS.isEmpty()) return;

            var camera = context.camera();
            var matrices = context.matrixStack();
            VertexConsumer lines = context.consumers().getBuffer(RenderLayer.getLines());
            int bottomY = context.world().getBottomY();
            int topY = bottomY + context.world().getHeight();

            matrices.push();
            matrices.translate(-camera.getPos().x, -camera.getPos().y, -camera.getPos().z);

            for (long packed : MARKED_CHUNKS.keySet()) {
                ChunkPos pos = new ChunkPos(packed);
                double minX = pos.getStartX();
                double minZ = pos.getStartZ();
                Box box = new Box(minX, bottomY, minZ, minX + 16.0, topY, minZ + 16.0);
                VertexRendering.drawBox(matrices, lines, box, 1.0f, 0.05f, 0.05f, 1.0f);
            }

            matrices.pop();
        });
    }

    public static void markChunk(double x, double z) {
        int chunkX = ((int) Math.floor(x)) >> 4;
        int chunkZ = ((int) Math.floor(z)) >> 4;
        long packed = ChunkPos.toLong(chunkX, chunkZ);
        MARKED_CHUNKS.put(packed, System.currentTimeMillis() + MARK_LIFETIME_MS);
    }

    public static boolean isInterestingSound(String soundId) {
        if (soundId == null) return false;
        String path = soundId.toLowerCase();

        if (path.startsWith("entity.player.attack.")) return true;

        return path.startsWith("entity.")
                && !path.startsWith("entity.player.")
                && !path.startsWith("entity.bat.")
                && !path.startsWith("entity.zombie.")
                && !path.startsWith("entity.zombified_piglin.");
    }
}
