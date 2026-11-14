package mod.cdv.gdb.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.tacz.guns.api.TimelessAPI;
import com.tacz.guns.api.item.IAttachment;
import com.tacz.guns.api.item.attachment.AttachmentType;
import com.tacz.guns.client.model.BedrockAttachmentModel;
import com.tacz.guns.client.model.BedrockGunModel;
import com.tacz.guns.client.model.bedrock.BedrockModel;
import com.tacz.guns.client.renderer.item.AttachmentItemRenderer;
import com.tacz.guns.client.renderer.item.GunItemRendererWrapper;
import com.tacz.guns.util.RenderDistance;
import mod.cdv.gdb.ClientEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.apache.commons.lang3.tuple.Pair;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.EnumMap;

@Mixin(value = BedrockGunModel.class, remap = false)
public class GunRenderMixin extends BedrockModel {

    @Shadow
    private ItemStack currentGunItem;

    @Shadow
    @Final
    private EnumMap<AttachmentType, ItemStack> currentAttachmentItem;

    //@Inject(
    //        method = "render",
    //        at = @At(
    //                value = "INVOKE",
    //                target = "Lcom/tacz/guns/client/model/BedrockAnimatedModel;render(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/world/item/ItemDisplayContext;Lnet/minecraft/client/renderer/RenderType;II)V",
    //                shift = At.Shift.AFTER
    //        )
    //)
    //private void renderOverlay(
    //        PoseStack poseStack, ItemStack gunItem, ItemDisplayContext transformType, RenderType renderType, int light, int overlay, CallbackInfo ci
    //) {
    //    ClientEvents.renderOverlay(gunItem, poseStack, transformType, light, (BedrockGunModel)(Object)this);
//
    //    for (AttachmentType value : AttachmentType.values()) {
    //        poseStack.pushPose();
    //        delegateRender((poseStack1, vertexBuffer1, transformType1, light1, overlay1) -> {
    //            PoseStack poseStack2 = new PoseStack();
    //            Matrix3f normal = new Matrix3f(poseStack.last().normal());
    //            Matrix4f pose = new Matrix4f(poseStack.last().pose());
    //            poseStack2.last().normal().mul(normal);
    //            poseStack2.last().pose().mul(pose);
    //            ItemStack attachmentItem = currentAttachmentItem.get(value);
    //            if(attachmentItem != null) {
    //                renderAttachmentWithDamage(attachmentItem, currentGunItem, poseStack, transformType, light, overlay);
    //            }
    //        });
    //        poseStack.popPose();
    //    }
    //}
//
    //@Unique
    //private static void renderAttachmentWithDamage(ItemStack attachmentItem, ItemStack gunItem, PoseStack poseStack, ItemDisplayContext transformType, int light, int overlay) {
    //    poseStack.translate(0.0F, -1.5F, (double)0.0F);
    //    Item var7 = attachmentItem.getItem();
    //    if (var7 instanceof IAttachment iAttachment) {
    //        ResourceLocation attachmentId = iAttachment.getAttachmentId(attachmentItem);
    //        TimelessAPI.getClientAttachmentIndex(attachmentId).ifPresentOrElse((attachmentIndex) -> {
    //            BedrockAttachmentModel model = attachmentIndex.getAttachmentModel();
    //            ResourceLocation texture = attachmentIndex.getModelTexture();
    //            if (model != null && texture != null) {
    //                Pair<BedrockAttachmentModel, ResourceLocation> lodModel = attachmentIndex.getLodModel();
    //                if (lodModel != null && !RenderDistance.inRenderHighPolyModelDistance(poseStack) && !transformType.firstPerson()) {
    //                    model = lodModel.getLeft();
    //                    texture = lodModel.getRight();
    //                }
//
    //                ClientEvents.renderAttachmentOverlay(model, attachmentItem, gunItem, poseStack, transformType, light, overlay);
    //                //model.render(attachmentItem, gunItem, poseStack, transformType, renderType, light, overlay);
    //            }
//
    //        }, () -> {
    //            MultiBufferSource bufferSource = Minecraft.getInstance().renderBuffers().bufferSource();
    //            VertexConsumer buffer = bufferSource.getBuffer(RenderType.entityTranslucent(MissingTextureAtlasSprite.getLocation()));
    //            AttachmentItemRenderer.SLOT_ATTACHMENT_MODEL.renderToBuffer(poseStack, buffer, light, overlay, 1.0F, 1.0F, 1.0F, 1.0F);
    //        });
    //    }
//
    //}
//
    //@Inject(
    //        method = "renderAccelerated",
    //        at = @At(
    //                value = "INVOKE",
    //                target = "Lcom/tacz/guns/client/model/BedrockAnimatedModel;render(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/world/item/ItemDisplayContext;Lnet/minecraft/client/renderer/RenderType;II)V",
    //                shift = At.Shift.AFTER
    //        )
    //)
    //private void renderOverlayAccelerated(
    //        PoseStack matrixStack, ItemStack gunItem, ItemDisplayContext transformType, RenderType renderType, int light, int overlay, CallbackInfo ci
    //) {
    //    ClientEvents.renderOverlay(gunItem, matrixStack, transformType, light, (BedrockGunModel)(Object)this);
    //}
}
