package de.fuzzlemann.ucutils.update;

import de.fuzzlemann.ucutils.Main;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.event.ClickEvent;
import net.minecraft.util.text.event.HoverEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;

/**
 * @author Fuzzlemann
 */
@Mod.EventBusSubscriber
@SideOnly(Side.CLIENT)
public class UpdateReminder {
    public static boolean updateNeeded;

    @SubscribeEvent
    public static void onJoin(EntityJoinWorldEvent e) {
        if (!updateNeeded) return;

        EntityPlayerSP p = Main.MINECRAFT.player;
        if (!e.getEntity().getName().equals(p.getName())) return;

        TextComponentString text = new TextComponentString("Es ist ein neues Update von UCUtils verf\u00fcgbar!");
        text.getStyle().setColor(TextFormatting.RED);

        TextComponentString hoverText = new TextComponentString("Download");
        hoverText.getStyle().setColor(TextFormatting.GREEN);

        text.getStyle().setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, hoverText));
        text.getStyle().setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "http://www.fuzzlemann.de/UCUtils.jar"));

        p.sendMessage(text);
    }

    public static void updateUpdateNeeded() throws IOException {
        updateNeeded = getCurrentVersion() < getLatestVersion();
    }

    private static int getCurrentVersion() {
        return parseVersion(Main.VERSION);
    }

    private static int getLatestVersion() throws IOException {
        URL url = new URL("http://www.fuzzlemann.de/latestversion.html");
        String result = IOUtils.toString(url, StandardCharsets.UTF_8);
        return parseVersion(result);
    }

    private static int parseVersion(String versionString) {
        return Integer.parseInt(versionString.split("-")[1].replace(".", ""));
    }
}
