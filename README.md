# Starloader-Bukkit

Because the answer is "why not?"

## What is this

Starloader-Bukkit is a derivative of Starloader however is meant to load minecraft server software like the minecraft vanilla server, spigot and paper instead of Galimulator. The main reason to use this is that this has easily accessible ASM without using a java agent. This could allow for better caching, but just about anything is possible with this (note: modifying bukkit plugins may or may not work - I certainly do not grantee it).

## Limitations and known bugs

- Spigot produces strange behaviour with the console, can likely be suppressed by going back to the vanilla console mode
- On paper, the `cache/patched_1.17.jar` jar has to be used instead of the paperclip one! (paperclip has to be run at least once before for that)
- The slf4j config is a bit misaligned, which is why color chars are not fully supported
- Don't bother disabling extensions, it may not really work due to a workaround for issues with headless mode

## Running

- In headless mode:
  - Run the jar (for the initial setup), it should crash very soon (Exit process with Ctrl + C)
  - Modify the `.slconfig.json` file to your liking (especially the `target-jar` entry, which will be the paper/spigot jar you would otherwise run (see limitations for an issue with paper))
  - Run the jar again, it should now work as intended, if not repeat step 2 until it works
- In desktop mode (headless mode way will also work):
  - You will also be able to use a fancy configuration that can be suppressed via the `--nogui`/`nogui` arguments and can be forcefully included via the `--slgui`/`slgui` argument.

## Licensing and redistributing

All code is licensed under the Apache 2.0 license, allowing you to redistribute
the source.

Additionally a few portions of our code (i. e. the whole selfmodification and extension system)
was largely written by Minestom contributors, who have contributed their code under
the Apache 2.0 license
