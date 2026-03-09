package Thexiaoyu.underworld.generator;

import Thexiaoyu.underworld.biome.GeoBiome;
import Thexiaoyu.underworld.util.FastNoise;
import org.bukkit.Material;
import org.bukkit.generator.BlockPopulator;
import org.bukkit.generator.LimitedRegion;
import org.bukkit.generator.WorldInfo;

import java.util.Random;

/**
 * 地心世界装饰生成器 - 适配岩洞地形
 *
 * 改进：
 * 1. 改进地面检测 - 适应多层岩洞结构
 * 2. 结构生成适配岩洞空间
 * 3. 更自然的装饰分布
 */
public class GeoCorePopulator extends BlockPopulator {

    private static final int SPACE_ELEVATOR_CHUNK_X = 0;
    private static final int SPACE_ELEVATOR_CHUNK_Z = 0;
    private static final int MEGA_LIGHT_CHUNK_X = 10;
    private static final int MEGA_LIGHT_CHUNK_Z = 10;

    private final FastNoise structureNoise;

    public GeoCorePopulator() {
        structureNoise = new FastNoise(42);
        structureNoise.SetNoiseType(FastNoise.NoiseType.Cellular);
        structureNoise.SetFrequency(0.01f);
    }

    @Override
    public void populate(WorldInfo worldInfo, Random random, int chunkX, int chunkZ, LimitedRegion region) {
        int worldX = chunkX << 4;
        int worldZ = chunkZ << 4;

        // 核心结构
        if (chunkX == SPACE_ELEVATOR_CHUNK_X && chunkZ == SPACE_ELEVATOR_CHUNK_Z) {
            generateSpaceElevator(region, worldInfo, worldX + 8, worldZ + 8, random);
        }
        if (chunkX == MEGA_LIGHT_CHUNK_X && chunkZ == MEGA_LIGHT_CHUNK_Z) {
            generateMegaLightSource(region, worldX + 8, worldZ + 8, random);
        }

        // ========== 遗迹生成（降低概率以适应岩洞） ==========

        // 1. 古代神殿
        if (random.nextFloat() < 0.015f) {
            generateAncientTemple(region, worldInfo, worldX + random.nextInt(10) + 3,
                    worldZ + random.nextInt(10) + 3, random);
        }

        // 3. 祭坛遗迹
        if (random.nextFloat() < 0.015f) {
            generateAltar(region, worldInfo, worldX + random.nextInt(10) + 3,
                    worldZ + random.nextInt(10) + 3, random);
        }

        // 4. 图书馆废墟
        if (random.nextFloat() < 0.01f) {
            generateRuinedLibrary(region, worldInfo, worldX + random.nextInt(8) + 4,
                    worldZ + random.nextInt(8) + 4, random);
        }

        // 5. 传送门遗迹
        if (random.nextFloat() < 0.008f) {
            generatePortalRuin(region, worldInfo, worldX + random.nextInt(10) + 3,
                    worldZ + random.nextInt(10) + 3, random);
        }

        // 6. 守卫塔楼
        if (random.nextFloat() < 0.02f) {
            generateWatchTower(region, worldInfo, worldX + random.nextInt(10) + 3,
                    worldZ + random.nextInt(10) + 3, random);
        }

        // 7. 铸造厂遗迹
        if (random.nextFloat() < 0.015f) {
            generateForgeRuin(region, worldInfo, worldX + random.nextInt(10) + 3,
                    worldZ + random.nextInt(10) + 3, random);
        }

        // 8. 墓地遗迹
        if (random.nextFloat() < 0.02f) {
            generateGraveyard(region, worldInfo, worldX + random.nextInt(10) + 3,
                    worldZ + random.nextInt(10) + 3, random);
        }

        // 9. 水晶圣殿
        if (random.nextFloat() < 0.012f) {
            generateCrystalShrine(region, worldInfo, worldX + random.nextInt(10) + 3,
                    worldZ + random.nextInt(10) + 3, random);
        }

        // 10. 机械遗迹
        if (random.nextFloat() < 0.012f) {
            generateMechanicalRuin(region, worldInfo, worldX + random.nextInt(10) + 3,
                    worldZ + random.nextInt(10) + 3, random);
        }

        // ========== 装饰生成 ==========

        // 大型水晶
        if (random.nextFloat() < 0.06f) {
            generateLargeCrystal(region, worldInfo, worldX + random.nextInt(12) + 2,
                    worldZ + random.nextInt(12) + 2, random);
        }

        // 下界裂隙
        if (random.nextFloat() < 0.025f) {
            generateNetherRift(region, worldInfo, worldX + random.nextInt(10) + 3,
                    worldZ + random.nextInt(10) + 3, random);
        }

        // 光源柱
        if (random.nextFloat() < 0.04f) {
            generateLightPillar(region, worldInfo, worldX + random.nextInt(14) + 1,
                    worldZ + random.nextInt(14) + 1, random);
        }

        // 绯红/诡异树
        if (random.nextFloat() < 0.03f) {
            generateNetherTree(region, worldInfo, worldX + random.nextInt(12) + 2,
                    worldZ + random.nextInt(12) + 2, random);
        }

        // 玄武岩柱
        if (random.nextFloat() < 0.04f) {
            generateBasaltPillar(region, worldInfo, worldX + random.nextInt(12) + 2,
                    worldZ + random.nextInt(12) + 2, random);
        }

        // 散布光源
        scatterLightSources(region, worldInfo, worldX, worldZ, random);
    }

    // ==================== 遗迹生成方法 ====================

    /**
     * 1. 古代神殿
     */
    private void generateAncientTemple(LimitedRegion region, WorldInfo worldInfo, int x, int z, Random random) {
        int y = findLargestCaveFloor(region, worldInfo, x, z, 12);
        if (y < 0 || !region.isInRegion(x, y, z)) return;

        // 检查是否有足够高度
        if (!checkCaveHeight(region, x, y, z, 12)) return;

        int width = 12;
        int length = 15;
        int height = 10;

        // 地基
        for (int dx = -1; dx <= width; dx++) {
            for (int dz = -1; dz <= length; dz++) {
                for (int dy = -2; dy <= 0; dy++) {
                    setBlockSafe(region, x + dx, y + dy, z + dz, Material.DEEPSLATE_BRICKS);
                }
            }
        }

        // 墙壁
        for (int dx = 0; dx < width; dx++) {
            for (int dz = 0; dz < length; dz++) {
                if (dx == 0 || dx == width - 1 || dz == 0 || dz == length - 1) {
                    int wallHeight = Math.min(height - random.nextInt(3),
                            getCeilingDistance(region, x + dx, y, z + dz) - 2);
                    for (int dy = 1; dy <= wallHeight; dy++) {
                        if (random.nextFloat() < 0.9f) {
                            Material mat = random.nextFloat() < 0.3f ?
                                    Material.CRACKED_DEEPSLATE_BRICKS : Material.DEEPSLATE_BRICKS;
                            setBlockSafe(region, x + dx, y + dy, z + dz, mat);
                        }
                    }
                }
            }
        }

        // 柱子
        int[][] pillars = {{2, 2}, {2, length-3}, {width-3, 2}, {width-3, length-3}};
        for (int[] p : pillars) {
            int pillarHeight = Math.min(height + 2, getCeilingDistance(region, x + p[0], y, z + p[1]) - 1);
            for (int dy = 1; dy <= pillarHeight; dy++) {
                setBlockSafe(region, x + p[0], y + dy, z + p[1], Material.POLISHED_DEEPSLATE);
            }
            if (pillarHeight >= height) {
                setBlockSafe(region, x + p[0], y + pillarHeight + 1, z + p[1], Material.SEA_LANTERN);
            }
        }

        // 祭台
        int altarX = x + width / 2;
        int altarZ = z + length - 4;
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                setBlockSafe(region, altarX + dx, y + 1, altarZ + dz, Material.POLISHED_DEEPSLATE);
            }
        }
        setBlockSafe(region, altarX, y + 2, altarZ, Material.LODESTONE);
        setBlockSafe(region, altarX, y + 3, altarZ, Material.END_ROD);

        // 入口
        for (int dy = 1; dy <= 4; dy++) {
            setBlockSafe(region, x + width/2, y + dy, z, Material.AIR);
            setBlockSafe(region, x + width/2 - 1, y + dy, z, Material.AIR);
        }
    }

    /**
     * 3. 祭坛遗迹
     */
    private void generateAltar(LimitedRegion region, WorldInfo worldInfo, int x, int z, Random random) {
        int y = findLargestCaveFloor(region, worldInfo, x, z, 10);
        if (y < 0 || !region.isInRegion(x, y, z)) return;

        // 圆形平台
        int radius = 5;
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                if (dx * dx + dz * dz <= radius * radius) {
                    setBlockSafe(region, x + dx, y, z + dz, Material.POLISHED_BLACKSTONE);
                    // 外圈装饰
                    if (dx * dx + dz * dz > (radius - 1) * (radius - 1)) {
                        if (random.nextFloat() < 0.3f) {
                            setBlockSafe(region, x + dx, y, z + dz, Material.DEEPSLATE);
                        }
                    }
                }
            }
        }

        // 中心祭坛
        setBlockSafe(region, x, y + 1, z, Material.LODESTONE);
        setBlockSafe(region, x, y + 2, z, Material.SOUL_LANTERN);

        // 四角火焰
        int[][] corners = {{3, 0}, {-3, 0}, {0, 3}, {0, -3}};
        for (int[] c : corners) {
            setBlockSafe(region, x + c[0], y + 1, z + c[1], Material.SOUL_CAMPFIRE);
        }

        // 符文柱
        for (int angle = 0; angle < 360; angle += 60) {
            double rad = Math.toRadians(angle);
            int px = x + (int)(Math.cos(rad) * 4);
            int pz = z + (int)(Math.sin(rad) * 4);

            int pillarHeight = Math.min(3, getCeilingDistance(region, px, y, pz) - 2);
            for (int dy = 1; dy <= pillarHeight; dy++) {
                setBlockSafe(region, px, y + dy, pz, Material.POLISHED_BLACKSTONE_WALL);
            }
            setBlockSafe(region, px, y + pillarHeight + 1, pz, Material.END_ROD);
        }
    }

    /**
     * 4. 图书馆废墟
     */
    private void generateRuinedLibrary(LimitedRegion region, WorldInfo worldInfo, int x, int z, Random random) {
        int y = findLargestCaveFloor(region, worldInfo, x, z, 10);
        if (y < 0 || !region.isInRegion(x, y, z)) return;
        if (!checkCaveHeight(region, x, y, z, 6)) return;

        int width = 10;
        int length = 12;

        // 地板
        for (int dx = 0; dx < width; dx++) {
            for (int dz = 0; dz < length; dz++) {
                setBlockSafe(region, x + dx, y, z + dz,
                        random.nextFloat() < 0.3f ? Material.DARK_OAK_PLANKS : Material.SPRUCE_PLANKS);
            }
        }

        // 书架
        for (int row = 0; row < 3; row++) {
            int shelfZ = z + 2 + row * 3;
            for (int dx = 1; dx < width - 1; dx++) {
                int maxHeight = getCeilingDistance(region, x + dx, y, shelfZ) - 1;
                int shelfHeight = Math.min(random.nextInt(3) + 2, maxHeight);
                for (int dy = 1; dy <= shelfHeight; dy++) {
                    if (random.nextFloat() < 0.8f) {
                        setBlockSafe(region, x + dx, y + dy, shelfZ, Material.BOOKSHELF);
                    }
                }
            }
        }

        // 讲台
        setBlockSafe(region, x + width/2, y + 1, z + length - 2, Material.LECTERN);
        setBlockSafe(region, x + width/2, y + 2, z + length - 2, Material.CANDLE);

        // 灯光
        if (getCeilingDistance(region, x + 2, y, z + 2) >= 5) {
            setBlockSafe(region, x + 2, y + 4, z + 2, Material.LANTERN);
        }
        if (getCeilingDistance(region, x + width - 3, y, z + 2) >= 5) {
            setBlockSafe(region, x + width - 3, y + 4, z + 2, Material.LANTERN);
        }
        if (getCeilingDistance(region, x + width/2, y, z + length - 3) >= 5) {
            setBlockSafe(region, x + width/2, y + 4, z + length - 3, Material.SEA_LANTERN);
        }
    }

    /**
     * 5. 传送门遗迹
     */
    private void generatePortalRuin(LimitedRegion region, WorldInfo worldInfo, int x, int z, Random random) {
        int y = findLargestCaveFloor(region, worldInfo, x, z, 8);
        if (y < 0 || !region.isInRegion(x, y, z)) return;
        if (!checkCaveHeight(region, x, y, z, 8)) return;

        // 平台
        for (int dx = -4; dx <= 4; dx++) {
            for (int dz = -4; dz <= 4; dz++) {
                if (Math.abs(dx) + Math.abs(dz) <= 6) {
                    setBlockSafe(region, x + dx, y, z + dz, Material.OBSIDIAN);
                }
            }
        }

        // 传送门框架
        int maxHeight = getCeilingDistance(region, x, y, z) - 1;
        int frameHeight = Math.min(5, maxHeight);

        for (int dy = 1; dy <= frameHeight; dy++) {
            if (random.nextFloat() < 0.7f) {
                setBlockSafe(region, x - 2, y + dy, z, Material.OBSIDIAN);
            }
            if (random.nextFloat() < 0.7f) {
                setBlockSafe(region, x + 2, y + dy, z, Material.OBSIDIAN);
            }
        }

        // 顶部
        if (frameHeight >= 5) {
            for (int dx = -1; dx <= 1; dx++) {
                if (random.nextFloat() < 0.5f) {
                    setBlockSafe(region, x + dx, y + 5, z, Material.OBSIDIAN);
                }
            }
        }

        // 角落装饰
        setBlockSafe(region, x - 2, y + 1, z - 1, Material.OBSIDIAN);
        setBlockSafe(region, x + 2, y + 1, z + 1, Material.OBSIDIAN);

        // 残余能量
        if (frameHeight >= 3) {
            setBlockSafe(region, x, y + 3, z, Material.END_GATEWAY);
        }
    }

    /**
     * 6. 守卫塔楼
     */
    private void generateWatchTower(LimitedRegion region, WorldInfo worldInfo, int x, int z, Random random) {
        int y = findLargestCaveFloor(region, worldInfo, x, z, 6);
        if (y < 0 || !region.isInRegion(x, y, z)) return;

        int maxHeight = getCeilingDistance(region, x, y, z) - 3;
        int height = Math.min(random.nextInt(10) + 10, maxHeight);

        if (height < 8) return;  // 太矮不生成

        // 基座
        for (int dx = -3; dx <= 3; dx++) {
            for (int dz = -3; dz <= 3; dz++) {
                for (int dy = -2; dy <= 0; dy++) {
                    setBlockSafe(region, x + dx, y + dy, z + dz, Material.STONE_BRICKS);
                }
            }
        }

        // 塔身
        for (int dy = 1; dy <= height; dy++) {
            for (int dx = -2; dx <= 2; dx++) {
                for (int dz = -2; dz <= 2; dz++) {
                    if (Math.abs(dx) == 2 || Math.abs(dz) == 2) {
                        Material mat = random.nextFloat() < 0.2f ?
                                Material.CRACKED_STONE_BRICKS : Material.STONE_BRICKS;
                        setBlockSafe(region, x + dx, y + dy, z + dz, mat);
                    }
                }
            }
            // 窗户
            if (dy % 5 == 3 && dy < height - 2) {
                setBlockSafe(region, x + 2, y + dy, z, Material.AIR);
                setBlockSafe(region, x - 2, y + dy, z, Material.AIR);
            }
        }

        // 顶部平台
        for (int dx = -3; dx <= 3; dx++) {
            for (int dz = -3; dz <= 3; dz++) {
                setBlockSafe(region, x + dx, y + height + 1, z + dz, Material.STONE_BRICK_SLAB);
            }
        }

        // 顶部灯光
        setBlockSafe(region, x, y + height + 2, z, Material.SEA_LANTERN);
        setBlockSafe(region, x, y + height + 3, z, Material.END_ROD);

        // 城垛
        for (int i = -3; i <= 3; i += 2) {
            setBlockSafe(region, x + i, y + height + 2, z + 3, Material.STONE_BRICK_WALL);
            setBlockSafe(region, x + i, y + height + 2, z - 3, Material.STONE_BRICK_WALL);
            setBlockSafe(region, x + 3, y + height + 2, z + i, Material.STONE_BRICK_WALL);
            setBlockSafe(region, x - 3, y + height + 2, z + i, Material.STONE_BRICK_WALL);
        }
    }

    /**
     * 7. 铸造厂遗迹
     */
    private void generateForgeRuin(LimitedRegion region, WorldInfo worldInfo, int x, int z, Random random) {
        int y = findLargestCaveFloor(region, worldInfo, x, z, 8);
        if (y < 0 || !region.isInRegion(x, y, z)) return;

        // 地板
        for (int dx = -4; dx <= 4; dx++) {
            for (int dz = -4; dz <= 4; dz++) {
                setBlockSafe(region, x + dx, y, z + dz,
                        random.nextFloat() < 0.3f ? Material.BLACKSTONE : Material.POLISHED_BLACKSTONE);
            }
        }

        // 熔炉区
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                setBlockSafe(region, x + dx + 2, y + 1, z + dz, Material.BLAST_FURNACE);
            }
        }

        // 熔岩池
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                setBlockSafe(region, x + dx - 2, y, z + dz, Material.LAVA);
            }
        }

        // 池边缘
        for (int dx = -2; dx <= 0; dx++) {
            setBlockSafe(region, x + dx - 2, y, z + 2, Material.MAGMA_BLOCK);
            setBlockSafe(region, x + dx - 2, y, z - 2, Material.MAGMA_BLOCK);
        }

        // 铁砧
        setBlockSafe(region, x, y + 1, z + 3, Material.ANVIL);
        setBlockSafe(region, x, y + 1, z - 3, Material.DAMAGED_ANVIL);

        // 金属块堆
        for (int i = 0; i < 5; i++) {
            int mx = x + random.nextInt(6) - 3;
            int mz = z + random.nextInt(6) - 3;
            Material[] metals = {Material.IRON_BLOCK, Material.COPPER_BLOCK, Material.RAW_IRON_BLOCK};
            setBlockSafe(region, mx, y + 1, mz, metals[random.nextInt(metals.length)]);
        }

        // 烟囱（如果空间足够）
        int chimneyHeight = Math.min(8, getCeilingDistance(region, x + 3, y, z - 3) - 1);
        if (chimneyHeight >= 5) {
            for (int dy = 1; dy <= chimneyHeight; dy++) {
                setBlockSafe(region, x + 3, y + dy, z - 3, Material.BRICKS);
                setBlockSafe(region, x + 4, y + dy, z - 3, Material.BRICKS);
                setBlockSafe(region, x + 3, y + dy, z - 4, Material.BRICKS);
                setBlockSafe(region, x + 4, y + dy, z - 4, Material.BRICKS);
            }
            setBlockSafe(region, x + 3, y + chimneyHeight + 1, z - 3, Material.CAMPFIRE);
        }
    }

    /**
     * 8. 墓地遗迹
     */
    private void generateGraveyard(LimitedRegion region, WorldInfo worldInfo, int x, int z, Random random) {
        int y = findLargestCaveFloor(region, worldInfo, x, z, 10);
        if (y < 0 || !region.isInRegion(x, y, z)) return;

        // 地面
        for (int dx = -5; dx <= 5; dx++) {
            for (int dz = -5; dz <= 5; dz++) {
                setBlockSafe(region, x + dx, y, z + dz,
                        random.nextFloat() < 0.4f ? Material.COARSE_DIRT : Material.PODZOL);
            }
        }

        // 墓碑
        int graveCount = random.nextInt(6) + 6;
        for (int i = 0; i < graveCount; i++) {
            int gx = x + random.nextInt(8) - 4;
            int gz = z + random.nextInt(8) - 4;

            Material[] graveMats = {Material.COBBLESTONE_WALL, Material.STONE_BRICK_WALL,
                    Material.MOSSY_COBBLESTONE_WALL, Material.DEEPSLATE_BRICK_WALL};
            Material graveMat = graveMats[random.nextInt(graveMats.length)];

            int maxHeight = getCeilingDistance(region, gx, y, gz) - 1;
            int graveHeight = Math.min(random.nextInt(2) + 1, maxHeight);

            for (int dy = 1; dy <= graveHeight; dy++) {
                setBlockSafe(region, gx, y + dy, gz, graveMat);
            }

            if (random.nextFloat() < 0.3f && graveHeight > 0) {
                setBlockSafe(region, gx, y + graveHeight + 1, gz, Material.SOUL_TORCH);
            }
        }

        // 中央大墓
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                setBlockSafe(region, x + dx, y + 1, z + dz, Material.POLISHED_DEEPSLATE);
            }
        }
        setBlockSafe(region, x, y + 2, z, Material.CHISELED_DEEPSLATE);
        setBlockSafe(region, x, y + 3, z, Material.SOUL_LANTERN);
    }

    /**
     * 9. 水晶圣殿
     */
    private void generateCrystalShrine(LimitedRegion region, WorldInfo worldInfo, int x, int z, Random random) {
        int y = findLargestCaveFloor(region, worldInfo, x, z, 12);
        if (y < 0 || !region.isInRegion(x, y, z)) return;

        // 紫水晶地基
        int radius = 6;
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                if (dx * dx + dz * dz <= radius * radius) {
                    setBlockSafe(region, x + dx, y, z + dz, Material.AMETHYST_BLOCK);
                    if (dx * dx + dz * dz <= 4) {
                        setBlockSafe(region, x + dx, y, z + dz, Material.BUDDING_AMETHYST);
                    }
                }
            }
        }

        // 水晶柱
        for (int angle = 0; angle < 360; angle += 45) {
            double rad = Math.toRadians(angle);
            int px = x + (int)(Math.cos(rad) * 5);
            int pz = z + (int)(Math.sin(rad) * 5);

            int maxHeight = getCeilingDistance(region, px, y, pz) - 1;
            int pillarHeight = Math.min(random.nextInt(4) + 5, maxHeight);

            for (int dy = 1; dy <= pillarHeight; dy++) {
                setBlockSafe(region, px, y + dy, pz, Material.AMETHYST_BLOCK);
            }
            setBlockSafe(region, px, y + pillarHeight + 1, pz, Material.AMETHYST_CLUSTER);
        }

        // 中心大水晶
        int maxCenterHeight = getCeilingDistance(region, x, y, z) - 2;
        int crystalHeight = Math.min(8, maxCenterHeight);

        for (int dy = 1; dy <= crystalHeight; dy++) {
            int r = Math.max(1, 3 - dy / 3);
            for (int dx = -r; dx <= r; dx++) {
                for (int dz = -r; dz <= r; dz++) {
                    if (dx * dx + dz * dz <= r * r) {
                        setBlockSafe(region, x + dx, y + dy, z + dz, Material.AMETHYST_BLOCK);
                    }
                }
            }
        }

        if (crystalHeight >= 8) {
            setBlockSafe(region, x, y + 9, z, Material.SEA_LANTERN);
            setBlockSafe(region, x, y + 10, z, Material.END_ROD);
        }
    }

    /**
     * 10. 机械遗迹
     */
    private void generateMechanicalRuin(LimitedRegion region, WorldInfo worldInfo, int x, int z, Random random) {
        int y = findLargestCaveFloor(region, worldInfo, x, z, 10);
        if (y < 0 || !region.isInRegion(x, y, z)) return;

        // 金属地板
        for (int dx = -5; dx <= 5; dx++) {
            for (int dz = -5; dz <= 5; dz++) {
                Material floor = random.nextFloat() < 0.3f ? Material.IRON_BLOCK : Material.COPPER_BLOCK;
                setBlockSafe(region, x + dx, y, z + dz, floor);
            }
        }

        // 齿轮结构
        setBlockSafe(region, x - 3, y + 1, z, Material.PISTON);
        setBlockSafe(region, x + 3, y + 1, z, Material.STICKY_PISTON);
        setBlockSafe(region, x, y + 1, z - 3, Material.PISTON);
        setBlockSafe(region, x, y + 1, z + 3, Material.PISTON);

        // 中央机器
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                for (int dy = 1; dy <= 3; dy++) {
                    Material mat = dy == 2 ? Material.REDSTONE_BLOCK : Material.IRON_BLOCK;
                    setBlockSafe(region, x + dx, y + dy, z + dz, mat);
                }
            }
        }

        // 管道
        for (int i = 1; i <= 4; i++) {
            setBlockSafe(region, x + i, y + 2, z, Material.LIGHTNING_ROD);
            setBlockSafe(region, x - i, y + 2, z, Material.LIGHTNING_ROD);
        }

        // 顶部装置
        if (getCeilingDistance(region, x, y, z) >= 6) {
            setBlockSafe(region, x, y + 4, z, Material.BEACON);
            setBlockSafe(region, x, y + 5, z, Material.END_ROD);
        }

        // 控制台
        setBlockSafe(region, x + 4, y + 1, z + 2, Material.OBSERVER);
        setBlockSafe(region, x + 4, y + 1, z + 3, Material.COMPARATOR);
        setBlockSafe(region, x + 4, y + 2, z + 2, Material.REDSTONE_LAMP);
    }

    // ==================== 装饰生成方法 ====================

    /**
     * 太空电梯
     */
    private void generateSpaceElevator(LimitedRegion region, WorldInfo worldInfo, int centerX, int centerZ, Random random) {
        int minY = worldInfo.getMinHeight() + 30;
        int maxY = worldInfo.getMaxHeight() - 10;

        if (!region.isInRegion(centerX, 100, centerZ)) return;

        for (int y = minY; y < maxY; y++) {
            // 核心
            for (int dx = -1; dx <= 0; dx++) {
                for (int dz = -1; dz <= 0; dz++) {
                    setBlockSafe(region, centerX + dx, y, centerZ + dz, Material.IRON_BLOCK);
                }
            }

            // 发光管道
            if (y % 3 == 0) {
                setBlockSafe(region, centerX + 1, y, centerZ + 1, Material.SEA_LANTERN);
                setBlockSafe(region, centerX - 2, y, centerZ + 1, Material.SEA_LANTERN);
                setBlockSafe(region, centerX + 1, y, centerZ - 2, Material.SEA_LANTERN);
                setBlockSafe(region, centerX - 2, y, centerZ - 2, Material.SEA_LANTERN);
            }

            // 平台
            if (y % 20 == 0) {
                for (int dx = -4; dx <= 3; dx++) {
                    for (int dz = -4; dz <= 3; dz++) {
                        if (Math.abs(dx) + Math.abs(dz) <= 5) {
                            setBlockSafe(region, centerX + dx, y, centerZ + dz, Material.POLISHED_DEEPSLATE);
                        }
                    }
                }
            }
        }
    }

    /**
     * 巨型光源
     */
    private void generateMegaLightSource(LimitedRegion region, int x, int z, Random random) {
        int baseY = 100;  // 降低高度以适应岩洞
        if (!region.isInRegion(x, baseY, z)) return;

        int maxRadius = 10;
        for (int dx = -maxRadius; dx <= maxRadius; dx++) {
            for (int dy = -maxRadius; dy <= maxRadius; dy++) {
                for (int dz = -maxRadius; dz <= maxRadius; dz++) {
                    double dist = Math.sqrt(dx * dx + dy * dy + dz * dz);
                    if (dist <= 4) {
                        setBlockSafe(region, x + dx, baseY + dy, z + dz, Material.GLOWSTONE);
                    } else if (dist <= 6) {
                        setBlockSafe(region, x + dx, baseY + dy, z + dz, Material.SEA_LANTERN);
                    } else if (dist <= 8 && random.nextFloat() < 0.5f) {
                        setBlockSafe(region, x + dx, baseY + dy, z + dz, Material.GLASS);
                    }
                }
            }
        }

        // 能量柱
        for (int dy = -40; dy <= 40; dy++) {
            if (Math.abs(dy) > maxRadius) {
                setBlockSafe(region, x, baseY + dy, z, Material.END_ROD);
            }
        }
    }

    /**
     * 大型水晶
     */
    private void generateLargeCrystal(LimitedRegion region, WorldInfo worldInfo, int x, int z, Random random) {
        int y = findLargestCaveFloor(region, worldInfo, x, z, 4);
        if (y < 0 || !hasSupport(region, x, y, z)) return;

        int maxHeight = getCeilingDistance(region, x, y, z) - 2;
        int height = Math.min(random.nextInt(10) + 8, maxHeight);

        if (height < 6) return;

        Material[] crystalTypes = {Material.AMETHYST_BLOCK, Material.QUARTZ_BLOCK, Material.PRISMARINE};
        Material crystalMat = crystalTypes[random.nextInt(crystalTypes.length)];

        // 基座
        for (int dx = -2; dx <= 2; dx++) {
            for (int dz = -2; dz <= 2; dz++) {
                if (dx * dx + dz * dz <= 4) {
                    setBlockSafe(region, x + dx, y, z + dz, crystalMat);
                }
            }
        }

        // 晶体
        for (int dy = 1; dy < height; dy++) {
            int radius = Math.max(1, 3 - dy / 3);
            for (int dx = -radius; dx <= radius; dx++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    if (dx * dx + dz * dz <= radius * radius) {
                        setBlockSafe(region, x + dx, y + dy, z + dz, crystalMat);
                    }
                }
            }
        }
        setBlockSafe(region, x, y + height, z, Material.END_ROD);
    }

    /**
     * 下界裂隙
     */
    private void generateNetherRift(LimitedRegion region, WorldInfo worldInfo, int x, int z, Random random) {
        int y = findLargestCaveFloor(region, worldInfo, x, z, 6);
        if (y < 0) return;

        int length = random.nextInt(10) + 6;
        int depth = random.nextInt(3) + 2;

        for (int i = 0; i < length; i++) {
            for (int w = -2; w <= 2; w++) {
                int rx = x + i;
                int rz = z + w;

                // 表面
                setBlockSafe(region, rx, y, rz,
                        random.nextFloat() < 0.5f ? Material.NETHERRACK : Material.CRIMSON_NYLIUM);

                // 裂隙
                for (int d = 1; d <= depth; d++) {
                    if (d == depth) {
                        setBlockSafe(region, rx, y - d, rz, Material.LAVA);
                    } else {
                        setBlockSafe(region, rx, y - d, rz, Material.NETHERRACK);
                    }
                }

                // 火焰
                if (random.nextFloat() < 0.1f) {
                    setBlockSafe(region, rx, y + 1, rz, Material.SOUL_FIRE);
                }
            }
        }
    }

    /**
     * 光源柱
     */
    private void generateLightPillar(LimitedRegion region, WorldInfo worldInfo, int x, int z, Random random) {
        int y = findLargestCaveFloor(region, worldInfo, x, z, 3);
        if (y < 0 || !hasSupport(region, x, y, z)) return;

        int maxHeight = getCeilingDistance(region, x, y, z) - 2;
        int height = Math.min(random.nextInt(8) + 6, maxHeight);

        if (height < 5) return;

        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                setBlockSafe(region, x + dx, y, z + dz, Material.DEEPSLATE_BRICKS);
            }
        }

        for (int dy = 1; dy < height; dy++) {
            setBlockSafe(region, x, y + dy, z, Material.POLISHED_DEEPSLATE_WALL);
            if (dy % 3 == 0) {
                setBlockSafe(region, x + 1, y + dy, z, Material.GLOWSTONE);
                setBlockSafe(region, x - 1, y + dy, z, Material.GLOWSTONE);
            }
        }

        setBlockSafe(region, x, y + height, z, Material.SEA_LANTERN);
        setBlockSafe(region, x, y + height + 1, z, Material.END_ROD);
    }

    /**
     * 下界树
     */
    private void generateNetherTree(LimitedRegion region, WorldInfo worldInfo, int x, int z, Random random) {
        int y = findLargestCaveFloor(region, worldInfo, x, z, 3);
        if (y < 0 || !hasSupport(region, x, y, z)) return;

        int maxHeight = getCeilingDistance(region, x, y, z) - 5;
        int height = Math.min(random.nextInt(6) + 8, maxHeight);

        if (height < 6) return;

        boolean isCrimson = random.nextBoolean();
        Material stemMat = isCrimson ? Material.CRIMSON_STEM : Material.WARPED_STEM;
        Material wartMat = isCrimson ? Material.NETHER_WART_BLOCK : Material.WARPED_WART_BLOCK;
        Material lightMat = Material.GLOWSTONE;

        // 树干
        for (int dy = 0; dy < height; dy++) {
            setBlockSafe(region, x, y + dy, z, stemMat);
        }

        // 树冠
        int topY = y + height;
        for (int dx = -3; dx <= 3; dx++) {
            for (int dz = -3; dz <= 3; dz++) {
                for (int dy = -2; dy <= 1; dy++) {
                    double dist = Math.sqrt(dx * dx + dz * dz);
                    if (dist <= 3 - Math.abs(dy)) {
                        Material mat = random.nextFloat() < 0.15f ? lightMat : wartMat;
                        setBlockSafe(region, x + dx, topY + dy, z + dz, mat);
                    }
                }
            }
        }
    }

    /**
     * 玄武岩柱
     */
    private void generateBasaltPillar(LimitedRegion region, WorldInfo worldInfo, int x, int z, Random random) {
        int y = findLargestCaveFloor(region, worldInfo, x, z, 2);
        if (y < 0 || !hasSupport(region, x, y, z)) return;

        int maxHeight = getCeilingDistance(region, x, y, z) - 2;
        int height = Math.min(random.nextInt(15) + 10, maxHeight);

        if (height < 8) return;

        int radius = random.nextInt(2) + 1;

        for (int dy = 0; dy < height; dy++) {
            for (int dx = -radius; dx <= radius; dx++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    if (dx * dx + dz * dz <= radius * radius) {
                        Material mat = random.nextFloat() < 0.2f ? Material.POLISHED_BASALT : Material.BASALT;
                        setBlockSafe(region, x + dx, y + dy, z + dz, mat);
                    }
                }
            }
        }

        if (random.nextFloat() < 0.5f) {
            setBlockSafe(region, x, y + height, z, Material.MAGMA_BLOCK);
        }
    }

    /**
     * 散布光源
     */
    private void scatterLightSources(LimitedRegion region, WorldInfo worldInfo, int worldX, int worldZ, Random random) {
        int lightCount = random.nextInt(2) + 1;

        for (int i = 0; i < lightCount; i++) {
            int lx = worldX + random.nextInt(16);
            int lz = worldZ + random.nextInt(16);
            int ly = findLargestCaveFloor(region, worldInfo, lx, lz, 1);

            if (ly > 0 && region.isInRegion(lx, ly + 1, lz)) {
                Material below = getBlockSafe(region, lx, ly, lz);
                Material current = getBlockSafe(region, lx, ly + 1, lz);

                if (below != null && below.isSolid() && current == Material.AIR) {
                    Material[] lights = {Material.LANTERN, Material.END_ROD, Material.SOUL_LANTERN};
                    setBlockSafe(region, lx, ly + 1, lz, lights[random.nextInt(lights.length)]);
                }
            }
        }
    }

    // ==================== 工具方法 ====================

    /**
     * 寻找最大的岩洞地面（优先寻找大空间）
     */
    private int findLargestCaveFloor(LimitedRegion region, WorldInfo worldInfo, int x, int z, int minSpaceNeeded) {
        int bestY = -1;
        int bestSpace = 0;

        // 从中间往下扫描
        for (int y = 90; y > worldInfo.getMinHeight() + 15; y--) {
            try {
                if (!region.isInRegion(x, y, z)) continue;

                Material block = region.getType(x, y, z);
                Material above = region.isInRegion(x, y + 1, z) ? region.getType(x, y + 1, z) : Material.AIR;

                // 找到地面（固体下方有空气）
                if (block.isSolid() && !above.isSolid() && above != Material.LAVA && block != Material.BEDROCK) {
                    // 计算上方空间
                    int space = getCeilingDistance(region, x, y + 1, z);

                    // 找空间最大的地面
                    if (space >= minSpaceNeeded && space > bestSpace) {
                        bestSpace = space;
                        bestY = y + 1;
                    }
                }
            } catch (Exception e) {
                continue;
            }
        }

        return bestY;
    }

    /**
     * 获取到天花板的距离
     */
    private int getCeilingDistance(LimitedRegion region, int x, int y, int z) {
        int distance = 0;
        for (int dy = 0; dy < 50; dy++) {
            try {
                if (!region.isInRegion(x, y + dy, z)) return distance;
                Material mat = region.getType(x, y + dy, z);
                if (mat.isSolid() || mat == Material.LAVA) {
                    return distance;
                }
                distance++;
            } catch (Exception e) {
                return distance;
            }
        }
        return distance;
    }

    /**
     * 检查岩洞高度是否足够
     */
    private boolean checkCaveHeight(LimitedRegion region, int x, int y, int z, int requiredHeight) {
        return getCeilingDistance(region, x, y, z) >= requiredHeight;
    }

    private boolean hasSupport(LimitedRegion region, int x, int y, int z) {
        for (int dy = 1; dy <= 3; dy++) {
            Material below = getBlockSafe(region, x, y - dy, z);
            if (below != null && below.isSolid()) return true;
        }
        return false;
    }

    private void setBlockSafe(LimitedRegion region, int x, int y, int z, Material material) {
        try {
            if (region.isInRegion(x, y, z)) {
                region.setType(x, y, z, material);
            }
        } catch (Exception e) {}
    }

    private Material getBlockSafe(LimitedRegion region, int x, int y, int z) {
        try {
            if (region.isInRegion(x, y, z)) return region.getType(x, y, z);
        } catch (Exception e) {}
        return null;
    }
}