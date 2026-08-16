package com.angryalchemist.ecstatic.render;

import com.angryalchemist.ecstatic.platform.Services;
import java.lang.reflect.Method;

final class IrisCompat {
    private static final boolean PRESENT = Services.PLATFORM.isModLoaded("iris") || Services.PLATFORM.isModLoaded("oculus");
    private static final Object NONE_PHASE;
    private static final Object TERRAIN_SOLID_PHASE;
    private static final Object TERRAIN_TRANSLUCENT_PHASE;
    private static final Method GET_PIPELINE_MANAGER;
    private static final Method GET_PIPELINE_NULLABLE;
    private static final Method GET_PHASE;
    private static final Method SET_PHASE;
    private static final Method IS_SHADER_PACK_IN_USE;
    private static final Object IRIS_API_INSTANCE;

    private IrisCompat() {
    }

    static boolean isShaderPackActive() {
        if (IS_SHADER_PACK_IN_USE == null) {
            return false;
        }

        try {
            return (Boolean)IS_SHADER_PACK_IN_USE.invoke(IRIS_API_INSTANCE);
        } catch (ReflectiveOperationException e) {
            return false;
        }
    }

    static Object beginNeutralPhase() {
        return beginPhase(NONE_PHASE);
    }

    static Object beginTerrainPhase() {
        return beginPhase(TERRAIN_SOLID_PHASE);
    }

    static Object beginTranslucentPhase() {
        return beginPhase(TERRAIN_TRANSLUCENT_PHASE);
    }

    private static Object beginPhase(Object targetPhase) {
        if (targetPhase != null && GET_PIPELINE_MANAGER != null && isShaderPackActive()) {
            try {
                Object pipelineManager = GET_PIPELINE_MANAGER.invoke(null);
                Object pipeline = GET_PIPELINE_NULLABLE.invoke(pipelineManager);
                if (pipeline == null) {
                    return null;
                }

                Object previousPhase = GET_PHASE.invoke(pipeline);
                SET_PHASE.invoke(pipeline, targetPhase);
                return new Object[]{pipeline, previousPhase};
            } catch (ReflectiveOperationException e) {
                return null;
            }
        } else {
            return null;
        }
    }

    static void endPhase(Object token) {
        if (token instanceof Object[] pair) {
            try {
                SET_PHASE.invoke(pair[0], pair[1]);
            } catch (ReflectiveOperationException var3) {
            }
        }
    }

    static {
        Object nonePhase = null;
        Object terrainSolidPhase = null;
        Object terrainTranslucentPhase = null;
        Method getPipelineManager = null;
        Method getPipelineNullable = null;
        Method getPhase = null;
        Method setPhase = null;
        Method isShaderPackInUse = null;
        Object irisApiInstance = null;
        if (PRESENT) {
            try {
                Class<?> irisClass = Class.forName("net.irisshaders.iris.Iris");
                Class<?> pipelineManagerClass = Class.forName("net.irisshaders.iris.pipeline.PipelineManager");
                Class<?> pipelineClass = Class.forName("net.irisshaders.iris.pipeline.WorldRenderingPipeline");
                Class<? extends Enum> phaseClass = (Class<? extends Enum>)Class.forName("net.irisshaders.iris.pipeline.WorldRenderingPhase");
                Class<?> irisApiClass = Class.forName("net.irisshaders.iris.api.v0.IrisApi");
                getPipelineManager = irisClass.getMethod("getPipelineManager");
                getPipelineNullable = pipelineManagerClass.getMethod("getPipelineNullable");
                getPhase = pipelineClass.getMethod("getPhase");
                setPhase = pipelineClass.getMethod("setPhase", phaseClass);
                Object none = Enum.valueOf(phaseClass, "NONE");
                nonePhase = none;
                Object terrainSolid = Enum.valueOf(phaseClass, "TERRAIN_SOLID");
                terrainSolidPhase = terrainSolid;
                Object terrainTranslucent = Enum.valueOf(phaseClass, "TERRAIN_TRANSLUCENT");
                terrainTranslucentPhase = terrainTranslucent;
                Method getInstance = irisApiClass.getMethod("getInstance");
                irisApiInstance = getInstance.invoke(null);
                isShaderPackInUse = irisApiClass.getMethod("isShaderPackInUse");
            } catch (ReflectiveOperationException | RuntimeException e) {
                getPipelineManager = null;
                getPipelineNullable = null;
                getPhase = null;
                setPhase = null;
                isShaderPackInUse = null;
                irisApiInstance = null;
                nonePhase = null;
                terrainSolidPhase = null;
                terrainTranslucentPhase = null;
            }
        }

        NONE_PHASE = nonePhase;
        TERRAIN_SOLID_PHASE = terrainSolidPhase;
        TERRAIN_TRANSLUCENT_PHASE = terrainTranslucentPhase;
        GET_PIPELINE_MANAGER = getPipelineManager;
        GET_PIPELINE_NULLABLE = getPipelineNullable;
        GET_PHASE = getPhase;
        SET_PHASE = setPhase;
        IS_SHADER_PACK_IN_USE = isShaderPackInUse;
        IRIS_API_INSTANCE = irisApiInstance;
    }
}
