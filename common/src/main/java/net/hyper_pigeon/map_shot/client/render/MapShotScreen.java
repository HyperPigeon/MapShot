package net.hyper_pigeon.map_shot.client.render;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.logging.LogUtils;
import net.hyper_pigeon.map_shot.Constants;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.GameNarrator;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.saveddata.maps.MapId;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.io.File;
import java.util.function.Consumer;

public class MapShotScreen extends Screen {

    private Button mapScreenshotButton;

    private Button decreaseScale;
    private Button increaseScale;
    private MapItemSavedData mapItemSavedData;
    private MapId mapId;

    private float scale = 1.0F;

    private static final Logger LOGGER = LogUtils.getLogger();

    public MapShotScreen(MapId mapId, MapItemSavedData mapItemSavedData) {
        super(GameNarrator.NO_TITLE);
        this.mapItemSavedData = mapItemSavedData;
        this.mapId = mapId;
    }

    protected void init() {
        this.mapScreenshotButton = (Button)this.addRenderableWidget(Button.builder(Constants.GUI_MAP_SCREENSHOT, (button) -> {
            this.minecraft.setScreen((Screen)null);
            int originalWidth = 128;
            int originalHeight = 128;

            int newWidth = (int) (originalWidth*this.scale);
            int newHeight = (int) (originalHeight*scale);

            float xRatio = (float) originalWidth /newWidth;
            float yRatio = (float) originalHeight /newHeight;

            NativeImage nativeimage = new NativeImage(newWidth,newHeight, false);
            for (int x = 0; x < newWidth; x++) {
                for (int y = 0; y < newHeight; y++) {
                    int nearestX = (int) Math.round(x * xRatio);
                    int nearestY = (int) Math.round(y * yRatio);

                    nearestX = Math.min(nearestX, originalWidth - 1);
                    nearestY = Math.min(nearestY, originalHeight - 1);

                    int k = nearestX + nearestY * 128;
                    nativeimage.setPixelRGBA(y, x, MapColor.getColorFromPackedId(this.mapItemSavedData.colors[k]));
                }
            }

            _grab(nativeimage, this.minecraft.gameDirectory, null, (component) -> {
                this.minecraft.execute(() -> this.minecraft.gui.getChat().addMessage(component));
            });
        }).bounds(this.width / 2 - 75, 175, 150, 30).build());

        this.decreaseScale = (Button)this.addRenderableWidget(Button.builder(Constants.GUI_DECREASE_SCALE, (button) -> {
            this.scale -= 1.0F;
            this.scale = Math.clamp(this.scale, 1.0f, 10F);
        }).bounds(this.width / 2 - 25, 215, 50, 20).build());

        this.increaseScale = (Button)this.addRenderableWidget(Button.builder(Constants.GUI_INCREASE_SCALE, (button) -> {
            this.scale += 1.0F;
            this.scale = Math.clamp(this.scale, 1.0f, 10F);
        }).bounds(this.width / 2 + 25, 215, 50, 20).build());
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        drawMap(guiGraphics, mapId, mapItemSavedData, (this.width/2) - 80,(this.height/20),1.2F);
        guiGraphics.drawString(minecraft.font, Component.translatable("gui.map_shot.scale", scale), this.width / 2 - 90, 222, 0xFFFFFFFF);
    }

    private static File getFile(File gameDirectory) {
        String s = Util.getFilenameFormattedDateTime();
        int i = 1;

        while(true) {
            File file1 = new File(gameDirectory, s + (i == 1 ? "" : "_" + i) + ".png");
            if (!file1.exists()) {
                return file1;
            }

            ++i;
        }
    }

    private static void _grab(NativeImage nativeImage, File gameDirectory, @Nullable String screenshotName, Consumer<Component> messageConsumer) {
        File file1 = new File(gameDirectory, "screenshots");
        file1.mkdir();
        File file2;
        if (screenshotName == null) {
            file2 = getFile(file1);
        } else {
            file2 = new File(file1, screenshotName);
        }

        Util.ioPool()
                .execute(
                        () -> {
                            try {
                                nativeImage.writeToFile(file2);
                                Component component = Component.literal(file2.getName())
                                        .withStyle(ChatFormatting.UNDERLINE)
                                        .withStyle(p_168608_ -> p_168608_.withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_FILE, file2.getAbsolutePath())));
                                messageConsumer.accept(Component.translatable("screenshot.success", component));
                            } catch (Exception exception) {
                                LOGGER.warn("Couldn't save screenshot", (Throwable)exception);
                                messageConsumer.accept(Component.translatable("screenshot.failure", exception.getMessage()));
                            } finally {
                                nativeImage.close();
                            }
                        }
                );
    }

    private void drawMap(GuiGraphics guiGraphics, @Nullable MapId mapIdComponent, @Nullable MapItemSavedData mapItemSavedData, int x, int y, float scale) {
        if (mapIdComponent != null && mapItemSavedData != null) {
            guiGraphics.pose().pushPose();
            guiGraphics.pose().translate((float)x, (float)y, 0.0F);
            guiGraphics.pose().scale(scale, scale, -1.0F);
            this.minecraft.gameRenderer.getMapRenderer().render(guiGraphics.pose(), guiGraphics.bufferSource(), mapIdComponent, mapItemSavedData, false, 15728880);
            guiGraphics.flush();
            guiGraphics.pose().popPose();
        }
    }



}
