/*
 * Copyright (c) 2014, DoubleDoorDevelopment
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *  Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 *
 *  Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 *
 *  Neither the name of the project nor the names of its
 *   contributors may be used to endorse or promote products derived from
 *   this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package net.doubledoordev.pay2spawn.client;

import net.doubledoordev.pay2spawn.Pay2Spawn;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.renderer.entity.*;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.util.ResourceLocation;

/**
 * @author Dries007
 */
public class Rendering
{
    public static void init()
    {
        Render render = RenderManager.instance.getEntityClassRenderObject(EntityZombie.class);
        if (render instanceof RenderBiped)
        {
            RenderManager.instance.entityRenderMap.put(EntityZombie.class, new CustomRender((RenderBiped) render));
        }
        else
        {
            Pay2Spawn.getLogger().warn("Zombie reskining won't work because the zombie renderer has been overridden by another mod. Class: " + render.getClass());
        }
    }

    public static class CustomRender extends RenderBiped
    {
        private final RenderBiped renderZombie;

        public CustomRender(RenderBiped render)
        {
            super(new ModelBiped(0.0F), 1.0F);
            setRenderManager(RenderManager.instance);
            renderZombie = render;
        }

        @Override
        public ResourceLocation getEntityTexture(EntityLiving p_110775_1_)
        {
            if (p_110775_1_.hasCustomNameTag())
            {
                String name = p_110775_1_.getCustomNameTag();
                ResourceLocation location = AbstractClientPlayer.getLocationSkin(name);
                AbstractClientPlayer.getDownloadImageSkin(location, name);
                return location;
            }
            else return renderZombie.getEntityTexture(p_110775_1_);
        }

        @Override
        public int shouldRenderPass(EntityLiving p_77032_1_, int p_77032_2_, float p_77032_3_)
        {
            if (p_77032_1_.hasCustomNameTag()) return super.shouldRenderPass(p_77032_1_, p_77032_2_, p_77032_3_);
            else return renderZombie.shouldRenderPass(p_77032_1_, p_77032_2_, p_77032_3_);
        }

        @Override
        public void doRender(EntityLiving p_76986_1_, double p_76986_2_, double p_76986_4_, double p_76986_6_, float p_76986_8_, float p_76986_9_)
        {
            if (p_76986_1_.hasCustomNameTag()) super.doRender(p_76986_1_, p_76986_2_, p_76986_4_, p_76986_6_, p_76986_8_, p_76986_9_);
            else renderZombie.doRender(p_76986_1_, p_76986_2_, p_76986_4_, p_76986_6_, p_76986_8_, p_76986_9_);
        }

        @Override
        public void renderEquippedItems(EntityLiving p_77029_1_, float p_77029_2_)
        {
            if (p_77029_1_.hasCustomNameTag()) super.renderEquippedItems(p_77029_1_, p_77029_2_);
            else renderZombie.renderEquippedItems(p_77029_1_, p_77029_2_);
        }
    }
}
