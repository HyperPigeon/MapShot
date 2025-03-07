package net.hyper_pigeon.map_shot;

import net.hyper_pigeon.map_shot.client.CommonClassClient;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;

@EventBusSubscriber(modid = Constants.MOD_ID, value = Dist.CLIENT, bus = EventBusSubscriber.Bus.MOD)
public class MapShotNeoForgeClient {

    @SubscribeEvent
    public void registerBindings(RegisterKeyMappingsEvent event) {
        event.register(CommonClassClient.OPEN_MAP_SCREENSHOT_SCREEN_KEY_MAPPING);
    }
}
