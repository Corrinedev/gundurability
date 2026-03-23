package mod.cdv.gdb.jei;

import com.tacz.guns.init.ModCreativeTabs;
import com.tacz.guns.item.AttachmentItem;
import com.tacz.guns.item.ModernKineticGunItem;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.RecipeTypes;
import mezz.jei.api.recipe.vanilla.IJeiAnvilRecipe;
import mezz.jei.api.recipe.vanilla.IVanillaRecipeFactory;
import mezz.jei.api.registration.IRecipeRegistration;
import mezz.jei.api.runtime.IIngredientManager;
import mod.cdv.gdb.DataLookup;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.RegistryObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

@JeiPlugin
public class JeiRepairPlugin implements IModPlugin {
    static final ResourceLocation pluginUid = ResourceLocation.fromNamespaceAndPath("gundb", "jei");
    @Override
    public ResourceLocation getPluginUid() {
        return pluginUid;
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        IIngredientManager ingredientManager = registration.getIngredientManager();
        IVanillaRecipeFactory vanillaRecipeFactory = registration.getVanillaRecipeFactory();
        registration.addRecipes(RecipeTypes.ANVIL, getGunRecipes(vanillaRecipeFactory, ingredientManager));
    }

    private List<IJeiAnvilRecipe> getGunRecipes(IVanillaRecipeFactory vanillaRecipeFactory, IIngredientManager ingredientManager) {
        HashMap<ResourceLocation, IJeiAnvilRecipe> recipes = new HashMap<>();

        for (RegistryObject<CreativeModeTab> entry : ModCreativeTabs.TABS.getEntries()) {
            entry.get().getDisplayItems();
            for (ItemStack allItemStack : ingredientManager.getAllItemStacks()) {
                if(allItemStack.getItem() instanceof ModernKineticGunItem gd) {
                    var mod = DataLookup.getModifiers(allItemStack);
                    if(mod != null && mod.enabled()) {
                        var noDurabilityItem = allItemStack.copy();
                        noDurabilityItem.setDamageValue(allItemStack.getMaxDamage());
                        recipes.putIfAbsent(gd.getGunId(noDurabilityItem), vanillaRecipeFactory.createAnvilRecipe(noDurabilityItem, Arrays.stream(mod.repairItem().getItems()).map(i -> i.copyWithCount(mod.repairCost())).toList(), List.of(allItemStack), gd.getGunId(noDurabilityItem)));
                    }
                } else if (allItemStack.getItem() instanceof AttachmentItem at) {
                    var mod = DataLookup.getModifiers(allItemStack);
                    if(mod != null && mod.enabled()) {
                        var noDurabilityItem = allItemStack.copy();
                        noDurabilityItem.setDamageValue(allItemStack.getMaxDamage());
                        recipes.putIfAbsent(at.getAttachmentId(noDurabilityItem), vanillaRecipeFactory.createAnvilRecipe(noDurabilityItem, Arrays.stream(mod.repairItem().getItems()).map(i -> i.copyWithCount(mod.repairCost())).toList(), List.of(allItemStack), at.getAttachmentId(noDurabilityItem)));
                    }
                }
            }
        }

        return new ArrayList<>(recipes.values());
    }
}
