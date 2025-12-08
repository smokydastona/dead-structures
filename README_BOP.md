# Lost Cities - BOP Integration Fork

This is a fork of [McJty's Lost Cities](https://github.com/McJtyMods/LostCities) that adds **optional** Biomes O' Plenty integration.

## 🌟 New Features

### Optional BOP Support
- **No hard dependency** - works with or without BOP installed
- **Graceful fallbacks** - BOP blocks automatically replaced with vanilla equivalents
- **New themed palettes** - Cherry blossom, wasteland, tropical, and mystical city styles
- **Vegetation decorations** - Vines, moss, mushrooms, and reeds from BOP

### What's Included

#### 🎨 4 New Palettes
1. **bop_cherry** - Pink cherry blossom themed cities
2. **bop_dead** - Post-apocalyptic wasteland aesthetic  
3. **bop_tropical** - Warm mahogany tropical cities
4. **bop_magic** - Mystical purple/blue magical districts

#### 🌿 Overgrown Vegetation
- Spanish moss hanging from trees
- Willow vines on buildings
- Toadstools in dark areas
- Reeds near water features

#### 📚 Complete Documentation
- `BOP_INTEGRATION.md` - Full technical guide
- `BOP_QUICKSTART.md` - User-friendly quick start
- `BiomesOPlentyCompat.java` - Developer reference

## 🚀 Quick Start

### Installation
1. Install Minecraft Forge for 1.20
2. Install Lost Cities (this fork)
3. **Optionally** install Biomes O' Plenty 18.0.0+

### Using BOP Palettes

Add to your profile or citystyle JSON:
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

See `BOP_QUICKSTART.md` for more examples!

## 🔧 How It Works

The integration uses a **soft dependency** system:
- BOP blocks specified in palettes alongside vanilla alternatives
- Lost Cities palette system tries BOP block first
- If BOP not loaded, falls back to vanilla automatically
- **Zero crashes or errors** either way

Example from `bop_cherry.json`:
```json
{
  "char": "u",
  "blocks": [
    {
      "random": 1,
      "block": "biomesoplenty:white_cherry_planks"
    },
    {
      "random": 1,
      "block": "minecraft:cherry_planks"
    }
  ]
}
```

## 📖 Documentation

- **[BOP_QUICKSTART.md](BOP_QUICKSTART.md)** - Start here! User guide with examples
- **[BOP_INTEGRATION.md](BOP_INTEGRATION.md)** - Full technical documentation
- **[Original Lost Cities Wiki](https://github.com/McJtyMods/LostCities/wiki)** - Base mod documentation

## 🛠️ For Developers

### Creating Custom BOP Palettes

1. Copy an existing palette JSON
2. Replace BOP block IDs with your choices
3. Always include vanilla fallbacks
4. Test with and without BOP installed

See `BiomesOPlentyCompat.java` for block ID constants.

### Build Instructions

This project uses **GitHub Actions** for compilation. Never build locally.

```bash
# Make your changes, then:
git add -A
git commit -m "descriptive message"
git push
```

GitHub Actions will automatically compile the mod. Download artifacts from the Actions tab.

**DO NOT** create releases or tags - the maintainer handles this manually.

## 🤝 Contributing

Contributions welcome! When adding BOP features:
1. Always provide vanilla fallbacks
2. Update documentation
3. Test both with and without BOP
4. Follow existing palette format

## 📜 License

This fork maintains the original **MIT License** from Lost Cities.

## 🙏 Credits

- **McJty** - Original Lost Cities mod
- **Glitchfiend** - Biomes O' Plenty mod
- **Contributors** - See original repo for full credits

## 🔗 Links

- **Original Lost Cities**: https://github.com/McJtyMods/LostCities
- **Biomes O' Plenty**: https://github.com/Glitchfiend/BiomesOPlenty
- **CurseForge**: [Link to your CurseForge page]
- **Modrinth**: [Link to your Modrinth page]

## ⚠️ Important Notes

- This is a **fork** - not affiliated with original Lost Cities development
- BOP integration is **optional** - mod works standalone
- Existing Lost Cities configs and profiles are **fully compatible**
- No changes to core world generation logic

## 🐛 Bug Reports

Please specify:
- Lost Cities version
- BOP installed? (Yes/No and version)
- Minecraft/Forge version
- Steps to reproduce

Submit issues to this fork's issue tracker.

## 📋 Version Compatibility

| Component | Version |
|-----------|---------|
| Minecraft | 1.20.x |
| Forge | 47+ |
| BOP (optional) | 18.0.0+ |
| Java | 17+ |

---

**Enjoy your enhanced Lost Cities with beautiful Biomes O' Plenty integration!** 🏙️🌸
