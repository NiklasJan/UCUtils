package de.fuzzlemann.ucutils.commands.police;

import de.fuzzlemann.ucutils.utils.command.Command;
import de.fuzzlemann.ucutils.utils.command.CommandExecutor;
import de.fuzzlemann.ucutils.utils.command.TabCompletion;
import de.fuzzlemann.ucutils.utils.math.Expression;
import de.fuzzlemann.ucutils.utils.police.Wanted;
import de.fuzzlemann.ucutils.utils.police.WantedManager;
import de.fuzzlemann.ucutils.utils.text.TextUtils;
import lombok.SneakyThrows;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Fuzzlemann
 */
@SideOnly(Side.CLIENT)
public class ASUCommand implements CommandExecutor, TabCompletion {

    @Override
    @Command(labels = "asu", usage = "/%label% [Spieler(...)] [Grund] (Variation) (-v/-b)")
    public boolean onCommand(EntityPlayerSP p, String[] args) {
        if (args.length < 2) return false;

        Set<Flag> flags = getFlag(args);
        int variationIndex = args.length - 1 - flags.size();

        int variation = 0;

        try {
            variation = Integer.parseInt(args[variationIndex]);
        } catch (NumberFormatException e) {
            variationIndex++;
        }

        if (Math.abs(variation) > 10) {
            TextUtils.error("Die Variation darf nicht gr\u00f6\u00dfer als 10 Wanteds sein.", p);
            return true;
        }

        int reasonIndex = variationIndex - 1;

        List<String> players = Arrays.asList(args).subList(0, reasonIndex);
        String reason = args[reasonIndex];

        Wanted wanted = WantedManager.getWanted(reason.replace('-', ' '));

        if (wanted == null) {
            TextUtils.error("Der Wantedgrund wurde nicht gefunden.", p);
            return true;
        }

        String wantedReason = wanted.getReason();
        int wantedAmount = wanted.getAmount();

        for (Flag flag : flags) {
            wantedReason = flag.modifyReason(wantedReason);
            wantedAmount = flag.modifyWanteds(wantedAmount);
        }

        giveWanteds(p, wantedReason, wantedAmount + variation, players);
        return true;
    }

    private void giveWanteds(EntityPlayerSP issuer, String reason, int amount, List<String> players) {
        for (String player : players) {
            issuer.sendChatMessage("/su " + amount + " " + player + " " + reason);
        }
    }

    private static Set<Flag> getFlag(String[] args) {
        Set<Flag> flags = new HashSet<>();

        for (int i = args.length - 1; i > args.length - Flag.values().length - 1; i--) {
            Flag flag = Flag.getFlag(args[i]);

            if (flag != null)
                flags.add(flag);
        }

        return flags;
    }

    @Override
    public List<String> getTabCompletions(EntityPlayerSP p, String[] args) {
        if (args.length != 2) return Collections.emptyList();

        String reason = args[args.length - 1].toLowerCase();
        List<String> wantedReasons = WantedManager.getWantedReasons()
                .stream()
                .map(wantedReason -> wantedReason.replace(' ', '-'))
                .collect(Collectors.toList());

        if (reason.isEmpty()) return wantedReasons;

        wantedReasons.removeIf(wantedReason -> !wantedReason.toLowerCase().startsWith(reason));

        Collections.sort(wantedReasons);
        return wantedReasons;
    }

    private enum Flag {
        TRIED("-v", "Versuchte/s ", "", "x/2"),
        SUBSIDY("-b", "Beihilfe bei der/dem ", "", "x-10");

        private final String flagArgument;
        private final String prependReason;
        private final String postponeReason;
        private final String wantedModification;

        Flag(String flagArgument, String prependReason, String postponeReason, String wantedModification) {
            this.flagArgument = flagArgument;
            this.prependReason = prependReason;
            this.postponeReason = postponeReason;
            this.wantedModification = wantedModification;
        }

        private String modifyReason(String reason) {
            return prependReason + reason + postponeReason;
        }

        @SneakyThrows
        private int modifyWanteds(int wanteds) {
            return (int) new Expression(wantedModification.replace("x", String.valueOf(wanteds))).evaluate();
        }

        public static Flag getFlag(String string) {
            for (Flag flag : Flag.values()) {
                if (flag.flagArgument.equalsIgnoreCase(string)) return flag;
            }

            return null;
        }
    }
}
