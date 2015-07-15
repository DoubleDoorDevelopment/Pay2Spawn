package net.doubledoordev.pay2spawn.types;

import com.google.gson.JsonObject;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.ModContainer;
import net.doubledoordev.pay2spawn.Pay2Spawn;
import net.doubledoordev.pay2spawn.network.CrashMessage;
import net.doubledoordev.pay2spawn.permissions.Node;
import net.doubledoordev.pay2spawn.types.guis.CrashTypeGui;
import net.doubledoordev.pay2spawn.util.Helper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;

import static net.doubledoordev.pay2spawn.util.Constants.*;

/**
 * @author Dries007
 */
public class CrashType extends TypeBase
{
    private static final String NAME = "crash";
    public static final String MESSAGE_KEY = "message";
    public static final HashMap<String, String> typeMap = new HashMap<>();
    public static String DEFAULTMESSAGE = "You have not gotten any error messages recently, so here is one, just to let you know that we haven't started caring.";
    public static RuntimeException crash;

    static
    {
        typeMap.put(MESSAGE_KEY, NBTTypes[STRING]);
    }

    @Override
    public String getName()
    {
        return NAME;
    }

    @Override
    public NBTTagCompound getExample()
    {
        NBTTagCompound root = new NBTTagCompound();
        root.setString(MESSAGE_KEY, DEFAULTMESSAGE);
        return root;
    }

    public static StackTraceElement[] getRandomStackTrace()
    {
        StackTraceElement[] list = new StackTraceElement[10 + RANDOM.nextInt(25)];
        for (int i = 0; i < list.length; i++)
        {
            list[i] = getRandomStackTraceElement();
        }
        return list;
    }

    public static StackTraceElement getRandomStackTraceElement()
    {
        ModContainer modContainer;
        do
        {
            modContainer = Helper.getRandomFromSet(Loader.instance().getModList());
        }
        while (modContainer == null || modContainer.getMod() == null);
        Object mod = modContainer.getMod();
        Class modClass = mod.getClass();
        Class rndClass;
        Method rndMethod = null;
        do
        {
            rndClass = getRandomClassFromPackage(RANDOM.nextInt(10), modClass.getPackage().getName(), modClass);
            if (rndClass == null) continue;
            rndMethod = Helper.getRandomFromSet(Arrays.asList(rndClass.getDeclaredMethods()));
        }
        while (rndMethod == null);
        return new StackTraceElement(rndClass.getName(), rndMethod.getName(), rndClass.getSimpleName() + ".class", RANDOM.nextInt(1000));
    }

    public static Class getRandomClassFromPackage(int recursion, String modPackage, Class startClass)
    {
        if (recursion == 0 || startClass == null) return startClass;
        HashSet<Class> classPool = new HashSet<>();
        for (Field field : startClass.getDeclaredFields())
        {
            if (field.getType().getName().startsWith(modPackage)) classPool.add(field.getType());
        }
        for (Method method : startClass.getMethods())
        {
            if (method.getReturnType().getName().startsWith(modPackage)) classPool.add(method.getReturnType());
            for (Class parameterType : method.getParameterTypes())
            {
                if (parameterType.getName().startsWith(modPackage)) classPool.add(parameterType);
            }
            for (Class exceptionType : method.getExceptionTypes())
            {
                if (exceptionType.getName().startsWith(modPackage)) classPool.add(exceptionType);
            }
        }
        if (classPool.isEmpty()) return startClass;
        return getRandomClassFromPackage(recursion - 1, modPackage, Helper.getRandomFromSet(classPool));
    }

    @Override
    public void spawnServerSide(EntityPlayerMP player, NBTTagCompound dataFromClient, NBTTagCompound rewardData)
    {
        Pay2Spawn.getSnw().sendTo(new CrashMessage(dataFromClient.getString(MESSAGE_KEY)), player);
    }

    @Override
    public void openNewGui(int rewardID, JsonObject data)
    {
        new CrashTypeGui(rewardID, NAME, data, typeMap);
    }

    @Override
    public Collection<Node> getPermissionNodes()
    {
        return Collections.singletonList(new Node(NAME));
    }

    @Override
    public Node getPermissionNode(EntityPlayer player, NBTTagCompound dataFromClient)
    {
        return new Node(NAME);
    }

    @Override
    public String replaceInTemplate(String id, JsonObject jsonObject)
    {
        switch (id)
        {
            case "message":
                return jsonObject.getAsJsonPrimitive(MESSAGE_KEY).getAsString();
        }
        return id;
    }

    @Override
    public boolean isInDefaultConfig()
    {
        return false;
    }
}
