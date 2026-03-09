package Thexiaoyu.underworld.biome;

import org.bukkit.Color;
import org.bukkit.block.Biome;

/**
 * 地心世界自定义生物群系
 *
 * 包含15种独特生物群系，每个都有独特的视觉效果和雾气
 *
 * @author Jinkra
 */
public enum GeoBiome {

    // ==================== 晶体类群系 ====================

    /**
     * 晶化森林 - 紫水晶构成的神秘森林
     */
    CRYSTAL_FOREST(
            "晶化森林", "Crystal Forest",
            1.3f, 90, Biome.LUSH_CAVES, 0.6f,
            new FogSettings(0x1a0a2e, 0x2d1b4e, 60)
    ),


    /**
     * 翡翠洞穴 - 绿色矿物和苔藓
     */
    EMERALD_CAVES(
            "翡翠洞穴", "Emerald Caves",
            1.1f, 80, Biome.LUSH_CAVES, 0.7f,
            new FogSettings(0x0a2a1a, 0x1a4a2a, 55)
    ),

    // ==================== 菌类群系 ====================

    /**
     * 发光菌落洞穴 - 巨大发光蘑菇群落
     */
    GLOWING_MUSHROOM_CAVES(
            "发光菌落", "Glowing Mushroom Caves",
            0.9f, 75, Biome.DRIPSTONE_CAVES, 1.0f,
            new FogSettings(0x1a2a1a, 0x2a4a3a, 70)
    ),

    /**
     * 绯红菌林 - 下界绯红森林渗透区
     */
    CRIMSON_GROWTH(
            "绯红菌林", "Crimson Growth",
            1.0f, 72, Biome.CRIMSON_FOREST, 0.8f,
            new FogSettings(0x330808, 0x661010, 45)
    ),


    // ==================== 熔岩/火焰群系 ====================

    /**
     * 熔岩河流域 - 流动的熔岩河流
     */
    MOLTEN_RIVERS(
            "熔岩河流", "Molten Rivers",
            0.7f, 60, Biome.BASALT_DELTAS, 0.8f,
            new FogSettings(0x2a1a0a, 0x4a2a10, 40)
    ),



    /**
     * 玄武岩柱林 - 巨大玄武岩柱群
     */
    BASALT_PILLARS(
            "玄武岩柱林", "Basalt Pillars",
            1.4f, 65, Biome.BASALT_DELTAS, 0.4f,
            new FogSettings(0x1a1a1a, 0x2a2a2a, 30)
    ),

    // ==================== 遗迹/文明群系 ====================

    /**
     * 古文明遗迹区 - 古老文明的残留建筑
     */
    ANCIENT_RUINS(
            "古文明遗迹", "Ancient Ruins",
            1.0f, 85, Biome.DEEP_DARK, 0.9f,
            new FogSettings(0x0a0a1a, 0x1a1a3a, 50)
    ),

    /**
     * 幽匿深渊 - Sculk生物蔓延区域
     */
    SCULK_DEPTHS(
            "幽匿深渊", "Sculk Depths",
            0.8f, 50, Biome.DEEP_DARK, 0.3f,
            new FogSettings(0x001020, 0x002040, 25)
    ),

    // ==================== 金属/矿物群系 ====================

    /**
     * 金属矿脉区 - 液态金属河流源头
     */
    METAL_VEINS(
            "金属矿脉", "Metal Veins",
            0.8f, 70, Biome.DRIPSTONE_CAVES, 0.5f,
            new FogSettings(0x2a2010, 0x4a3820, 45)
    ),

    /**
     * 黄金裂隙 - 金矿富集区域
     */
    GOLDEN_FISSURE(
            "黄金裂隙", "Golden Fissure",
            0.9f, 65, Biome.NETHER_WASTES, 0.6f,
            new FogSettings(0x3a2a0a, 0x5a4a1a, 50)
    ),

    // ==================== 黑暗/虚空群系 ====================

    /**
     * 黑曜石平原 - 广阔的黑曜石地表
     */
    OBSIDIAN_PLAINS(
            "黑曜石平原", "Obsidian Plains",
            0.5f, 65, Biome.END_HIGHLANDS, 0.4f,
            new FogSettings(0x0a0010, 0x1a0020, 20)
    ),

    /**
     * 下界渗透区 - 下界能量渗透区域
     */
    NETHER_BLEED(
            "下界渗透", "Nether Bleed",
            0.85f, 70, Biome.NETHER_WASTES, 0.7f,
            new FogSettings(0x200808, 0x401010, 35)
    ),

    /**
     * 虚空边境 - 接近虚空的边缘地带
     */
    VOID_EXPANSE(
            "虚空边境", "Void Expanse",
            0.4f, 55, Biome.END_BARRENS, 0.3f,
            new FogSettings(0x050508, 0x0a0a10, 15)
    );

    private final String displayNameCN;
    private final String displayNameEN;
    private final float heightMultiplier;
    private final int baseHeight;
    private final Biome vanillaBiome;
    private final float lightDensity;
    private final FogSettings fogSettings;

    GeoBiome(String displayNameCN, String displayNameEN,
             float heightMultiplier, int baseHeight,
             Biome vanillaBiome, float lightDensity,
             FogSettings fogSettings) {
        this.displayNameCN = displayNameCN;
        this.displayNameEN = displayNameEN;
        this.heightMultiplier = heightMultiplier;
        this.baseHeight = baseHeight;
        this.vanillaBiome = vanillaBiome;
        this.lightDensity = lightDensity;
        this.fogSettings = fogSettings;
    }

    public String getDisplayNameCN() { return displayNameCN; }
    public String getDisplayNameEN() { return displayNameEN; }
    public float getHeightMultiplier() { return heightMultiplier; }
    public int getBaseHeight() { return baseHeight; }
    public Biome getVanillaBiome() { return vanillaBiome; }
    public float getLightDensity() { return lightDensity; }
    public FogSettings getFogSettings() { return fogSettings; }
    public int getFogColor() { return fogSettings.fogColor; }
    public int getSkyColor() { return fogSettings.skyColor; }
    public int getFogDensity() { return fogSettings.density; }

    public String getDescription() {
        switch (this) {
            case CRYSTAL_FOREST: return "由紫水晶、方解石构成的神秘森林。晶体散发着梦幻的紫色光芒。";
            case EMERALD_CAVES: return "翠绿色的矿物与苔藓交织，充满生机的地下绿洲。";
            case GLOWING_MUSHROOM_CAVES: return "巨大发光蘑菇群落，是地心世界最主要的光源。";
            case CRIMSON_GROWTH: return "下界绯红森林的能量渗透至此，形成了血红色的菌林。";
            case MOLTEN_RIVERS: return "熔岩流淌形成的河流地带，是下界能量渗透的核心区域。";
            case BASALT_PILLARS: return "巨大的玄武岩柱如森林般矗立，是远古火山活动的遗迹。";
            case ANCIENT_RUINS: return "古老文明的遗迹，破碎的建筑暗示着曾经辉煌的过去。";
            case SCULK_DEPTHS: return "幽匿生物蔓延的深渊，黑暗中潜伏着未知的恐怖。";
            case METAL_VEINS: return "富含金属矿物的区域，液态金属在此汇聚。";
            case GOLDEN_FISSURE: return "金矿富集的裂隙地带，闪耀着诱人的金黄色光芒。";
            case OBSIDIAN_PLAINS: return "广阔的黑曜石平原，漆黑中偶尔闪烁紫光。";
            case NETHER_BLEED: return "下界能量渗透最强烈的区域，地狱岩和灵魂土蔓延。";
            case VOID_EXPANSE: return "接近虚空的边缘，末地石漂浮在无尽黑暗中。";
            default: return "未知区域";
        }
    }


    /**
     * 雾气设置
     */
    public static class FogSettings {
        public final int fogColor;
        public final int skyColor;
        public final int density;

        public FogSettings(int fogColor, int skyColor, int density) {
            this.fogColor = fogColor;
            this.skyColor = skyColor;
            this.density = density;
        }

        public Color getFogColorBukkit() {
            return Color.fromRGB(fogColor);
        }

        public Color getSkyColorBukkit() {
            return Color.fromRGB(skyColor);
        }
    }

    public static GeoBiome fromName(String name) {
        for (GeoBiome biome : values()) {
            if (biome.name().equalsIgnoreCase(name) ||
                    biome.displayNameCN.equals(name) ||
                    biome.displayNameEN.equalsIgnoreCase(name)) {
                return biome;
            }
        }
        return null;
    }
}