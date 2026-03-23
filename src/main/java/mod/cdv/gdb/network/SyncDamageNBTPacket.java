package mod.cdv.gdb.network;

import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record SyncDamageNBTPacket(ItemStack stack, int damage) {
    public void encode(FriendlyByteBuf buf) {
        buf.writeItemStack(stack, false);
        buf.writeInt(damage);
    }

    public static SyncDamageNBTPacket decode(FriendlyByteBuf buf) {
        return new SyncDamageNBTPacket(buf.readItem(), buf.readInt());
    }

    public void handle(Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            if(stack.getTag() != null) {
                stack.setDamageValue(damage);
            }
        });
        context.setPacketHandled(true);
    }
}
