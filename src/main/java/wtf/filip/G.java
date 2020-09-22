package wtf.filip;

import net.minecraft.launchwrapper.IClassTransformer;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;

import static org.objectweb.asm.Opcodes.*;


/**
 * @author Filip
 */
public class G implements IClassTransformer {

    @Override
    public byte[] transform(String name, String transformedName, byte[] basicClass) {
        if (basicClass == null)
            return null;
        if (transformedName.equalsIgnoreCase("net.minecraft.client.gui.GuiIngame")) {
            ClassNode classNode = new ClassNode();
            ClassReader classReader = new ClassReader(basicClass);
            classReader.accept(classNode, 0);
            int counter = 0;
            for (MethodNode m : classNode.methods) {
                if (m.desc.startsWith("(Lnet/minecraft/client/gui/ScaledResolution;F)V") || m.desc.startsWith("(Lavr;" +
                        "F)V")) {
                    System.out.println("Found " + m.name + " " + m.desc);
                    for (AbstractInsnNode n : m.instructions.toArray()) {
                        if (n.getOpcode() == INVOKEVIRTUAL) {
                            counter++;
                            if (counter == 7) {
                                InsnList toInsert = new InsnList();
                                LabelNode a = new LabelNode();
                                AbstractInsnNode aac = m.instructions.get(m.instructions.indexOf(n) - 13);
                                toInsert.add(new MethodInsnNode(GETSTATIC, Type.getInternalName(E.class), "hotbar", "Z",
                                        false));
                                toInsert.add(new JumpInsnNode(IFEQ, a));
                                m.instructions.insertBefore(aac, toInsert);
                                m.instructions.insert(n, a);
                                break;
                            }
                        }
                    }
                }
            }
            ClassWriter classWriter = new ClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
            try {
                classNode.accept(classWriter);
                return classWriter.toByteArray();
            } catch (Exception e) {
                System.out.printf("Execption transforming %s : %s", transformedName, e.getClass().getSimpleName());
            }
            return classWriter.toByteArray();
        } else return basicClass;
    }
}
