package net.doubledoordev.pay2spawn.util;

/**
 * This exists so logging utilities can make sure there not dealing with a fake crash.
 * @author Dries007
 */
public class DramaException extends RuntimeException
{
    public DramaException(String message)
    {
        super(message);
    }
}
