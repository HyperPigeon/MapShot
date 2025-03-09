package net.hyper_pigeon.map_shot.client;

import net.minecraft.client.KeyMapping;
import org.lwjgl.glfw.GLFW;

public class CommonClassClient {
    private static final String CATEGORY = "category.map_shot.controls";

    public static final KeyMapping OPEN_MAP_SCREENSHOT_SCREEN_KEY_MAPPING = new KeyMapping("key.map_shot.open_map_screenshot_screen", GLFW.GLFW_KEY_F12, CATEGORY);

}
