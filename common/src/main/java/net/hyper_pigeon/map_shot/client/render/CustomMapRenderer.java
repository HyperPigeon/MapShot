package net.hyper_pigeon.map_shot.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.resources.MapDecorationTextureManager;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.saveddata.maps.MapDecoration;
import net.minecraft.world.level.saveddata.maps.MapDecorationTypes;
import net.minecraft.world.level.saveddata.maps.MapId;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import org.joml.Matrix4f;

import java.util.Iterator;

public class CustomMapRenderer implements AutoCloseable {
    private static final int WIDTH = 128;
    private static final int HEIGHT = 128;
    final TextureManager textureManager;
    final MapDecorationTextureManager decorationTextures;
    private final Int2ObjectMap<CustomMapRenderer.MapInstance> maps = new Int2ObjectOpenHashMap();

    public CustomMapRenderer(TextureManager textureManager, MapDecorationTextureManager decorationTextures) {
        this.textureManager = textureManager;
        this.decorationTextures = decorationTextures;
    }

    public void update(MapId mapId, MapItemSavedData mapData) {
        this.getOrCreateMapInstance(mapId, mapData).forceUpload();
    }

    public void render(PoseStack poseStack, MultiBufferSource buffer, MapId mapId, MapItemSavedData mapData, boolean active, int packedLight) {
        this.getOrCreateMapInstance(mapId, mapData).draw(poseStack, buffer, active, packedLight);
    }

    private CustomMapRenderer.MapInstance getOrCreateMapInstance(MapId mapId, MapItemSavedData mapData) {
        return (CustomMapRenderer.MapInstance)this.maps.compute(mapId.id(), (p_182563_, p_182564_) -> {
            if (p_182564_ == null) {
                return new CustomMapRenderer.MapInstance(p_182563_, mapData);
            } else {
                p_182564_.replaceMapData(mapData);
                return p_182564_;
            }
        });
    }

    public void resetData() {
        ObjectIterator var1 = this.maps.values().iterator();

        while(var1.hasNext()) {
            CustomMapRenderer.MapInstance maprenderer$mapinstance = (CustomMapRenderer.MapInstance)var1.next();
            maprenderer$mapinstance.close();
        }

        this.maps.clear();
    }

    public void close() {
        this.resetData();
    }

    class MapInstance implements AutoCloseable {
        private MapItemSavedData data;
        private final DynamicTexture texture;
        private final RenderType renderType;
        private boolean requiresUpload = true;

        MapInstance(int id, MapItemSavedData data) {
            this.data = data;
            this.texture = new DynamicTexture(128, 128, true);
            ResourceLocation resourcelocation = CustomMapRenderer.this.textureManager.register("map/" + id, this.texture);
            this.renderType = RenderType.text(resourcelocation);
        }

        void replaceMapData(MapItemSavedData data) {
            boolean flag = this.data != data;
            this.data = data;
            this.requiresUpload |= flag;
        }

        public void forceUpload() {
            this.requiresUpload = true;
        }

        private void updateTexture() {
            for(int i = 0; i < 128; ++i) {
                for(int j = 0; j < 128; ++j) {
                    int k = j + i * 128;
                    this.texture.getPixels().setPixelRGBA(j, i, MapColor.getColorFromPackedId(this.data.colors[k]));
                }
            }

            this.texture.upload();
        }

        void draw(PoseStack poseStack, MultiBufferSource bufferSource, boolean active, int packedLight) {
            if (this.requiresUpload) {
                this.updateTexture();
                this.requiresUpload = false;
            }

            Matrix4f matrix4f = poseStack.last().pose();
            VertexConsumer vertexconsumer = bufferSource.getBuffer(this.renderType);
            vertexconsumer.addVertex(matrix4f, 0.0F, 128.0F, -0.01F).setColor(-1).setUv(0.0F, 1.0F).setLight(packedLight);
            vertexconsumer.addVertex(matrix4f, 128.0F, 128.0F, -0.01F).setColor(-1).setUv(1.0F, 1.0F).setLight(packedLight);
            vertexconsumer.addVertex(matrix4f, 128.0F, 0.0F, -0.01F).setColor(-1).setUv(1.0F, 0.0F).setLight(packedLight);
            vertexconsumer.addVertex(matrix4f, 0.0F, 0.0F, -0.01F).setColor(-1).setUv(0.0F, 0.0F).setLight(packedLight);
            int k = 0;
            Iterator var11 = this.data.getDecorations().iterator();

            while(true) {
                MapDecoration mapdecoration;
                do {
                    if (!var11.hasNext()) {
                        return;
                    }

                    mapdecoration = (MapDecoration)var11.next();
                } while(active && !mapdecoration.renderOnFrame());

                boolean isNotPlayerDeco = !(mapdecoration.type().equals(MapDecorationTypes.PLAYER) || mapdecoration.type().equals(MapDecorationTypes.PLAYER_OFF_MAP) || mapdecoration.type().equals(MapDecorationTypes.PLAYER_OFF_LIMITS));
                if(isNotPlayerDeco) {
                    poseStack.pushPose();
                    poseStack.translate(0.0F + (float)mapdecoration.x() / 2.0F + 64.0F, 0.0F + (float)mapdecoration.y() / 2.0F + 64.0F, -0.02F);
                    poseStack.mulPose(Axis.ZP.rotationDegrees((float)(mapdecoration.rot() * 360) / 16.0F));
                    poseStack.scale(4.0F, 4.0F, 3.0F);
                    poseStack.translate(-0.125F, 0.125F, 0.0F);
                    Matrix4f matrix4f1 = poseStack.last().pose();
                    float f1 = -0.001F;
                    TextureAtlasSprite textureatlassprite = CustomMapRenderer.this.decorationTextures.get(mapdecoration);
                    float f2 = textureatlassprite.getU0();
                    float f3 = textureatlassprite.getV0();
                    float f4 = textureatlassprite.getU1();
                    float f5 = textureatlassprite.getV1();
                    VertexConsumer vertexconsumer1 = bufferSource.getBuffer(RenderType.text(textureatlassprite.atlasLocation()));
                    vertexconsumer1.addVertex(matrix4f1, -1.0F, 1.0F, (float)k * -0.001F).setColor(-1).setUv(f2, f3).setLight(packedLight);
                    vertexconsumer1.addVertex(matrix4f1, 1.0F, 1.0F, (float)k * -0.001F).setColor(-1).setUv(f4, f3).setLight(packedLight);
                    vertexconsumer1.addVertex(matrix4f1, 1.0F, -1.0F, (float)k * -0.001F).setColor(-1).setUv(f4, f5).setLight(packedLight);
                    vertexconsumer1.addVertex(matrix4f1, -1.0F, -1.0F, (float)k * -0.001F).setColor(-1).setUv(f2, f5).setLight(packedLight);
                    poseStack.popPose();
                    if (mapdecoration.name().isPresent()) {
                        Font font = Minecraft.getInstance().font;
                        Component component = (Component)mapdecoration.name().get();
                        float f6 = (float)font.width(component);
                        float f7 = Mth.clamp(25.0F / f6, 0.0F, 0.6666667F);
                        poseStack.pushPose();
                        poseStack.translate(0.0F + (float)mapdecoration.x() / 2.0F + 64.0F - f6 * f7 / 2.0F, 0.0F + (float)mapdecoration.y() / 2.0F + 64.0F + 4.0F, -0.025F);
                        poseStack.scale(f7, f7, 1.0F);
                        poseStack.translate(0.0F, 0.0F, -0.1F);
                        font.drawInBatch(component, 0.0F, 0.0F, -1, false, poseStack.last().pose(), bufferSource, Font.DisplayMode.NORMAL, Integer.MIN_VALUE, packedLight);
                        poseStack.popPose();
                    }
                }
                ++k;
            }
        }

        public void close() {
            this.texture.close();
        }
    }
}
