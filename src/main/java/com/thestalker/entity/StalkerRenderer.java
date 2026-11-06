package com.thestalker.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

public class StalkerRenderer extends EntityRenderer<StalkerEntity> {

    public StalkerRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.shadowRadius = 0.0f;
    }

    @Override
    public ResourceLocation getTextureLocation(StalkerEntity entity) {
        int index = entity.getImageIndex();
        System.out.println("[StalkerRenderer] index = " + index);
        
        // Если он выглядывает из-за двери/дерева, меняем текстуру на Jumpscare (с раскинутыми руками)
        if (entity.isPeeking() && index != 5) {
            return new ResourceLocation("stalker", "textures/entity/stalkerjumpscare.png");
        }

        if (index == 0) {
            return new ResourceLocation("stalker", "textures/entity/stalkerjumpscare.png");
        }
        return new ResourceLocation("stalker", "textures/entity/stalker" + index + ".png");
    }

    private float getHeight(StalkerEntity entity) {
        return switch (entity.getImageIndex()) {
            case 0 -> 5.0f;
            case 1 -> 4.0f;
            case 2 -> 7.0f;
            case 3 -> 4.0f;
            case 4 -> 4.0f;
            case 5 -> 5.0f;
            case 6 -> 5.0f;
            default -> 3.0f;
        };
    }

    @Override
    public void render(StalkerEntity entity, float entityYaw, float partialTick,
                       PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
        float height = getHeight(entity);
        float halfH = height / 2.0f;
        float halfW = height / 2.0f;

        poseStack.pushPose();
        poseStack.translate(0.0f, halfH, 0.0f);

        // Поворот к камере (Billboard)
        Camera camera = this.entityRenderDispatcher.camera;
        poseStack.mulPose(Axis.YP.rotationDegrees(-camera.getYRot()));
        poseStack.mulPose(Axis.XP.rotationDegrees(camera.getXRot()));
        poseStack.translate(0.0f, -halfH, 0.0f);

        // ==========================================
        // АНИМАЦИЯ ВЫГЛЯДЫВАНИЯ И РУК
        // ==========================================
        if (entity.isPeeking()) {
            if (entity.getImageIndex() == 5) {
                // Quadrupedal выезжает снизу (из дырки)
                poseStack.translate(0.0f, -0.5f, 0.0f);
                poseStack.mulPose(Axis.XP.rotationDegrees(20.0f));
                poseStack.scale(0.9f, 0.9f, 0.9f);
            } else {
                // Bipedal / Tormentor выглядывают из-за двери или дерева
                
                // 1. Сдвигаем вбок (прячется за косяком)
                poseStack.translate(0.3f, 0.0f, 0.0f);
                
                // 2. Наклоняем (заглядывает из-за угла)
                poseStack.mulPose(Axis.ZP.rotationDegrees(-20.0f)); // Наклон вбок
                poseStack.mulPose(Axis.XP.rotationDegrees(10.0f));  // Наклон вперед
                
                // 3. Растягиваем (эффект длинных рук)
                poseStack.scale(1.3f, 1.1f, 1.0f); 
                
                // 4. Выдвигаем вперед (эффект 3D)
                poseStack.translate(0.0f, 0.0f, 0.2f);
            }
        }

        Matrix4f pose = poseStack.last().pose();
        Matrix3f normal = poseStack.last().normal();

        VertexConsumer vc = bufferSource.getBuffer(
                RenderType.entityTranslucent(getTextureLocation(entity)));

        vc.vertex(pose, -halfW, 0.0f, 0.0f).color(255, 255, 255, 255)
                .uv(0.0f, 1.0f).overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(packedLight).normal(normal, 0.0f, 1.0f, 0.0f).endVertex();
        vc.vertex(pose, halfW, 0.0f, 0.0f).color(255, 255, 255, 255)
                .uv(1.0f, 1.0f).overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(packedLight).normal(normal, 0.0f, 1.0f, 0.0f).endVertex();
        vc.vertex(pose, halfW, height, 0.0f).color(255, 255, 255, 255)
                .uv(1.0f, 0.0f).overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(packedLight).normal(normal, 0.0f, 1.0f, 0.0f).endVertex();
        vc.vertex(pose, -halfW, height, 0.0f).color(255, 255, 255, 255)
                .uv(0.0f, 0.0f).overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(packedLight).normal(normal, 0.0f, 1.0f, 0.0f).endVertex();

        poseStack.popPose();
        super.render(entity, entityYaw, partialTick, poseStack, bufferSource, packedLight);
    }
}
