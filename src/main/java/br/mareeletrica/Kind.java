package br.mareeletrica;

public enum Kind {
    WATER_WHEEL("water_wheel", 8_000, 8_000, false),
    SEA_TURBINE("sea_turbine", 0, 24_000, false),
    PURIFIER("purifier", 2_000_000, 4_000_000, false),
    CORE_SMALL("core_small", 20_000, 0, false),
    CORE_MEDIUM("core_medium", 100_000, 0, false),
    CORE_LARGE("core_large", 500_000, 0, false),
    FURNACE("electric_furnace", 8_000, 0, true),
    CRUSHER("crusher", 12_000, 0, true),
    IRRIGATOR("irrigator", 8_000, 0, true),
    MAGNET("magnet", 8_000, 0, true),
    HEALER("healer", 16_000, 0, true),
    LIFT("vortex_lift", 8_000, 0, true),
    LAMP("floodlight", 40_000, 0, true),
    REPELLER("repeller", 12_000, 0, true),
    COOKER("electric_cooker", 8_000, 0, true),
    RECYCLER("recycler", 12_000, 0, true),
    STONE_GENERATOR("stone_generator", 12_000, 0, true),
    OXYGEN_STATION("oxygen_station", 12_000, 0, true),
    HARVESTER("harvester", 12_000, 0, true),
    PLANTER("planter", 8_000, 0, true),
    FISHER("auto_fisher", 12_000, 0, true),
    ORE_SCANNER("ore_scanner", 16_000, 0, true),
    TELEPORT_PAD("teleport_pad", 16_000, 0, true),
    CHARGER("charging_station", 32_000, 0, true),
    XP_COLLECTOR("xp_collector", 12_000, 0, true),
    WEATHER_CONTROLLER("weather_controller", 40_000, 0, true),
    TURRET("automatic_turret", 16_000, 0, true),
    PRESS("component_press", 12_000, 0, true),
    SAWMILL("electric_sawmill", 8_000, 0, true),
    FREEZER("cryogenic_freezer", 12_000, 0, true),
    REPAIRER("electric_repairer", 16_000, 0, true),
    DISENCHANTER("disenchanter", 16_000, 0, true),
    ANIMAL_FEEDER("animal_feeder", 8_000, 0, true),
    SHEARER("automatic_shearer", 8_000, 0, true),
    EXTINGUISHER("fire_suppressor", 8_000, 0, true),
    BRIDGE_PROJECTOR("bridge_projector", BridgePower.CAPACITY, 0, true),
    ASSEMBLER("component_assembler", 16_000, 0, true),
    ITEM_TRANSFER("item_transfer", 8_000, 0, true),
    SOLAR_PANEL("solar_panel", 12_000, 0, false),
    COMPACTOR("material_compactor", 12_000, 0, true),
    BUCKET_FILLER("bucket_filler", 8_000, 0, true),
    EFFECT_CLEANSER("effect_cleanser", 16_000, 0, true),
    FOOD_STATION("food_station", 8_000, 0, true),
    PRESENCE_SENSOR("presence_sensor", 4_000, 0, true),
    APIARY_EXTRACTOR("apiary_extractor", 8_000, 0, true),
    MINING_BOOSTER("mining_booster", 16_000, 0, true),
    POTABILIZER("water_potabilizer", 32_000, 0, true),
    GREENHOUSE("hydroponic_greenhouse", 64_000, 0, true),
    QUARRY("ore_drilling_rig", 1_000_000, 0, true),
    BUILDER("construction_projector", 2_000_000, 0, true),
    LEVELER("terrain_leveler", 4_000_000, 0, true),
    GEOTHERMAL("geothermal_reactor", 2_000_000, 0, false),
    ABYSSAL("abyssal_dynamo", 0, 4_000_000, false),
    ROVER_BASE("explorer_base", 1_000_000, 0, true),
    MORTAR("six_barrel_mortar", 120_000, 0, true),
    EXTERMINATOR("monster_exterminator", 4_000_000, 0, true),
    GAS_REFILL("gas_refiller", 500_000, 0, true),
    GIANT_WHEEL("giant_water_wheel", 1_000_000, 1_000_000, false),
    CURRENT_TURBINE("current_turbine", 750_000, 750_000, false),
    FALL_TURBINE("waterfall_turbine", 1_500_000, 1_500_000, false),
    PRESSURE_TURBINE("pressure_turbine", 2_000_000, 2_000_000, false),
    CORE_QUANTUM("quantum_core", 100_000_000, 0, false),
    POWER_CONTROLLER("power_controller", 200_000, 0, true),
    AUTO_CRANK("auto_crank", 60_000, 0, true),
    WIND_SMALL("wind_turbine_small", 40_000, 0, false),
    WIND_MEDIUM("wind_turbine_medium", 120_000, 0, false),
    WIND_GIANT("wind_turbine_giant", 400_000, 0, false),
    WINDMILL("farm_windmill", 60_000, 0, false),
    WIND_ROOF("rooftop_turbine", 40_000, 0, false),
    WIND_MAST("anemometer_mast", 1_000, 0, false),
    WIND_BATTERY("wind_battery", 1_500_000, 0, false),
    WIND_INVERTER("wind_inverter", 80_000, 0, false),
    WIND_PANEL("wind_control_panel", 1_000, 0, false),
    ARMOR_STATION("armor_charging_station", 200_000, 0, true),
    HANGAR("vehicle_hangar", 500_000, 0, true),
    SHIELD("shield_generator", 2_000_000, 0, true);

    public final String id;
    public final int capacity;
    public final int dirtyCapacity;
    public final boolean consumer;
    Kind(String id, int capacity, int dirtyCapacity, boolean consumer) {
        this.id = id; this.capacity = capacity;
        this.dirtyCapacity = dirtyCapacity; this.consumer = consumer;
    }
    public boolean hydro() { return this==GIANT_WHEEL||this==CURRENT_TURBINE||this==FALL_TURBINE||this==PRESSURE_TURBINE; }
    public boolean waterStructure(){return hydro()||this==ABYSSAL;}
    /** 0.90.0: how many blocks wide/tall the machine is built when there is room (1 = classic single block). */
    public int bigSize() {
        return switch(this) {
            case FURNACE,CRUSHER,HEALER,MAGNET,COOKER,RECYCLER,STONE_GENERATOR,OXYGEN_STATION,HARVESTER,PLANTER,FISHER,ORE_SCANNER,CHARGER,XP_COLLECTOR,TURRET,
                 PRESS,SAWMILL,FREEZER,REPAIRER,DISENCHANTER,ANIMAL_FEEDER,SHEARER,EXTINGUISHER,ASSEMBLER,COMPACTOR,BUCKET_FILLER,EFFECT_CLEANSER,FOOD_STATION,
                 APIARY_EXTRACTOR,CORE_MEDIUM,POWER_CONTROLLER -> 2;
            case WEATHER_CONTROLLER,CORE_LARGE,ARMOR_STATION,HANGAR,BUILDER,LEVELER,AUTO_CRANK -> 3;
            default -> 1;
        };
    }
    /** built as a bigSize() cube when there is room, otherwise as the classic single block */
    public boolean scalable() { return bigSize()>1; }
    public boolean large() { return scalable()||this==CORE_QUANTUM||this==PURIFIER||hydro() || this == GAS_REFILL || this == EXTERMINATOR || this == GREENHOUSE || this == QUARRY || this == GEOTHERMAL || this == ABYSSAL || this == MORTAR || this == ROVER_BASE; }
    public int structureHeight() { return scalable()?bigSize():this==GIANT_WHEEL?7:this == EXTERMINATOR || this == GAS_REFILL ? 3 : this == QUARRY ? 8 : this == MORTAR ? 2 : 3; }
    public int structureWidth() { return scalable()?bigSize():this==GIANT_WHEEL?7:this == EXTERMINATOR || this == GAS_REFILL ? 3 : this == QUARRY ? 6 : 3; }
    public int structureParts() { return structureWidth()*structureWidth()*structureHeight(); }
    public boolean core() { return this == CORE_SMALL || this == CORE_MEDIUM || this == CORE_LARGE || this==CORE_QUANTUM || this==WIND_BATTERY || this==WIND_INVERTER; }
    /** Output voltage of a core: 1 low, 2 medium, 3 high, 4 extreme. 0 = not a core. */
    public int voltage() { return switch(this){case CORE_SMALL->1;case CORE_MEDIUM,WIND_BATTERY->2;case CORE_LARGE,WIND_INVERTER->3;case CORE_QUANTUM->4;default->0;}; }
    /** Highest voltage a consumer takes before it overloads and burns out, from its size. */
    public int maxVoltage() { return capacity<=20_000?1:capacity<=150_000?2:capacity<=2_000_000?3:4; }
    public static String voltageName(int v){return switch(v){case 1->"baixa";case 2->"média";case 3->"alta";default->"extrema";};}
    /** Wind energy family (0.46.0). */
    public boolean wind() { return ordinal()>=WIND_SMALL.ordinal() && ordinal()<=WIND_PANEL.ordinal(); }
    /** Wind turbines that generate power. */
    public boolean windTurbine() { return this==WIND_SMALL||this==WIND_MEDIUM||this==WIND_GIANT||this==WINDMILL||this==WIND_ROOF; }
    public boolean storage() { return this==EXTERMINATOR || processor() || this == PURIFIER || gridInventory() || this == CHARGER || this == MORTAR || this == ROVER_BASE; }
    public boolean gridInventory() { return this == QUARRY || this == LEVELER || this == MAGNET || this == HARVESTER || this == PLANTER || this == FISHER || this == ANIMAL_FEEDER || this == SHEARER || this == ITEM_TRANSFER || this == FOOD_STATION || this == APIARY_EXTRACTOR; }
    public boolean outputInventory() { return this == QUARRY || this == LEVELER || this == MAGNET || this == HARVESTER || this == FISHER || this == SHEARER || this == ITEM_TRANSFER || this == APIARY_EXTRACTOR; }
    public boolean actionButton() { return this == QUARRY || this == LEVELER || this == MORTAR || this == ROVER_BASE || this == ORE_SCANNER || this == TELEPORT_PAD || this == XP_COLLECTOR || this == WEATHER_CONTROLLER; }
    public boolean industrial() { return this == PRESS || this == SAWMILL || this == FREEZER || this == REPAIRER || this == DISENCHANTER || this == ASSEMBLER; }
    public boolean utilityProcessor() { return this == COMPACTOR || this == BUCKET_FILLER; }
    public boolean processor() { return this == GAS_REFILL || this == GREENHOUSE || industrial() || utilityProcessor() || this == POTABILIZER || this == FURNACE || this == CRUSHER || this == COOKER || this == RECYCLER || this == STONE_GENERATOR; }
    public int inputs() { return this == ASSEMBLER || this == GREENHOUSE ? 3 : this == REPAIRER || this == POTABILIZER ? 2 : this == STONE_GENERATOR ? 0 : 1; }
    public int duration() { return this == GAS_REFILL || this == GREENHOUSE ? 100 : this == COOKER ? 20 : this == FREEZER ? 60 : utilityProcessor() || this == FURNACE || this == STONE_GENERATOR || this == PRESS || this == SAWMILL ? 40 : 80; }
    public static Kind at(int ordinal) {
        return values()[Math.max(0, Math.min(values().length - 1, ordinal))];
    }
}
