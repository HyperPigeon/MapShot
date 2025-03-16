package net.hyper_pigeon.map_shot.mixin;

import net.hyper_pigeon.map_shot.client.CommonClassClient;
import net.hyper_pigeon.map_shot.client.render.screen.MapShotScreen;
import net.minecraft.client.KeyboardHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.MapItem;
import net.minecraft.world.level.saveddata.maps.MapId;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(KeyboardHandler.class)
public class KeyboardHandlerMixin {

    @Shadow
    @Final
    private Minecraft minecraft;

    @Inject(method="keyPress", at = @At("TAIL"), cancellable = true)
    public void openMapShotScreen(long windowPointer, int key, int scanCode, int action, int modifiers, CallbackInfo ci){
        if(CommonClassClient.OPEN_MAP_SCREENSHOT_SCREEN_KEY_MAPPING.matches(key,scanCode) && this.minecraft.player != null) {
            ItemStack mapStack = null;
            if(minecraft.player.getMainHandItem().is(Items.FILLED_MAP)){
                mapStack = minecraft.player.getMainHandItem();
            }
            else if(minecraft.player.getOffhandItem().is(Items.FILLED_MAP)){
                mapStack = minecraft.player.getOffhandItem();
            }

            if(mapStack != null) {
                MapItemSavedData mapItemSavedData = MapItem.getSavedData(mapStack, minecraft.level);
                MapId mapId = mapStack.get(DataComponents.MAP_ID);
                minecraft.setScreen(new MapShotScreen(mapId,mapItemSavedData));
                ci.cancel();
            }

        }
    }
}
