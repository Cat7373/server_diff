package net.minecraft.server;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import java.util.Random;

public class PortalTravelAgent {

    private final WorldServer world;
    private final Random b;
    private final Long2ObjectMap<PortalTravelAgent.ChunkCoordinatesPortal> c = new Long2ObjectOpenHashMap(4096);

    public PortalTravelAgent(WorldServer worldserver) {
        this.world = worldserver;
        this.b = new Random(worldserver.getSeed());
    }

    public void a(Entity entity, float f) {
        if (this.world.worldProvider.getDimensionManager().getDimensionID() != 1) {
            if (!this.b(entity, f)) {
                this.a(entity);
                this.b(entity, f);
            }
        } else {
            int i = MathHelper.floor(entity.locX);
            int j = MathHelper.floor(entity.locY) - 1;
            int k = MathHelper.floor(entity.locZ);
            boolean flag = true;
            boolean flag1 = false;

            for (int l = -2; l <= 2; ++l) {
                for (int i1 = -2; i1 <= 2; ++i1) {
                    for (int j1 = -1; j1 < 3; ++j1) {
                        int k1 = i + i1 * 1 + l * 0;
                        int l1 = j + j1;
                        int i2 = k + i1 * 0 - l * 1;
                        boolean flag2 = j1 < 0;

                        this.world.setTypeUpdate(new BlockPosition(k1, l1, i2), flag2 ? Blocks.OBSIDIAN.getBlockData() : Blocks.AIR.getBlockData());
                    }
                }
            }

            entity.setPositionRotation((double) i, (double) j, (double) k, entity.yaw, 0.0F);
            entity.motX = 0.0D;
            entity.motY = 0.0D;
            entity.motZ = 0.0D;
        }
    }

    public boolean b(Entity entity, float f) {
        boolean flag = true;
        double d0 = -1.0D;
        int i = MathHelper.floor(entity.locX);
        int j = MathHelper.floor(entity.locZ);
        boolean flag1 = true;
        Object object = BlockPosition.ZERO;
        long k = ChunkCoordIntPair.a(i, j);

        if (this.c.containsKey(k)) {
            PortalTravelAgent.ChunkCoordinatesPortal portaltravelagent_chunkcoordinatesportal = (PortalTravelAgent.ChunkCoordinatesPortal) this.c.get(k);

            d0 = 0.0D;
            object = portaltravelagent_chunkcoordinatesportal;
            portaltravelagent_chunkcoordinatesportal.b = this.world.getTime();
            flag1 = false;
        } else {
            BlockPosition blockposition = new BlockPosition(entity);

            for (int l = -128; l <= 128; ++l) {
                BlockPosition blockposition1;

                for (int i1 = -128; i1 <= 128; ++i1) {
                    for (BlockPosition blockposition2 = blockposition.a(l, this.world.Z() - 1 - blockposition.getY(), i1); blockposition2.getY() >= 0; blockposition2 = blockposition1) {
                        blockposition1 = blockposition2.down();
                        if (this.world.getType(blockposition2).getBlock() == Blocks.PORTAL) {
                            for (blockposition1 = blockposition2.down(); this.world.getType(blockposition1).getBlock() == Blocks.PORTAL; blockposition1 = blockposition1.down()) {
                                blockposition2 = blockposition1;
                            }

                            double d1 = blockposition2.n(blockposition);

                            if (d0 < 0.0D || d1 < d0) {
                                d0 = d1;
                                object = blockposition2;
                            }
                        }
                    }
                }
            }
        }

        if (d0 >= 0.0D) {
            if (flag1) {
                this.c.put(k, new PortalTravelAgent.ChunkCoordinatesPortal((BlockPosition) object, this.world.getTime()));
            }

            double d2 = (double) ((BlockPosition) object).getX() + 0.5D;
            double d3 = (double) ((BlockPosition) object).getZ() + 0.5D;
            ShapeDetector.ShapeDetectorCollection shapedetector_shapedetectorcollection = Blocks.PORTAL.c(this.world, (BlockPosition) object);
            boolean flag2 = shapedetector_shapedetectorcollection.getFacing().e().c() == EnumDirection.EnumAxisDirection.NEGATIVE;
            double d4 = shapedetector_shapedetectorcollection.getFacing().k() == EnumDirection.EnumAxis.X ? (double) shapedetector_shapedetectorcollection.a().getZ() : (double) shapedetector_shapedetectorcollection.a().getX();
            double d5 = (double) (shapedetector_shapedetectorcollection.a().getY() + 1) - entity.getPortalOffset().y * (double) shapedetector_shapedetectorcollection.e();

            if (flag2) {
                ++d4;
            }

            if (shapedetector_shapedetectorcollection.getFacing().k() == EnumDirection.EnumAxis.X) {
                d3 = d4 + (1.0D - entity.getPortalOffset().x) * (double) shapedetector_shapedetectorcollection.d() * (double) shapedetector_shapedetectorcollection.getFacing().e().c().a();
            } else {
                d2 = d4 + (1.0D - entity.getPortalOffset().x) * (double) shapedetector_shapedetectorcollection.d() * (double) shapedetector_shapedetectorcollection.getFacing().e().c().a();
            }

            float f1 = 0.0F;
            float f2 = 0.0F;
            float f3 = 0.0F;
            float f4 = 0.0F;

            if (shapedetector_shapedetectorcollection.getFacing().opposite() == entity.getPortalDirection()) {
                f1 = 1.0F;
                f2 = 1.0F;
            } else if (shapedetector_shapedetectorcollection.getFacing().opposite() == entity.getPortalDirection().opposite()) {
                f1 = -1.0F;
                f2 = -1.0F;
            } else if (shapedetector_shapedetectorcollection.getFacing().opposite() == entity.getPortalDirection().e()) {
                f3 = 1.0F;
                f4 = -1.0F;
            } else {
                f3 = -1.0F;
                f4 = 1.0F;
            }

            double d6 = entity.motX;
            double d7 = entity.motZ;

            entity.motX = d6 * (double) f1 + d7 * (double) f4;
            entity.motZ = d6 * (double) f3 + d7 * (double) f2;
            entity.yaw = f - (float) (entity.getPortalDirection().opposite().get2DRotationValue() * 90) + (float) (shapedetector_shapedetectorcollection.getFacing().get2DRotationValue() * 90);
            if (entity instanceof EntityPlayer) {
                ((EntityPlayer) entity).playerConnection.a(d2, d5, d3, entity.yaw, entity.pitch);
            } else {
                entity.setPositionRotation(d2, d5, d3, entity.yaw, entity.pitch);
            }

            return true;
        } else {
            return false;
        }
    }

    public boolean a(Entity entity) {
        boolean flag = true;
        double d0 = -1.0D;
        int i = MathHelper.floor(entity.locX);
        int j = MathHelper.floor(entity.locY);
        int k = MathHelper.floor(entity.locZ);
        int l = i;
        int i1 = j;
        int j1 = k;
        int k1 = 0;
        int l1 = this.b.nextInt(4);
        BlockPosition.MutableBlockPosition blockposition_mutableblockposition = new BlockPosition.MutableBlockPosition();

        int i2;
        double d1;
        int j2;
        double d2;
        int k2;
        int l2;
        int i3;
        int j3;
        int k3;
        int l3;
        int i4;
        int j4;
        int k4;
        double d3;
        double d4;

        for (i2 = i - 16; i2 <= i + 16; ++i2) {
            d1 = (double) i2 + 0.5D - entity.locX;

            for (j2 = k - 16; j2 <= k + 16; ++j2) {
                d2 = (double) j2 + 0.5D - entity.locZ;

                label271:
                for (k2 = this.world.Z() - 1; k2 >= 0; --k2) {
                    if (this.world.isEmpty(blockposition_mutableblockposition.c(i2, k2, j2))) {
                        while (k2 > 0 && this.world.isEmpty(blockposition_mutableblockposition.c(i2, k2 - 1, j2))) {
                            --k2;
                        }

                        for (l2 = l1; l2 < l1 + 4; ++l2) {
                            i3 = l2 % 2;
                            j3 = 1 - i3;
                            if (l2 % 4 >= 2) {
                                i3 = -i3;
                                j3 = -j3;
                            }

                            for (k3 = 0; k3 < 3; ++k3) {
                                for (l3 = 0; l3 < 4; ++l3) {
                                    for (i4 = -1; i4 < 4; ++i4) {
                                        j4 = i2 + (l3 - 1) * i3 + k3 * j3;
                                        k4 = k2 + i4;
                                        int l4 = j2 + (l3 - 1) * j3 - k3 * i3;

                                        blockposition_mutableblockposition.c(j4, k4, l4);
                                        if (i4 < 0 && !this.world.getType(blockposition_mutableblockposition).getMaterial().isBuildable() || i4 >= 0 && !this.world.isEmpty(blockposition_mutableblockposition)) {
                                            continue label271;
                                        }
                                    }
                                }
                            }

                            d3 = (double) k2 + 0.5D - entity.locY;
                            d4 = d1 * d1 + d3 * d3 + d2 * d2;
                            if (d0 < 0.0D || d4 < d0) {
                                d0 = d4;
                                l = i2;
                                i1 = k2;
                                j1 = j2;
                                k1 = l2 % 4;
                            }
                        }
                    }
                }
            }
        }

        if (d0 < 0.0D) {
            for (i2 = i - 16; i2 <= i + 16; ++i2) {
                d1 = (double) i2 + 0.5D - entity.locX;

                for (j2 = k - 16; j2 <= k + 16; ++j2) {
                    d2 = (double) j2 + 0.5D - entity.locZ;

                    label219:
                    for (k2 = this.world.Z() - 1; k2 >= 0; --k2) {
                        if (this.world.isEmpty(blockposition_mutableblockposition.c(i2, k2, j2))) {
                            while (k2 > 0 && this.world.isEmpty(blockposition_mutableblockposition.c(i2, k2 - 1, j2))) {
                                --k2;
                            }

                            for (l2 = l1; l2 < l1 + 2; ++l2) {
                                i3 = l2 % 2;
                                j3 = 1 - i3;

                                for (k3 = 0; k3 < 4; ++k3) {
                                    for (l3 = -1; l3 < 4; ++l3) {
                                        i4 = i2 + (k3 - 1) * i3;
                                        j4 = k2 + l3;
                                        k4 = j2 + (k3 - 1) * j3;
                                        blockposition_mutableblockposition.c(i4, j4, k4);
                                        if (l3 < 0 && !this.world.getType(blockposition_mutableblockposition).getMaterial().isBuildable() || l3 >= 0 && !this.world.isEmpty(blockposition_mutableblockposition)) {
                                            continue label219;
                                        }
                                    }
                                }

                                d3 = (double) k2 + 0.5D - entity.locY;
                                d4 = d1 * d1 + d3 * d3 + d2 * d2;
                                if (d0 < 0.0D || d4 < d0) {
                                    d0 = d4;
                                    l = i2;
                                    i1 = k2;
                                    j1 = j2;
                                    k1 = l2 % 2;
                                }
                            }
                        }
                    }
                }
            }
        }

        int i5 = l;
        int j5 = i1;

        j2 = j1;
        int k5 = k1 % 2;
        int l5 = 1 - k5;

        if (k1 % 4 >= 2) {
            k5 = -k5;
            l5 = -l5;
        }

        if (d0 < 0.0D) {
            i1 = MathHelper.clamp(i1, 70, this.world.Z() - 10);
            j5 = i1;

            for (k2 = -1; k2 <= 1; ++k2) {
                for (l2 = 1; l2 < 3; ++l2) {
                    for (i3 = -1; i3 < 3; ++i3) {
                        j3 = i5 + (l2 - 1) * k5 + k2 * l5;
                        k3 = j5 + i3;
                        l3 = j2 + (l2 - 1) * l5 - k2 * k5;
                        boolean flag1 = i3 < 0;

                        this.world.setTypeUpdate(new BlockPosition(j3, k3, l3), flag1 ? Blocks.OBSIDIAN.getBlockData() : Blocks.AIR.getBlockData());
                    }
                }
            }
        }

        IBlockData iblockdata = Blocks.PORTAL.getBlockData().set(BlockPortal.AXIS, k5 == 0 ? EnumDirection.EnumAxis.Z : EnumDirection.EnumAxis.X);

        for (l2 = 0; l2 < 4; ++l2) {
            for (i3 = 0; i3 < 4; ++i3) {
                for (j3 = -1; j3 < 4; ++j3) {
                    k3 = i5 + (i3 - 1) * k5;
                    l3 = j5 + j3;
                    i4 = j2 + (i3 - 1) * l5;
                    boolean flag2 = i3 == 0 || i3 == 3 || j3 == -1 || j3 == 3;

                    this.world.setTypeAndData(new BlockPosition(k3, l3, i4), flag2 ? Blocks.OBSIDIAN.getBlockData() : iblockdata, 2);
                }
            }

            for (i3 = 0; i3 < 4; ++i3) {
                for (j3 = -1; j3 < 4; ++j3) {
                    k3 = i5 + (i3 - 1) * k5;
                    l3 = j5 + j3;
                    i4 = j2 + (i3 - 1) * l5;
                    BlockPosition blockposition = new BlockPosition(k3, l3, i4);

                    this.world.applyPhysics(blockposition, this.world.getType(blockposition).getBlock(), false);
                }
            }
        }

        return true;
    }

    public void a(long i) {
        if (i % 100L == 0L) {
            long j = i - 300L;
            ObjectIterator objectiterator = this.c.values().iterator();

            while (objectiterator.hasNext()) {
                PortalTravelAgent.ChunkCoordinatesPortal portaltravelagent_chunkcoordinatesportal = (PortalTravelAgent.ChunkCoordinatesPortal) objectiterator.next();

                if (portaltravelagent_chunkcoordinatesportal == null || portaltravelagent_chunkcoordinatesportal.b < j) {
                    objectiterator.remove();
                }
            }
        }

    }

    public class ChunkCoordinatesPortal extends BlockPosition {

        public long b;

        public ChunkCoordinatesPortal(BlockPosition blockposition, long i) {
            super(blockposition.getX(), blockposition.getY(), blockposition.getZ());
            this.b = i;
        }
    }
}
