package mod.cdv.gdb.resource;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.mojang.datafixers.util.Either;
import com.tacz.guns.api.GunProperty;
import com.tacz.guns.api.item.GunTabType;
import com.tacz.guns.api.item.attachment.AttachmentType;
import mod.cdv.gdb.DataLookup;
import mod.cdv.gdb.Util;
import mod.cdv.gdb.network.NetworkHandler;
import mod.cdv.gdb.network.SyncGunModifiersPacket;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.tags.TagKey;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.server.ServerLifecycleHooks;

import java.util.*;

import static com.tacz.guns.api.item.GunTabType.*;

public class ResourceLoader extends SimpleJsonResourceReloadListener {
    public static final ResourceLoader INSTANCE = new ResourceLoader();

    public ResourceLoader() {
        super(new Gson(), "gundb");
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> map, ResourceManager resourceManager, ProfilerFiller profilerFiller) {
        TreeMap<ResourceLocation, GunModifier> gunModifiers = new TreeMap<>((a, b) -> {
            boolean aIsGunDb = a.getNamespace().equals("gundb");
            boolean bIsGunDb = b.getNamespace().equals("gundb");

            if (aIsGunDb && !bIsGunDb) return 1;
            if (!aIsGunDb && bIsGunDb) return -1;

            int nsCompare = a.getNamespace().compareTo(b.getNamespace());
            if (nsCompare != 0) return nsCompare;

            return a.getPath().compareTo(b.getPath());
        });
        map.forEach((re, e) -> {
            var root = e.getAsJsonObject();
            String name = re.getPath();
            name = name.replace('/', ':');

            //Category or gunId
            Either<Either<GunTabType, AttachmentType>, ResourceLocation> target = null;
            for (var v : GunTabType.values()) {
                if (name.equalsIgnoreCase(v.name())) {
                    target = Either.left(Either.left(v));
                    break;
                }
            }
            for (var v : AttachmentType.values()) {
                if (name.equalsIgnoreCase(v.name())) {
                    target = Either.left(Either.right(v));
                    break;
                }
            }


            // Use ResourceLocation if no Enum matches are found
            if (target == null) {
                target = Either.right(ResourceLocation.parse(name));
            }

            // Disable for the target
            if (root.has("enabled") && !root.get("enabled").getAsBoolean()) {
                gunModifiers.put(re,
                         GunModifier.createDisabled(target)
                );
                return;
            }


            var repairItemRaw = root.getAsJsonPrimitive("RepairItem").getAsString();

            //durability
            int maxDurability;
            if(root.has("MaxDurability")) {
                maxDurability = root.get("MaxDurability").getAsInt();
            } else {
                maxDurability = 1500;
            }

            //repair cost
            int repairCost = root.has("RepairCost") ? root.get("RepairCost").getAsInt() : 5;
            float xpCost = root.has("XpCost") ? root.get("XpCost").getAsFloat() : 0.5f;

            //preventFiring
            boolean preventFiring = root.has("FireOnZero") && root.get("FireOnZero").getAsBoolean();

            //jam
            boolean jam = root.has("Jam") && root.get("Jam").getAsBoolean();
            float jamChance = root.has("JamChance") ? root.get("JamChance").getAsFloat() : 0.05f;
            float jamThreshold = root.has("JamThreshold") ? root.get("JamThreshold").getAsFloat() : 0.65f;
            int jamTimeMS = root.has("JamTimeMS") ? root.get("JamTimeMS").getAsInt() : 2000;

            //repair item
            Ingredient repairItem;
            if(repairItemRaw.startsWith("#")) {
                repairItem = Ingredient.of(TagKey.create(Registries.ITEM, ResourceLocation.parse(repairItemRaw.substring(1))));
            } else {
                repairItem = Ingredient.of(ForgeRegistries.ITEMS.getValue(ResourceLocation.parse(repairItemRaw)));
            }

            //Stats
            var statsArr = root.getAsJsonArray("Stats");
            var statsTemp = new ArrayList<StatModifier>();

            for (JsonElement jsonElement : statsArr) {
                var obj = jsonElement.getAsJsonObject();
                JsonElement typeElement = obj.get("Type");
                GunProperty<?>[] gunProperties = new GunProperty[]{};
                if(typeElement.isJsonPrimitive()) {
                    gunProperties = new GunProperty[]{Util.getGunProperty(typeElement.getAsString())};
                } else if (typeElement.isJsonArray()) {
                    var propTemp = new ArrayList<GunProperty<?>>();
                    for (JsonElement prop : typeElement.getAsJsonArray()) {
                        propTemp.add(Util.getGunProperty(prop.getAsString()));
                    }
                    gunProperties = propTemp.toArray(GunProperty[]::new);
                }
                statsTemp.add(
                        new StatModifier(
                                gunProperties,
                                obj.get("ReductionThreshold").getAsFloat(),
                                obj.get("MaxReduction").getAsFloat()
                        )
                );
            }
            gunModifiers.put(re,
                    new GunModifier(
                            statsTemp.toArray(StatModifier[]::new),
                            repairItem,
                            target,
                            maxDurability,
                            repairCost,
                            xpCost,
                            preventFiring,
                            jam,
                            jamChance,
                            jamThreshold,
                            jamTimeMS
                    )
            );
        });

        ArrayList<GunModifier> finalModifiers = new ArrayList<>(gunModifiers.values());
        DataLookup.gunModifiers.clear();
        DataLookup.modifierCache.clear();
        DataLookup.gunModifiers.addAll(finalModifiers);
        if(ServerLifecycleHooks.getCurrentServer() != null)
            NetworkHandler.sendToAllClients(new SyncGunModifiersPacket(finalModifiers));
    }
}
