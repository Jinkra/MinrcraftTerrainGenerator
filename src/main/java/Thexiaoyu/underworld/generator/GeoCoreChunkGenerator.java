package Thexiaoyu.underworld.generator;

import Thexiaoyu.underworld.Underworld;
import Thexiaoyu.underworld.biome.GeoBiome;
import Thexiaoyu.underworld.util.FastNoise;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.type.PointedDripstone;
import org.bukkit.generator.BiomeProvider;
import org.bukkit.generator.BlockPopulator;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.generator.WorldInfo;

import java.util.*;

/**
 * 地心世界岩洞生成器 - 完整岩洞系统
 *
 * 核心特性：
 * 1. 3D密度场生成完整岩洞网络
 * 2. 无地表概念，整个世界由岩洞组成
 * 3. 多层噪声创建复杂岩洞结构
 * 4. 群系影响岩洞形态和材质
 * 5. 动态检测地面/天花板放置装饰
 *
 * @author Jinkra
 */
public class GeoCoreChunkGenerator extends ChunkGenerator {

    private final Underworld plugin;

    // 噪声生成器
    private FastNoise caveDensityNoise;      // 主岩洞密度
    private FastNoise caveShapeNoise;        // 岩洞形状变化
    private FastNoise caveDetailNoise;       // 岩洞细节
    private FastNoise largeChamberNoise;     // 大型空腔
    private FastNoise verticalVariation;     // 垂直变化
    private FastNoise erosionNoise;          // 侵蚀效果

    private FastNoise biomeNoise;
    private FastNoise biomeNoise2;
    private FastNoise biomeNoise3;

    private FastNoise stalactiteNoise;
    private FastNoise pillarNoise;
    private FastNoise decorationNoise;
    private FastNoise blendNoise;

    // 世界参数
    private static final int MIN_Y = -64;
    private static final int MAX_Y = 130;
    private static final int BEDROCK_CEILING_Y = MAX_Y - 5;
    private static final int BEDROCK_FLOOR_Y = MIN_Y + 10;
    private static final int LAVA_LEVEL = MIN_Y + 20;

    // 岩洞密度阈值
    private static final float CAVE_THRESHOLD = 0.0f;  // 低于此值为空气

    // 群系混合参数
    private static final int BLEND_RADIUS = 32;

    private final GeoBiomeProvider biomeProvider;

    public GeoCoreChunkGenerator(Underworld plugin) {
        this.plugin = plugin;
        this.biomeProvider = new GeoBiomeProvider();
        initNoiseGenerators();
    }

    private void initNoiseGenerators() {
        long seed = 1337;

        // 主岩洞密度噪声 - 创建基础岩洞结构
        caveDensityNoise = new FastNoise((int) seed);
        caveDensityNoise.SetNoiseType(FastNoise.NoiseType.SimplexFractal);
        caveDensityNoise.SetFractalOctaves(4);
        caveDensityNoise.SetFractalLacunarity(2.0f);
        caveDensityNoise.SetFractalGain(0.5f);
        caveDensityNoise.SetFrequency(0.008f);

        // 岩洞形状变化 - 创建不规则形状
        caveShapeNoise = new FastNoise((int) (seed + 100));
        caveShapeNoise.SetNoiseType(FastNoise.NoiseType.SimplexFractal);
        caveShapeNoise.SetFractalOctaves(3);
        caveShapeNoise.SetFrequency(0.015f);

        // 岩洞细节噪声 - 增加表面细节
        caveDetailNoise = new FastNoise((int) (seed + 200));
        caveDetailNoise.SetNoiseType(FastNoise.NoiseType.Simplex);
        caveDetailNoise.SetFrequency(0.04f);

        // 大型空腔 - 创建巨大的岩洞空间
        largeChamberNoise = new FastNoise((int) (seed + 300));
        largeChamberNoise.SetNoiseType(FastNoise.NoiseType.SimplexFractal);
        largeChamberNoise.SetFractalOctaves(2);
        largeChamberNoise.SetFrequency(0.003f);

        // 垂直变化 - 影响岩洞的垂直密度
        verticalVariation = new FastNoise((int) (seed + 400));
        verticalVariation.SetNoiseType(FastNoise.NoiseType.SimplexFractal);
        verticalVariation.SetFractalOctaves(3);
        verticalVariation.SetFrequency(0.005f);

        // 侵蚀效果 - 创建不规则边缘
        erosionNoise = new FastNoise((int) (seed + 500));
        erosionNoise.SetNoiseType(FastNoise.NoiseType.Cellular);
        erosionNoise.SetCellularDistanceFunction(FastNoise.CellularDistanceFunction.Natural);
        erosionNoise.SetCellularReturnType(FastNoise.CellularReturnType.Distance);
        erosionNoise.SetFrequency(0.02f);

        // 群系噪声
        biomeNoise = new FastNoise((int) (seed + 1000));
        biomeNoise.SetNoiseType(FastNoise.NoiseType.SimplexFractal);
        biomeNoise.SetFractalOctaves(2);
        biomeNoise.SetFrequency(0.0001f);

        biomeNoise2 = new FastNoise((int) (seed + 1100));
        biomeNoise2.SetNoiseType(FastNoise.NoiseType.Simplex);
        biomeNoise2.SetFrequency(0.0004f);

        biomeNoise3 = new FastNoise((int) (seed + 1200));
        biomeNoise3.SetNoiseType(FastNoise.NoiseType.Simplex);
        biomeNoise3.SetFrequency(0.0003f);

        // 装饰噪声
        stalactiteNoise = new FastNoise((int) (seed + 2000));
        stalactiteNoise.SetNoiseType(FastNoise.NoiseType.Simplex);
        stalactiteNoise.SetFrequency(0.08f);

        pillarNoise = new FastNoise((int) (seed + 2100));
        pillarNoise.SetNoiseType(FastNoise.NoiseType.Cellular);
        pillarNoise.SetCellularDistanceFunction(FastNoise.CellularDistanceFunction.Euclidean);
        pillarNoise.SetCellularReturnType(FastNoise.CellularReturnType.Distance);
        pillarNoise.SetFrequency(0.01f);

        decorationNoise = new FastNoise((int) (seed + 2200));
        decorationNoise.SetNoiseType(FastNoise.NoiseType.Simplex);
        decorationNoise.SetFrequency(0.05f);

        blendNoise = new FastNoise((int) (seed + 2300));
        blendNoise.SetNoiseType(FastNoise.NoiseType.Simplex);
        blendNoise.SetFrequency(0.015f);
    }

    @Override
    public void generateNoise(WorldInfo worldInfo, Random random, int chunkX, int chunkZ, ChunkData chunkData) {
        int worldX = chunkX << 4;
        int worldZ = chunkZ << 4;

        // 预计算群系数据
        BlendedBiomeParams[][] biomeCache = new BlendedBiomeParams[16][16];
        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                int realX = worldX + x;
                int realZ = worldZ + z;
                biomeCache[x][z] = getBlendedBiomeParams(realX, realZ);
            }
        }

        // 生成基岩层
        generateBedrockLayers(chunkData, random);

        // 主岩洞生成 - 使用3D密度场
        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                int realX = worldX + x;
                int realZ = worldZ + z;

                BlendedBiomeParams biomeParams = biomeCache[x][z];

                for (int y = BEDROCK_FLOOR_Y + 1; y < BEDROCK_CEILING_Y; y++) {
                    float density = calculateCaveDensity(realX, y, realZ, biomeParams);

                    if (density > CAVE_THRESHOLD) {
                        // 固体区域 - 放置方块
                        Material block = getBlockForBiome(biomeParams.primaryBiome, y, density, random);
                        chunkData.setBlock(x, y, z, block);
                    } else {
                        // 空气区域
                        chunkData.setBlock(x, y, z, Material.AIR);

                        // 在低处填充熔岩
                        if (y <= LAVA_LEVEL) {
                            chunkData.setBlock(x, y, z, Material.WATER);
                        }
                    }
                }
            }
        }

        // 第二遍：装饰生成（需要知道周围方块状态）
        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                int realX = worldX + x;
                int realZ = worldZ + z;
                BlendedBiomeParams biomeParams = biomeCache[x][z];

                // 生成钟乳石
                generateStalactites(chunkData, x, z, realX, realZ, biomeParams, random);

                // 生成地面装饰
                generateFloorDecorations(chunkData, x, z, realX, realZ, biomeParams, random);

                // 生成天花板装饰
                generateCeilingDecorations(chunkData, x, z, realX, realZ, biomeParams, random);

                // 生成柱子
                generateCavePillars(chunkData, x, z, realX, realZ, biomeParams, random);
            }
        }
    }

    /**
     * 计算3D岩洞密度
     * 返回值 > CAVE_THRESHOLD 为固体，否则为空气
     */
    private float calculateCaveDensity(int x, int y, int z, BlendedBiomeParams biomeParams) {
        // 基础密度 - 主岩洞结构
        float baseDensity = caveDensityNoise.GetNoise(x, y, z);

        // 形状变化
        float shapeVariation = caveShapeNoise.GetNoise(x, y, z) * 0.4f;

        // 细节噪声
        float detail = caveDetailNoise.GetNoise(x, y, z) * 0.15f;

        // 大型空腔
        float chamber = largeChamberNoise.GetNoise(x, y, z);
        float chamberEffect = 0;
        if (chamber > 0.6f) {
            chamberEffect = -(chamber - 0.6f) * 2.0f;  // 创建大空间
        }

        // 垂直变化 - 让某些高度更密集或更空旷
        float verticalEffect = verticalVariation.GetNoise(x * 0.5f, y * 1.5f, z * 0.5f) * 0.3f;

        // 侵蚀效果 - 让边缘不规则
        float erosion = erosionNoise.GetNoise(x, y, z) * 0.2f;

        // 群系影响
        float biomeModifier = biomeParams.caveDensityModifier;

        // 垂直密度梯度 - 让上下部分更密集
        float yNormalized = (float)(y - BEDROCK_FLOOR_Y) / (BEDROCK_CEILING_Y - BEDROCK_FLOOR_Y);
        float verticalGradient = 0;

        if (yNormalized < 0.15f) {
            // 底部更密集
            verticalGradient = (0.15f - yNormalized) * 0.8f;
        } else if (yNormalized > 0.85f) {
            // 顶部更密集
            verticalGradient = (yNormalized - 0.85f) * 1.2f;
        }

        // 组合所有因素
        float finalDensity = baseDensity + shapeVariation + detail + chamberEffect +
                verticalEffect + erosion + verticalGradient;

        // 应用群系修正
        finalDensity *= biomeModifier;

        return finalDensity;
    }

    private static class BlendedBiomeParams {
        GeoBiome primaryBiome;
        float caveDensityModifier;  // 影响岩洞密度
        float caveHeightBias;       // 影响岩洞高度倾向

        BlendedBiomeParams(GeoBiome primary, float densityMod, float heightBias) {
            this.primaryBiome = primary;
            this.caveDensityModifier = densityMod;
            this.caveHeightBias = heightBias;
        }
    }

    private BlendedBiomeParams getBlendedBiomeParams(int x, int z) {
        GeoBiome centerBiome = getBiomeAt(x, z);

        float totalWeight = 0;
        float blendedDensityMod = 0;
        float blendedHeightBias = 0;

        for (int dx = -BLEND_RADIUS; dx <= BLEND_RADIUS; dx += BLEND_RADIUS / 4) {
            for (int dz = -BLEND_RADIUS; dz <= BLEND_RADIUS; dz += BLEND_RADIUS / 4) {
                int sampleX = x + dx;
                int sampleZ = z + dz;

                GeoBiome sampleBiome = getBiomeAt(sampleX, sampleZ);

                float distance = (float) Math.sqrt(dx * dx + dz * dz);
                float weight = (float) Math.exp(-distance * distance / (2 * BLEND_RADIUS * BLEND_RADIUS / 8));

                float noiseOffset = blendNoise.GetNoise(sampleX, sampleZ) * 0.1f;
                weight *= (1 + noiseOffset);
                weight = Math.max(0.01f, weight);

                blendedDensityMod += getBiomeDensityModifier(sampleBiome) * weight;
                blendedHeightBias += getBiomeHeightBias(sampleBiome) * weight;
                totalWeight += weight;
            }
        }

        if (totalWeight > 0) {
            blendedDensityMod /= totalWeight;
            blendedHeightBias /= totalWeight;
        } else {
            blendedDensityMod = getBiomeDensityModifier(centerBiome);
            blendedHeightBias = getBiomeHeightBias(centerBiome);
        }

        return new BlendedBiomeParams(centerBiome, blendedDensityMod, blendedHeightBias);
    }

    /**
     * 获取群系的密度修正值
     * > 1.0 = 更密集的岩洞（更多石头）
     * < 1.0 = 更空旷的岩洞（更多空间）
     */
    private float getBiomeDensityModifier(GeoBiome biome) {
        switch (biome) {
            case CRYSTAL_FOREST:
            case EMERALD_CAVES:
                return 0.85f;  // 更空旷，利于展示水晶
            case GLOWING_MUSHROOM_CAVES:
                return 0.9f;
            case CRIMSON_GROWTH:
                return 0.95f;
            case MOLTEN_RIVERS:
                return 1.1f;   // 更密集，突出熔岩河
            case BASALT_PILLARS:
                return 1.15f;  // 密集，突出柱子
            case ANCIENT_RUINS:
                return 0.8f;   // 空旷，适合遗迹
            case SCULK_DEPTHS:
                return 1.05f;
            case METAL_VEINS:
            case GOLDEN_FISSURE:
                return 1.0f;
            case OBSIDIAN_PLAINS:
                return 1.2f;   // 最密集
            case NETHER_BLEED:
                return 0.95f;
            case VOID_EXPANSE:
                return 0.75f;  // 最空旷
            default:
                return 1.0f;
        }
    }

    /**
     * 获取群系的高度倾向
     * 正值 = 倾向于上部，负值 = 倾向于下部
     */
    private float getBiomeHeightBias(GeoBiome biome) {
        switch (biome) {
            case CRYSTAL_FOREST:
            case EMERALD_CAVES:
                return 0.1f;
            case MOLTEN_RIVERS:
                return -0.2f;  // 倾向于低处
            case VOID_EXPANSE:
                return 0.2f;   // 倾向于高处
            default:
                return 0.0f;
        }
    }

    /**
     * 根据群系和密度值选择方块
     */
    private Material getBlockForBiome(GeoBiome biome, int y, float density, Random random) {
        // 密度越高越接近核心层
        boolean isCore = density > 0.4f;
        boolean isSurface = density < 0.15f;

        switch (biome) {
            case CRYSTAL_FOREST:
                if (isSurface && random.nextFloat() < 0.2f) return Material.AMETHYST_BLOCK;
                if (random.nextFloat() < 0.05f) return Material.CALCITE;
                return isCore ? Material.DEEPSLATE : Material.TUFF;

            case EMERALD_CAVES:
                if (isSurface) {
                    if (random.nextFloat() < 0.08f) return Material.EMERALD_BLOCK;
                    if (random.nextFloat() < 0.3f) return Material.MOSS_BLOCK;
                }
                return isCore ? Material.DEEPSLATE : Material.ROOTED_DIRT;

            case GLOWING_MUSHROOM_CAVES:
                if (isSurface && random.nextFloat() < 0.4f) return Material.MOSS_BLOCK;
                if (random.nextFloat() < 0.1f) return Material.MUD;
                return isCore ? Material.DEEPSLATE : Material.SCULK;

            case CRIMSON_GROWTH:
                if (isSurface && random.nextFloat() < 0.5f) return Material.CRIMSON_NYLIUM;
                return isCore ? Material.BASALT : Material.NETHERRACK;


            case MOLTEN_RIVERS:
                if (random.nextFloat() < 0.15f) return Material.MAGMA_BLOCK;
                if (random.nextFloat() < 0.1f) return Material.GILDED_BLACKSTONE;
                return isCore ? Material.BASALT : Material.BLACKSTONE;


            case BASALT_PILLARS:
                if (random.nextFloat() < 0.2f) return Material.POLISHED_BASALT;
                return Material.BASALT;

            case ANCIENT_RUINS:
                if (isSurface && random.nextFloat() < 0.15f) return Material.POLISHED_DEEPSLATE;
                if (random.nextFloat() < 0.1f) return Material.DEEPSLATE_BRICKS;
                return isCore ? Material.DEEPSLATE : Material.DEEPSLATE_TILES;

            case SCULK_DEPTHS:
                if (isSurface && random.nextFloat() < 0.3f) return Material.SCULK;
                if (random.nextFloat() < 0.05f) return Material.SCULK_CATALYST;
                return isCore ? Material.DEEPSLATE : Material.SCULK;

            case METAL_VEINS:
                if (random.nextFloat() < 0.15f) return Material.RAW_COPPER_BLOCK;
                if (random.nextFloat() < 0.1f) return Material.RAW_IRON_BLOCK;
                return isCore ? Material.DEEPSLATE : Material.TUFF;

            case GOLDEN_FISSURE:
                if (random.nextFloat() < 0.12f) return Material.RAW_GOLD_BLOCK;
                if (random.nextFloat() < 0.08f) return Material.GOLD_BLOCK;
                return isCore ? Material.BASALT : Material.NETHERRACK;

            case OBSIDIAN_PLAINS:
                return Material.DEEPSLATE;

            case NETHER_BLEED:
                if (isSurface && random.nextFloat() < 0.3f) return Material.CRIMSON_NYLIUM;
                if (random.nextFloat() < 0.1f) return Material.GLOWSTONE;
                return isCore ? Material.BASALT : Material.NETHERRACK;

            case VOID_EXPANSE:
                if (random.nextFloat() < 0.2f) return Material.PURPUR_BLOCK;
                return isCore ? Material.END_STONE : Material.END_STONE_BRICKS;

            default:
                return isCore ? Material.DEEPSLATE : Material.STONE;
        }
    }

    /**
     * 生成钟乳石
     */
    private void generateStalactites(ChunkData chunkData, int x, int z, int realX, int realZ,
                                     BlendedBiomeParams biomeParams, Random random) {
        float stalactiteChance = stalactiteNoise.GetNoise(realX, realZ);

        if (stalactiteChance > 0.5f) {
            // 从上往下找天花板
            for (int y = BEDROCK_CEILING_Y - 1; y > BEDROCK_FLOOR_Y + 20; y--) {
                Material current = chunkData.getType(x, y, z);
                Material below = chunkData.getType(x, y - 1, z);

                // 找到天花板（固体下方是空气）
                if (current.isSolid() && !below.isSolid() && below != Material.LAVA) {
                    int length = random.nextInt(8) + 4;
                    int radius = random.nextInt(2) + 1;

                    // 生成钟乳石
                    for (int dy = 0; dy < length; dy++) {
                        int ty = y - dy - 1;
                        if (ty <= BEDROCK_FLOOR_Y + 10) break;

                        Material checkMat = chunkData.getType(x, ty, z);
                        if (checkMat.isSolid()) break;

                        float taperFactor = 1.0f - (float)dy / length * 0.7f;
                        int currentRadius = Math.max(0, (int)(radius * taperFactor));

                        if (currentRadius == 0 && dy == length - 1) {
                            // 尖端
                            BlockData dripstoneData = Material.POINTED_DRIPSTONE.createBlockData();
                            if (dripstoneData instanceof PointedDripstone) {
                                PointedDripstone pointedDripstone = (PointedDripstone) dripstoneData;
                                pointedDripstone.setVerticalDirection(BlockFace.DOWN);
                                pointedDripstone.setThickness(PointedDripstone.Thickness.TIP);
                                chunkData.setBlock(x, ty, z, dripstoneData);
                            }
                        } else {
                            chunkData.setBlock(x, ty, z, Material.DRIPSTONE_BLOCK);
                        }
                    }
                    break;  // 只生成一个钟乳石
                }
            }
        }
    }

    /**
     * 生成地面装饰
     */
    private void generateFloorDecorations(ChunkData chunkData, int x, int z, int realX, int realZ,
                                          BlendedBiomeParams biomeParams, Random random) {
        // 从下往上找地面
        for (int y = BEDROCK_FLOOR_Y + 1; y < BEDROCK_CEILING_Y - 5; y++) {
            Material current = chunkData.getType(x, y, z);
            Material below = chunkData.getType(x, y - 1, z);

            // 找到地面（空气上方有固体）
            if (!current.isSolid() && current != Material.LAVA && below.isSolid()) {
                float decorChance = decorationNoise.GetNoise(realX, y, realZ);

                if (decorChance > 0.6f && random.nextFloat() < 0.15f) {
                    Material decoration = getFloorDecoration(biomeParams.primaryBiome, random);
                    if (decoration != null) {
                        chunkData.setBlock(x, y, z, decoration);
                    }
                }
                break;  // 只装饰最下面的地面
            }
        }
    }

    /**
     * 生成天花板装饰
     */
    private void generateCeilingDecorations(ChunkData chunkData, int x, int z, int realX, int realZ,
                                            BlendedBiomeParams biomeParams, Random random) {
        // 从上往下找天花板
        for (int y = BEDROCK_CEILING_Y - 1; y > BEDROCK_FLOOR_Y + 20; y--) {
            Material current = chunkData.getType(x, y, z);
            Material below = chunkData.getType(x, y - 1, z);

            // 找到天花板
            if (current.isSolid() && !below.isSolid() && below != Material.LAVA) {
                float decorChance = decorationNoise.GetNoise(realX, y, realZ);

                if (decorChance > 0.65f && random.nextFloat() < 0.08f) {
                    // 悬挂光源
                    chunkData.setBlock(x, y - 1, z, Material.GLOWSTONE);
                }
                break;
            }
        }
    }

    /**
     * 获取地面装饰
     */
    private Material getFloorDecoration(GeoBiome biome, Random random) {
        switch (biome) {
            case CRYSTAL_FOREST:
                Material[] buds = {Material.SMALL_AMETHYST_BUD, Material.MEDIUM_AMETHYST_BUD,
                        Material.LARGE_AMETHYST_BUD, Material.AMETHYST_CLUSTER};
                return buds[random.nextInt(buds.length)];

            case EMERALD_CAVES:
                return random.nextFloat() < 0.5f ? Material.AZALEA : Material.FLOWERING_AZALEA;

            case GLOWING_MUSHROOM_CAVES:
                return random.nextBoolean() ? Material.BROWN_MUSHROOM : Material.RED_MUSHROOM;

            case CRIMSON_GROWTH:
                Material[] crimsonPlants = {Material.CRIMSON_FUNGUS, Material.CRIMSON_ROOTS, Material.NETHER_SPROUTS};
                return crimsonPlants[random.nextInt(crimsonPlants.length)];

            case ANCIENT_RUINS:
                Material[] lights = {Material.LANTERN, Material.SOUL_LANTERN, Material.SEA_LANTERN};
                return lights[random.nextInt(lights.length)];

            case SCULK_DEPTHS:
                return Material.SCULK_VEIN;

            case NETHER_BLEED:
                return random.nextFloat() < 0.5f ? Material.SOUL_FIRE : Material.FIRE;

            case VOID_EXPANSE:
                return Material.END_ROD;

            default:
                return null;
        }
    }

    /**
     * 生成岩洞柱子（连接地面和天花板）
     */
    private void generateCavePillars(ChunkData chunkData, int x, int z, int realX, int realZ,
                                     BlendedBiomeParams biomeParams, Random random) {
        float pillarValue = pillarNoise.GetNoise(realX, realZ);

        if (pillarValue < 0.03f) {
            // 找到地面和天花板
            int floorY = -1;
            int ceilingY = -1;

            // 找地面
            for (int y = BEDROCK_FLOOR_Y + 1; y < BEDROCK_CEILING_Y - 10; y++) {
                Material below = chunkData.getType(x, y - 1, z);
                Material current = chunkData.getType(x, y, z);
                if (below.isSolid() && !current.isSolid() && current != Material.LAVA) {
                    floorY = y;
                    break;
                }
            }

            // 找天花板（从地面往上找）
            if (floorY > 0) {
                for (int y = floorY + 1; y < BEDROCK_CEILING_Y; y++) {
                    Material current = chunkData.getType(x, y, z);
                    Material above = chunkData.getType(x, y + 1, z);
                    if (!current.isSolid() && above.isSolid()) {
                        ceilingY = y;
                        break;
                    }
                }
            }

            // 如果距离合适，生成柱子
            if (floorY > 0 && ceilingY > 0 && (ceilingY - floorY) >= 15 && (ceilingY - floorY) <= 60) {
                int radius = random.nextInt(2) + 2;
                Material pillarMat = getPillarMaterial(biomeParams.primaryBiome, random);

                for (int y = floorY; y <= ceilingY; y++) {
                    for (int dx = -radius; dx <= radius; dx++) {
                        for (int dz = -radius; dz <= radius; dz++) {
                            if (dx * dx + dz * dz <= radius * radius) {
                                int tx = x + dx;
                                int tz = z + dz;
                                if (tx >= 0 && tx < 16 && tz >= 0 && tz < 16) {
                                    chunkData.setBlock(tx, y, tz, pillarMat);
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * 获取柱子材质
     */
    private Material getPillarMaterial(GeoBiome biome, Random random) {
        switch (biome) {
            case BASALT_PILLARS:
                return random.nextFloat() < 0.3f ? Material.POLISHED_BASALT : Material.BASALT;
            case CRYSTAL_FOREST:
            case EMERALD_CAVES:
                return Material.CALCITE;
            case ANCIENT_RUINS:
                return Material.POLISHED_DEEPSLATE;
            case CRIMSON_GROWTH:
            case NETHER_BLEED:
                return Material.BLACKSTONE;
            default:
                return Material.DEEPSLATE;
        }
    }

    /**
     * 生成基岩层
     */
    private void generateBedrockLayers(ChunkData chunkData, Random random) {
        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                // 顶部基岩
                for (int y = MAX_Y - 1; y >= BEDROCK_CEILING_Y; y--) {
                    int distFromTop = MAX_Y - 1 - y;
                    if (distFromTop < 2 || random.nextDouble() < 0.8 - (distFromTop * 0.15)) {
                        chunkData.setBlock(x, y, z, Material.BEDROCK);
                    } else {
                        chunkData.setBlock(x, y, z, Material.DEEPSLATE);
                    }
                }

                // 底部基岩
                for (int y = MIN_Y; y <= BEDROCK_FLOOR_Y; y++) {
                    int distFromBottom = y - MIN_Y;
                    if (distFromBottom < 3 || random.nextDouble() < 0.8 - (distFromBottom * 0.1)) {
                        chunkData.setBlock(x, y, z, Material.BEDROCK);
                    } else {
                        chunkData.setBlock(x, y, z, Material.DEEPSLATE);
                    }
                }
            }
        }
    }

    public GeoBiome getBiomeAt(int x, int z) {
        float biomeValue = biomeNoise.GetNoise(x, z);
        float biomeValue2 = biomeNoise2.GetNoise(x, z);
        float biomeValue3 = biomeNoise3.GetNoise(x, z);

        float combined = biomeValue * 0.5f + biomeValue2 * 0.3f + biomeValue3 * 0.2f;

        if (combined < -0.55f) {
            return GeoBiome.SCULK_DEPTHS;
        } else if (combined < -0.4f) {
            return GeoBiome.GLOWING_MUSHROOM_CAVES;
        } else if (combined < -0.25f) {
            return GeoBiome.VOID_EXPANSE;
        } else if (combined < 0.2f) {
            return GeoBiome.OBSIDIAN_PLAINS;
        } else if (combined < 0.35f) {
            return biomeValue3 > 0 ? GeoBiome.METAL_VEINS : GeoBiome.GOLDEN_FISSURE;
        } else if (combined < 0.45f) {
            return GeoBiome.MOLTEN_RIVERS;
        } else if (combined < 0.55f) {
            return GeoBiome.BASALT_PILLARS;
        } else if (combined < 0.65f) {
            return GeoBiome.NETHER_BLEED;
        } else if (combined < 0.75f) {
            return biomeValue2 > 0.3f ? GeoBiome.CRYSTAL_FOREST : GeoBiome.EMERALD_CAVES;
        } else {
            return GeoBiome.ANCIENT_RUINS;
        }
    }

    @Override
    public void generateSurface(WorldInfo worldInfo, Random random, int chunkX, int chunkZ, ChunkData chunkData) {
    }

    @Override
    public void generateBedrock(WorldInfo worldInfo, Random random, int chunkX, int chunkZ, ChunkData chunkData) {
    }

    @Override
    public void generateCaves(WorldInfo worldInfo, Random random, int chunkX, int chunkZ, ChunkData chunkData) {
    }

    @Override
    public BiomeProvider getDefaultBiomeProvider(WorldInfo worldInfo) {
        return biomeProvider;
    }

    @Override
    public List<BlockPopulator> getDefaultPopulators(World world) {
        List<BlockPopulator> populators = new ArrayList<>();
        populators.add(new GeoCorePopulator());
        return populators;
    }

    @Override
    public Location getFixedSpawnLocation(World world, Random random) {
        // 在世界中心寻找安全位置
        return findSafeSpawnLocation(world, 0, 0);
    }

    /**
     * 寻找安全的出生点
     */
    private Location findSafeSpawnLocation(World world, int centerX, int centerZ) {
        // 在中心区域搜索
        for (int searchRadius = 0; searchRadius < 50; searchRadius += 5) {
            for (int angle = 0; angle < 360; angle += 30) {
                double rad = Math.toRadians(angle);
                int x = centerX + (int)(Math.cos(rad) * searchRadius);
                int z = centerZ + (int)(Math.sin(rad) * searchRadius);

                // 从中间往下找地面
                for (int y = 80; y > BEDROCK_FLOOR_Y + 5; y--) {
                    Material block = world.getBlockAt(x, y - 1, z).getType();
                    Material current = world.getBlockAt(x, y, z).getType();
                    Material above = world.getBlockAt(x, y + 1, z).getType();
                    Material above2 = world.getBlockAt(x, y + 2, z).getType();

                    if (block.isSolid() &&
                            !current.isSolid() && current != Material.LAVA &&
                            !above.isSolid() && above != Material.LAVA &&
                            !above2.isSolid() && above2 != Material.LAVA) {
                        return new Location(world, x + 0.5, y, z + 0.5);
                    }
                }
            }
        }

        // 如果没找到，返回默认高度
        return new Location(world, centerX + 0.5, 70, centerZ + 0.5);
    }

    @Override
    public boolean shouldGenerateNoise() {
        return true;
    }

    @Override
    public boolean shouldGenerateSurface() {
        return false;
    }

    @Override
    public boolean shouldGenerateCaves() {
        return false;
    }

    @Override
    public boolean shouldGenerateDecorations() {
        return false;
    }

    @Override
    public boolean shouldGenerateMobs() {
        return true;
    }

    @Override
    public boolean shouldGenerateStructures() {
        return false;
    }

    private class GeoBiomeProvider extends BiomeProvider {
        @Override
        public Biome getBiome(WorldInfo worldInfo, int x, int y, int z) {
            GeoBiome geoBiome = getBiomeAt(x, z);
            return geoBiome.getVanillaBiome();
        }

        @Override
        public List<Biome> getBiomes(WorldInfo worldInfo) {
            List<Biome> biomes = new ArrayList<>();
            for (GeoBiome geoBiome : GeoBiome.values()) {
                biomes.add(geoBiome.getVanillaBiome());
            }
            return biomes;
        }
    }
}