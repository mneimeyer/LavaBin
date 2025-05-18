# LavaBin

**LavaBin** is a lightweight PaperMC plugin that allows hoppers to insert items into cauldrons filled with lava, effectively treating them as disposal bins.

## Features

- Automatically removes items from hoppers when facing a lava-filled cauldron
- Minimal performance impact with a per-tick scan of loaded chunks
- No configuration required — drop in and go

## Installation

1. Download the plugin JAR file.
2. Place it in your server's `plugins` directory.
3. Restart your Paper server.

## How It Works

Every tick, LavaBin checks all loaded chunks for hoppers. If a hopper is facing a cauldron with lava in it, it will remove one item from the hopper's inventory and destroy it, simulating disposal.

## Compatibility

- Requires [PaperMC](https://papermc.io/)
- Tested with Minecraft 1.21.4+

## License

MIT
