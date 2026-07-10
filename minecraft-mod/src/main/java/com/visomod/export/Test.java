package com.visomod.export;
import java.lang.reflect.Method;
public class Test {
    public static void printMethods() {
        try {
            Object shaper = net.minecraft.client.Minecraft.getInstance().getModelManager().getBlockStateModelSet();
            System.out.println("SHAPER CLASS: " + shaper.getClass().getName());
            for(Method m : shaper.getClass().getMethods()) {
                System.out.println("METHOD: " + m.getName() + " -> " + m.getReturnType().getName());
            }
        } catch(Exception e) {}
    }
}
