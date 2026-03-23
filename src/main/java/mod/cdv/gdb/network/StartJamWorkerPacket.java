package mod.cdv.gdb.network;

import mod.cdv.gdb.DataLookup;
import mod.cdv.gdb.GunDurability;
import mod.cdv.gdb.TimedWork;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record StartJamWorkerPacket() {
    public void encode(FriendlyByteBuf buf) {
    }

    public static StartJamWorkerPacket decode(FriendlyByteBuf buf) {
        return new StartJamWorkerPacket();
    }

    public void handle(Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            if(context.getSender() == null) return;
            var plr = context.getSender();
            var modifiers = DataLookup.getModifiers(plr.getMainHandItem());
            if(modifiers != null)
                GunDurability.jamWorker.put(plr, new TimedWork<>(modifiers.jamTimeMS(), p -> {
                    var item = p.getMainHandItem();
                    if (item.getTag() != null && item.getTag().contains("Jammed") && item.getTag().getBoolean("Jammed")) {
                        item.getTag().putBoolean("Jammed", false);
                        NetworkHandler.sendToClient(new SyncJammedPacket(false), plr);
                    }
                }));
        });
        context.setPacketHandled(true);
    }
}
