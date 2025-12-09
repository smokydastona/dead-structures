# Copilot Instructions — Forgotten Cities (Dead Structures)

You are working on **Forgotten Cities**, a Minecraft 1.20.1 Forge mod (formerly Lost Cities) that generates post-apocalyptic cities with modern features, zombie mechanics, and boss encounters. The mod is **100% server-side** - clients need only vanilla Minecraft.

## Architecture Overview

### Core Components
- **`worldgen/`** - Procedural city generation, structure placement, boss spawning, zombie AI
  - `lost/` - Original Lost Cities generation engine (building patterns, chunk management)
  - `PillagerOutpostIntegration.java` - Wastelord boss spawning (500HP Evoker, 450HP Illusioner)
  - `ApocalypseZombieHandler.java` - Zombie block-breaking, lunge attacks, speed scaling, body odor
  - `ApocalypseDifficultyScaling.java` - Progressive spawning (20-400 zombies based on player advancement)
  - `PillagerOutpostStructurePlacer.java` - NBT structure placement with feature generation
- **`compat/`** - Mod integration layers (BOP, Lootr, JEI, TOP - all optional soft dependencies)
- **`config/`** - Profile system for city styles, building types, world generation rules
- **`varia/PerformanceOptimizer.java`** - LRU caches for chunk data, BlockState pooling

### Data-Driven Generation (JSON-based)
All in `src/main/resources/data/lostcities/lostcities/`:
- **`buildings/`** - Building type definitions (factories, libraries, shopping centers, townhalls)
- **`parts/`** - Reusable structure components (floors, interiors, facades, rooftops)
- **`palettes/`** - Block palette definitions (modern materials: concrete, deepslate, tuff, mud bricks)
- **`styles/`** - City appearance styles (standard, desert, jungle, snowy, BOP variants)
- **`citystyles/`** - Complete city generation presets combining palettes and buildings
- **`loot_tables/`** - Custom loot (vending machines, office supplies, factory components)

### Key Integrations
- **Lootr (optional)** - Per-player loot chests (automatically converts vanilla chests w/ loot tables)
- **BOP (optional)** - Enhanced palettes and biome-specific city styles (cherry, wasteland)
- **Modern Tweaks** - Integrated modern building content (factories, data centers, monorails, shopping malls)

## Critical Development Patterns

### 1. Server-Side Architecture
**ALL features must work server-side only:**
- Use vanilla entity types (Zombie, Evoker, Illusioner) - no custom entities requiring client mods
- NBT data for custom attributes (boss health, AI modifications)
- Server-side event handlers in `worldgen/` package
- Example: `ApocalypseZombieHandler` modifies zombie AI without client-side code

### 2. RandomSource vs Random (Minecraft 1.20+)
**ALWAYS use `net.minecraft.util.RandomSource`:**
```java
// CORRECT
RandomSource random = level.getRandom();
RandomSource random = RandomSource.create(seed);

// WRONG - will not compile
Random random = new Random(seed);
Random random = level.getRandom(); // Type error
```

### 3. JSON Schema Conventions
**Style files** use lowercase keys:
```json
{
  "randompalettes": [[{"factor": 1.0, "palette": "lostcities:modern"}]]  // CORRECT
  "randomPaletteChoices": [...]  // WRONG - causes registry errors
}
```

**Stuff files** (decorative columns) require specific structure:
```json
{
  "column": ":::h",
  "maxheight": 120,
  "blocks": {"if_any": ["#forge:stone"]},
  "buildings": {"if_any": ["lostcities:building1"]}
}
// NOT action-based systems - use palettes for block replacements
```

### 4. Performance Optimization
**Use LRU caches from `PerformanceOptimizer`:**
```java
private static final PerformanceOptimizer.LRUCache<ChunkCoord, BuildingInfo> CACHE = 
    new PerformanceOptimizer.LRUCache<>("CacheName");

// AtomicInteger required for lambda expressions
AtomicInteger count = new AtomicInteger(0);
list.forEach(item -> count.incrementAndGet());
```

### 5. Compatibility Layer Pattern
**All mod integrations via `compat/` package:**
```java
// Check if mod loaded
if (LootrCompat.isLootrLoaded()) {
    LootrCompat.logStatus(); // Log integration
}
// No hard dependencies - graceful degradation
```

## Build & Deployment Workflow

### GitHub Actions (Automated)
**NEVER build locally. Always use CI/CD:**
```bash
git add .
git commit -m "descriptive change"
git push
```
- Auto-versioning: `1.0.{run_number}` (e.g., 1.0.8, 1.0.9)
- Auto-release: Creates GitHub release with JAR upload
- JAR name: `forgotten-cities-1.0.X.jar`

### Version Management
- Version set in workflow: `.github/workflows/build.yml`
- **DO NOT manually create tags or releases**
- **DO NOT edit version in build.gradle** (uses -Pversion from workflow)

### Error Checking Workflow
After ANY code change:
1. Run `get_errors` tool across entire codebase
2. Fix ALL errors systematically (not just one file)
3. Re-validate after each fix
4. Push only when 100% error-free

## Common Issues & Solutions

### Compilation Errors
- **"Cannot find PerformanceOptimizer"** → Import from `mcjty.lostcities.varia` not `worldgen`
- **"RandomSource cannot be converted to Random"** → Use `RandomSource.create()` not `new Random()`
- **"No key randompalettes"** → JSON uses `randomPaletteChoices` (wrong) instead of `randompalettes`
- **Lambda "must be final or effectively final"** → Use `AtomicInteger` or `AtomicLong` for counters

### Registry Errors
- BOP files must use correct schema (`randompalettes` not `randomPaletteChoices`)
- Stuff files need column-based structure, not action-based
- Check logs for "Failed to parse" errors - usually JSON syntax/schema issues

## Project-Specific Conventions

### Package Structure
- `worldgen/` - World generation, bosses, zombie AI (server-side features)
- `compat/` - Optional mod integrations (soft dependencies only)
- `setup/` - Forge initialization, event registration
- `varia/` - Utility classes (performance, helpers)

### Naming
- Mod ID: `lostcities` (for compatibility)
- Display name: "LostCities" / "Forgotten Cities"
- JAR name: `forgotten-cities-{version}.jar`
- Namespace: All data files use `lostcities:` prefix

### Boss Design Philosophy
- Extreme difficulty (500HP Evoker, 450HP Illusioner)
- Max-level vanilla enchantments (Sharpness X, Power VI)
- Permanent status effects (Strength VI, Resistance IV, Regen III)
- Knockback immunity
- Custom loot with NBT (legendary weapons, totems)

### Zombie Apocalypse Mechanics
- Block breaking: weak blocks 1/30 chance, strong blocks 1/70
- Lunge attacks when 3+ zombies nearby (launch at target)
- Speed scaling: 0.18 (Easy) → 0.30 (Hard)
- Body odor: Rotten flesh in inventory weakens zombies (-20% damage)
- Progressive spawning: 20 zombies (early) → 400 (endgame) based on 15 advancement milestones

## Files to Reference
- **Boss spawning**: `worldgen/PillagerOutpostIntegration.java`
- **Zombie AI**: `worldgen/ApocalypseZombieHandler.java`, `ApocalypseDifficultyScaling.java`
- **Performance**: `varia/PerformanceOptimizer.java`
- **Mod compat**: `compat/LootrCompat.java`, `compat/BiomesOPlentyCompat.java`
- **CI/CD**: `.github/workflows/build.yml`
- **Modern content**: `data/lostcities/lostcities/parts/factory/`, `parts/shopping/`, `parts/library/`