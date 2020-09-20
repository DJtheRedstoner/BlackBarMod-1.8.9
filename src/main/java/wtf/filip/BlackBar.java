package wtf.filip;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import net.minecraftforge.client.ClientCommandHandler;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.awt.*;
import java.io.File;
import java.io.FileWriter;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * @author Filip
 */
@Mod(modid = "blackbar", name = "BlackBar", version = BlackBar.VERSION)
public class BlackBar {
    public static final String VERSION = "1.0.0";
    private static final File config = new File("./config/bb.json");
    @Mod.Instance("blackbar")
    public static BlackBar instance;
    private int opacity = 97;
    private boolean name = true, coords = true, time = true, date = true, enabled = true;

    public static int toRGBA(Color c) {
        return c.getRed() | c.getGreen() << 8 | c.getBlue() << 16 | c.getAlpha() << 24;
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        loadConfig();
        MinecraftForge.EVENT_BUS.register(new Render());
        ClientCommandHandler.instance.registerCommand(new BlackBarCommand());
    }


    private void loadConfig() {
        try {
            if (!config.exists()) {
                config.createNewFile();
            } else {
                Reader reader = Files.newBufferedReader(Paths.get(config.getPath()));
                JsonObject object = new Gson().fromJson(reader, JsonObject.class);
                enabled = object.get("enabled").getAsBoolean();
                opacity = object.get("opacity").getAsInt();
                name = object.get("name").getAsBoolean();
                coords = object.get("coords").getAsBoolean();
                time = object.get("time").getAsBoolean();
                date = object.get("date").getAsBoolean();

                reader.close();
            }
        } catch (Exception ignored) {

        }
    }

    private void saveConfig() {
        try {
            Writer writer = new FileWriter(config.getPath());
            JsonObject a = new JsonObject();
            a.addProperty("enabled", enabled);
            a.addProperty("opacity", opacity);
            a.addProperty("name", name);
            a.addProperty("coords", coords);
            a.addProperty("time", time);
            a.addProperty("date", date);

            new Gson().toJson(a, writer);
            writer.close();
        } catch (Exception ignored) {
        }

    }

    static class Render {
        private static final DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        private static final DateTimeFormatter timeFormat = DateTimeFormatter.ofPattern("HH:mm:ss");

        @SubscribeEvent
        public void render(TickEvent.RenderTickEvent event) {
            if (event.phase.equals(TickEvent.Phase.END))
                if (BlackBar.instance.enabled)
                    if (Minecraft.getMinecraft().currentScreen == null) {
                        FontRenderer fontRenderer = Minecraft.getMinecraft().fontRendererObj;

                        ScaledResolution res = new ScaledResolution(Minecraft.getMinecraft());
                        int blackBarHeight = fontRenderer.FONT_HEIGHT * 2 + 4;
                        GlStateManager.pushMatrix();
                        GlStateManager.enableAlpha();
                        GlStateManager.enableBlend();
                        Gui.drawRect(2, res.getScaledHeight() - blackBarHeight, res.getScaledWidth() - 2,
                                res.getScaledHeight() - 2,
                                BlackBar.toRGBA(new Color(0, 0, 0, BlackBar.instance.opacity)));
                        LocalDateTime now = LocalDateTime.now();
                        String date = dateFormat.format(now);
                        String time = timeFormat.format(now);
                        int x = (int) Minecraft.getMinecraft().thePlayer.posX;
                        int y = (int) Minecraft.getMinecraft().thePlayer.posY;
                        int z = (int) Minecraft.getMinecraft().thePlayer.posZ;
                        String coords = "PosX " + x + " PosY " + y + " PoxZ " + z;
                        String name = "Name " + Minecraft.getMinecraft().thePlayer.getName();
                        if (BlackBar.instance.date) {
                            String text = "Date " + date;
                            fontRenderer.drawString(text,
                                    res.getScaledWidth() - fontRenderer.getStringWidth(text) - 3,
                                    res.getScaledHeight() - fontRenderer.FONT_HEIGHT - 2, -1, true);
                        }
                        if (BlackBar.instance.time) {
                            String text = "Time " + time;
                            fontRenderer.drawString(text,
                                    res.getScaledWidth() - fontRenderer.getStringWidth(text) - 3,
                                    res.getScaledHeight() - fontRenderer.FONT_HEIGHT * 2 - 2, -1, true);
                        }
                        if (BlackBar.instance.name)
                            fontRenderer.drawString(name, 3,
                                    res.getScaledHeight() - fontRenderer.FONT_HEIGHT * 2 - 2, -1, true);
                        if (BlackBar.instance.coords)
                            fontRenderer.drawString(coords, 3,
                                    res.getScaledHeight() - fontRenderer.FONT_HEIGHT - 2, -1, true);
                        GlStateManager.disableBlend();
                        GlStateManager.disableAlpha();
                        GlStateManager.popMatrix();
                    }
        }
    }

    static class BlackBarCommand extends CommandBase {

        private final String PREFIX =
                EnumChatFormatting.AQUA + "(" + EnumChatFormatting.GOLD + "BlackBar" + EnumChatFormatting.AQUA + ")" + EnumChatFormatting.
                        RESET;

        @Override
        public String getCommandName() {
            return "blackbar";
        }

        @Override
        public String getCommandUsage(ICommandSender sender) {
            return "/blackbar \n /blackbar <opacity> \n /blackbar <time/date/name/coords/fps/ping> ";
        }

        @Override
        public void processCommand(ICommandSender sender, String[] args) {
            if (args.length == 0)
                BlackBar.instance.enabled = !BlackBar.instance.enabled;
            else if (args.length == 1) {
                try {
                    int op = Integer.parseInt(args[0]);
                    if (op > 255 || op < 0)
                        Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText(PREFIX + " Enter opacity " +
                                "value " +
                                "between " +
                                "0 and 255"));
                    else
                        BlackBar.instance.opacity = op;
                } catch (NumberFormatException e) {
                    switch (args[0].toLowerCase()) {
                        case "time":
                            BlackBar.instance.time = !BlackBar.instance.time;
                            break;
                        case "date":
                            BlackBar.instance.date = !BlackBar.instance.date;
                            break;
                        case "name":
                            BlackBar.instance.name = !BlackBar.instance.name;
                            break;
                        case "coords":
                            BlackBar.instance.coords = !BlackBar.instance.coords;
                            break;
                        default:
                            Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText(PREFIX + " " + getCommandUsage(sender)));
                            break;
                    }
                }

            }
            BlackBar.instance.saveConfig();
        }

        @Override
        public int getRequiredPermissionLevel() {
            return -1;
        }

    }

}
