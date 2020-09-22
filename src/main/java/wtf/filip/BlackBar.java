package wtf.filip;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.network.NetworkPlayerInfo;
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
import java.util.HashMap;
import java.util.Map;

/**
 * @author Filip
 */
@Mod(modid = "blackbar", name = "BlackBar", version = BlackBar.VERSION)
public class BlackBar {
    public static final String VERSION = "1.0.3";
    private static final File config = new File("./config/bb.json");
    @Mod.Instance("blackbar")
    public static BlackBar instance;
    private final Map<String, Boolean> configMap = new HashMap<>();
    public boolean hotbar = false;
    private int opacity = 97;
    private boolean enabled = true;
    private boolean semiColon = false;

    public static int toRGBA(Color c) {
        return c.getRed() | c.getGreen() << 8 | c.getBlue() << 16 | c.getAlpha() << 24;
    }

    private void f(String key) {
        configMap.put(key, !configMap.get(key));
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        loadConfig();
        MinecraftForge.EVENT_BUS.register(new Render());
        ClientCommandHandler.instance.registerCommand(new BlackBarCommand());
    }

    private void loadConfig() {
        if (!config.exists()) {
            try {
                config.createNewFile();
            } catch (Exception ignored) {
            }
            l();
        } else {
            try (Reader reader = Files.newBufferedReader(Paths.get(config.getPath()))) {
                JsonObject object = new Gson().fromJson(reader, JsonObject.class);
                enabled = object.get("enabled").getAsBoolean();
                semiColon = object.get("semiColon").getAsBoolean();
                opacity = object.get("opacity").getAsInt();
                hotbar = object.get("hotbar").getAsBoolean();
                E.hotbar = hotbar;
                Map<String, Boolean> o = new Gson().fromJson(object.getAsJsonObject("configMap").toString(),
                        new TypeToken<Map<String,
                                Boolean>>() {
                        }.getType());
                configMap.putAll(o);
            } catch (Exception e) {
                e.printStackTrace();
                config.delete();
                l();
            }
        }

    }

    private void l() {
        configMap.put("name", true);
        configMap.put("coords", true);
        configMap.put("date", true);
        configMap.put("time", true);
        configMap.put("ping", true);
        configMap.put("fps", true);
        configMap.put("border", true);
        saveConfig();
    }

    private void saveConfig() {
        try (Writer writer = new FileWriter(config.getPath())) {
            new Gson().toJson(BlackBar.instance, writer);
        } catch (Exception ignored) {
        }
    }

    static class Render {
        private static final DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        private static final DateTimeFormatter timeFormat = DateTimeFormatter.ofPattern("HH:mm:ss");

        @SubscribeEvent
        public void render(TickEvent.RenderTickEvent event) {
            if (event.phase.equals(TickEvent.Phase.END) && BlackBar.instance.enabled && Minecraft.getMinecraft().currentScreen == null && Minecraft.getMinecraft().thePlayer != null) {
                FontRenderer fontRenderer = Minecraft.getMinecraft().fontRendererObj;
                ScaledResolution res = new ScaledResolution(Minecraft.getMinecraft());
                int blackBarHeight = fontRenderer.FONT_HEIGHT * 2 + 4;
                GlStateManager.pushMatrix();
                GlStateManager.enableAlpha();
                GlStateManager.enableBlend();
                if (BlackBar.instance.configMap.get("border")) {
                    // TODO Fix this shit
//                    GL11.glColor4f(1f, 1f, 1f, 1f);
//                    GL11.glLineWidth(5.0f);
//                    GL11.glEnable(GL11.GL_LINE_STRIP);
//                    GL11.glVertex2d(1, res.getScaledHeight() - blackBarHeight - 1);
//                    GL11.glVertex2d(res.getScaledWidth() - 1, res.getScaledHeight() - blackBarHeight - 1);
//                    GL11.glVertex2d(res.getScaledWidth() - 1, res.getScaledHeight() - 1);
//                    GL11.glVertex2d(1, res.getScaledHeight() - 1);
//                    GL11.glVertex2d(1, res.getScaledHeight() - blackBarHeight - 1);
//                    GL11.glEnd();
                }

                GlStateManager.translate(0, 0, -100f);
                Gui.drawRect(2, res.getScaledHeight() - blackBarHeight, res.getScaledWidth() - 2,
                        res.getScaledHeight() - 2,
                        BlackBar.toRGBA(new Color(0, 0, 0, BlackBar.instance.opacity)));

                boolean d = BlackBar.instance.configMap.get("date");
                boolean t = BlackBar.instance.configMap.get("time");
                boolean n = BlackBar.instance.configMap.get("name");
                boolean c = BlackBar.instance.configMap.get("coords");
                boolean pi = BlackBar.instance.configMap.get("ping");
                boolean fp = BlackBar.instance.configMap.get("fps");
                LocalDateTime now = LocalDateTime.now();
                String s = BlackBar.instance.semiColon ? ":" : "";
                if (d) {
                    String text = String.format("Date%s %s", s, dateFormat.format(now));
                    fontRenderer.drawString(text,
                            res.getScaledWidth() - fontRenderer.getStringWidth(text) - 3,
                            res.getScaledHeight() - fontRenderer.FONT_HEIGHT - 2, -1, true);
                }
                if (t) {
                    String text = String.format("Time%s %s", s, timeFormat.format(now));
                    fontRenderer.drawString(text,
                            res.getScaledWidth() - fontRenderer.getStringWidth(text) - 3,
                            res.getScaledHeight() - fontRenderer.FONT_HEIGHT * 2 - 2, -1, true);
                }
                if (n) {
                    String name = String.format("Name%s %s", s,
                            EnumChatFormatting.RED + Minecraft.getMinecraft().thePlayer.getName());
                    fontRenderer.drawString(name, 3,
                            res.getScaledHeight() - fontRenderer.FONT_HEIGHT * 2 - 2, -1, true);
                }
                if (c) {
                    String coords = String.format("PosX%s %s PosY%s %s PosZ%s %s", s,
                            (int) Minecraft.getMinecraft().thePlayer.posX, s, (int) Minecraft.getMinecraft().thePlayer.posY, s, (int) Minecraft.getMinecraft().thePlayer.posZ);
                    fontRenderer.drawString(coords, 3,
                            res.getScaledHeight() - fontRenderer.FONT_HEIGHT - 2, -1, true);
                }
                if (pi) {
                    int ping = 0;
                    NetworkPlayerInfo p =
                            Minecraft.getMinecraft().getNetHandler().getPlayerInfo(Minecraft.getMinecraft().thePlayer.getGameProfile().getId());
                    if (p != null) {
                        ping = p.getResponseTime();
                    }
                    String ta = String.format("Ping%s %s", s, EnumChatFormatting.GREEN.toString() + ping);
                    fontRenderer.drawString(ta,
                            ((res.getScaledWidth() / 2) - 90) - fontRenderer.getStringWidth(ta) - 3,
                            res.getScaledHeight() - fontRenderer.FONT_HEIGHT * 2 - 2, -1, true);
                }
                if (fp) {
                    String fa = String.format("FPS%s %s", s, Minecraft.getDebugFPS());
                    fontRenderer.drawString(fa,
                            ((res.getScaledWidth() / 2) - 90) - fontRenderer.getStringWidth(fa) - 3,
                            res.getScaledHeight() - fontRenderer.FONT_HEIGHT - 2, -1, true);
                }
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
            return "/blackbar \n /blackbar <opacity> \n /blackbar <time/date/name/coords/fps/ping/border> ";
        }

        @Override
        public void processCommand(ICommandSender sender, String[] args) {
            if (args.length == 0)
                BlackBar.instance.enabled = !BlackBar.instance.enabled;
            else if (args.length == 1) {
                try {
                    int op = Integer.parseInt(args[0]);
                    if (op > 255 || op < 0)
                        Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText(
                                PREFIX + " Enter opacity value between 0 and 255"));
                    else
                        BlackBar.instance.opacity = op;
                } catch (NumberFormatException e) {
                    switch (args[0].toLowerCase()) {
                        case "hotbar":
                            BlackBar.instance.hotbar = !BlackBar.instance.hotbar;
                            E.hotbar = BlackBar.instance.hotbar;
                            send("hotbar", false);
                            break;
                        case "time":
                            send("time", true);
                            break;
                        case ":":
                            BlackBar.instance.semiColon = !BlackBar.instance.semiColon;
                            send("semi", false);
                            break;
                        case "date":
                            send("date", true);
                            break;
                        case "name":
                            send("name", true);
                            break;
                        case "coords":
                            send("coords", true);
                            break;
                        case "ping":
                            send("ping", true);
                            break;
                        case "fps":
                            send("fps", true);
                            break;
                        case "border":
                            send("border", true);
                            break;
                        default:
                            send(getCommandUsage(sender), false);
                            break;
                    }
                }
            }
            BlackBar.instance.saveConfig();
        }

        private void send(String text, boolean config) {
            if (config) {
                BlackBar.instance.f(text);
                text += BlackBar.instance.configMap.get(text) ? " was enabled" : " was disabled";
            }
            Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText(PREFIX + " " + text));
        }

        @Override
        public int getRequiredPermissionLevel() {
            return -1;
        }

    }

}
