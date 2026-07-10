package com.visomod.export;
public class TestResource {
    public static void test() throws Exception {
        java.lang.reflect.Field f = net.minecraft.client.renderer.texture.SpriteContents.class.getDeclaredField("byMipLevel");
        System.out.println("Field found: " + f.getName());
    }
}
