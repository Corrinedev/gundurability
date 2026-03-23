package mod.cdv.gdb.network;

import mod.cdv.gdb.GunDurability;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;
import net.minecraftforge.server.ServerLifecycleHooks;

public class NetworkHandler {
    private static final String PROTOCOL_VERSION = "1";
    public static final SimpleChannel INSTANCE = NetworkRegistry.newSimpleChannel(
        ResourceLocation.fromNamespaceAndPath(GunDurability.MODID, "main"),
        () -> PROTOCOL_VERSION,
        PROTOCOL_VERSION::equals,
        PROTOCOL_VERSION::equals
    );

    private static int packetId = 0;
    
    private static int id() {
        return packetId++;
    }

    public static void register() {
        INSTANCE.messageBuilder(SyncGunModifiersPacket.class, id())
            .encoder(SyncGunModifiersPacket::encode)
            .decoder(SyncGunModifiersPacket::decode)
            .consumerMainThread(SyncGunModifiersPacket::handle)
            .add();
        INSTANCE.messageBuilder(SyncJammedPacket.class, id())
                .encoder(SyncJammedPacket::encode)
                .decoder(SyncJammedPacket::decode)
                .consumerMainThread(SyncJammedPacket::handle)
                .add();
        INSTANCE.messageBuilder(StartJamWorkerPacket.class, id())
                .encoder(StartJamWorkerPacket::encode)
                .decoder(StartJamWorkerPacket::decode)
                .consumerMainThread(StartJamWorkerPacket::handle)
                .add();
        INSTANCE.messageBuilder(SyncDamageNBTPacket.class, id())
                .encoder(SyncDamageNBTPacket::encode)
                .decoder(SyncDamageNBTPacket::decode)
                .consumerMainThread(SyncDamageNBTPacket::handle)
                .add();
    }

    public static void sendToClient(Object packet, ServerPlayer player) {
        INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), packet);
    }

    public static void sendToServer(Object packet) {
        INSTANCE.send(PacketDistributor.SERVER.noArg(), packet);
    }

    public static void sendToAllClients(Object packet) {
        INSTANCE.send(PacketDistributor.ALL.noArg(), packet);
    }
}
