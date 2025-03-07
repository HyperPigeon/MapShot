package net.hyper_pigeon.map_shot;

import net.minecraft.network.chat.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Constants {

	public static final String MOD_ID = "map_shot";
	public static final String MOD_NAME = "MapShot";
	public static final Logger LOG = LoggerFactory.getLogger(MOD_NAME);

	public static final Component GUI_NORMAL_SCREENSHOT = Component.translatable("gui.map_shot.normal_screenshot");
	public static final Component GUI_MAP_SCREENSHOT = Component.translatable("gui.map_shot.map_screenshot");

	public static final Component GUI_INCREASE_SCALE = Component.translatable("gui.map_shot.increase_scale");
	public static final Component GUI_DECREASE_SCALE = Component.translatable("gui.map_shot.decrease_scale");
}