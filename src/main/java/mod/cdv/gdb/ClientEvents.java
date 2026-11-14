package mod.cdv.gdb;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.tacz.guns.api.TimelessAPI;
import com.tacz.guns.api.client.animation.statemachine.LuaAnimationStateMachine;
import com.tacz.guns.api.event.common.GunShootEvent;
import com.tacz.guns.api.item.IAttachment;
import com.tacz.guns.api.item.IGun;
import com.tacz.guns.client.animation.statemachine.GunAnimationStateContext;
import com.tacz.guns.client.model.BedrockAttachmentModel;
import com.tacz.guns.client.model.BedrockGunModel;
import com.tacz.guns.client.model.IFunctionalRenderer;
import com.tacz.guns.client.model.bedrock.BedrockPart;
import com.tacz.guns.client.renderer.item.AttachmentItemRenderer;
import com.tacz.guns.client.resource.index.ClientGunIndex;
import com.tacz.guns.client.resource.pojo.model.BonesItem;
import com.tacz.guns.client.resource.pojo.model.CubesItem;
import com.tacz.guns.client.sound.SoundPlayManager;
import com.tacz.guns.compat.oculus.OculusCompat;
import com.tacz.guns.resource.modifier.AttachmentPropertyManager;
import com.tacz.guns.resource.pojo.data.gun.GunData;
import com.tacz.guns.util.RenderDistance;
import mod.cdv.gdb.resource.GunModifier;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.function.Function;

@Mod.EventBusSubscriber(Dist.CLIENT)
public final class ClientEvents {
    public static final ResourceLocation RUST_OVERLAY = ResourceLocation.fromNamespaceAndPath("gundb", "textures/overlay/rust.png");
    public static final ResourceLocation WEAR_OVERLAY = ResourceLocation.fromNamespaceAndPath("gundb", "textures/overlay/wear.png");
    public static final ResourceLocation CRACK_OVERLAY = ResourceLocation.fromNamespaceAndPath("gundb", "textures/overlay/cracked.png");

    @SubscribeEvent
    public static void shootEvent(GunShootEvent event) {
        var item = event.getGunItemStack();
        if (event.getLogicalSide().isServer()) return;
        GunModifier modifiers = DataLookup.getModifiers(item);
        if(modifiers == null) return;
        //Jam cancel
        if(item.getTag() != null && item.getTag().contains("Jammed") && item.getTag().getBoolean("Jammed")) {
            event.setCanceled(true);
            TimelessAPI.getGunDisplay(event.getGunItemStack()).ifPresent(display -> SoundPlayManager.playDryFireSound(event.getShooter(), display));
            return;
        }
        //Zero durability cancel
        if (item.getDamageValue() == item.getMaxDamage()) {
            if(!modifiers.preventFiring()) {
                event.setCanceled(true);
                TimelessAPI.getGunDisplay(event.getGunItemStack()).ifPresent(display -> SoundPlayManager.playDryFireSound(event.getShooter(), display));
            }
            return;
        }
        AttachmentPropertyManager.postChangeEvent(event.getShooter(), item);
    }

    @SubscribeEvent
    public static void animateTick(TickEvent.ClientTickEvent event) {
        if(event.phase == TickEvent.Phase.START) return;

        var player = Minecraft.getInstance().player;
        if(player == null) return;
        var mainHandItem = player.getMainHandItem();
        GunModifier modifiers = DataLookup.getModifiers(mainHandItem);
        if(modifiers == null) return;
        var iGun = IGun.getIGunOrNull(mainHandItem);
        if(modifiers.jam() && Util.getOrSetTag(mainHandItem, "Jammed", false)) {
            GunData gunData = TimelessAPI.getClientGunIndex(iGun.getGunId(mainHandItem)).map(ClientGunIndex::getGunData).orElse(null);
            if (gunData != null) {
                TimelessAPI.getGunDisplay(mainHandItem).ifPresent((gunIndex) -> {
                    LuaAnimationStateMachine<GunAnimationStateContext> animationStateMachine = gunIndex.getAnimationStateMachine();
                    if (animationStateMachine != null) {
                        if (animationStateMachine.getAnimationController().containPrototype("unjam_idle")) {
                            animationStateMachine.trigger("unjam_idle");
                        }
                    }
                });
            }
        }
    }

    public static void renderAttachmentOverlay(
            BedrockAttachmentModel model, ItemStack attachmentStack, ItemStack gunStack, PoseStack matrixStack, ItemDisplayContext ctx, int light, int overlay
    ) {
        GunModifier modifiers = DataLookup.getModifiers(attachmentStack);
        IAttachment attachment = IAttachment.getIAttachmentOrNull(attachmentStack);
        if (modifiers == null || attachment == null)
            return;
        float durabilityPercent = ((float) attachmentStack.getMaxDamage() - attachmentStack.getDamageValue()) / attachmentStack.getMaxDamage();

        float alpha = getOverlayEasing(durabilityPercent, modifiers.jamThreshold());
        if(alpha == 0.00f)
            return;

        RenderType renderType = RenderType.crumbling(RUST_OVERLAY);
        model.render(attachmentStack, gunStack, matrixStack, ctx, renderType, light, overlay);
    }

    public static void renderAttachment(ItemStack attachmentItem, ItemStack gunItem, PoseStack poseStack, ItemDisplayContext transformType, int light, int overlay) {
        poseStack.translate(0.0F, -1.5F, (double)0.0F);
        Item var7 = attachmentItem.getItem();
        if (var7 instanceof IAttachment iAttachment) {
            ResourceLocation attachmentId = iAttachment.getAttachmentId(attachmentItem);
            TimelessAPI.getClientAttachmentIndex(attachmentId).ifPresentOrElse((attachmentIndex) -> {
                BedrockAttachmentModel model = attachmentIndex.getAttachmentModel();
                ResourceLocation texture = attachmentIndex.getModelTexture();
                if (model != null && texture != null) {
                    Pair<BedrockAttachmentModel, ResourceLocation> lodModel = attachmentIndex.getLodModel();
                    if (lodModel != null && !RenderDistance.inRenderHighPolyModelDistance(poseStack) && !transformType.firstPerson()) {
                        model = lodModel.getLeft();
                        texture = lodModel.getRight();
                    }

                    RenderType renderType = RenderType.crumbling(RUST_OVERLAY);
                    model.render(attachmentItem, gunItem, poseStack, transformType, renderType, light, overlay);
                }

            }, () -> {
                MultiBufferSource bufferSource = Minecraft.getInstance().renderBuffers().bufferSource();
                VertexConsumer buffer = bufferSource.getBuffer(RenderType.entityTranslucent(MissingTextureAtlasSprite.getLocation()));
                AttachmentItemRenderer.SLOT_ATTACHMENT_MODEL.renderToBuffer(poseStack, buffer, light, overlay, 1.0F, 1.0F, 1.0F, 1.0F);
            });
        }

    }

    public static void renderOverlay(
            ItemStack stack, PoseStack poseStack, ItemDisplayContext ctx, int light, BedrockGunModel gunModel
    ) {
        GunModifier modifiers = DataLookup.getModifiers(stack);

        if (modifiers == null)
            return;
        float durabilityPercent = ((float) stack.getMaxDamage() - stack.getDamageValue()) / stack.getMaxDamage();
        if(modifiers.jamThreshold() < durabilityPercent)
            return;

        float alpha = getOverlayEasing(durabilityPercent, modifiers.jamThreshold());
        if(alpha == 0.00f)
            return;

        gunModel.render(
                poseStack,
                ctx,
                RenderType.crumbling(RUST_OVERLAY),
                light,
                OverlayTexture.NO_OVERLAY,
                1.0f, 1.0f, 1.0f, alpha
        );

    }

    public static float getOverlayEasing(float durabilityPercent, float jamThreshold) {
        if (durabilityPercent >= jamThreshold) {
            return 0.00f;
        }

        float progress = (durabilityPercent - jamThreshold) / (0.0f - jamThreshold);
        progress = Math.max(0.0f, Math.min(1.0f, progress));
        return progress * progress * 0.75f;
    }
}
