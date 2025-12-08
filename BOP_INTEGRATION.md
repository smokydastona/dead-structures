# Biomes O' Plenty Integration for Lost Cities

This integration adds optional support for Biomes O' Plenty blocks and biomes in Lost Cities. **BOP is NOT required** - all features gracefully degrade to vanilla equivalents.

## How It Works

### Soft Dependency System
The integration uses a "soft dependency" approach:
- **BOP installed**: Cities use BOP wood types, plants, and decorative blocks
- **BOP not installed**: Cities automatically fall back to vanilla equivalents
- **No crashes or errors** either way

### Technical Implementation

#### 1. Block Fallback in Palettes
Palettes use the `blocks` array with `random` weights to specify BOP and vanilla alternatives:

```json
{
  "char": "u",
  "blocks": [
    {
      "random": 1,
      "block": "biomesoplenty:mahogany_planks"
    },
    {
      "random": 1,
      "block": "minecraft:jungle_planks"
    }
  ]
}
```

If BOP isn't loaded, the BOP block simply won't exist and only vanilla blocks will spawn.

#### 2. BiomesOPlentyCompat Helper Class
Located at: `src/main/java/mcjty/lostcities/compat/BiomesOPlentyCompat.java`

Provides:
- Runtime detection of BOP
- Constants for BOP block names
- Helper methods for conditional block loading
- Logging of integration status

## Available BOP Palettes

### bop_cherry.json
- **Theme**: Cherry blossom districts
- **BOP Woods**: White cherry
- **Fallback**: Vanilla cherry wood
- **Colors**: Pink terracotta, pink glass
- **Use Case**: Beautiful residential areas, parks

### bop_dead.json
- **Theme**: Post-apocalyptic wasteland
- **BOP Woods**: Dead wood
- **Fallback**: Dark oak
- **Colors**: Gray concrete, weathered stone
- **Use Case**: Abandoned districts, ruins, dystopian areas

### bop_tropical.json
- **Theme**: Tropical/jungle cities
- **BOP Woods**: Mahogany
- **Fallback**: Jungle wood
- **Colors**: Orange terracotta, sandstone
- **Use Case**: Warm climate cities, coastal areas

### bop_magic.json
- **Theme**: Mystical/arcane districts
- **BOP Woods**: Magic wood, Umbran wood
- **Fallback**: Warped stem, dark oak
- **Colors**: Purple, purpur blocks, obsidian
- **Use Case**: Wizard towers, enchanted districts

## BOP Decorative Elements

### bop_overgrown.json (Stuff Settings)
Adds atmospheric vegetation to cities:
- **Willow vines** on grass blocks
- **Spanish moss** hanging from leaves
- **Toadstools** in dark areas
- **Reeds** near water

All degrade to vanilla vines/mushrooms/grass if BOP absent.

## Creating Your Own BOP Palettes

### Step 1: Choose BOP Blocks
Common BOP wood types available:
- `white_cherry` (light pink wood)
- `dead` (gray/weathered wood)
- `fir` (evergreen wood)
- `willow` (pale green wood)
- `magic` (blue magical wood)
- `mahogany` (rich brown wood)
- `redwood` (reddish wood)
- `umbran` (dark purple wood)
- `hellbark` (nether wood)
- `jacaranda` (purple wood)

Each has: `_planks`, `_log`, `_stairs`, `_slab`, `_fence`, `_fence_gate`

### Step 2: Specify Fallbacks
For each BOP block, choose a similar vanilla block:

```json
{
  "char": "L",
  "blocks": [
    {
      "random": 1,
      "block": "biomesoplenty:redwood_log[axis=y]"
    },
    {
      "random": 1,
      "block": "minecraft:spruce_log[axis=y]"
    }
  ]
}
```

### Step 3: Use in Styles
Reference your palette in a style JSON:

```json
{
  "randomPaletteChoices": [
    [
      {
        "factor": 1.0,
        "palette": "lostcities:bop_cherry"
      }
    ]
  ]
}
```

## Integration with City Styles

### Biome-Specific Palettes
You can assign BOP palettes to specific biomes using conditions:

```json
{
  "conditions": [
    {
      "biome": "biomesoplenty:cherry_blossom_grove"
    }
  ],
  "style": "lostcities:cherry_style"
}
```

### Mixed Palette Buildings
Create variety by mixing vanilla and BOP palettes:

```json
{
  "randomPaletteChoices": [
    [
      {
        "factor": 2.0,
        "palette": "lostcities:default"
      },
      {
        "factor": 1.0,
        "palette": "lostcities:bop_tropical"
      }
    ]
  ]
}
```

## Advanced: BOP Plant Generation

### Scattered Buildings
Create BOP plant-themed structures:

1. Design building parts with BOP flowers/plants
2. Use the `scattered` system to place them
3. They'll only appear if BOP is installed

Example palette entry for BOP plants:
```json
{
  "char": "P",
  "blocks": [
    {
      "random": 1,
      "block": "biomesoplenty:pink_daffodil"
    },
    {
      "random": 1,
      "block": "minecraft:pink_tulip"
    }
  ]
}
```

## Testing Your Integration

### With BOP Installed
1. Load world with Lost Cities + BOP
2. Verify BOP blocks appear in cities
3. Check logs for: `"Biomes O' Plenty detected - enabling integration features"`

### Without BOP Installed
1. Load world with Lost Cities only
2. Verify vanilla fallbacks work
3. Check logs for: `"Biomes O' Plenty not found - using vanilla fallbacks"`
4. **Ensure no errors or crashes**

## Build Configuration

### build.gradle
BOP should be specified as an **optional** dependency:

```gradle
dependencies {
    // Optional soft dependency - NOT required at runtime
    compileOnly "com.github.glitchfiend:biomesoplenty:${bop_version}"
}
```

### mods.toml
Mark BOP as optional:

```toml
[[dependencies.lostcities]]
    modId = "biomesoplenty"
    mandatory = false
    versionRange = "[18.0.0,)"
    ordering = "AFTER"
    side = "BOTH"
```

## Best Practices

### 1. Always Provide Fallbacks
Every BOP block MUST have a vanilla equivalent in the `blocks` array.

### 2. Use Equal Random Weights
Use `"random": 1` for both BOP and vanilla blocks so they have equal chance when BOP is present.

### 3. Thematic Matching
Match BOP woods to similar vanilla woods:
- Cherry → Cherry
- Dead → Dark Oak
- Mahogany → Jungle
- Magic → Warped
- Redwood → Spruce
- Umbran → Dark Oak

### 4. Test Both Scenarios
Always test with and without BOP installed.

### 5. Graceful Degradation
Design palettes so that even without BOP, the aesthetic still makes sense.

## Troubleshooting

### BOP Blocks Not Appearing
- Check BOP is actually installed
- Verify block names match current BOP version
- Check logs for warnings about missing blocks

### Crashes When BOP Not Installed
- Ensure ALL BOP blocks have vanilla fallbacks
- Don't use BOP-only features in code without checks
- Use the `BiomesOPlentyCompat` class for runtime checks

### Performance Issues
- Don't overuse the `blocks` randomization system
- Limit complex vegetation in high-density areas
- Use probabilities wisely in stuff settings

## Future Expansion Ideas

1. **BOP Biome-Specific Styles**
   - Lavender fields → Purple palette
   - Maple forest → Orange/red palette
   - Mystic grove → Glowing blocks

2. **BOP Building Materials**
   - White sand beaches
   - Mud brick buildings
   - Black/orange sandstone variants

3. **BOP Loot Integration**
   - BOP saplings in planters
   - BOP flowers in city parks
   - BOP food in chests

4. **Seasonal Variations**
   - Different BOP woods for different city "ages"
   - Overgrown vs maintained districts

## Contributing

When adding new BOP integrations:
1. Update `BiomesOPlentyCompat.java` with new block constants
2. Create palette JSONs with fallbacks
3. Test with and without BOP
4. Document in this file
5. Submit PR with clear description

## License

This integration follows Lost Cities' MIT license. BOP content used with permission through soft dependency system.
