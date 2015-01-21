package net.doubledoordev.pay2spawn.network;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import net.doubledoordev.pay2spawn.Pay2Spawn;
import net.doubledoordev.pay2spawn.util.CountdownTickHandler;
import net.doubledoordev.pay2spawn.util.RewardsDB;

/**
 * @author Dries007
 */
public class SaleMessage implements IMessage
{
    private int amount;
    private int time;

    public SaleMessage()
    {

    }

    public SaleMessage(int time, int amount)
    {
        this.time = time;
        this.amount = amount;
    }

    @Override
    public void fromBytes(ByteBuf buf)
    {

    }

    @Override
    public void toBytes(ByteBuf buf)
    {

    }

    public static class Handler implements IMessageHandler<SaleMessage, IMessage>
    {
        @Override
        public IMessage onMessage(SaleMessage message, MessageContext ctx)
        {
            if (ctx.side.isClient())
            {
                Pay2Spawn.getRewardsDB().addSale(message.time, message.amount);
            }
            return null;
        }
    }
}
