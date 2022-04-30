package fwg.generator;

import java.util.List;
import java.util.Random;

import fwg.deco.DecoDungeons;
import fwg.deco.DecoSurvival;
import fwg.deco.old.OldGenLakes;
import fwg.deco.old.OldGenMinable;
import fwg.map.MapGenOLD;
import fwg.map.MapGenOLDCaves;
import fwg.util.PerlinNoise;
import net.minecraft.block.Block;
import net.minecraft.block.BlockSand;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.init.Blocks;
import net.minecraft.util.IProgressUpdate;
import net.minecraft.world.ChunkPosition;
import net.minecraft.world.SpawnerAnimals;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.gen.MapGenBase;
import net.minecraft.world.gen.MapGenCaves;
import net.minecraft.world.gen.feature.WorldGenFlowers;
import net.minecraft.world.gen.feature.WorldGenLiquids;
import net.minecraft.world.gen.structure.MapGenStronghold;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.terraingen.PopulateChunkEvent;

public class ChunkGeneratorCaveSurv implements IChunkProvider
{
    private Random rand;
    private World world;
    private double[] densities;

    private BiomeGenBase[] biomesForGeneration;
    int[][] field_73203_h = new int[32][32];
    private MapGenOLD caveGenerator = new MapGenOLDCaves();
    private MapGenStronghold strongholdGenerator = new MapGenStronghold();
	
	public PerlinNoise perlin1;
	public PerlinNoise perlin2;
	
    public ChunkGeneratorCaveSurv(World par1World, long par2)
    {
        world = par1World;
        rand = new Random(par2);
		
		perlin1 = new PerlinNoise(par2);
		perlin2 = new PerlinNoise(par2 + 100L);
    }

    public void generateTerrain(int par1, int par2, Block[] blocks, BiomeGenBase[] par4ArrayOfBiomeGenBase)
    {
		int jj = 0;
		int i = par1 << 4;
		int j = par2 << 4;
		for (int k = i; k < i + 16; k++)
		{
			for (int m = j; m < j + 16; m++)
			{
				for (int i3 = 0; i3 < 128; i3++)
				{
					Block i4 = Blocks.stone;
					float dis = (float) Math.sqrt((0D-k)*(0D-k) + (0D-m)*(0D-m) + (256D - i3 * 4)*(256D - i3 * 4));
					
					if(dis < 100)
					{
						float stength = -(80F / 300f) * dis + 80F;
						float n = 0;
						n += perlin1.turbulence3(k / 50f, m / 50f, i3 / 50f, 4f) * stength;
						n += perlin2.turbulence3(k / 25f, m / 25f, i3 / 25f, 4f) * (stength / 2f);
						float i2 = 50 + n - dis;
						
						if(i2 > 0 && i3 > 52)
						{
							i4 = Blocks.air;
						}
					}
					
					if(i3 == 127 || i3 == 0 || i3 == 126 - rand.nextInt(4) || i3 == 1 + rand.nextInt(4))
					{
						i4 = Blocks.bedrock;
					}

					blocks[jj++] = i4;
				}
			}
		}
    }

    public Chunk loadChunk(int par1, int par2)
    {
        return this.provideChunk(par1, par2);
    }

    public Chunk provideChunk(int par1, int par2)
    {
        this.rand.setSeed((long)par1 * 341873128712L + (long)par2 * 132897987541L);
        Block[] var3 = new Block[32768];
        this.biomesForGeneration = this.world.getWorldChunkManager().loadBlockGeneratorData(this.biomesForGeneration, par1 * 16, par2 * 16, 16, 16);
        this.generateTerrain(par1, par2, var3, this.biomesForGeneration);
        
        this.caveGenerator.generate(this, this.world, par1, par2, var3);
		this.strongholdGenerator.func_151539_a(this, this.world, par1, par2, var3);
		
		if(world.getWorldInfo().getSpawnX() != 0 && world.getWorldInfo().getSpawnZ() != 0)
		{
			world.getWorldInfo().setSpawnPosition(0, 60, 0);
		}
		
        Chunk var4 = new Chunk(this.world, var3, par1, par2);
        byte[] var5 = var4.getBiomeArray();

        for (int var6 = 0; var6 < var5.length; ++var6)
        {
            var5[var6] = (byte)this.biomesForGeneration[var6].biomeID;
        }

        var4.generateSkylightMap();
        return var4;
    }

    public boolean chunkExists(int par1, int par2)
    {
        return true;
    }

    public void populate(IChunkProvider par1IChunkProvider, int par2, int par3)
    {
        BlockSand.fallInstantly = true;
        int var4 = par2 * 16;
        int var5 = par3 * 16;
        BiomeGenBase var6 = this.world.getBiomeGenForCoords(var4 + 16, var5 + 16);
		rand.setSeed(world.getSeed());
		long l1 = (rand.nextLong() / 2L) * 2L + 1L;
		long l2 = (rand.nextLong() / 2L) * 2L + 1L;
		rand.setSeed((long)par2 * l1 + (long)par3 * l2 ^ world.getSeed());
		
        MinecraftForge.EVENT_BUS.post(new PopulateChunkEvent.Pre(par1IChunkProvider, world, rand, par2, par3, false));
        
		this.strongholdGenerator.generateStructuresInChunk(this.world, this.rand, par2, par3);

		//======================== DECO =================================

		if(par2 == 0 && par3 == 0)
		{
			(new DecoSurvival(13)).generate(world, rand, 0, 0, 0);
		}
		
		if(rand.nextInt(4) == 0)
		{
			int i1 = var4 + rand.nextInt(16) + 8;
			int l4 = rand.nextInt(128);
			int i8 = var5 + rand.nextInt(16) + 8;
			(new OldGenLakes(Blocks.water)).generate(world, rand, i1, l4, i8);
		}
		if(rand.nextInt(8) == 0)
		{
			int j1 = var4 + rand.nextInt(16) + 8;
			int i5 = rand.nextInt(rand.nextInt(120) + 8);
			int j8 = var5 + rand.nextInt(16) + 8;
			if(i5 < 64 || rand.nextInt(10) == 0)
			{
				(new OldGenLakes(Blocks.lava)).generate(world, rand, j1, i5, j8);
			}
		} 
		
		for (int var42 = 0; var42 < 30; ++var42)
		{
			int var66 = var4 + this.rand.nextInt(16) + 8;
			int var67 = this.rand.nextInt(128);
			int var68 = var5 + this.rand.nextInt(16) + 8;

			if ((new DecoDungeons(0, false, true, false, false)).generate(this.world, this.rand, var66, var67, var68))
			{
				;
			}
		}	
        
		for(int j2 = 0; j2 < 16; j2++)
		{
			int l5 = var4 + rand.nextInt(16);
			int i9 = rand.nextInt(90);
			int l11 = var5 + rand.nextInt(16);
			(new OldGenMinable(Blocks.dirt, 32, 2)).generate(world, rand, l5, i9, l11);
		}

		for(int k2 = 0; k2 < 10; k2++)
		{
			int i6 = var4 + rand.nextInt(16);
			int j9 = rand.nextInt(128);
			int i12 = var5 + rand.nextInt(16);
			(new OldGenMinable(Blocks.gravel, 32, 2)).generate(world, rand, i6, j9, i12);
		}

		for(int i3 = 0; i3 < 20; i3++)
		{
			int j6 = var4 + rand.nextInt(16);
			int k9 = rand.nextInt(128);
			int j12 = var5 + rand.nextInt(16);
			(new OldGenMinable(Blocks.coal_ore, 16, 2)).generate(world, rand, j6, k9, j12);
		}

		for(int j3 = 0; j3 < 20; j3++)
		{
			int k6 = var4 + rand.nextInt(16);
			int l9 = rand.nextInt(64);
			int k12 = var5 + rand.nextInt(16);
			(new OldGenMinable(Blocks.iron_ore, 8, 2)).generate(world, rand, k6, l9, k12);
		}

		for(int k3 = 0; k3 < 2; k3++)
		{
			int l6 = var4 + rand.nextInt(16);
			int i10 = rand.nextInt(32);
			int l12 = var5 + rand.nextInt(16);
			(new OldGenMinable(Blocks.gold_ore, 8, 2)).generate(world, rand, l6, i10, l12);
		}

		for(int l3 = 0; l3 < 8; l3++)
		{
			int i7 = var4 + rand.nextInt(16);
			int j10 = rand.nextInt(16);
			int i13 = var5 + rand.nextInt(16);
			(new OldGenMinable(Blocks.redstone_ore, 7, 2)).generate(world, rand, i7, j10, i13);
		}

		for(int i4 = 0; i4 < 1; i4++)
		{
			int j7 = var4 + rand.nextInt(16);
			int k10 = rand.nextInt(16);
			int j13 = var5 + rand.nextInt(16);
			(new OldGenMinable(Blocks.diamond_ore, 7, 2)).generate(world, rand, j7, k10, j13);
		}

		for(int j4 = 0; j4 < 1; j4++)
		{
			int k7 = var4 + rand.nextInt(16);
			int l10 = rand.nextInt(16) + rand.nextInt(16);
			int k13 = var5 + rand.nextInt(16);
			(new OldGenMinable(Blocks.lapis_ore, 6, 2)).generate(world, rand, k7, l10, k13);
		}
		
        for (int l5 = 0; l5 < 3 + rand.nextInt(6); ++l5)
        {
            int i1 = var4 + rand.nextInt(16);
            int j1 = rand.nextInt(28) + 4;
            int k1 = var5 + rand.nextInt(16);
            Block s1 = world.getBlock(i1, j1, k1);

            if (s1 == Blocks.stone)
            {
            	world.setBlock(i1, j1, k1, Blocks.emerald_ore, 0, 2);
            }
        }

		for (int lqw = 0; lqw < 50; ++lqw)
		{
			int lqw1 = var4 + rand.nextInt(16) + 8;
			int lqw2 = rand.nextInt(rand.nextInt(120) + 8);
			int lqw3 = var5 + rand.nextInt(16) + 8;
			(new WorldGenLiquids(Blocks.flowing_water)).generate(world, rand, lqw1, lqw2, lqw3);
		}

		for (int lql = 0; lql < 20; ++lql)
		{
			int lql1 = var4 + rand.nextInt(16) + 8;
			int lql2 = rand.nextInt(rand.nextInt(rand.nextInt(112) + 8) + 8);
			int lql3 = var5 + rand.nextInt(16) + 8;
			(new WorldGenLiquids(Blocks.flowing_water)).generate(world, rand, lql1, lql2, lql3);
		}

		if (rand.nextInt(4) == 0)
		{
			int nbm1 = var4 + rand.nextInt(16) + 8;
			int nbm2 = rand.nextInt(128);
			int nbm3 = var5 + rand.nextInt(16) + 8;
			(new WorldGenFlowers(Blocks.brown_mushroom)).generate(world, rand, nbm1, nbm2, nbm3);
		}

		if (rand.nextInt(8) == 0)
		{
			int nrm1 = var4 + rand.nextInt(16) + 8;
			int nrm2 = rand.nextInt(128);
			int nrm3 = var5 + rand.nextInt(16) + 8;
			(new WorldGenFlowers(Blocks.red_mushroom)).generate(world, rand, nrm1, nrm2, nrm3);
		}

        MinecraftForge.EVENT_BUS.post(new PopulateChunkEvent.Post(par1IChunkProvider, world, rand, par2, par3, false));
		
        BlockSand.fallInstantly = false;
    }

    public boolean saveChunks(boolean par1, IProgressUpdate par2IProgressUpdate)
    {
        return true;
    }
	
    public boolean unloadQueuedChunks()
    {
        return false;
    }

    public boolean unload100OldestChunks()
    {
        return false;
    }

    public boolean canSave()
    {
        return true;
    }

    public String makeString()
    {
        return "RandomLevelSource";
    }

    public List getPossibleCreatures(EnumCreatureType par1EnumCreatureType, int par2, int par3, int par4)
    {
        BiomeGenBase var5 = this.world.getBiomeGenForCoords(par2, par4);
        return var5 == null ? null : var5.getSpawnableList(par1EnumCreatureType);
    }

    public ChunkPosition func_147416_a(World par1World, String par2Str, int par3, int par4, int par5)
    {
        return "Stronghold".equals(par2Str) && this.strongholdGenerator != null ? this.strongholdGenerator.func_151545_a(par1World, par3, par4, par5) : null;
    }

    public int getLoadedChunkCount()
    {
        return 0;
    }

    public void saveExtraData() {}

    public void recreateStructures(int par1, int par2)
    {
		this.strongholdGenerator.func_151539_a(this, this.world, par1, par2, (Block[])null);
    }
}
