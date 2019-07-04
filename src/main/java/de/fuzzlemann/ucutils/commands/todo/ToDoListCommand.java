package de.fuzzlemann.ucutils.commands.todo;

import de.fuzzlemann.ucutils.utils.command.api.Command;
import de.fuzzlemann.ucutils.utils.text.Message;
import de.fuzzlemann.ucutils.utils.text.MessagePart;
import de.fuzzlemann.ucutils.utils.text.TextUtils;
import de.fuzzlemann.ucutils.utils.todo.ToDo;
import de.fuzzlemann.ucutils.utils.todo.ToDoManager;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.event.ClickEvent;
import net.minecraft.util.text.event.HoverEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * @author Fuzzlemann
 */
@SideOnly(Side.CLIENT)
public class ToDoListCommand {

    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm");

    @Command({"todo", "todolist"})
    public boolean onCommand() {
        List<ToDo> toDoList = ToDoManager.getToDoList();
        if (toDoList.isEmpty()) {
            TextUtils.simpleMessage("Deine ToDo-Liste ist derzeit leer.");
            return true;
        }

        Message.Builder builder = Message.builder();

        builder.of("» ").color(TextFormatting.DARK_GRAY).advance()
                .of("ToDos\n").color(TextFormatting.DARK_AQUA).advance();

        for (ToDo toDo : toDoList) {
            builder.space().space()
                    .of("*").color(TextFormatting.DARK_GRAY)
                    .hoverEvent(HoverEvent.Action.SHOW_TEXT, MessagePart.simple("Erstellt am " + dateFormat.format(new Date(toDo.getCreated())), TextFormatting.GRAY)).advance()
                    .space()
                    .of(toDo.getMessage()).color(toDo.isDone() ? TextFormatting.GREEN : TextFormatting.RED)
                    .hoverEvent(HoverEvent.Action.SHOW_TEXT, MessagePart.simple(toDo.isDone() ? "Erledigt" : "Nicht erledigt", toDo.isDone() ? TextFormatting.GREEN : TextFormatting.RED)).advance()
                    .space();

            if (!toDo.isDone()) {
                builder.of("[✓]").color(TextFormatting.GREEN)
                        .hoverEvent(HoverEvent.Action.SHOW_TEXT, MessagePart.simple("Markiere den Eintrag als erledigt", TextFormatting.GREEN))
                        .clickEvent(ClickEvent.Action.RUN_COMMAND, "/donetodo " + toDo.getID()).advance()
                        .space();
            }

            builder.of("[✗]").color(TextFormatting.RED)
                    .hoverEvent(HoverEvent.Action.SHOW_TEXT, MessagePart.simple("Lösche den Eintrag", TextFormatting.RED))
                    .clickEvent(ClickEvent.Action.RUN_COMMAND, "/removetodo " + toDo.getID()).advance()
                    .space()
                    .of("[␈]\n").color(TextFormatting.DARK_GRAY)
                    .hoverEvent(HoverEvent.Action.SHOW_TEXT, MessagePart.simple("Verändere den Eintrag", TextFormatting.DARK_GRAY))
                    .clickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/modifytodo " + toDo.getID() + " " + toDo.getMessage()).advance();
        }

        builder.send();
        return true;
    }
}