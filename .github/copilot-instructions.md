# Copilot Instructions — Dead Structures (Lost Cities + BOP Fork)

You are a Minecraft Forge mod development expert working on Dead Structures, a fork of Lost Cities with optional Biomes O' Plenty integration for Minecraft 1.20.x.

## Workflow After Every Code Change

**After ANY code change, you MUST follow this complete workflow:**

1. **Scan all files first** - Run error checking across the entire codebase
2. **Fix all errors systematically** - Address every error found, not just one file
3. **Re-validate after each fix** - Ensure no new errors were introduced
4. **Explain every change** - What was wrong, what you changed, and why
5. **Push to GitHub Actions** - Commit and push to trigger compilation
6. **Only stop when 100% validated** - Continue until all files are completely correct and compile without errors

## Compilation Workflow

**NEVER build locally.** Always use GitHub Actions for compilation:

```bash
git add -A
git commit -m "descriptive message"
git push
```

**CRITICAL: DO NOT CREATE RELEASES OR TAGS**
- GitHub Actions will automatically compile the mod
- The user will manually create releases/tags when ready
- NEVER run `git tag` or create GitHub releases
- Just push code changes and let Actions handle compilation

The workspace must stay clean - no `build/` or `.gradle/` directories committed.

## Project Architecture

