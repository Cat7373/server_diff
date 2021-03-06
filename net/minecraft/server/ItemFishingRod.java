package net.minecraft.server;

public class ItemFishingRod extends Item {

    public ItemFishingRod() {
        this.setMaxDurability(64);
        this.d(1);
        this.a(CreativeModeTab.i);
        this.a(new MinecraftKey("cast"), new IDynamicTexture() {
        });
    }

    public InteractionResultWrapper<ItemStack> a(World world, EntityHuman entityhuman, EnumHand enumhand) {
        ItemStack itemstack = entityhuman.b(enumhand);

        if (entityhuman.hookedFish != null) {
            int i = entityhuman.hookedFish.j();

            itemstack.damage(i, entityhuman);
            entityhuman.a(enumhand);
        } else {
            world.a((EntityHuman) null, entityhuman.locX, entityhuman.locY, entityhuman.locZ, SoundEffects.I, SoundCategory.NEUTRAL, 0.5F, 0.4F / (ItemFishingRod.j.nextFloat() * 0.4F + 0.8F));
            if (!world.isClientSide) {
                EntityFishingHook entityfishinghook = new EntityFishingHook(world, entityhuman);

                world.addEntity(entityfishinghook);
            }

            entityhuman.a(enumhand);
            entityhuman.b(StatisticList.b((Item) this));
        }

        return new InteractionResultWrapper(EnumInteractionResult.SUCCESS, itemstack);
    }

    public int c() {
        return 1;
    }
}
