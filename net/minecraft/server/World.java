package net.minecraft.server;

import com.google.common.base.Function;
import com.google.common.base.Objects;
import com.google.common.base.Predicate;
import com.google.common.collect.Lists;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import javax.annotation.Nullable;

public abstract class World implements IBlockAccess {

    private int a = 63;
    protected boolean d;
    public final List<Entity> entityList = Lists.newArrayList();
    protected final List<Entity> f = Lists.newArrayList();
    public final List<TileEntity> tileEntityList = Lists.newArrayList();
    public final List<TileEntity> tileEntityListTick = Lists.newArrayList();
    private final List<TileEntity> b = Lists.newArrayList();
    private final List<TileEntity> tileEntityListUnload = Lists.newArrayList();
    public final List<EntityHuman> players = Lists.newArrayList();
    public final List<Entity> j = Lists.newArrayList();
    protected final IntHashMap<Entity> entitiesById = new IntHashMap();
    private final long I = 16777215L;
    private int J;
    protected int l = (new Random()).nextInt();
    protected final int m = 1013904223;
    protected float n;
    protected float o;
    protected float p;
    protected float q;
    private int K;
    public final Random random = new Random();
    public WorldProvider worldProvider;
    protected NavigationListener t = new NavigationListener();
    protected List<IWorldAccess> u;
    protected IChunkProvider chunkProvider;
    protected final IDataManager dataManager;
    public WorldData worldData;
    protected boolean isLoading;
    public PersistentCollection worldMaps;
    protected PersistentVillage villages;
    protected LootTableRegistry B;
    public final MethodProfiler methodProfiler;
    private final Calendar L;
    public Scoreboard scoreboard;
    public final boolean isClientSide;
    public boolean allowMonsters;
    public boolean allowAnimals;
    private boolean M;
    private final WorldBorder N;
    int[] H;

    protected World(IDataManager idatamanager, WorldData worlddata, WorldProvider worldprovider, MethodProfiler methodprofiler, boolean flag) {
        this.u = Lists.newArrayList(new IWorldAccess[] { this.t});
        this.L = Calendar.getInstance();
        this.scoreboard = new Scoreboard();
        this.allowMonsters = true;
        this.allowAnimals = true;
        this.H = new int['\u8000'];
        this.dataManager = idatamanager;
        this.methodProfiler = methodprofiler;
        this.worldData = worlddata;
        this.worldProvider = worldprovider;
        this.isClientSide = flag;
        this.N = worldprovider.getWorldBorder();
    }

    public World b() {
        return this;
    }

    public BiomeBase getBiome(final BlockPosition blockposition) {
        if (this.isLoaded(blockposition)) {
            Chunk chunk = this.getChunkAtWorldCoords(blockposition);

            try {
                return chunk.getBiome(blockposition, this.worldProvider.k());
            } catch (Throwable throwable) {
                CrashReport crashreport = CrashReport.a(throwable, "Getting biome");
                CrashReportSystemDetails crashreportsystemdetails = crashreport.a("Coordinates of biome request");

                crashreportsystemdetails.a("Location", new CrashReportCallable() {
                    public String a() throws Exception {
                        return CrashReportSystemDetails.a(blockposition);
                    }

                    public Object call() throws Exception {
                        return this.a();
                    }
                });
                throw new ReportedException(crashreport);
            }
        } else {
            return this.worldProvider.k().getBiome(blockposition, Biomes.c);
        }
    }

    public WorldChunkManager getWorldChunkManager() {
        return this.worldProvider.k();
    }

    protected abstract IChunkProvider n();

    public void a(WorldSettings worldsettings) {
        this.worldData.d(true);
    }

    @Nullable
    public MinecraftServer getMinecraftServer() {
        return null;
    }

    public IBlockData c(BlockPosition blockposition) {
        BlockPosition blockposition1;

        for (blockposition1 = new BlockPosition(blockposition.getX(), this.K(), blockposition.getZ()); !this.isEmpty(blockposition1.up()); blockposition1 = blockposition1.up()) {
            ;
        }

        return this.getType(blockposition1);
    }

    private boolean isValidLocation(BlockPosition blockposition) {
        return !this.E(blockposition) && blockposition.getX() >= -30000000 && blockposition.getZ() >= -30000000 && blockposition.getX() < 30000000 && blockposition.getZ() < 30000000;
    }

    private boolean E(BlockPosition blockposition) {
        return blockposition.getY() < 0 || blockposition.getY() >= 256;
    }

    public boolean isEmpty(BlockPosition blockposition) {
        return this.getType(blockposition).getMaterial() == Material.AIR;
    }

    public boolean isLoaded(BlockPosition blockposition) {
        return this.a(blockposition, true);
    }

    public boolean a(BlockPosition blockposition, boolean flag) {
        return this.isChunkLoaded(blockposition.getX() >> 4, blockposition.getZ() >> 4, flag);
    }

    public boolean areChunksLoaded(BlockPosition blockposition, int i) {
        return this.areChunksLoaded(blockposition, i, true);
    }

    public boolean areChunksLoaded(BlockPosition blockposition, int i, boolean flag) {
        return this.isAreaLoaded(blockposition.getX() - i, blockposition.getY() - i, blockposition.getZ() - i, blockposition.getX() + i, blockposition.getY() + i, blockposition.getZ() + i, flag);
    }

    public boolean areChunksLoadedBetween(BlockPosition blockposition, BlockPosition blockposition1) {
        return this.areChunksLoadedBetween(blockposition, blockposition1, true);
    }

    public boolean areChunksLoadedBetween(BlockPosition blockposition, BlockPosition blockposition1, boolean flag) {
        return this.isAreaLoaded(blockposition.getX(), blockposition.getY(), blockposition.getZ(), blockposition1.getX(), blockposition1.getY(), blockposition1.getZ(), flag);
    }

    public boolean a(StructureBoundingBox structureboundingbox) {
        return this.b(structureboundingbox, true);
    }

    public boolean b(StructureBoundingBox structureboundingbox, boolean flag) {
        return this.isAreaLoaded(structureboundingbox.a, structureboundingbox.b, structureboundingbox.c, structureboundingbox.d, structureboundingbox.e, structureboundingbox.f, flag);
    }

    private boolean isAreaLoaded(int i, int j, int k, int l, int i1, int j1, boolean flag) {
        if (i1 >= 0 && j < 256) {
            i >>= 4;
            k >>= 4;
            l >>= 4;
            j1 >>= 4;

            for (int k1 = i; k1 <= l; ++k1) {
                for (int l1 = k; l1 <= j1; ++l1) {
                    if (!this.isChunkLoaded(k1, l1, flag)) {
                        return false;
                    }
                }
            }

            return true;
        } else {
            return false;
        }
    }

    protected abstract boolean isChunkLoaded(int i, int j, boolean flag);

    public Chunk getChunkAtWorldCoords(BlockPosition blockposition) {
        return this.getChunkAt(blockposition.getX() >> 4, blockposition.getZ() >> 4);
    }

    public Chunk getChunkAt(int i, int j) {
        return this.chunkProvider.getChunkAt(i, j);
    }

    public boolean b(int i, int j) {
        return this.isChunkLoaded(i, j, false) ? true : this.chunkProvider.e(i, j);
    }

    public boolean setTypeAndData(BlockPosition blockposition, IBlockData iblockdata, int i) {
        if (this.E(blockposition)) {
            return false;
        } else if (!this.isClientSide && this.worldData.getType() == WorldType.DEBUG_ALL_BLOCK_STATES) {
            return false;
        } else {
            Chunk chunk = this.getChunkAtWorldCoords(blockposition);
            Block block = iblockdata.getBlock();
            IBlockData iblockdata1 = chunk.a(blockposition, iblockdata);

            if (iblockdata1 == null) {
                return false;
            } else {
                if (iblockdata.c() != iblockdata1.c() || iblockdata.d() != iblockdata1.d()) {
                    this.methodProfiler.a("checkLight");
                    this.w(blockposition);
                    this.methodProfiler.b();
                }

                if ((i & 2) != 0 && (!this.isClientSide || (i & 4) == 0) && chunk.isReady()) {
                    this.notify(blockposition, iblockdata1, iblockdata, i);
                }

                if (!this.isClientSide && (i & 1) != 0) {
                    this.update(blockposition, iblockdata1.getBlock(), true);
                    if (iblockdata.o()) {
                        this.updateAdjacentComparators(blockposition, block);
                    }
                } else if (!this.isClientSide && (i & 16) == 0) {
                    this.c(blockposition, block);
                }

                return true;
            }
        }
    }

    public boolean setAir(BlockPosition blockposition) {
        return this.setTypeAndData(blockposition, Blocks.AIR.getBlockData(), 3);
    }

    public boolean setAir(BlockPosition blockposition, boolean flag) {
        IBlockData iblockdata = this.getType(blockposition);
        Block block = iblockdata.getBlock();

        if (iblockdata.getMaterial() == Material.AIR) {
            return false;
        } else {
            this.triggerEffect(2001, blockposition, Block.getCombinedId(iblockdata));
            if (flag) {
                block.b(this, blockposition, iblockdata, 0);
            }

            return this.setTypeAndData(blockposition, Blocks.AIR.getBlockData(), 3);
        }
    }

    public boolean setTypeUpdate(BlockPosition blockposition, IBlockData iblockdata) {
        return this.setTypeAndData(blockposition, iblockdata, 3);
    }

    public void notify(BlockPosition blockposition, IBlockData iblockdata, IBlockData iblockdata1, int i) {
        for (int j = 0; j < this.u.size(); ++j) {
            ((IWorldAccess) this.u.get(j)).a(this, blockposition, iblockdata, iblockdata1, i);
        }

    }

    public void update(BlockPosition blockposition, Block block, boolean flag) {
        if (this.worldData.getType() != WorldType.DEBUG_ALL_BLOCK_STATES) {
            this.applyPhysics(blockposition, block, flag);
        }

    }

    public void a(int i, int j, int k, int l) {
        int i1;

        if (k > l) {
            i1 = l;
            l = k;
            k = i1;
        }

        if (this.worldProvider.m()) {
            for (i1 = k; i1 <= l; ++i1) {
                this.c(EnumSkyBlock.SKY, new BlockPosition(i, i1, j));
            }
        }

        this.b(i, k, j, i, l, j);
    }

    public void b(BlockPosition blockposition, BlockPosition blockposition1) {
        this.b(blockposition.getX(), blockposition.getY(), blockposition.getZ(), blockposition1.getX(), blockposition1.getY(), blockposition1.getZ());
    }

    public void b(int i, int j, int k, int l, int i1, int j1) {
        for (int k1 = 0; k1 < this.u.size(); ++k1) {
            ((IWorldAccess) this.u.get(k1)).a(i, j, k, l, i1, j1);
        }

    }

    public void c(BlockPosition blockposition, Block block) {
        this.b(blockposition.west(), block, blockposition);
        this.b(blockposition.east(), block, blockposition);
        this.b(blockposition.down(), block, blockposition);
        this.b(blockposition.up(), block, blockposition);
        this.b(blockposition.north(), block, blockposition);
        this.b(blockposition.south(), block, blockposition);
    }

    public void applyPhysics(BlockPosition blockposition, Block block, boolean flag) {
        this.a(blockposition.west(), block, blockposition);
        this.a(blockposition.east(), block, blockposition);
        this.a(blockposition.down(), block, blockposition);
        this.a(blockposition.up(), block, blockposition);
        this.a(blockposition.north(), block, blockposition);
        this.a(blockposition.south(), block, blockposition);
        if (flag) {
            this.c(blockposition, block);
        }

    }

    public void a(BlockPosition blockposition, Block block, EnumDirection enumdirection) {
        if (enumdirection != EnumDirection.WEST) {
            this.a(blockposition.west(), block, blockposition);
        }

        if (enumdirection != EnumDirection.EAST) {
            this.a(blockposition.east(), block, blockposition);
        }

        if (enumdirection != EnumDirection.DOWN) {
            this.a(blockposition.down(), block, blockposition);
        }

        if (enumdirection != EnumDirection.UP) {
            this.a(blockposition.up(), block, blockposition);
        }

        if (enumdirection != EnumDirection.NORTH) {
            this.a(blockposition.north(), block, blockposition);
        }

        if (enumdirection != EnumDirection.SOUTH) {
            this.a(blockposition.south(), block, blockposition);
        }

    }

    public void a(BlockPosition blockposition, final Block block, BlockPosition blockposition1) {
        if (!this.isClientSide) {
            IBlockData iblockdata = this.getType(blockposition);

            try {
                iblockdata.doPhysics(this, blockposition, block, blockposition1);
            } catch (Throwable throwable) {
                CrashReport crashreport = CrashReport.a(throwable, "Exception while updating neighbours");
                CrashReportSystemDetails crashreportsystemdetails = crashreport.a("Block being updated");

                crashreportsystemdetails.a("Source block type", new CrashReportCallable() {
                    public String a() throws Exception {
                        try {
                            return String.format("ID #%d (%s // %s)", new Object[] { Integer.valueOf(Block.getId(block)), block.a(), block.getClass().getCanonicalName()});
                        } catch (Throwable throwable) {
                            return "ID #" + Block.getId(block);
                        }
                    }

                    public Object call() throws Exception {
                        return this.a();
                    }
                });
                CrashReportSystemDetails.a(crashreportsystemdetails, blockposition, iblockdata);
                throw new ReportedException(crashreport);
            }
        }
    }

    public void b(BlockPosition blockposition, final Block block, BlockPosition blockposition1) {
        if (!this.isClientSide) {
            IBlockData iblockdata = this.getType(blockposition);

            if (iblockdata.getBlock() == Blocks.dk) {
                try {
                    ((BlockObserver) iblockdata.getBlock()).b(iblockdata, this, blockposition, block, blockposition1);
                } catch (Throwable throwable) {
                    CrashReport crashreport = CrashReport.a(throwable, "Exception while updating neighbours");
                    CrashReportSystemDetails crashreportsystemdetails = crashreport.a("Block being updated");

                    crashreportsystemdetails.a("Source block type", new CrashReportCallable() {
                        public String a() throws Exception {
                            try {
                                return String.format("ID #%d (%s // %s)", new Object[] { Integer.valueOf(Block.getId(block)), block.a(), block.getClass().getCanonicalName()});
                            } catch (Throwable throwable) {
                                return "ID #" + Block.getId(block);
                            }
                        }

                        public Object call() throws Exception {
                            return this.a();
                        }
                    });
                    CrashReportSystemDetails.a(crashreportsystemdetails, blockposition, iblockdata);
                    throw new ReportedException(crashreport);
                }
            }
        }
    }

    public boolean a(BlockPosition blockposition, Block block) {
        return false;
    }

    public boolean h(BlockPosition blockposition) {
        return this.getChunkAtWorldCoords(blockposition).c(blockposition);
    }

    public boolean i(BlockPosition blockposition) {
        if (blockposition.getY() >= this.K()) {
            return this.h(blockposition);
        } else {
            BlockPosition blockposition1 = new BlockPosition(blockposition.getX(), this.K(), blockposition.getZ());

            if (!this.h(blockposition1)) {
                return false;
            } else {
                for (blockposition1 = blockposition1.down(); blockposition1.getY() > blockposition.getY(); blockposition1 = blockposition1.down()) {
                    IBlockData iblockdata = this.getType(blockposition1);

                    if (iblockdata.c() > 0 && !iblockdata.getMaterial().isLiquid()) {
                        return false;
                    }
                }

                return true;
            }
        }
    }

    public int j(BlockPosition blockposition) {
        if (blockposition.getY() < 0) {
            return 0;
        } else {
            if (blockposition.getY() >= 256) {
                blockposition = new BlockPosition(blockposition.getX(), 255, blockposition.getZ());
            }

            return this.getChunkAtWorldCoords(blockposition).a(blockposition, 0);
        }
    }

    public int getLightLevel(BlockPosition blockposition) {
        return this.c(blockposition, true);
    }

    public int c(BlockPosition blockposition, boolean flag) {
        if (blockposition.getX() >= -30000000 && blockposition.getZ() >= -30000000 && blockposition.getX() < 30000000 && blockposition.getZ() < 30000000) {
            if (flag && this.getType(blockposition).f()) {
                int i = this.c(blockposition.up(), false);
                int j = this.c(blockposition.east(), false);
                int k = this.c(blockposition.west(), false);
                int l = this.c(blockposition.south(), false);
                int i1 = this.c(blockposition.north(), false);

                if (j > i) {
                    i = j;
                }

                if (k > i) {
                    i = k;
                }

                if (l > i) {
                    i = l;
                }

                if (i1 > i) {
                    i = i1;
                }

                return i;
            } else if (blockposition.getY() < 0) {
                return 0;
            } else {
                if (blockposition.getY() >= 256) {
                    blockposition = new BlockPosition(blockposition.getX(), 255, blockposition.getZ());
                }

                Chunk chunk = this.getChunkAtWorldCoords(blockposition);

                return chunk.a(blockposition, this.J);
            }
        } else {
            return 15;
        }
    }

    public BlockPosition getHighestBlockYAt(BlockPosition blockposition) {
        return new BlockPosition(blockposition.getX(), this.c(blockposition.getX(), blockposition.getZ()), blockposition.getZ());
    }

    public int c(int i, int j) {
        int k;

        if (i >= -30000000 && j >= -30000000 && i < 30000000 && j < 30000000) {
            if (this.isChunkLoaded(i >> 4, j >> 4, true)) {
                k = this.getChunkAt(i >> 4, j >> 4).b(i & 15, j & 15);
            } else {
                k = 0;
            }
        } else {
            k = this.K() + 1;
        }

        return k;
    }

    @Deprecated
    public int d(int i, int j) {
        if (i >= -30000000 && j >= -30000000 && i < 30000000 && j < 30000000) {
            if (!this.isChunkLoaded(i >> 4, j >> 4, true)) {
                return 0;
            } else {
                Chunk chunk = this.getChunkAt(i >> 4, j >> 4);

                return chunk.w();
            }
        } else {
            return this.K() + 1;
        }
    }

    public int getBrightness(EnumSkyBlock enumskyblock, BlockPosition blockposition) {
        if (blockposition.getY() < 0) {
            blockposition = new BlockPosition(blockposition.getX(), 0, blockposition.getZ());
        }

        if (!this.isValidLocation(blockposition)) {
            return enumskyblock.c;
        } else if (!this.isLoaded(blockposition)) {
            return enumskyblock.c;
        } else {
            Chunk chunk = this.getChunkAtWorldCoords(blockposition);

            return chunk.getBrightness(enumskyblock, blockposition);
        }
    }

    public void a(EnumSkyBlock enumskyblock, BlockPosition blockposition, int i) {
        if (this.isValidLocation(blockposition)) {
            if (this.isLoaded(blockposition)) {
                Chunk chunk = this.getChunkAtWorldCoords(blockposition);

                chunk.a(enumskyblock, blockposition, i);
                this.m(blockposition);
            }
        }
    }

    public void m(BlockPosition blockposition) {
        for (int i = 0; i < this.u.size(); ++i) {
            ((IWorldAccess) this.u.get(i)).a(blockposition);
        }

    }

    public float n(BlockPosition blockposition) {
        return this.worldProvider.o()[this.getLightLevel(blockposition)];
    }

    public IBlockData getType(BlockPosition blockposition) {
        if (this.E(blockposition)) {
            return Blocks.AIR.getBlockData();
        } else {
            Chunk chunk = this.getChunkAtWorldCoords(blockposition);

            return chunk.getBlockData(blockposition);
        }
    }

    public boolean B() {
        return this.J < 4;
    }

    @Nullable
    public MovingObjectPosition rayTrace(Vec3D vec3d, Vec3D vec3d1) {
        return this.rayTrace(vec3d, vec3d1, false, false, false);
    }

    @Nullable
    public MovingObjectPosition rayTrace(Vec3D vec3d, Vec3D vec3d1, boolean flag) {
        return this.rayTrace(vec3d, vec3d1, flag, false, false);
    }

    @Nullable
    public MovingObjectPosition rayTrace(Vec3D vec3d, Vec3D vec3d1, boolean flag, boolean flag1, boolean flag2) {
        if (!Double.isNaN(vec3d.x) && !Double.isNaN(vec3d.y) && !Double.isNaN(vec3d.z)) {
            if (!Double.isNaN(vec3d1.x) && !Double.isNaN(vec3d1.y) && !Double.isNaN(vec3d1.z)) {
                int i = MathHelper.floor(vec3d1.x);
                int j = MathHelper.floor(vec3d1.y);
                int k = MathHelper.floor(vec3d1.z);
                int l = MathHelper.floor(vec3d.x);
                int i1 = MathHelper.floor(vec3d.y);
                int j1 = MathHelper.floor(vec3d.z);
                BlockPosition blockposition = new BlockPosition(l, i1, j1);
                IBlockData iblockdata = this.getType(blockposition);
                Block block = iblockdata.getBlock();

                if ((!flag1 || iblockdata.c(this, blockposition) != Block.k) && block.a(iblockdata, flag)) {
                    MovingObjectPosition movingobjectposition = iblockdata.a(this, blockposition, vec3d, vec3d1);

                    if (movingobjectposition != null) {
                        return movingobjectposition;
                    }
                }

                MovingObjectPosition movingobjectposition1 = null;
                int k1 = 200;

                while (k1-- >= 0) {
                    if (Double.isNaN(vec3d.x) || Double.isNaN(vec3d.y) || Double.isNaN(vec3d.z)) {
                        return null;
                    }

                    if (l == i && i1 == j && j1 == k) {
                        return flag2 ? movingobjectposition1 : null;
                    }

                    boolean flag3 = true;
                    boolean flag4 = true;
                    boolean flag5 = true;
                    double d0 = 999.0D;
                    double d1 = 999.0D;
                    double d2 = 999.0D;

                    if (i > l) {
                        d0 = (double) l + 1.0D;
                    } else if (i < l) {
                        d0 = (double) l + 0.0D;
                    } else {
                        flag3 = false;
                    }

                    if (j > i1) {
                        d1 = (double) i1 + 1.0D;
                    } else if (j < i1) {
                        d1 = (double) i1 + 0.0D;
                    } else {
                        flag4 = false;
                    }

                    if (k > j1) {
                        d2 = (double) j1 + 1.0D;
                    } else if (k < j1) {
                        d2 = (double) j1 + 0.0D;
                    } else {
                        flag5 = false;
                    }

                    double d3 = 999.0D;
                    double d4 = 999.0D;
                    double d5 = 999.0D;
                    double d6 = vec3d1.x - vec3d.x;
                    double d7 = vec3d1.y - vec3d.y;
                    double d8 = vec3d1.z - vec3d.z;

                    if (flag3) {
                        d3 = (d0 - vec3d.x) / d6;
                    }

                    if (flag4) {
                        d4 = (d1 - vec3d.y) / d7;
                    }

                    if (flag5) {
                        d5 = (d2 - vec3d.z) / d8;
                    }

                    if (d3 == -0.0D) {
                        d3 = -1.0E-4D;
                    }

                    if (d4 == -0.0D) {
                        d4 = -1.0E-4D;
                    }

                    if (d5 == -0.0D) {
                        d5 = -1.0E-4D;
                    }

                    EnumDirection enumdirection;

                    if (d3 < d4 && d3 < d5) {
                        enumdirection = i > l ? EnumDirection.WEST : EnumDirection.EAST;
                        vec3d = new Vec3D(d0, vec3d.y + d7 * d3, vec3d.z + d8 * d3);
                    } else if (d4 < d5) {
                        enumdirection = j > i1 ? EnumDirection.DOWN : EnumDirection.UP;
                        vec3d = new Vec3D(vec3d.x + d6 * d4, d1, vec3d.z + d8 * d4);
                    } else {
                        enumdirection = k > j1 ? EnumDirection.NORTH : EnumDirection.SOUTH;
                        vec3d = new Vec3D(vec3d.x + d6 * d5, vec3d.y + d7 * d5, d2);
                    }

                    l = MathHelper.floor(vec3d.x) - (enumdirection == EnumDirection.EAST ? 1 : 0);
                    i1 = MathHelper.floor(vec3d.y) - (enumdirection == EnumDirection.UP ? 1 : 0);
                    j1 = MathHelper.floor(vec3d.z) - (enumdirection == EnumDirection.SOUTH ? 1 : 0);
                    blockposition = new BlockPosition(l, i1, j1);
                    IBlockData iblockdata1 = this.getType(blockposition);
                    Block block1 = iblockdata1.getBlock();

                    if (!flag1 || iblockdata1.getMaterial() == Material.PORTAL || iblockdata1.c(this, blockposition) != Block.k) {
                        if (block1.a(iblockdata1, flag)) {
                            MovingObjectPosition movingobjectposition2 = iblockdata1.a(this, blockposition, vec3d, vec3d1);

                            if (movingobjectposition2 != null) {
                                return movingobjectposition2;
                            }
                        } else {
                            movingobjectposition1 = new MovingObjectPosition(MovingObjectPosition.EnumMovingObjectType.MISS, vec3d, enumdirection, blockposition);
                        }
                    }
                }

                return flag2 ? movingobjectposition1 : null;
            } else {
                return null;
            }
        } else {
            return null;
        }
    }

    public void a(@Nullable EntityHuman entityhuman, BlockPosition blockposition, SoundEffect soundeffect, SoundCategory soundcategory, float f, float f1) {
        this.a(entityhuman, (double) blockposition.getX() + 0.5D, (double) blockposition.getY() + 0.5D, (double) blockposition.getZ() + 0.5D, soundeffect, soundcategory, f, f1);
    }

    public void a(@Nullable EntityHuman entityhuman, double d0, double d1, double d2, SoundEffect soundeffect, SoundCategory soundcategory, float f, float f1) {
        for (int i = 0; i < this.u.size(); ++i) {
            ((IWorldAccess) this.u.get(i)).a(entityhuman, soundeffect, soundcategory, d0, d1, d2, f, f1);
        }

    }

    public void a(double d0, double d1, double d2, SoundEffect soundeffect, SoundCategory soundcategory, float f, float f1, boolean flag) {}

    public void a(BlockPosition blockposition, @Nullable SoundEffect soundeffect) {
        for (int i = 0; i < this.u.size(); ++i) {
            ((IWorldAccess) this.u.get(i)).a(soundeffect, blockposition);
        }

    }

    public void addParticle(EnumParticle enumparticle, double d0, double d1, double d2, double d3, double d4, double d5, int... aint) {
        this.a(enumparticle.c(), enumparticle.e(), d0, d1, d2, d3, d4, d5, aint);
    }

    public void a(int i, double d0, double d1, double d2, double d3, double d4, double d5, int... aint) {
        for (int j = 0; j < this.u.size(); ++j) {
            ((IWorldAccess) this.u.get(j)).a(i, false, true, d0, d1, d2, d3, d4, d5, aint);
        }

    }

    private void a(int i, boolean flag, double d0, double d1, double d2, double d3, double d4, double d5, int... aint) {
        for (int j = 0; j < this.u.size(); ++j) {
            ((IWorldAccess) this.u.get(j)).a(i, flag, d0, d1, d2, d3, d4, d5, aint);
        }

    }

    public boolean strikeLightning(Entity entity) {
        this.j.add(entity);
        return true;
    }

    public boolean addEntity(Entity entity) {
        int i = MathHelper.floor(entity.locX / 16.0D);
        int j = MathHelper.floor(entity.locZ / 16.0D);
        boolean flag = entity.attachedToPlayer;

        if (entity instanceof EntityHuman) {
            flag = true;
        }

        if (!flag && !this.isChunkLoaded(i, j, false)) {
            return false;
        } else {
            if (entity instanceof EntityHuman) {
                EntityHuman entityhuman = (EntityHuman) entity;

                this.players.add(entityhuman);
                this.everyoneSleeping();
            }

            this.getChunkAt(i, j).a(entity);
            this.entityList.add(entity);
            this.b(entity);
            return true;
        }
    }

    protected void b(Entity entity) {
        for (int i = 0; i < this.u.size(); ++i) {
            ((IWorldAccess) this.u.get(i)).a(entity);
        }

    }

    protected void c(Entity entity) {
        for (int i = 0; i < this.u.size(); ++i) {
            ((IWorldAccess) this.u.get(i)).b(entity);
        }

    }

    public void kill(Entity entity) {
        if (entity.isVehicle()) {
            entity.az();
        }

        if (entity.isPassenger()) {
            entity.stopRiding();
        }

        entity.die();
        if (entity instanceof EntityHuman) {
            this.players.remove(entity);
            this.everyoneSleeping();
            this.c(entity);
        }

    }

    public void removeEntity(Entity entity) {
        entity.b(false);
        entity.die();
        if (entity instanceof EntityHuman) {
            this.players.remove(entity);
            this.everyoneSleeping();
        }

        int i = entity.ab;
        int j = entity.ad;

        if (entity.aa && this.isChunkLoaded(i, j, true)) {
            this.getChunkAt(i, j).b(entity);
        }

        this.entityList.remove(entity);
        this.c(entity);
    }

    public void addIWorldAccess(IWorldAccess iworldaccess) {
        this.u.add(iworldaccess);
    }

    public List<AxisAlignedBB> getCubes(@Nullable Entity entity, AxisAlignedBB axisalignedbb) {
        ArrayList arraylist = Lists.newArrayList();
        int i = MathHelper.floor(axisalignedbb.a) - 1;
        int j = MathHelper.f(axisalignedbb.d) + 1;
        int k = MathHelper.floor(axisalignedbb.b) - 1;
        int l = MathHelper.f(axisalignedbb.e) + 1;
        int i1 = MathHelper.floor(axisalignedbb.c) - 1;
        int j1 = MathHelper.f(axisalignedbb.f) + 1;
        WorldBorder worldborder = this.getWorldBorder();
        boolean flag = entity != null && entity.br();
        boolean flag1 = entity != null && this.a(worldborder, entity);
        IBlockData iblockdata = Blocks.STONE.getBlockData();
        BlockPosition.PooledBlockPosition blockposition_pooledblockposition = BlockPosition.PooledBlockPosition.s();

        int k1;

        for (int l1 = i; l1 < j; ++l1) {
            for (k1 = i1; k1 < j1; ++k1) {
                int i2 = (l1 != i && l1 != j - 1 ? 0 : 1) + (k1 != i1 && k1 != j1 - 1 ? 0 : 1);

                if (i2 != 2 && this.isLoaded(blockposition_pooledblockposition.f(l1, 64, k1))) {
                    for (int j2 = k; j2 < l; ++j2) {
                        if (i2 <= 0 || j2 != k && j2 != l - 1) {
                            blockposition_pooledblockposition.f(l1, j2, k1);
                            if (entity != null) {
                                if (flag && flag1) {
                                    entity.k(false);
                                } else if (!flag && !flag1) {
                                    entity.k(true);
                                }
                            }

                            IBlockData iblockdata1 = iblockdata;

                            if (worldborder.a((BlockPosition) blockposition_pooledblockposition) || !flag1) {
                                iblockdata1 = this.getType(blockposition_pooledblockposition);
                            }

                            iblockdata1.a(this, blockposition_pooledblockposition, axisalignedbb, arraylist, entity);
                        }
                    }
                }
            }
        }

        blockposition_pooledblockposition.t();
        if (entity != null) {
            List list = this.getEntities(entity, axisalignedbb.g(0.25D));

            for (k1 = 0; k1 < list.size(); ++k1) {
                Entity entity1 = (Entity) list.get(k1);

                if (!entity.x(entity1)) {
                    AxisAlignedBB axisalignedbb1 = entity1.ag();

                    if (axisalignedbb1 != null && axisalignedbb1.c(axisalignedbb)) {
                        arraylist.add(axisalignedbb1);
                    }

                    axisalignedbb1 = entity.j(entity1);
                    if (axisalignedbb1 != null && axisalignedbb1.c(axisalignedbb)) {
                        arraylist.add(axisalignedbb1);
                    }
                }
            }
        }

        return arraylist;
    }

    public boolean a(WorldBorder worldborder, Entity entity) {
        double d0 = worldborder.b();
        double d1 = worldborder.c();
        double d2 = worldborder.d();
        double d3 = worldborder.e();

        if (entity.br()) {
            ++d0;
            ++d1;
            --d2;
            --d3;
        } else {
            --d0;
            --d1;
            ++d2;
            ++d3;
        }

        return entity.locX > d0 && entity.locX < d2 && entity.locZ > d1 && entity.locZ < d3;
    }

    public boolean b(AxisAlignedBB axisalignedbb) {
        ArrayList arraylist = Lists.newArrayList();
        int i = MathHelper.floor(axisalignedbb.a) - 1;
        int j = MathHelper.f(axisalignedbb.d) + 1;
        int k = MathHelper.floor(axisalignedbb.b) - 1;
        int l = MathHelper.f(axisalignedbb.e) + 1;
        int i1 = MathHelper.floor(axisalignedbb.c) - 1;
        int j1 = MathHelper.f(axisalignedbb.f) + 1;
        BlockPosition.PooledBlockPosition blockposition_pooledblockposition = BlockPosition.PooledBlockPosition.s();

        try {
            for (int k1 = i; k1 < j; ++k1) {
                for (int l1 = i1; l1 < j1; ++l1) {
                    int i2 = (k1 != i && k1 != j - 1 ? 0 : 1) + (l1 != i1 && l1 != j1 - 1 ? 0 : 1);

                    if (i2 != 2 && this.isLoaded(blockposition_pooledblockposition.f(k1, 64, l1))) {
                        for (int j2 = k; j2 < l; ++j2) {
                            if (i2 <= 0 || j2 != k && j2 != l - 1) {
                                blockposition_pooledblockposition.f(k1, j2, l1);
                                if (k1 < -30000000 || k1 >= 30000000 || l1 < -30000000 || l1 >= 30000000) {
                                    boolean flag = true;

                                    return flag;
                                }

                                IBlockData iblockdata = this.getType(blockposition_pooledblockposition);

                                iblockdata.a(this, blockposition_pooledblockposition, axisalignedbb, arraylist, (Entity) null);
                                if (!arraylist.isEmpty()) {
                                    boolean flag1 = true;

                                    return flag1;
                                }
                            }
                        }
                    }
                }
            }

            return false;
        } finally {
            blockposition_pooledblockposition.t();
        }
    }

    public int a(float f) {
        float f1 = this.c(f);
        float f2 = 1.0F - (MathHelper.cos(f1 * 6.2831855F) * 2.0F + 0.5F);

        f2 = MathHelper.a(f2, 0.0F, 1.0F);
        f2 = 1.0F - f2;
        f2 = (float) ((double) f2 * (1.0D - (double) (this.j(f) * 5.0F) / 16.0D));
        f2 = (float) ((double) f2 * (1.0D - (double) (this.h(f) * 5.0F) / 16.0D));
        f2 = 1.0F - f2;
        return (int) (f2 * 11.0F);
    }

    public float c(float f) {
        return this.worldProvider.a(this.worldData.getDayTime(), f);
    }

    public float E() {
        return WorldProvider.a[this.worldProvider.a(this.worldData.getDayTime())];
    }

    public float d(float f) {
        float f1 = this.c(f);

        return f1 * 6.2831855F;
    }

    public BlockPosition p(BlockPosition blockposition) {
        return this.getChunkAtWorldCoords(blockposition).f(blockposition);
    }

    public BlockPosition q(BlockPosition blockposition) {
        Chunk chunk = this.getChunkAtWorldCoords(blockposition);

        BlockPosition blockposition1;
        BlockPosition blockposition2;

        for (blockposition1 = new BlockPosition(blockposition.getX(), chunk.g() + 16, blockposition.getZ()); blockposition1.getY() >= 0; blockposition1 = blockposition2) {
            blockposition2 = blockposition1.down();
            Material material = chunk.getBlockData(blockposition2).getMaterial();

            if (material.isSolid() && material != Material.LEAVES) {
                break;
            }
        }

        return blockposition1;
    }

    public boolean b(BlockPosition blockposition, Block block) {
        return true;
    }

    public void a(BlockPosition blockposition, Block block, int i) {}

    public void a(BlockPosition blockposition, Block block, int i, int j) {}

    public void b(BlockPosition blockposition, Block block, int i, int j) {}

    public void tickEntities() {
        this.methodProfiler.a("entities");
        this.methodProfiler.a("global");

        int i;
        Entity entity;

        for (i = 0; i < this.j.size(); ++i) {
            entity = (Entity) this.j.get(i);

            try {
                ++entity.ticksLived;
                entity.A_();
            } catch (Throwable throwable) {
                CrashReport crashreport = CrashReport.a(throwable, "Ticking entity");
                CrashReportSystemDetails crashreportsystemdetails = crashreport.a("Entity being ticked");

                if (entity == null) {
                    crashreportsystemdetails.a("Entity", (Object) "~~NULL~~");
                } else {
                    entity.appendEntityCrashDetails(crashreportsystemdetails);
                }

                throw new ReportedException(crashreport);
            }

            if (entity.dead) {
                this.j.remove(i--);
            }
        }

        this.methodProfiler.c("remove");
        this.entityList.removeAll(this.f);

        int j;

        for (i = 0; i < this.f.size(); ++i) {
            entity = (Entity) this.f.get(i);
            int k = entity.ab;

            j = entity.ad;
            if (entity.aa && this.isChunkLoaded(k, j, true)) {
                this.getChunkAt(k, j).b(entity);
            }
        }

        for (i = 0; i < this.f.size(); ++i) {
            this.c((Entity) this.f.get(i));
        }

        this.f.clear();
        this.l();
        this.methodProfiler.c("regular");

        CrashReportSystemDetails crashreportsystemdetails1;
        CrashReport crashreport1;

        for (i = 0; i < this.entityList.size(); ++i) {
            entity = (Entity) this.entityList.get(i);
            Entity entity1 = entity.bB();

            if (entity1 != null) {
                if (!entity1.dead && entity1.w(entity)) {
                    continue;
                }

                entity.stopRiding();
            }

            this.methodProfiler.a("tick");
            if (!entity.dead && !(entity instanceof EntityPlayer)) {
                try {
                    this.g(entity);
                } catch (Throwable throwable1) {
                    crashreport1 = CrashReport.a(throwable1, "Ticking entity");
                    crashreportsystemdetails1 = crashreport1.a("Entity being ticked");
                    entity.appendEntityCrashDetails(crashreportsystemdetails1);
                    throw new ReportedException(crashreport1);
                }
            }

            this.methodProfiler.b();
            this.methodProfiler.a("remove");
            if (entity.dead) {
                j = entity.ab;
                int l = entity.ad;

                if (entity.aa && this.isChunkLoaded(j, l, true)) {
                    this.getChunkAt(j, l).b(entity);
                }

                this.entityList.remove(i--);
                this.c(entity);
            }

            this.methodProfiler.b();
        }

        this.methodProfiler.c("blockEntities");
        this.M = true;
        Iterator iterator = this.tileEntityListTick.iterator();

        while (iterator.hasNext()) {
            TileEntity tileentity = (TileEntity) iterator.next();

            if (!tileentity.y() && tileentity.u()) {
                BlockPosition blockposition = tileentity.getPosition();

                if (this.isLoaded(blockposition) && this.N.a(blockposition)) {
                    try {
                        this.methodProfiler.a(tileentity.getClass().getSimpleName());
                        ((ITickable) tileentity).F_();
                        this.methodProfiler.b();
                    } catch (Throwable throwable2) {
                        crashreport1 = CrashReport.a(throwable2, "Ticking block entity");
                        crashreportsystemdetails1 = crashreport1.a("Block entity being ticked");
                        tileentity.a(crashreportsystemdetails1);
                        throw new ReportedException(crashreport1);
                    }
                }
            }

            if (tileentity.y()) {
                iterator.remove();
                this.tileEntityList.remove(tileentity);
                if (this.isLoaded(tileentity.getPosition())) {
                    this.getChunkAtWorldCoords(tileentity.getPosition()).d(tileentity.getPosition());
                }
            }
        }

        this.M = false;
        if (!this.tileEntityListUnload.isEmpty()) {
            this.tileEntityListTick.removeAll(this.tileEntityListUnload);
            this.tileEntityList.removeAll(this.tileEntityListUnload);
            this.tileEntityListUnload.clear();
        }

        this.methodProfiler.c("pendingBlockEntities");
        if (!this.b.isEmpty()) {
            for (int i1 = 0; i1 < this.b.size(); ++i1) {
                TileEntity tileentity1 = (TileEntity) this.b.get(i1);

                if (!tileentity1.y()) {
                    if (!this.tileEntityList.contains(tileentity1)) {
                        this.a(tileentity1);
                    }

                    if (this.isLoaded(tileentity1.getPosition())) {
                        Chunk chunk = this.getChunkAtWorldCoords(tileentity1.getPosition());
                        IBlockData iblockdata = chunk.getBlockData(tileentity1.getPosition());

                        chunk.a(tileentity1.getPosition(), tileentity1);
                        this.notify(tileentity1.getPosition(), iblockdata, iblockdata, 3);
                    }
                }
            }

            this.b.clear();
        }

        this.methodProfiler.b();
        this.methodProfiler.b();
    }

    protected void l() {}

    public boolean a(TileEntity tileentity) {
        boolean flag = this.tileEntityList.add(tileentity);

        if (flag && tileentity instanceof ITickable) {
            this.tileEntityListTick.add(tileentity);
        }

        if (this.isClientSide) {
            BlockPosition blockposition = tileentity.getPosition();
            IBlockData iblockdata = this.getType(blockposition);

            this.notify(blockposition, iblockdata, iblockdata, 2);
        }

        return flag;
    }

    public void b(Collection<TileEntity> collection) {
        if (this.M) {
            this.b.addAll(collection);
        } else {
            Iterator iterator = collection.iterator();

            while (iterator.hasNext()) {
                TileEntity tileentity = (TileEntity) iterator.next();

                this.a(tileentity);
            }
        }

    }

    public void g(Entity entity) {
        this.entityJoinedWorld(entity, true);
    }

    public void entityJoinedWorld(Entity entity, boolean flag) {
        int i = MathHelper.floor(entity.locX);
        int j = MathHelper.floor(entity.locZ);
        boolean flag1 = true;

        if (!flag || this.isAreaLoaded(i - 32, 0, j - 32, i + 32, 0, j + 32, true)) {
            entity.M = entity.locX;
            entity.N = entity.locY;
            entity.O = entity.locZ;
            entity.lastYaw = entity.yaw;
            entity.lastPitch = entity.pitch;
            if (flag && entity.aa) {
                ++entity.ticksLived;
                if (entity.isPassenger()) {
                    entity.aw();
                } else {
                    entity.A_();
                }
            }

            this.methodProfiler.a("chunkCheck");
            if (Double.isNaN(entity.locX) || Double.isInfinite(entity.locX)) {
                entity.locX = entity.M;
            }

            if (Double.isNaN(entity.locY) || Double.isInfinite(entity.locY)) {
                entity.locY = entity.N;
            }

            if (Double.isNaN(entity.locZ) || Double.isInfinite(entity.locZ)) {
                entity.locZ = entity.O;
            }

            if (Double.isNaN((double) entity.pitch) || Double.isInfinite((double) entity.pitch)) {
                entity.pitch = entity.lastPitch;
            }

            if (Double.isNaN((double) entity.yaw) || Double.isInfinite((double) entity.yaw)) {
                entity.yaw = entity.lastYaw;
            }

            int k = MathHelper.floor(entity.locX / 16.0D);
            int l = MathHelper.floor(entity.locY / 16.0D);
            int i1 = MathHelper.floor(entity.locZ / 16.0D);

            if (!entity.aa || entity.ab != k || entity.ac != l || entity.ad != i1) {
                if (entity.aa && this.isChunkLoaded(entity.ab, entity.ad, true)) {
                    this.getChunkAt(entity.ab, entity.ad).a(entity, entity.ac);
                }

                if (!entity.bv() && !this.isChunkLoaded(k, i1, true)) {
                    entity.aa = false;
                } else {
                    this.getChunkAt(k, i1).a(entity);
                }
            }

            this.methodProfiler.b();
            if (flag && entity.aa) {
                Iterator iterator = entity.bx().iterator();

                while (iterator.hasNext()) {
                    Entity entity1 = (Entity) iterator.next();

                    if (!entity1.dead && entity1.bB() == entity) {
                        this.g(entity1);
                    } else {
                        entity1.stopRiding();
                    }
                }
            }

        }
    }

    public boolean c(AxisAlignedBB axisalignedbb) {
        return this.a(axisalignedbb, (Entity) null);
    }

    public boolean a(AxisAlignedBB axisalignedbb, @Nullable Entity entity) {
        List list = this.getEntities((Entity) null, axisalignedbb);

        for (int i = 0; i < list.size(); ++i) {
            Entity entity1 = (Entity) list.get(i);

            if (!entity1.dead && entity1.i && entity1 != entity && (entity == null || entity1.x(entity))) {
                return false;
            }
        }

        return true;
    }

    public boolean d(AxisAlignedBB axisalignedbb) {
        int i = MathHelper.floor(axisalignedbb.a);
        int j = MathHelper.f(axisalignedbb.d);
        int k = MathHelper.floor(axisalignedbb.b);
        int l = MathHelper.f(axisalignedbb.e);
        int i1 = MathHelper.floor(axisalignedbb.c);
        int j1 = MathHelper.f(axisalignedbb.f);
        BlockPosition.PooledBlockPosition blockposition_pooledblockposition = BlockPosition.PooledBlockPosition.s();

        for (int k1 = i; k1 < j; ++k1) {
            for (int l1 = k; l1 < l; ++l1) {
                for (int i2 = i1; i2 < j1; ++i2) {
                    IBlockData iblockdata = this.getType(blockposition_pooledblockposition.f(k1, l1, i2));

                    if (iblockdata.getMaterial() != Material.AIR) {
                        blockposition_pooledblockposition.t();
                        return true;
                    }
                }
            }
        }

        blockposition_pooledblockposition.t();
        return false;
    }

    public boolean containsLiquid(AxisAlignedBB axisalignedbb) {
        int i = MathHelper.floor(axisalignedbb.a);
        int j = MathHelper.f(axisalignedbb.d);
        int k = MathHelper.floor(axisalignedbb.b);
        int l = MathHelper.f(axisalignedbb.e);
        int i1 = MathHelper.floor(axisalignedbb.c);
        int j1 = MathHelper.f(axisalignedbb.f);
        BlockPosition.PooledBlockPosition blockposition_pooledblockposition = BlockPosition.PooledBlockPosition.s();

        for (int k1 = i; k1 < j; ++k1) {
            for (int l1 = k; l1 < l; ++l1) {
                for (int i2 = i1; i2 < j1; ++i2) {
                    IBlockData iblockdata = this.getType(blockposition_pooledblockposition.f(k1, l1, i2));

                    if (iblockdata.getMaterial().isLiquid()) {
                        blockposition_pooledblockposition.t();
                        return true;
                    }
                }
            }
        }

        blockposition_pooledblockposition.t();
        return false;
    }

    public boolean f(AxisAlignedBB axisalignedbb) {
        int i = MathHelper.floor(axisalignedbb.a);
        int j = MathHelper.f(axisalignedbb.d);
        int k = MathHelper.floor(axisalignedbb.b);
        int l = MathHelper.f(axisalignedbb.e);
        int i1 = MathHelper.floor(axisalignedbb.c);
        int j1 = MathHelper.f(axisalignedbb.f);

        if (this.isAreaLoaded(i, k, i1, j, l, j1, true)) {
            BlockPosition.PooledBlockPosition blockposition_pooledblockposition = BlockPosition.PooledBlockPosition.s();
            int k1 = i;

            while (true) {
                if (k1 >= j) {
                    blockposition_pooledblockposition.t();
                    break;
                }

                for (int l1 = k; l1 < l; ++l1) {
                    for (int i2 = i1; i2 < j1; ++i2) {
                        Block block = this.getType(blockposition_pooledblockposition.f(k1, l1, i2)).getBlock();

                        if (block == Blocks.FIRE || block == Blocks.FLOWING_LAVA || block == Blocks.LAVA) {
                            blockposition_pooledblockposition.t();
                            return true;
                        }
                    }
                }

                ++k1;
            }
        }

        return false;
    }

    public boolean a(AxisAlignedBB axisalignedbb, Material material, Entity entity) {
        int i = MathHelper.floor(axisalignedbb.a);
        int j = MathHelper.f(axisalignedbb.d);
        int k = MathHelper.floor(axisalignedbb.b);
        int l = MathHelper.f(axisalignedbb.e);
        int i1 = MathHelper.floor(axisalignedbb.c);
        int j1 = MathHelper.f(axisalignedbb.f);

        if (!this.isAreaLoaded(i, k, i1, j, l, j1, true)) {
            return false;
        } else {
            boolean flag = false;
            Vec3D vec3d = Vec3D.a;
            BlockPosition.PooledBlockPosition blockposition_pooledblockposition = BlockPosition.PooledBlockPosition.s();

            for (int k1 = i; k1 < j; ++k1) {
                for (int l1 = k; l1 < l; ++l1) {
                    for (int i2 = i1; i2 < j1; ++i2) {
                        blockposition_pooledblockposition.f(k1, l1, i2);
                        IBlockData iblockdata = this.getType(blockposition_pooledblockposition);
                        Block block = iblockdata.getBlock();

                        if (iblockdata.getMaterial() == material) {
                            double d0 = (double) ((float) (l1 + 1) - BlockFluids.e(((Integer) iblockdata.get(BlockFluids.LEVEL)).intValue()));

                            if ((double) l >= d0) {
                                flag = true;
                                vec3d = block.a(this, (BlockPosition) blockposition_pooledblockposition, entity, vec3d);
                            }
                        }
                    }
                }
            }

            blockposition_pooledblockposition.t();
            if (vec3d.b() > 0.0D && entity.bg()) {
                vec3d = vec3d.a();
                double d1 = 0.014D;

                entity.motX += vec3d.x * 0.014D;
                entity.motY += vec3d.y * 0.014D;
                entity.motZ += vec3d.z * 0.014D;
            }

            return flag;
        }
    }

    public boolean a(AxisAlignedBB axisalignedbb, Material material) {
        int i = MathHelper.floor(axisalignedbb.a);
        int j = MathHelper.f(axisalignedbb.d);
        int k = MathHelper.floor(axisalignedbb.b);
        int l = MathHelper.f(axisalignedbb.e);
        int i1 = MathHelper.floor(axisalignedbb.c);
        int j1 = MathHelper.f(axisalignedbb.f);
        BlockPosition.PooledBlockPosition blockposition_pooledblockposition = BlockPosition.PooledBlockPosition.s();

        for (int k1 = i; k1 < j; ++k1) {
            for (int l1 = k; l1 < l; ++l1) {
                for (int i2 = i1; i2 < j1; ++i2) {
                    if (this.getType(blockposition_pooledblockposition.f(k1, l1, i2)).getMaterial() == material) {
                        blockposition_pooledblockposition.t();
                        return true;
                    }
                }
            }
        }

        blockposition_pooledblockposition.t();
        return false;
    }

    public Explosion explode(@Nullable Entity entity, double d0, double d1, double d2, float f, boolean flag) {
        return this.createExplosion(entity, d0, d1, d2, f, false, flag);
    }

    public Explosion createExplosion(@Nullable Entity entity, double d0, double d1, double d2, float f, boolean flag, boolean flag1) {
        Explosion explosion = new Explosion(this, entity, d0, d1, d2, f, flag, flag1);

        explosion.a();
        explosion.a(true);
        return explosion;
    }

    public float a(Vec3D vec3d, AxisAlignedBB axisalignedbb) {
        double d0 = 1.0D / ((axisalignedbb.d - axisalignedbb.a) * 2.0D + 1.0D);
        double d1 = 1.0D / ((axisalignedbb.e - axisalignedbb.b) * 2.0D + 1.0D);
        double d2 = 1.0D / ((axisalignedbb.f - axisalignedbb.c) * 2.0D + 1.0D);
        double d3 = (1.0D - Math.floor(1.0D / d0) * d0) / 2.0D;
        double d4 = (1.0D - Math.floor(1.0D / d2) * d2) / 2.0D;

        if (d0 >= 0.0D && d1 >= 0.0D && d2 >= 0.0D) {
            int i = 0;
            int j = 0;

            for (float f = 0.0F; f <= 1.0F; f = (float) ((double) f + d0)) {
                for (float f1 = 0.0F; f1 <= 1.0F; f1 = (float) ((double) f1 + d1)) {
                    for (float f2 = 0.0F; f2 <= 1.0F; f2 = (float) ((double) f2 + d2)) {
                        double d5 = axisalignedbb.a + (axisalignedbb.d - axisalignedbb.a) * (double) f;
                        double d6 = axisalignedbb.b + (axisalignedbb.e - axisalignedbb.b) * (double) f1;
                        double d7 = axisalignedbb.c + (axisalignedbb.f - axisalignedbb.c) * (double) f2;

                        if (this.rayTrace(new Vec3D(d5 + d3, d6, d7 + d4), vec3d) == null) {
                            ++i;
                        }

                        ++j;
                    }
                }
            }

            return (float) i / (float) j;
        } else {
            return 0.0F;
        }
    }

    public boolean douseFire(@Nullable EntityHuman entityhuman, BlockPosition blockposition, EnumDirection enumdirection) {
        blockposition = blockposition.shift(enumdirection);
        if (this.getType(blockposition).getBlock() == Blocks.FIRE) {
            this.a(entityhuman, 1009, blockposition, 0);
            this.setAir(blockposition);
            return true;
        } else {
            return false;
        }
    }

    @Nullable
    public TileEntity getTileEntity(BlockPosition blockposition) {
        if (this.E(blockposition)) {
            return null;
        } else {
            TileEntity tileentity = null;

            if (this.M) {
                tileentity = this.F(blockposition);
            }

            if (tileentity == null) {
                tileentity = this.getChunkAtWorldCoords(blockposition).a(blockposition, Chunk.EnumTileEntityState.IMMEDIATE);
            }

            if (tileentity == null) {
                tileentity = this.F(blockposition);
            }

            return tileentity;
        }
    }

    @Nullable
    private TileEntity F(BlockPosition blockposition) {
        for (int i = 0; i < this.b.size(); ++i) {
            TileEntity tileentity = (TileEntity) this.b.get(i);

            if (!tileentity.y() && tileentity.getPosition().equals(blockposition)) {
                return tileentity;
            }
        }

        return null;
    }

    public void setTileEntity(BlockPosition blockposition, @Nullable TileEntity tileentity) {
        if (!this.E(blockposition)) {
            if (tileentity != null && !tileentity.y()) {
                if (this.M) {
                    tileentity.setPosition(blockposition);
                    Iterator iterator = this.b.iterator();

                    while (iterator.hasNext()) {
                        TileEntity tileentity1 = (TileEntity) iterator.next();

                        if (tileentity1.getPosition().equals(blockposition)) {
                            tileentity1.z();
                            iterator.remove();
                        }
                    }

                    this.b.add(tileentity);
                } else {
                    this.getChunkAtWorldCoords(blockposition).a(blockposition, tileentity);
                    this.a(tileentity);
                }
            }

        }
    }

    public void s(BlockPosition blockposition) {
        TileEntity tileentity = this.getTileEntity(blockposition);

        if (tileentity != null && this.M) {
            tileentity.z();
            this.b.remove(tileentity);
        } else {
            if (tileentity != null) {
                this.b.remove(tileentity);
                this.tileEntityList.remove(tileentity);
                this.tileEntityListTick.remove(tileentity);
            }

            this.getChunkAtWorldCoords(blockposition).d(blockposition);
        }

    }

    public void b(TileEntity tileentity) {
        this.tileEntityListUnload.add(tileentity);
    }

    public boolean t(BlockPosition blockposition) {
        AxisAlignedBB axisalignedbb = this.getType(blockposition).c(this, blockposition);

        return axisalignedbb != Block.k && axisalignedbb.a() >= 1.0D;
    }

    public boolean d(BlockPosition blockposition, boolean flag) {
        if (this.E(blockposition)) {
            return false;
        } else {
            Chunk chunk = this.chunkProvider.getLoadedChunkAt(blockposition.getX() >> 4, blockposition.getZ() >> 4);

            if (chunk != null && !chunk.isEmpty()) {
                IBlockData iblockdata = this.getType(blockposition);

                return iblockdata.getMaterial().k() && iblockdata.h();
            } else {
                return flag;
            }
        }
    }

    public void H() {
        int i = this.a(1.0F);

        if (i != this.J) {
            this.J = i;
        }

    }

    public void setSpawnFlags(boolean flag, boolean flag1) {
        this.allowMonsters = flag;
        this.allowAnimals = flag1;
    }

    public void doTick() {
        this.t();
    }

    protected void I() {
        if (this.worldData.hasStorm()) {
            this.o = 1.0F;
            if (this.worldData.isThundering()) {
                this.q = 1.0F;
            }
        }

    }

    protected void t() {
        if (this.worldProvider.m()) {
            if (!this.isClientSide) {
                boolean flag = this.getGameRules().getBoolean("doWeatherCycle");

                if (flag) {
                    int i = this.worldData.z();

                    if (i > 0) {
                        --i;
                        this.worldData.i(i);
                        this.worldData.setThunderDuration(this.worldData.isThundering() ? 1 : 2);
                        this.worldData.setWeatherDuration(this.worldData.hasStorm() ? 1 : 2);
                    }

                    int j = this.worldData.getThunderDuration();

                    if (j <= 0) {
                        if (this.worldData.isThundering()) {
                            this.worldData.setThunderDuration(this.random.nextInt(12000) + 3600);
                        } else {
                            this.worldData.setThunderDuration(this.random.nextInt(168000) + 12000);
                        }
                    } else {
                        --j;
                        this.worldData.setThunderDuration(j);
                        if (j <= 0) {
                            this.worldData.setThundering(!this.worldData.isThundering());
                        }
                    }

                    int k = this.worldData.getWeatherDuration();

                    if (k <= 0) {
                        if (this.worldData.hasStorm()) {
                            this.worldData.setWeatherDuration(this.random.nextInt(12000) + 12000);
                        } else {
                            this.worldData.setWeatherDuration(this.random.nextInt(168000) + 12000);
                        }
                    } else {
                        --k;
                        this.worldData.setWeatherDuration(k);
                        if (k <= 0) {
                            this.worldData.setStorm(!this.worldData.hasStorm());
                        }
                    }
                }

                this.p = this.q;
                if (this.worldData.isThundering()) {
                    this.q = (float) ((double) this.q + 0.01D);
                } else {
                    this.q = (float) ((double) this.q - 0.01D);
                }

                this.q = MathHelper.a(this.q, 0.0F, 1.0F);
                this.n = this.o;
                if (this.worldData.hasStorm()) {
                    this.o = (float) ((double) this.o + 0.01D);
                } else {
                    this.o = (float) ((double) this.o - 0.01D);
                }

                this.o = MathHelper.a(this.o, 0.0F, 1.0F);
            }
        }
    }

    protected void j() {}

    public void a(BlockPosition blockposition, IBlockData iblockdata, Random random) {
        this.d = true;
        iblockdata.getBlock().b(this, blockposition, iblockdata, random);
        this.d = false;
    }

    public boolean u(BlockPosition blockposition) {
        return this.e(blockposition, false);
    }

    public boolean v(BlockPosition blockposition) {
        return this.e(blockposition, true);
    }

    public boolean e(BlockPosition blockposition, boolean flag) {
        BiomeBase biomebase = this.getBiome(blockposition);
        float f = biomebase.a(blockposition);

        if (f >= 0.15F) {
            return false;
        } else {
            if (blockposition.getY() >= 0 && blockposition.getY() < 256 && this.getBrightness(EnumSkyBlock.BLOCK, blockposition) < 10) {
                IBlockData iblockdata = this.getType(blockposition);
                Block block = iblockdata.getBlock();

                if ((block == Blocks.WATER || block == Blocks.FLOWING_WATER) && ((Integer) iblockdata.get(BlockFluids.LEVEL)).intValue() == 0) {
                    if (!flag) {
                        return true;
                    }

                    boolean flag1 = this.G(blockposition.west()) && this.G(blockposition.east()) && this.G(blockposition.north()) && this.G(blockposition.south());

                    if (!flag1) {
                        return true;
                    }
                }
            }

            return false;
        }
    }

    private boolean G(BlockPosition blockposition) {
        return this.getType(blockposition).getMaterial() == Material.WATER;
    }

    public boolean f(BlockPosition blockposition, boolean flag) {
        BiomeBase biomebase = this.getBiome(blockposition);
        float f = biomebase.a(blockposition);

        if (f >= 0.15F) {
            return false;
        } else if (!flag) {
            return true;
        } else {
            if (blockposition.getY() >= 0 && blockposition.getY() < 256 && this.getBrightness(EnumSkyBlock.BLOCK, blockposition) < 10) {
                IBlockData iblockdata = this.getType(blockposition);

                if (iblockdata.getMaterial() == Material.AIR && Blocks.SNOW_LAYER.canPlace(this, blockposition)) {
                    return true;
                }
            }

            return false;
        }
    }

    public boolean w(BlockPosition blockposition) {
        boolean flag = false;

        if (this.worldProvider.m()) {
            flag |= this.c(EnumSkyBlock.SKY, blockposition);
        }

        flag |= this.c(EnumSkyBlock.BLOCK, blockposition);
        return flag;
    }

    private int a(BlockPosition blockposition, EnumSkyBlock enumskyblock) {
        if (enumskyblock == EnumSkyBlock.SKY && this.h(blockposition)) {
            return 15;
        } else {
            IBlockData iblockdata = this.getType(blockposition);
            int i = enumskyblock == EnumSkyBlock.SKY ? 0 : iblockdata.d();
            int j = iblockdata.c();

            if (j >= 15 && iblockdata.d() > 0) {
                j = 1;
            }

            if (j < 1) {
                j = 1;
            }

            if (j >= 15) {
                return 0;
            } else if (i >= 14) {
                return i;
            } else {
                BlockPosition.PooledBlockPosition blockposition_pooledblockposition = BlockPosition.PooledBlockPosition.s();
                EnumDirection[] aenumdirection = EnumDirection.values();
                int k = aenumdirection.length;

                for (int l = 0; l < k; ++l) {
                    EnumDirection enumdirection = aenumdirection[l];

                    blockposition_pooledblockposition.j(blockposition).d(enumdirection);
                    int i1 = this.getBrightness(enumskyblock, blockposition_pooledblockposition) - j;

                    if (i1 > i) {
                        i = i1;
                    }

                    if (i >= 14) {
                        return i;
                    }
                }

                blockposition_pooledblockposition.t();
                return i;
            }
        }
    }

    public boolean c(EnumSkyBlock enumskyblock, BlockPosition blockposition) {
        if (!this.areChunksLoaded(blockposition, 17, false)) {
            return false;
        } else {
            int i = 0;
            int j = 0;

            this.methodProfiler.a("getBrightness");
            int k = this.getBrightness(enumskyblock, blockposition);
            int l = this.a(blockposition, enumskyblock);
            int i1 = blockposition.getX();
            int j1 = blockposition.getY();
            int k1 = blockposition.getZ();
            int l1;
            int i2;
            int j2;
            int k2;
            int l2;
            int i3;
            int j3;
            int k3;

            if (l > k) {
                this.H[j++] = 133152;
            } else if (l < k) {
                this.H[j++] = 133152 | k << 18;

                while (i < j) {
                    l1 = this.H[i++];
                    i2 = (l1 & 63) - 32 + i1;
                    j2 = (l1 >> 6 & 63) - 32 + j1;
                    k2 = (l1 >> 12 & 63) - 32 + k1;
                    int l3 = l1 >> 18 & 15;
                    BlockPosition blockposition1 = new BlockPosition(i2, j2, k2);

                    l2 = this.getBrightness(enumskyblock, blockposition1);
                    if (l2 == l3) {
                        this.a(enumskyblock, blockposition1, 0);
                        if (l3 > 0) {
                            i3 = MathHelper.a(i2 - i1);
                            j3 = MathHelper.a(j2 - j1);
                            k3 = MathHelper.a(k2 - k1);
                            if (i3 + j3 + k3 < 17) {
                                BlockPosition.PooledBlockPosition blockposition_pooledblockposition = BlockPosition.PooledBlockPosition.s();
                                EnumDirection[] aenumdirection = EnumDirection.values();
                                int i4 = aenumdirection.length;

                                for (int j4 = 0; j4 < i4; ++j4) {
                                    EnumDirection enumdirection = aenumdirection[j4];
                                    int k4 = i2 + enumdirection.getAdjacentX();
                                    int l4 = j2 + enumdirection.getAdjacentY();
                                    int i5 = k2 + enumdirection.getAdjacentZ();

                                    blockposition_pooledblockposition.f(k4, l4, i5);
                                    int j5 = Math.max(1, this.getType(blockposition_pooledblockposition).c());

                                    l2 = this.getBrightness(enumskyblock, blockposition_pooledblockposition);
                                    if (l2 == l3 - j5 && j < this.H.length) {
                                        this.H[j++] = k4 - i1 + 32 | l4 - j1 + 32 << 6 | i5 - k1 + 32 << 12 | l3 - j5 << 18;
                                    }
                                }

                                blockposition_pooledblockposition.t();
                            }
                        }
                    }
                }

                i = 0;
            }

            this.methodProfiler.b();
            this.methodProfiler.a("checkedPosition < toCheckCount");

            while (i < j) {
                l1 = this.H[i++];
                i2 = (l1 & 63) - 32 + i1;
                j2 = (l1 >> 6 & 63) - 32 + j1;
                k2 = (l1 >> 12 & 63) - 32 + k1;
                BlockPosition blockposition2 = new BlockPosition(i2, j2, k2);
                int k5 = this.getBrightness(enumskyblock, blockposition2);

                l2 = this.a(blockposition2, enumskyblock);
                if (l2 != k5) {
                    this.a(enumskyblock, blockposition2, l2);
                    if (l2 > k5) {
                        i3 = Math.abs(i2 - i1);
                        j3 = Math.abs(j2 - j1);
                        k3 = Math.abs(k2 - k1);
                        boolean flag = j < this.H.length - 6;

                        if (i3 + j3 + k3 < 17 && flag) {
                            if (this.getBrightness(enumskyblock, blockposition2.west()) < l2) {
                                this.H[j++] = i2 - 1 - i1 + 32 + (j2 - j1 + 32 << 6) + (k2 - k1 + 32 << 12);
                            }

                            if (this.getBrightness(enumskyblock, blockposition2.east()) < l2) {
                                this.H[j++] = i2 + 1 - i1 + 32 + (j2 - j1 + 32 << 6) + (k2 - k1 + 32 << 12);
                            }

                            if (this.getBrightness(enumskyblock, blockposition2.down()) < l2) {
                                this.H[j++] = i2 - i1 + 32 + (j2 - 1 - j1 + 32 << 6) + (k2 - k1 + 32 << 12);
                            }

                            if (this.getBrightness(enumskyblock, blockposition2.up()) < l2) {
                                this.H[j++] = i2 - i1 + 32 + (j2 + 1 - j1 + 32 << 6) + (k2 - k1 + 32 << 12);
                            }

                            if (this.getBrightness(enumskyblock, blockposition2.north()) < l2) {
                                this.H[j++] = i2 - i1 + 32 + (j2 - j1 + 32 << 6) + (k2 - 1 - k1 + 32 << 12);
                            }

                            if (this.getBrightness(enumskyblock, blockposition2.south()) < l2) {
                                this.H[j++] = i2 - i1 + 32 + (j2 - j1 + 32 << 6) + (k2 + 1 - k1 + 32 << 12);
                            }
                        }
                    }
                }
            }

            this.methodProfiler.b();
            return true;
        }
    }

    public boolean a(boolean flag) {
        return false;
    }

    @Nullable
    public List<NextTickListEntry> a(Chunk chunk, boolean flag) {
        return null;
    }

    @Nullable
    public List<NextTickListEntry> a(StructureBoundingBox structureboundingbox, boolean flag) {
        return null;
    }

    public List<Entity> getEntities(@Nullable Entity entity, AxisAlignedBB axisalignedbb) {
        return this.getEntities(entity, axisalignedbb, IEntitySelector.e);
    }

    public List<Entity> getEntities(@Nullable Entity entity, AxisAlignedBB axisalignedbb, @Nullable Predicate<? super Entity> predicate) {
        ArrayList arraylist = Lists.newArrayList();
        int i = MathHelper.floor((axisalignedbb.a - 2.0D) / 16.0D);
        int j = MathHelper.floor((axisalignedbb.d + 2.0D) / 16.0D);
        int k = MathHelper.floor((axisalignedbb.c - 2.0D) / 16.0D);
        int l = MathHelper.floor((axisalignedbb.f + 2.0D) / 16.0D);

        for (int i1 = i; i1 <= j; ++i1) {
            for (int j1 = k; j1 <= l; ++j1) {
                if (this.isChunkLoaded(i1, j1, true)) {
                    this.getChunkAt(i1, j1).a(entity, axisalignedbb, arraylist, predicate);
                }
            }
        }

        return arraylist;
    }

    public <T extends Entity> List<T> a(Class<? extends T> oclass, Predicate<? super T> predicate) {
        ArrayList arraylist = Lists.newArrayList();
        Iterator iterator = this.entityList.iterator();

        while (iterator.hasNext()) {
            Entity entity = (Entity) iterator.next();

            if (oclass.isAssignableFrom(entity.getClass()) && predicate.apply(entity)) {
                arraylist.add(entity);
            }
        }

        return arraylist;
    }

    public <T extends Entity> List<T> b(Class<? extends T> oclass, Predicate<? super T> predicate) {
        ArrayList arraylist = Lists.newArrayList();
        Iterator iterator = this.players.iterator();

        while (iterator.hasNext()) {
            Entity entity = (Entity) iterator.next();

            if (oclass.isAssignableFrom(entity.getClass()) && predicate.apply(entity)) {
                arraylist.add(entity);
            }
        }

        return arraylist;
    }

    public <T extends Entity> List<T> a(Class<? extends T> oclass, AxisAlignedBB axisalignedbb) {
        return this.a(oclass, axisalignedbb, IEntitySelector.e);
    }

    public <T extends Entity> List<T> a(Class<? extends T> oclass, AxisAlignedBB axisalignedbb, @Nullable Predicate<? super T> predicate) {
        int i = MathHelper.floor((axisalignedbb.a - 2.0D) / 16.0D);
        int j = MathHelper.f((axisalignedbb.d + 2.0D) / 16.0D);
        int k = MathHelper.floor((axisalignedbb.c - 2.0D) / 16.0D);
        int l = MathHelper.f((axisalignedbb.f + 2.0D) / 16.0D);
        ArrayList arraylist = Lists.newArrayList();

        for (int i1 = i; i1 < j; ++i1) {
            for (int j1 = k; j1 < l; ++j1) {
                if (this.isChunkLoaded(i1, j1, true)) {
                    this.getChunkAt(i1, j1).a(oclass, axisalignedbb, arraylist, predicate);
                }
            }
        }

        return arraylist;
    }

    @Nullable
    public <T extends Entity> T a(Class<? extends T> oclass, AxisAlignedBB axisalignedbb, T t0) {
        List list = this.a(oclass, axisalignedbb);
        Entity entity = null;
        double d0 = Double.MAX_VALUE;

        for (int i = 0; i < list.size(); ++i) {
            Entity entity1 = (Entity) list.get(i);

            if (entity1 != t0 && IEntitySelector.e.apply(entity1)) {
                double d1 = t0.h(entity1);

                if (d1 <= d0) {
                    entity = entity1;
                    d0 = d1;
                }
            }
        }

        return entity;
    }

    @Nullable
    public Entity getEntity(int i) {
        return (Entity) this.entitiesById.get(i);
    }

    public void b(BlockPosition blockposition, TileEntity tileentity) {
        if (this.isLoaded(blockposition)) {
            this.getChunkAtWorldCoords(blockposition).e();
        }

    }

    public int a(Class<?> oclass) {
        int i = 0;
        Iterator iterator = this.entityList.iterator();

        while (iterator.hasNext()) {
            Entity entity = (Entity) iterator.next();

            if ((!(entity instanceof EntityInsentient) || !((EntityInsentient) entity).isPersistent()) && oclass.isAssignableFrom(entity.getClass())) {
                ++i;
            }
        }

        return i;
    }

    public void a(Collection<Entity> collection) {
        this.entityList.addAll(collection);
        Iterator iterator = collection.iterator();

        while (iterator.hasNext()) {
            Entity entity = (Entity) iterator.next();

            this.b(entity);
        }

    }

    public void c(Collection<Entity> collection) {
        this.f.addAll(collection);
    }

    public boolean a(Block block, BlockPosition blockposition, boolean flag, EnumDirection enumdirection, @Nullable Entity entity) {
        IBlockData iblockdata = this.getType(blockposition);
        AxisAlignedBB axisalignedbb = flag ? null : block.getBlockData().c(this, blockposition);

        return axisalignedbb != Block.k && !this.a(axisalignedbb.a(blockposition), entity) ? false : (iblockdata.getMaterial() == Material.ORIENTABLE && block == Blocks.ANVIL ? true : iblockdata.getMaterial().isReplaceable() && block.canPlace(this, blockposition, enumdirection));
    }

    public int K() {
        return this.a;
    }

    public void b(int i) {
        this.a = i;
    }

    public int getBlockPower(BlockPosition blockposition, EnumDirection enumdirection) {
        return this.getType(blockposition).b(this, blockposition, enumdirection);
    }

    public WorldType L() {
        return this.worldData.getType();
    }

    public int getBlockPower(BlockPosition blockposition) {
        byte b0 = 0;
        int i = Math.max(b0, this.getBlockPower(blockposition.down(), EnumDirection.DOWN));

        if (i >= 15) {
            return i;
        } else {
            i = Math.max(i, this.getBlockPower(blockposition.up(), EnumDirection.UP));
            if (i >= 15) {
                return i;
            } else {
                i = Math.max(i, this.getBlockPower(blockposition.north(), EnumDirection.NORTH));
                if (i >= 15) {
                    return i;
                } else {
                    i = Math.max(i, this.getBlockPower(blockposition.south(), EnumDirection.SOUTH));
                    if (i >= 15) {
                        return i;
                    } else {
                        i = Math.max(i, this.getBlockPower(blockposition.west(), EnumDirection.WEST));
                        if (i >= 15) {
                            return i;
                        } else {
                            i = Math.max(i, this.getBlockPower(blockposition.east(), EnumDirection.EAST));
                            return i >= 15 ? i : i;
                        }
                    }
                }
            }
        }
    }

    public boolean isBlockFacePowered(BlockPosition blockposition, EnumDirection enumdirection) {
        return this.getBlockFacePower(blockposition, enumdirection) > 0;
    }

    public int getBlockFacePower(BlockPosition blockposition, EnumDirection enumdirection) {
        IBlockData iblockdata = this.getType(blockposition);

        return iblockdata.m() ? this.getBlockPower(blockposition) : iblockdata.a((IBlockAccess) this, blockposition, enumdirection);
    }

    public boolean isBlockIndirectlyPowered(BlockPosition blockposition) {
        return this.getBlockFacePower(blockposition.down(), EnumDirection.DOWN) > 0 ? true : (this.getBlockFacePower(blockposition.up(), EnumDirection.UP) > 0 ? true : (this.getBlockFacePower(blockposition.north(), EnumDirection.NORTH) > 0 ? true : (this.getBlockFacePower(blockposition.south(), EnumDirection.SOUTH) > 0 ? true : (this.getBlockFacePower(blockposition.west(), EnumDirection.WEST) > 0 ? true : this.getBlockFacePower(blockposition.east(), EnumDirection.EAST) > 0))));
    }

    public int z(BlockPosition blockposition) {
        int i = 0;
        EnumDirection[] aenumdirection = EnumDirection.values();
        int j = aenumdirection.length;

        for (int k = 0; k < j; ++k) {
            EnumDirection enumdirection = aenumdirection[k];
            int l = this.getBlockFacePower(blockposition.shift(enumdirection), enumdirection);

            if (l >= 15) {
                return 15;
            }

            if (l > i) {
                i = l;
            }
        }

        return i;
    }

    @Nullable
    public EntityHuman findNearbyPlayer(Entity entity, double d0) {
        return this.a(entity.locX, entity.locY, entity.locZ, d0, false);
    }

    @Nullable
    public EntityHuman b(Entity entity, double d0) {
        return this.a(entity.locX, entity.locY, entity.locZ, d0, true);
    }

    @Nullable
    public EntityHuman a(double d0, double d1, double d2, double d3, boolean flag) {
        Predicate predicate = flag ? IEntitySelector.d : IEntitySelector.e;

        return this.a(d0, d1, d2, d3, predicate);
    }

    @Nullable
    public EntityHuman a(double d0, double d1, double d2, double d3, Predicate<Entity> predicate) {
        double d4 = -1.0D;
        EntityHuman entityhuman = null;

        for (int i = 0; i < this.players.size(); ++i) {
            EntityHuman entityhuman1 = (EntityHuman) this.players.get(i);

            if (predicate.apply(entityhuman1)) {
                double d5 = entityhuman1.d(d0, d1, d2);

                if ((d3 < 0.0D || d5 < d3 * d3) && (d4 == -1.0D || d5 < d4)) {
                    d4 = d5;
                    entityhuman = entityhuman1;
                }
            }
        }

        return entityhuman;
    }

    public boolean isPlayerNearby(double d0, double d1, double d2, double d3) {
        for (int i = 0; i < this.players.size(); ++i) {
            EntityHuman entityhuman = (EntityHuman) this.players.get(i);

            if (IEntitySelector.e.apply(entityhuman)) {
                double d4 = entityhuman.d(d0, d1, d2);

                if (d3 < 0.0D || d4 < d3 * d3) {
                    return true;
                }
            }
        }

        return false;
    }

    @Nullable
    public EntityHuman a(Entity entity, double d0, double d1) {
        return this.a(entity.locX, entity.locY, entity.locZ, d0, d1, (Function) null, (Predicate) null);
    }

    @Nullable
    public EntityHuman a(BlockPosition blockposition, double d0, double d1) {
        return this.a((double) ((float) blockposition.getX() + 0.5F), (double) ((float) blockposition.getY() + 0.5F), (double) ((float) blockposition.getZ() + 0.5F), d0, d1, (Function) null, (Predicate) null);
    }

    @Nullable
    public EntityHuman a(double d0, double d1, double d2, double d3, double d4, @Nullable Function<EntityHuman, Double> function, @Nullable Predicate<EntityHuman> predicate) {
        double d5 = -1.0D;
        EntityHuman entityhuman = null;

        for (int i = 0; i < this.players.size(); ++i) {
            EntityHuman entityhuman1 = (EntityHuman) this.players.get(i);

            if (!entityhuman1.abilities.isInvulnerable && entityhuman1.isAlive() && !entityhuman1.isSpectator() && (predicate == null || predicate.apply(entityhuman1))) {
                double d6 = entityhuman1.d(d0, entityhuman1.locY, d2);
                double d7 = d3;

                if (entityhuman1.isSneaking()) {
                    d7 = d3 * 0.800000011920929D;
                }

                if (entityhuman1.isInvisible()) {
                    float f = entityhuman1.cO();

                    if (f < 0.1F) {
                        f = 0.1F;
                    }

                    d7 *= (double) (0.7F * f);
                }

                if (function != null) {
                    d7 *= ((Double) Objects.firstNonNull(function.apply(entityhuman1), Double.valueOf(1.0D))).doubleValue();
                }

                if ((d4 < 0.0D || Math.abs(entityhuman1.locY - d1) < d4 * d4) && (d3 < 0.0D || d6 < d7 * d7) && (d5 == -1.0D || d6 < d5)) {
                    d5 = d6;
                    entityhuman = entityhuman1;
                }
            }
        }

        return entityhuman;
    }

    @Nullable
    public EntityHuman a(String s) {
        for (int i = 0; i < this.players.size(); ++i) {
            EntityHuman entityhuman = (EntityHuman) this.players.get(i);

            if (s.equals(entityhuman.getName())) {
                return entityhuman;
            }
        }

        return null;
    }

    @Nullable
    public EntityHuman b(UUID uuid) {
        for (int i = 0; i < this.players.size(); ++i) {
            EntityHuman entityhuman = (EntityHuman) this.players.get(i);

            if (uuid.equals(entityhuman.getUniqueID())) {
                return entityhuman;
            }
        }

        return null;
    }

    public void checkSession() throws ExceptionWorldConflict {
        this.dataManager.checkSession();
    }

    public long getSeed() {
        return this.worldData.getSeed();
    }

    public long getTime() {
        return this.worldData.getTime();
    }

    public long getDayTime() {
        return this.worldData.getDayTime();
    }

    public void setDayTime(long i) {
        this.worldData.setDayTime(i);
    }

    public BlockPosition getSpawn() {
        BlockPosition blockposition = new BlockPosition(this.worldData.b(), this.worldData.c(), this.worldData.d());

        if (!this.getWorldBorder().a(blockposition)) {
            blockposition = this.getHighestBlockYAt(new BlockPosition(this.getWorldBorder().getCenterX(), 0.0D, this.getWorldBorder().getCenterZ()));
        }

        return blockposition;
    }

    public void A(BlockPosition blockposition) {
        this.worldData.setSpawn(blockposition);
    }

    public boolean a(EntityHuman entityhuman, BlockPosition blockposition) {
        return true;
    }

    public void broadcastEntityEffect(Entity entity, byte b0) {}

    public IChunkProvider getChunkProvider() {
        return this.chunkProvider;
    }

    public void playBlockAction(BlockPosition blockposition, Block block, int i, int j) {
        this.getType(blockposition).a(this, blockposition, i, j);
    }

    public IDataManager getDataManager() {
        return this.dataManager;
    }

    public WorldData getWorldData() {
        return this.worldData;
    }

    public GameRules getGameRules() {
        return this.worldData.w();
    }

    public void everyoneSleeping() {}

    public float h(float f) {
        return (this.p + (this.q - this.p) * f) * this.j(f);
    }

    public float j(float f) {
        return this.n + (this.o - this.n) * f;
    }

    public boolean V() {
        return (double) this.h(1.0F) > 0.9D;
    }

    public boolean W() {
        return (double) this.j(1.0F) > 0.2D;
    }

    public boolean isRainingAt(BlockPosition blockposition) {
        if (!this.W()) {
            return false;
        } else if (!this.h(blockposition)) {
            return false;
        } else if (this.p(blockposition).getY() > blockposition.getY()) {
            return false;
        } else {
            BiomeBase biomebase = this.getBiome(blockposition);

            return biomebase.c() ? false : (this.f(blockposition, false) ? false : biomebase.d());
        }
    }

    public boolean C(BlockPosition blockposition) {
        BiomeBase biomebase = this.getBiome(blockposition);

        return biomebase.e();
    }

    @Nullable
    public PersistentCollection X() {
        return this.worldMaps;
    }

    public void a(String s, PersistentBase persistentbase) {
        this.worldMaps.a(s, persistentbase);
    }

    @Nullable
    public PersistentBase a(Class<? extends PersistentBase> oclass, String s) {
        return this.worldMaps.get(oclass, s);
    }

    public int b(String s) {
        return this.worldMaps.a(s);
    }

    public void a(int i, BlockPosition blockposition, int j) {
        for (int k = 0; k < this.u.size(); ++k) {
            ((IWorldAccess) this.u.get(k)).a(i, blockposition, j);
        }

    }

    public void triggerEffect(int i, BlockPosition blockposition, int j) {
        this.a((EntityHuman) null, i, blockposition, j);
    }

    public void a(@Nullable EntityHuman entityhuman, int i, BlockPosition blockposition, int j) {
        try {
            for (int k = 0; k < this.u.size(); ++k) {
                ((IWorldAccess) this.u.get(k)).a(entityhuman, i, blockposition, j);
            }

        } catch (Throwable throwable) {
            CrashReport crashreport = CrashReport.a(throwable, "Playing level event");
            CrashReportSystemDetails crashreportsystemdetails = crashreport.a("Level event being played");

            crashreportsystemdetails.a("Block coordinates", (Object) CrashReportSystemDetails.a(blockposition));
            crashreportsystemdetails.a("Event source", (Object) entityhuman);
            crashreportsystemdetails.a("Event type", (Object) Integer.valueOf(i));
            crashreportsystemdetails.a("Event data", (Object) Integer.valueOf(j));
            throw new ReportedException(crashreport);
        }
    }

    public int getHeight() {
        return 256;
    }

    public int Z() {
        return this.worldProvider.n() ? 128 : 256;
    }

    public Random a(int i, int j, int k) {
        long l = (long) i * 341873128712L + (long) j * 132897987541L + this.getWorldData().getSeed() + (long) k;

        this.random.setSeed(l);
        return this.random;
    }

    public CrashReportSystemDetails a(CrashReport crashreport) {
        CrashReportSystemDetails crashreportsystemdetails = crashreport.a("Affected level", 1);

        crashreportsystemdetails.a("Level name", (Object) (this.worldData == null ? "????" : this.worldData.getName()));
        crashreportsystemdetails.a("All players", new CrashReportCallable() {
            public String a() {
                return World.this.players.size() + " total; " + World.this.players;
            }

            public Object call() throws Exception {
                return this.a();
            }
        });
        crashreportsystemdetails.a("Chunk stats", new CrashReportCallable() {
            public String a() {
                return World.this.chunkProvider.getName();
            }

            public Object call() throws Exception {
                return this.a();
            }
        });

        try {
            this.worldData.a(crashreportsystemdetails);
        } catch (Throwable throwable) {
            crashreportsystemdetails.a("Level Data Unobtainable", throwable);
        }

        return crashreportsystemdetails;
    }

    public void c(int i, BlockPosition blockposition, int j) {
        for (int k = 0; k < this.u.size(); ++k) {
            IWorldAccess iworldaccess = (IWorldAccess) this.u.get(k);

            iworldaccess.b(i, blockposition, j);
        }

    }

    public Calendar ac() {
        if (this.getTime() % 600L == 0L) {
            this.L.setTimeInMillis(MinecraftServer.aw());
        }

        return this.L;
    }

    public Scoreboard getScoreboard() {
        return this.scoreboard;
    }

    public void updateAdjacentComparators(BlockPosition blockposition, Block block) {
        Iterator iterator = EnumDirection.EnumDirectionLimit.HORIZONTAL.iterator();

        while (iterator.hasNext()) {
            EnumDirection enumdirection = (EnumDirection) iterator.next();
            BlockPosition blockposition1 = blockposition.shift(enumdirection);

            if (this.isLoaded(blockposition1)) {
                IBlockData iblockdata = this.getType(blockposition1);

                if (Blocks.UNPOWERED_COMPARATOR.E(iblockdata)) {
                    iblockdata.doPhysics(this, blockposition1, block, blockposition);
                } else if (iblockdata.m()) {
                    blockposition1 = blockposition1.shift(enumdirection);
                    iblockdata = this.getType(blockposition1);
                    if (Blocks.UNPOWERED_COMPARATOR.E(iblockdata)) {
                        iblockdata.doPhysics(this, blockposition1, block, blockposition);
                    }
                }
            }
        }

    }

    public DifficultyDamageScaler D(BlockPosition blockposition) {
        long i = 0L;
        float f = 0.0F;

        if (this.isLoaded(blockposition)) {
            f = this.E();
            i = this.getChunkAtWorldCoords(blockposition).x();
        }

        return new DifficultyDamageScaler(this.getDifficulty(), this.getDayTime(), i, f);
    }

    public EnumDifficulty getDifficulty() {
        return this.getWorldData().getDifficulty();
    }

    public int af() {
        return this.J;
    }

    public void c(int i) {
        this.J = i;
    }

    public void d(int i) {
        this.K = i;
    }

    public PersistentVillage ai() {
        return this.villages;
    }

    public WorldBorder getWorldBorder() {
        return this.N;
    }

    public boolean e(int i, int j) {
        BlockPosition blockposition = this.getSpawn();
        int k = i * 16 + 8 - blockposition.getX();
        int l = j * 16 + 8 - blockposition.getZ();
        boolean flag = true;

        return k >= -128 && k <= 128 && l >= -128 && l <= 128;
    }

    public void a(Packet<?> packet) {
        throw new UnsupportedOperationException("Can\'t send packets to server unless you\'re on the client.");
    }

    public LootTableRegistry ak() {
        return this.B;
    }

    @Nullable
    public BlockPosition a(String s, BlockPosition blockposition, boolean flag) {
        return null;
    }
}
