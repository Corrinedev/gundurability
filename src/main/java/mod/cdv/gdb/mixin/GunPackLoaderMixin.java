package mod.cdv.gdb.mixin;

import com.tacz.guns.config.PreLoadConfig;
import com.tacz.guns.resource.GunPackLoader;
import mod.cdv.gdb.resource.ResourceLoader;
import net.minecraft.server.packs.repository.Pack;
import net.minecraftforge.fml.loading.FMLPaths;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.io.IOException;
import java.net.URISyntaxException;

@Mixin(value = GunPackLoader.class, remap = false)
public class GunPackLoaderMixin {

    @Inject(method = "discoverExtensions", at = @At(value = "INVOKE", target = "Lcom/tacz/guns/resource/GunPackLoader;scanExtensions(Ljava/nio/file/Path;)Ljava/util/List;"))
    void injectCustomDefault(CallbackInfoReturnable<Pack> cir) {
        if (!PreLoadConfig.override.get()) {
            try {
                ResourceLoader.mergeFolderFromJar("/animations", FMLPaths.GAMEDIR.get().resolve("tacz/tacz_default_gun/assets/tacz/animations/"));
            } catch (IOException | URISyntaxException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
