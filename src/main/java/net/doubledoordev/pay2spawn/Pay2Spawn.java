package net.doubledoordev.pay2spawn;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;

@Mod(
        modid = Pay2Spawn.MOD_ID,
        name = Pay2Spawn.MOD_NAME,
        version = Pay2Spawn.VERSION
)
public class Pay2Spawn
{

    public static final String MOD_ID = "Pay2Spawn";
    public static final String MOD_NAME = "Pay2Spawn";
    public static final String VERSION = "3.0.0";

    @EventHandler
    public void init(FMLInitializationEvent event)
    {

    }
}
