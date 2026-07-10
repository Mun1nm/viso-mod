import java.lang.reflect.Method;
public class dumper2 {
    public static void main(String[] args) throws Exception {
        Class<?> clazz = Class.forName("net.minecraft.client.renderer.texture.TextureAtlasSprite");
        for (Method m : clazz.getMethods()) {
            if (m.getName().startsWith("get")) {
                System.out.println("Method: " + m.getName() + " Return: " + m.getReturnType().getName());
            }
        }
    }
}
