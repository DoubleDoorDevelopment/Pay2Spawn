package net.doubledoordev.pay2spawn.checkers;

import com.google.common.io.Files;
import net.doubledoordev.pay2spawn.Pay2Spawn;
import net.doubledoordev.pay2spawn.hud.DonationsBasedHudEntry;
import net.doubledoordev.pay2spawn.hud.Hud;
import net.doubledoordev.pay2spawn.util.Donation;
import net.minecraft.client.util.JsonException;
import net.minecraftforge.common.config.Configuration;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.Charset;

import static net.doubledoordev.pay2spawn.util.Constants.BASECAT_TRACKERS;
import static net.doubledoordev.pay2spawn.util.Constants.GSON;

/**
 * @author Dries007
 */
public class FileSystemChecker extends AbstractChecker implements Runnable
{
    public static final FileSystemChecker INSTANCE = new FileSystemChecker();
    public static final String NAME = "filesystem";
    public final static String CAT = BASECAT_TRACKERS + '.' + NAME;
    public final static FileFilter FILTER = new FileFilter()
    {
        @Override
        public boolean accept(File pathname)
        {
            return pathname.isFile() && pathname.getName().endsWith(".json");
        }
    };
    DonationsBasedHudEntry topDonationsBasedHudEntry, recentDonationsBasedHudEntry;
    boolean enabled  = false;
    String path;
    private File folder;

    @Override
    public String getName()
    {
        return NAME;
    }

    @Override
    public void init()
    {
        Hud.INSTANCE.set.add(topDonationsBasedHudEntry);
        Hud.INSTANCE.set.add(recentDonationsBasedHudEntry);

        new Thread(this, getName()).start();
    }

    @Override
    public boolean enabled()
    {
        return enabled;
    }

    @Override
    public void doConfig(Configuration configuration)
    {
        configuration.addCustomCategoryComment(CAT, "This is the checker for IRC bots and other local programs compatible with the FileSystemChecker\nFind more details here: https://gist.github.com/dries007/ce6b417c27155d0f0b7d");
        enabled = configuration.get(CAT, "enabled", enabled).getBoolean(enabled);
        folder = new File(configuration.get(CAT, "path", "p2s_filesystemchecker", "Can be relative to minecraft run directory or absolute.").getString());
        if (folder.exists() && !folder.isDirectory()) enabled = false;
        else if (!folder.exists()) folder.mkdir();

        recentDonationsBasedHudEntry = new DonationsBasedHudEntry("recent" + NAME + ".txt", CAT + ".recentDonations", -1, 2, 5, "$name: $$amount", "-- Recent donations --", CheckerHandler.RECENT_DONATION_COMPARATOR);
        topDonationsBasedHudEntry = new DonationsBasedHudEntry("top" + NAME + ".txt", CAT + ".topDonations", -1, 1, 5, "$name: $$amount", "-- Top donations --", CheckerHandler.AMOUNT_DONATION_COMPARATOR);
    }

    @Override
    public DonationsBasedHudEntry[] getDonationsBasedHudEntries()
    {
        return new DonationsBasedHudEntry[] {topDonationsBasedHudEntry, recentDonationsBasedHudEntry};
    }

    @Override
    public void run()
    {
        while (true)
        {
            try
            {
                for (File file : folder.listFiles(FILTER))
                {
                    Donation donation = null;
                    try
                    {
                        donation = GSON.fromJson(Files.toString(file, Charset.defaultCharset()), Donation.class);
                    }
                    catch (Exception e)
                    {
                        File error = new File(folder, file.getName() + ".error");
                        error.createNewFile();
                        PrintWriter pw = new PrintWriter(error);
                        pw.println("This file caused an error, it was most likely not formatted properly. Below is the original file, then the error report.");
                        pw.println();
                        pw.println(Files.toString(file, Charset.defaultCharset()));
                        pw.println();
                        pw.println();
                        e.printStackTrace(pw);
                        pw.flush();
                        pw.close();
                    }
                    file.delete();
                    if (donation != null) process(donation, true, this);
                }
                doWait(1);
            }
            catch (IOException e)
            {
                Pay2Spawn.getLogger().error("IO error with FileSystemChecker!", e);
                doWait(1);
            }
        }
    }
}
