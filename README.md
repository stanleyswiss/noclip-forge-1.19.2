# Noclip (Forge 1.19.2)

Minimal noclip mod for Minecraft Forge 1.19.2 (43.x). Toggle with **N**. 
Multiplayer requires the **server** to also install the mod.

## How this repo builds without local SDK
We don't commit the Forge MDK. The GitHub Actions workflow downloads the MDK for 1.19.2 (43.5.0), overlays this repo's `src` & `resources`, then runs Gradle to build and uploads the JAR as an artifact.

## Usage
1. Push this repo to GitHub.
2. Go to **Actions → Build**; download the artifact when it finishes.
3. Drop the resulting JAR in your `mods/` folder. (Servers must install it too.)

## Change key
Edit `ClientInit.java` and change `GLFW.GLFW_KEY_N`.
