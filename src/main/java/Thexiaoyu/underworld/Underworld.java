package Thexiaoyu.underworld;

import Thexiaoyu.underworld.generator.GeoCoreChunkGenerator;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.plugin.java.JavaPlugin;

public class Underworld extends JavaPlugin {

    private static Underworld instance;
    private GeoCoreChunkGenerator chunkGenerator;

    @Override
    public void onEnable() {
        instance = this;

        // 初始化区块生成器
        this.chunkGenerator = new GeoCoreChunkGenerator(this);
    }

    @Override
    public void onDisable() {
    }

    @Override
    public ChunkGenerator getDefaultWorldGenerator(String worldName, String id) {
        getLogger().info("为世界 '" + worldName + "' 提供地心世界生成器");
        return chunkGenerator;
    }

    /**
     * 获取插件实例
     */
    public static Underworld getInstance() {
        return instance;
    }

    /**
     * 获取区块生成器
     */
    public GeoCoreChunkGenerator getChunkGenerator() {
        return chunkGenerator;
    }

}