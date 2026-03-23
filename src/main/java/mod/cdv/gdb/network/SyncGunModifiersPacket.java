package mod.cdv.gdb.network;

import com.mojang.datafixers.util.Either;
import com.tacz.guns.api.GunProperty;
import com.tacz.guns.api.item.GunTabType;
import com.tacz.guns.api.item.attachment.AttachmentType;
import mod.cdv.gdb.DataLookup;
import mod.cdv.gdb.resource.GunModifier;
import mod.cdv.gdb.resource.StatModifier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.network.NetworkEvent;

import java.util.ArrayList;
import java.util.function.Supplier;

public record SyncGunModifiersPacket(ArrayList<GunModifier> modifiers) {

    public void encode(FriendlyByteBuf buf) {
        buf.writeInt(modifiers.size());

        for (GunModifier modifier : modifiers) {
            //enabled
            buf.writeBoolean(modifier.enabled());
            // Write StatModifier array
            buf.writeInt(modifier.modifiers().length);
            for (StatModifier statMod : modifier.modifiers()) {
                encodeStatModifier(buf, statMod);
            }

            // Write repair item (Ingredient)
            modifier.repairItem().toNetwork(buf);
            // Write Either<GunTabType, String> target
            encodeTarget(buf, modifier.target());
            // Write max durability
            buf.writeInt(modifier.maxDurability());
            // repair cost
            buf.writeInt(modifier.repairCost());
            buf.writeFloat(modifier.xpCost());
            //preventFiring
            buf.writeBoolean(modifier.preventFiring());
            //jam
            buf.writeBoolean(modifier.jam());
            buf.writeFloat(modifier.jamChance());
            buf.writeFloat(modifier.jamThreshold());
            buf.writeInt(modifier.jamTimeMS());
        }
    }

    public static SyncGunModifiersPacket decode(FriendlyByteBuf buf) {
        int size = buf.readInt();
        ArrayList<GunModifier> modifiers = new ArrayList<>();

        for (int i = 0; i < size; i++) {
            boolean enabled = buf.readBoolean();
            // Read StatModifier array
            int modifierCount = buf.readInt();
            StatModifier[] statModifiers = new StatModifier[modifierCount];
            for (int j = 0; j < modifierCount; j++) {
                statModifiers[j] = decodeStatModifier(buf);
            }

            // Read repair item
            Ingredient repairItem = Ingredient.fromNetwork(buf);

            // Read target
            Either<Either<GunTabType, AttachmentType>, ResourceLocation> target = decodeTarget(buf);

            // Read max durability
            int maxDurability = buf.readInt();

            int repairCost = buf.readInt();
            float xpCost = buf.readFloat();
            boolean preventFiring = buf.readBoolean();

            boolean jam = buf.readBoolean();
            float jamChance = buf.readFloat();
            float jamThreshold = buf.readFloat();
            int jamTimeMS = buf.readInt();

            modifiers.add(new GunModifier(enabled, statModifiers, repairItem, target, maxDurability, repairCost, xpCost, preventFiring, jam, jamChance, jamThreshold, jamTimeMS));
        }

        return new SyncGunModifiersPacket(modifiers);
    }

    private static void encodeStatModifier(FriendlyByteBuf buf, StatModifier statMod) {
        // Write GunProperty array as strings
        buf.writeInt(statMod.types().length);
        for (GunProperty<?> prop : statMod.types()) {
            buf.writeUtf(prop.name());
        }

        // Write floats
        buf.writeFloat(statMod.reductionThreshold());
        buf.writeFloat(statMod.maxReduction());
    }

    private static StatModifier decodeStatModifier(FriendlyByteBuf buf) {
        int typeCount = buf.readInt();
        GunProperty<?>[] types = new GunProperty<?>[typeCount];

        for (int i = 0; i < typeCount; i++) {
            String propName = buf.readUtf();
            types[i] = GunProperty.of(propName, Object.class); // GunProperty will handle the type internally
        }

        float reductionThreshold = buf.readFloat();
        float maxReduction = buf.readFloat();

        return new StatModifier(types, reductionThreshold, maxReduction);
    }

    private static void encodeTarget(FriendlyByteBuf buf, Either<Either<GunTabType, AttachmentType>, ResourceLocation> target) {
        target.ifLeft(t -> t.ifLeft(tabType -> {
            buf.writeBoolean(true); // true = GunTabType
            buf.writeBoolean(false); // false = AttachmentType
            buf.writeUtf(tabType.name());
        }).ifRight(
                attachmentType -> {
                    buf.writeBoolean(false); // false = GunTabType
                    buf.writeBoolean(true); // true = AttachmentType
                    buf.writeUtf(attachmentType.name());
                }
        )).ifRight(gunId -> {
            buf.writeBoolean(false); // false = String (gun ID)
            buf.writeBoolean(false); // false = String (gun ID)
            buf.writeUtf(gunId.toString());
        });
    }

    private static Either<Either<GunTabType, AttachmentType>, ResourceLocation> decodeTarget(FriendlyByteBuf buf) {
        boolean isTabType = buf.readBoolean();
        boolean isAttachmentType = buf.readBoolean();

        if (isTabType) {
            String tabTypeName = buf.readUtf();
            GunTabType tabType = GunTabType.valueOf(tabTypeName.toUpperCase());
            return Either.left(Either.left(tabType));
        } else if (isAttachmentType) {
            String tabTypeName = buf.readUtf();
            AttachmentType tabType = AttachmentType.valueOf(tabTypeName.toUpperCase());
            return Either.left(Either.right(tabType));
        } else {
            String gunId = buf.readUtf();
            return Either.right(ResourceLocation.parse(gunId));
        }
    }

    public void handle(Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            // Apply the modifiers on the client side
            // You'll need to create a client-side storage similar to ServerLookup
            DataLookup.setModifiers(modifiers);
            System.out.println("Client Modifiers:" + modifiers);
        });
        context.setPacketHandled(true);
    }
}