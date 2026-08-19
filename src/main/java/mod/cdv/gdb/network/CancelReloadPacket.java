package mod.cdv.gdb.network;

import com.tacz.guns.api.entity.IGunOperator;
import com.tacz.guns.api.entity.ReloadState;
import com.tacz.guns.item.ModernKineticGunItem;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record CancelReloadPacket() {
    public void encode(FriendlyByteBuf buf) {
    }

    public static CancelReloadPacket decode(FriendlyByteBuf buf) {
        return new CancelReloadPacket();
    }

    public void handle(Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            ItemStack item = context.getSender().getMainHandItem();
            if(item.getItem() instanceof ModernKineticGunItem gunItem) {
                IGunOperator.fromLivingEntity(context.getSender()).cancelReload();
                IGunOperator.fromLivingEntity(context.getSender()).getDataHolder().reloadStateType = ReloadState.StateType.NOT_RELOADING;
            }
        });
        context.setPacketHandled(true);
    }
}
