package net.hyper_pigeon.map_shot;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.hyper_pigeon.map_shot.client.CommonClassClient;
import net.minecraft.client.KeyMapping;

public class MapShotFabricClient implements ClientModInitializer {
    public static KeyMapping OPEN_MAP_SCREENSHOT_SCREEN;
    @Override
    public void onInitializeClient() {
        OPEN_MAP_SCREENSHOT_SCREEN = KeyBindingHelper.registerKeyBinding(CommonClassClient.OPEN_MAP_SCREENSHOT_SCREEN_KEY_MAPPING);
    }
}
