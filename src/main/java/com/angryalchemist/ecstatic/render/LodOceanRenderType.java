package com.angryalchemist.ecstatic.render;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat.Mode;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.RenderType.CompositeState;

public final class LodOceanRenderType extends RenderType {
    public static final RenderType OCEAN = create();

    private LodOceanRenderType(
        String name, VertexFormat format, Mode mode, int bufferSize, boolean affectsCrumbling, boolean sortOnUpload, Runnable setupState, Runnable clearState
    ) {
        super(name, format, mode, bufferSize, affectsCrumbling, sortOnUpload, setupState, clearState);
    }

    private static RenderType create() {
        CompositeState state = CompositeState.builder()
            .setShaderState(POSITION_COLOR_SHADER)
            .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
            .setDepthTestState(LEQUAL_DEPTH_TEST)
            .setCullState(NO_CULL)
            .setWriteMaskState(COLOR_WRITE)
            .createCompositeState(false);

        try {
            Method createMethod = findCreateMethod();
            createMethod.setAccessible(true);
            return (RenderType)createMethod.invoke(null, "ecstatic_ocean", DefaultVertexFormat.POSITION_COLOR, Mode.TRIANGLES, 256, state);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Failed to create ecstatic_ocean RenderType", e);
        }
    }

    private static Method findCreateMethod() {
        for (Method method : RenderType.class.getDeclaredMethods()) {
            if (Modifier.isStatic(method.getModifiers())) {
                Class<?>[] params = method.getParameterTypes();
                if (params.length == 5
                    && params[0] == String.class
                    && params[1] == VertexFormat.class
                    && params[2] == Mode.class
                    && params[3] == int.class
                    && params[4] == CompositeState.class) {
                    return method;
                }
            }
        }

        throw new IllegalStateException("Could not find RenderType's 5-arg create(...) overload by signature");
    }
}
