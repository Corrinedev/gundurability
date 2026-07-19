package mod.cdv.gdb.network;

import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record SyncJammedPacket(boolean jammed) {
    public void encode(FriendlyByteBuf buf) {
        buf.writeBoolean(jammed);
    }

    public static SyncJammedPacket decode(FriendlyByteBuf buf) {
        return new SyncJammedPacket(buf.readBoolean());
    }

    public void handle(Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            ItemStack item = Minecraft.getInstance().player.getMainHandItem();
            if(item.getTag() != null) {
                item.getTag().putBoolean("Jammed", jammed);
            }
        });
        context.setPacketHandled(true);
    }
}
