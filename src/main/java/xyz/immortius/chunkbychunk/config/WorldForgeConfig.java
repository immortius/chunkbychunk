package xyz.immortius.chunkbychunk.config;

import xyz.immortius.chunkbychunk.config.system.Comment;
import xyz.immortius.chunkbychunk.config.system.IntRange;
import xyz.immortius.chunkbychunk.config.system.Name;

/**
 * Configuration specifically for the world forge
 */
public class WorldForgeConfig {
    @Name("production_rate")
    @IntRange(min = 1, max = 256)
    @Comment("The rate at which the world forge processes consumed blocks, in fuel per tick")
    private int productionRate = 1;

    @Name("soil_fuel_value")
    @Comment("The value of fuel provided by soils (dirt, sand, gravel, etc). 0 to disallow use as fuel")
    @IntRange(min = 0, max = 256)
    private int soilFuelValue = 2;

    @Name("stone_fuel_value")
    @Comment("The value of fuel provided by raw stones (cobblestone, deep slate cobblestone, etc). 0 to disallow use as fuel")
    @IntRange(min = 0, max = 256)
    private int stoneFuelValue = 4;

    @Name("strong_fuel_value")
    @Comment("The value of fuel provided by more valuable materials (none by default, this is for modpacks and extensions)")
    @IntRange(min = 0, max = 256)
    private int strongFuelValue = 8;

    @Name("fragment_fuel_cost")
    @IntRange(min = 1, max = 256)
    @Comment("The cost in fuel to produce a single world fragment")
    private int fragmentFuelCost = 64;

    public int getFragmentFuelCost() {
        return fragmentFuelCost;
    }

    public void setFragmentFuelCost(int fragmentFuelCost) {
        this.fragmentFuelCost = fragmentFuelCost;
    }

    public int getProductionRate() {
        return productionRate;
    }

    public void setProductionRate(int productionRate) {
        this.productionRate = productionRate;
    }

    public int getSoilFuelValue() {
        return soilFuelValue;
    }

    public void setSoilFuelValue(int soilFuelValue) {
        this.soilFuelValue = soilFuelValue;
    }

    public int getStoneFuelValue() {
        return stoneFuelValue;
    }

    public void setStoneFuelValue(int stoneFuelValue) {
        this.stoneFuelValue = stoneFuelValue;
    }

    public int getStrongFuelValue() { return strongFuelValue; }

    public void setStrongFuelValue(int strongFuelValue) {
        this.strongFuelValue = strongFuelValue;
    }
}
