package com.thestalker.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.thestalker.entity.StalkerEntity;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

public class StalkerRenderer
extends EntityRenderer<StalkerEntity> {
    public StalkerRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.f_114477_ = 0.0f;
    }

    public ResourceLocation getTextureLocation(StalkerEntity entity) {
        int index = entity.getImageIndex();
        if (index == 0) {
            return new ResourceLocation("stalker", "textures/entity/stalkerjumpscare.png");
        }
        return new ResourceLocation("stalker", "textures/entity/stalker" + index + ".png");
    }

    private float getHeight(StalkerEntity entity) {
        int index = entity.getImageIndex();
        switch (index) {
            case 0: {
                return 5.0f;
            }
            case 1: {
                return 4.0f;
            }
            case 2: {
                return 7.0f;
            }
            case 3: {
                return 4.0f;
            }
            case 4: {
                return 4.0f;
            }
            case 5: {
                return 5.0f;
            }
            case 6: {
                return 5.0f;
            }
        }
        return 3.0f;
    }

    public void render(StalkerEntity entity, float entityYaw, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
        float height = this.getHeight(entity);
        float halfH = height / 2.0f;
        float halfW = height / 2.0f;
        poseStack.m_85836_();
        poseStack.m_252880_(0.0f, halfH, 0.0f);
        Camera camera = this.f_114476_.f_114358_;
        poseStack.m_252781_(Axis.f_252436_.m_252977_(-camera.m_90590_()));
        poseStack.m_252781_(Axis.f_252529_.m_252977_(camera.m_90589_()));
        poseStack.m_252880_(0.0f, -halfH, 0.0f);
        Matrix4f pose = poseStack.m_85850_().m_252922_();
        Matrix3f normal = poseStack.m_85850_().m_252943_();
        VertexConsumer vc = bufferSource.m_6299_(RenderType.m_110458_((ResourceLocation)this.getTextureLocation(entity)));
        vc.m_252986_(pose, -halfW, 0.0f, 0.0f).m_6122_(255, 255, 255, 255).m_7421_(0.0f, 1.0f).m_86008_(OverlayTexture.f_118083_).m_85969_(packedLight).m_252939_(normal, 0.0f, 1.0f, 0.0f).m_5752_();
        vc.m_252986_(pose, halfW, 0.0f, 0.0f).m_6122_(255, 255, 255, 255).m_7421_(1.0f, 1.0f).m_86008_(OverlayTexture.f_118083_).m_85969_(packedLight).m_252939_(normal, 0.0f, 1.0f, 0.0f).m_5752_();
        vc.m_252986_(pose, halfW, height, 0.0f).m_6122_(255, 255, 255, 255).m_7421_(1.0f, 0.0f).m_86008_(OverlayTexture.f_118083_).m_85969_(packedLight).m_252939_(normal, 0.0f, 1.0f, 0.0f).m_5752_();
        vc.m_252986_(pose, -halfW, height, 0.0f).m_6122_(255, 255, 255, 255).m_7421_(0.0f, 0.0f).m_86008_(OverlayTexture.f_118083_).m_85969_(packedLight).m_252939_(normal, 0.0f, 1.0f, 0.0f).m_5752_();
        poseStack.m_85849_();
        super.m_7392_((Entity)entity, entityYaw, partialTick, poseStack, bufferSource, packedLight);
    }
}

