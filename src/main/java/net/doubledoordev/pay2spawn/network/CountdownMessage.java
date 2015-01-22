package net.doubledoordev.pay2spawn.network;

import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import net.doubledoordev.pay2spawn.util.CountdownTickHandler;

/**
 * @author Dries007
 */
public class CountdownMessage implements IMessage
{
    private String name;
    private int remaining;
    private boolean addToHUD;

    public CountdownMessage()
    {

    }

    public CountdownMessage(CountdownTickHandler.QueEntry entry)
    {
        this.name = entry.name;
        this.remaining = entry.remaining;
        this.addToHUD = entry.addToHUD;
    }

    @Override
    public void fromBytes(ByteBuf buf)
    {
        name = ByteBufUtils.readUTF8String(buf);
        remaining = buf.readInt();
        addToHUD = buf.readBoolean();
    }

    @Override
    public void toBytes(ByteBuf buf)
    {
        ByteBufUtils.writeUTF8String(buf, name);
        buf.writeInt(remaining);
        buf.writeBoolean(addToHUD);
    }

    public static class Handler implements IMessageHandler<CountdownMessage, IMessage>
    {
        @Override
        public IMessage onMessage(CountdownMessage message, MessageContext ctx)
        {
            if (ctx.side.isClient())
            {
                CountdownTickHandler.INSTANCE.add(new CountdownTickHandler.QueEntry(message.name, message.remaining, message.addToHUD));
            }
            return null;
        }
    }
}
