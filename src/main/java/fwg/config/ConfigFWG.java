package fwg.config;

import java.io.File;

import net.minecraft.launchwrapper.Launch;
import net.minecraftforge.common.config.Configuration;

public class ConfigFWG
{
	private static final File CONFIG_FILE = new File(Launch.minecraftHome, "config/fwg.cfg");
	public static Configuration config;
	public static int[] biomeIDs = new int[3];
	
	public static boolean enableCustomDungeonLoot;
	
	public static float biomeScaleTemperatureRegion;
	public static float biomeScaleSmallRegion;
	public static float biomeScale;
	public static boolean oneBiomePerSmallRegion;
	public static float smallRegionProbability;
	
	public static void load()
	{
		config = new Configuration(CONFIG_FILE);
		
		for(int c = 0; c < biomeIDs.length; c++)
		{
			biomeIDs[c] = 185 + c;
		}
		
		try 
		{
			config.load();

			biomeIDs[0] = config.get("Biome IDs", "Default 1", 185).getInt();
			biomeIDs[1] = config.get("Biome IDs", "Default 2", 186).getInt();
			biomeIDs[2] = config.get("Biome IDs", "Default Snow", 187).getInt();
			
			enableCustomDungeonLoot = config.getBoolean("Enable custom dungeon loot", "Structures", true, "Set this to false to use Forge's API for populating dungeon chest contents instead of OWG's algorithm.");
			
			config.getCategory("biomes").setComment("The biome placement algorithm works in the following way:\n"
			+ "The world is divided into random small regions scaled by `biomeScaleSmallRegion`. Some of these regions will be chosen to contain \"small\" biomes, like Thaumcraft's magical biomes. The probability of this is determined by `smallRegionProbability`.\n"
			+ "If `oneBiomePerSmallRegion` is set, a single biome will be used for the entire small region. Otherwise, a small region may contain multiple \"small\" biomes, the size of which are determined by `biomeScale`.\n"
			+ "The world is also separately divided into large temperature regions scaled by `biomeScaleTemperatureRegion`. Each region may be of 4 different types: \"snow\", \"hot\", \"cold\" and \"wet\". These determine the set of biomes that can be placed in the region.\n"
			+ "Finally, each temperature region is divided into biome regions. The size of these are determined by `biomeScale`.");
			biomeScaleTemperatureRegion = config.getFloat("biomeScaleTemperatureRegion", "biomes", 330f, 1, Float.POSITIVE_INFINITY, "Scale of biome temperature regions. Larger is larger, scales linearly.");
			biomeScaleSmallRegion = config.getFloat("biomeScaleSmallRegion", "biomes", 140f, 1, Float.POSITIVE_INFINITY, "Scale of small biome regions. Larger is larger, scales linearly.");
			biomeScale = config.getFloat("biomeScale", "biomes", 90f, 1, Float.POSITIVE_INFINITY, "Scale of biomes. Larger is larger, scales linearly.");
			smallRegionProbability = config.getFloat("smallRegionProbability", "biomes", 0.08f, 0, 1, "Probability of small region placement.");
			oneBiomePerSmallRegion = config.getBoolean("oneBiomePerSmallRegion", "biomes", false, "Choose a single biome to use for each small region.");
		}
		catch (Exception e) 
		{
			for(int c = 0; c < biomeIDs.length; c++)
			{
				biomeIDs[c] = 185 + c;
			}
		}
		finally 
		{
			if (config.hasChanged()) 
			{
				config.save();
			}
		}
	}
}