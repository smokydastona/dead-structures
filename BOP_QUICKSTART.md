# Quick Start: Using BOP Palettes in Lost Cities

## What You Get

This fork adds **optional** Biomes O' Plenty integration to Lost Cities. Cities can now use beautiful BOP wood types and decorations!

**Important**: BOP is NOT required. Everything falls back to vanilla Minecraft blocks if BOP isn't installed.

## Available Themed Palettes

### 🌸 Cherry Blossom (`bop_cherry`)
- Pink and white aesthetic
- BOP white cherry wood (or vanilla cherry)
- Perfect for: Beautiful districts, parks, residential areas

### ☠️ Wasteland (`bop_dead`)
- Gray, weathered appearance
- BOP dead wood (or dark oak)
- Perfect for: Abandoned cities, ruins, post-apocalyptic areas

### 🌴 Tropical (`bop_tropical`)
- Warm, vibrant colors
- BOP mahogany wood (or jungle wood)
- Perfect for: Coastal cities, jungle regions

### 🔮 Mystical (`bop_magic`)
- Purple and blue magical theme
- BOP magic/umbran wood (or warped/dark oak)
- Perfect for: Fantasy districts, wizard areas

## How to Use

### Using Palettes in Your World

1. **Install Lost Cities** (and optionally Biomes O' Plenty)

2. **Create or edit a profile** in `config/lostcities/`

3. **Reference a BOP palette** in your profile's style:

```json
{
  "styles": {
    "standard": {
      "randomPaletteChoices": [
        [
          {
            "factor": 1.0,
            "palette": "lostcities:bop_cherry"
          }
        ]
      ]
    }
  }
}
```

4. **Mix palettes** for variety:

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
      },
      {
        "factor": 1.0,
        "palette": "lostcities:bop_magic"
      }
    ]
  ]
}
```

### Using Overgrown Vegetation

Add atmospheric BOP plants to cities:

```json
{
  "stuff": ["lostcities:bop_overgrown"]
}
```

This adds:
- Willow vines and spanish moss
- Toadstools and mushrooms
- Reeds near water
- All with vanilla fallbacks

## Creating Custom BOP Palettes

See `BOP_INTEGRATION.md` for detailed instructions on creating your own palettes.

### Quick Template

```json
{
  "palette": [
    {
      "char": "u",
      "blocks": [
        {
          "random": 1,
          "block": "biomesoplenty:WOOD_TYPE_planks"
        },
        {
          "random": 1,
          "block": "minecraft:VANILLA_FALLBACK"
        }
      ]
    }
  ]
}
```

## FAQ

**Q: Do I need BOP installed?**
A: No! It's completely optional. Palettes use vanilla blocks if BOP isn't present.

**Q: What happens if I remove BOP from an existing world?**
A: BOP blocks in cities become their vanilla equivalents automatically.

**Q: Can I use BOP and vanilla palettes together?**
A: Yes! Mix them freely in your profiles.

**Q: Which BOP version is supported?**
A: 18.0.0+ for Minecraft 1.20

**Q: Will this slow down world generation?**
A: No performance impact - the palette system is already optimized.

**Q: Can I use this on servers?**
A: Yes! Server doesn't need BOP if you're using fallbacks.

## Support

- Issues: [GitHub Issues](https://github.com/YourUsername/LostCities/issues)
- Documentation: See `BOP_INTEGRATION.md`
- Original Lost Cities: [McJtyMods/LostCities](https://github.com/McJtyMods/LostCities)

## Examples

### Biome-Specific Palettes
```json
{
  "conditions": [
    {
      "biome": "biomesoplenty:cherry_blossom_grove"
    }
  ],
  "palette": "lostcities:bop_cherry"
}
```

### Mixed District Styles
```json
{
  "citystyles": {
    "diverse_city": {
      "styles": [
        "lostcities:standard",
        "lostcities:cherry_district",
        "lostcities:wasteland_district"
      ]
    }
  }
}
```

Enjoy your enhanced Lost Cities experience! 🏙️
