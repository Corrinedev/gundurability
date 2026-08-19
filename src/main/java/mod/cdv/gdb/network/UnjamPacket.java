package mod.cdv.gdb.network;

import mod.cdv.gdb.DataLookup;
import mod.cdv.gdb.GunDurability;
import mod.cdv.gdb.TimedWork;
import mod.cdv.gdb.Util;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record UnjamPacket() {
    public void encode(FriendlyByteBuf buf) {
    }

    public static UnjamPacket decode(FriendlyByteBuf buf) {
        return new UnjamPacket();
    }

    public void handle(Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            if(context.getSender() == null) return;
            var plr = context.getSender();
            var modifiers = DataLookup.getModifiers(plr.getMainHandItem());
            if(modifiers != null) {
                var item = plr.getMainHandItem();
                if (Util.getOrSetTag(item, "Jammed", false)) {
                    item.getTag().putBoolean("Jammed", false);
                    NetworkHandler.sendToClient(new SyncJammedPacket(false), plr);
                }
            }
        });
        context.setPacketHandled(true);
    }
}
