import java.lang.reflect.Method;
public class dumper {
    public static void main(String[] args) throws Exception {
        Class<?> clazz = Class.forName("net.minecraft.client.resources.model.geometry.BakedQuad");
        for (Method m : clazz.getMethods()) {
            System.out.println("Method: " + m.getName() + " Return: " + m.getReturnType().getName());
        }
    }
}
