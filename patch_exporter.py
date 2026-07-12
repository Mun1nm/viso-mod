import re

def patch_file(filepath):
    with open(filepath, 'r') as f:
        content = f.read()

    # Find the BufferedImage creation line
    old_code = """
                                                            java.awt.image.BufferedImage img = new java.awt.image.BufferedImage(width, height, java.awt.image.BufferedImage.TYPE_INT_ARGB);
                                                            for (int py = 0; py < height; py++) {
                                                                for (int px = 0; px < width; px++) {
"""
    new_code = """
                                                            int frameHeight = height;
                                                            if (height > width && height % width == 0) frameHeight = width;
                                                            java.awt.image.BufferedImage img = new java.awt.image.BufferedImage(width, frameHeight, java.awt.image.BufferedImage.TYPE_INT_ARGB);
                                                            for (int py = 0; py < frameHeight; py++) {
                                                                for (int px = 0; px < width; px++) {
"""
    
    if old_code.strip() in content:
        # replace just those lines carefully
        content = content.replace(old_code, new_code)
    else:
        # regex approach
        content = re.sub(
            r'java\.awt\.image\.BufferedImage img = new java\.awt\.image\.BufferedImage\(width, height, java\.awt\.image\.BufferedImage\.TYPE_INT_ARGB\);\s*for \(int py = 0; py < height; py\+\+\) \{\s*for \(int px = 0; px < width; px\+\+\) \{',
            '''int frameHeight = height;
                                                            if (height > width && height % width == 0) frameHeight = width;
                                                            java.awt.image.BufferedImage img = new java.awt.image.BufferedImage(width, frameHeight, java.awt.image.BufferedImage.TYPE_INT_ARGB);
                                                            for (int py = 0; py < frameHeight; py++) {
                                                                for (int px = 0; px < width; px++) {''',
            content
        )

    with open(filepath, 'w') as f:
        f.write(content)

patch_file('/Users/joaoaveraldo/Documents/Minecraft/viso-mod/minecraft-mod/src/main/java/com/visomod/export/StructureExporter.java')
patch_file('/Users/joaoaveraldo/Documents/Minecraft/viso-mod/minecraft-mod/src/main/java/com/visomod/export/EntityModelExtractor.java')

