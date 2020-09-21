package wtf.filip;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
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
import java.util.HashMap;
import java.util.Map;

/**
 * @author Filip
 */
@Mod(modid = "blackbar", name = "BlackBar", version = BlackBar.VERSION)
public class BlackBar {
    public static final String VERSION = "1.0.2";
    private static final File config = new File("./config/bb.json");
    @Mod.Instance("blackbar")
    public static BlackBar instance;
    private final Map<String, C> configMap = new HashMap<>();
    private int opacity = 97;
    private boolean enabled = true;

    public static int toRGBA(Color c) {
        return c.getRed() | c.getGreen() << 8 | c.getBlue() << 16 | c.getAlpha() << 24;
    }

    private void f(String key) {
        configMap.get(key).b = !configMap.get(key).b;
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
                opacity = object.get("opacity").getAsInt();
                Map<String, C> o = new Gson().fromJson(object.getAsJsonObject("configMap").toString(), new TypeToken<Map<String,
                        C>>() {
                }.getType());
                configMap.putAll(o);
            } catch (Exception e) {
                config.delete();
                l();
            }
        }

    }

    private void l() {
        configMap.put("name", new C(true, 0, 0));
        configMap.put("coords", new C(true, 0, 0));
        configMap.put("date", new C(true, 0, 0));
        configMap.put("time", new C(true, 0, 0));
        configMap.put("ping", new C(true, 0, 0));
        configMap.put("fps", new C(true, 0, 0));
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
                Gui.drawRect(2, res.getScaledHeight() - blackBarHeight, res.getScaledWidth() - 2,
                        res.getScaledHeight() - 2,
                        BlackBar.toRGBA(new Color(0, 0, 0, BlackBar.instance.opacity)));

                C d = BlackBar.instance.configMap.get("date");
                C t = BlackBar.instance.configMap.get("time");
                C n = BlackBar.instance.configMap.get("name");
                C c = BlackBar.instance.configMap.get("coords");
                C pi = BlackBar.instance.configMap.get("ping");
                C fp = BlackBar.instance.configMap.get("fps");
                LocalDateTime now = LocalDateTime.now();

                if (d.b) {
                    String date = dateFormat.format(now);
                    String text = "Date " + date;
                    fontRenderer.drawString(text,
                            res.getScaledWidth() - fontRenderer.getStringWidth(text) - 3,
                            res.getScaledHeight() - fontRenderer.FONT_HEIGHT - 2, -1, true);
                }
                if (t.b) {
                    String time = timeFormat.format(now);
                    String text = "Time " + time;
                    fontRenderer.drawString(text,
                            res.getScaledWidth() - fontRenderer.getStringWidth(text) - 3,
                            res.getScaledHeight() - fontRenderer.FONT_HEIGHT * 2 - 2, -1, true);
                }
                if (n.b) {
                    String name = "Name " + Minecraft.getMinecraft().thePlayer.getName();
                    fontRenderer.drawString(name, 3,
                            res.getScaledHeight() - fontRenderer.FONT_HEIGHT * 2 - 2, -1, true);
                }
                if (c.b) {
                    int x = (int) Minecraft.getMinecraft().thePlayer.posX;
                    int y = (int) Minecraft.getMinecraft().thePlayer.posY;
                    int z = (int) Minecraft.getMinecraft().thePlayer.posZ;
                    String coords = "PosX " + x + " PosY " + y + " PoxZ " + z;
                    fontRenderer.drawString(coords, 3,
                            res.getScaledHeight() - fontRenderer.FONT_HEIGHT - 2, -1, true);
                }
                if (pi.b) {
                    int p = 0;
                    if (Minecraft.getMinecraft().getNetHandler() != null) {
                        p =
                                Minecraft.getMinecraft().getNetHandler().getPlayerInfo(Minecraft.getMinecraft().thePlayer.getUniqueID()).getResponseTime();
                    }
                    String ta = "Ping " + EnumChatFormatting.GREEN + p;
                    fontRenderer.drawString(ta,
                            ((res.getScaledWidth() / 2) - 90) - fontRenderer.getStringWidth(ta) - 3,
                            res.getScaledHeight() - fontRenderer.FONT_HEIGHT * 2 - 2, -1, true);
                }
                if (fp.b) {
                    int f = Minecraft.getDebugFPS();
                    String fa = "FPS " + f;
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
                        Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText(
                                PREFIX + " Enter opacity value between 0 and 255"));
                    else
                        BlackBar.instance.opacity = op;
                } catch (NumberFormatException e) {
                    switch (args[0].toLowerCase()) {
                        case "time":
                            BlackBar.instance.f("time");
                            break;
                        case "date":
                            BlackBar.instance.f("date");
                            break;
                        case "name":
                            BlackBar.instance.f("name");
                            break;
                        case "coords":
                            BlackBar.instance.f("coords");
                            break;
                        case "ping":
                            BlackBar.instance.f("ping");
                            break;
                        case "fps":
                            BlackBar.instance.f("fps");
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

    static class C {
        public boolean b;
        public double x, y;

        private C(boolean b, double x, double y) {
            this.b = b;
            this.x = x;
            this.y = y;
        }
    }

}
