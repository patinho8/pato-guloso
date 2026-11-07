package br.mareeletrica.mixin;

import java.util.List;
import java.util.Set;
import net.neoforged.fml.loading.LoadingModList;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

/** Selects the vanilla terrain fix before renderer mixins are prepared. */
public final class MareMixinPlugin implements IMixinConfigPlugin {
    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        if(mixinClassName.endsWith("PunchyWeaponCompatMixin")){var mods=LoadingModList.get();return mods!=null&&mods.getModFileById("punchy")!=null;}
        if (!mixinClassName.equals("br.mareeletrica.mixin.client.LevelRendererMixin")) return true;
        var mods = LoadingModList.get();
        // Embeddium replaces setupRender and already centers terrain on the camera.
        // require=0 cannot suppress a conflict with another mixin's overwrite.
        if (mods != null && mods.getModFileById("embeddium") != null) {
            System.getLogger("MareEletrica/Compatibility").log(System.Logger.Level.INFO,
                    "Embeddium detected: using its camera-centered terrain renderer.");
            return false;
        }
        return true;
    }
    @Override public void onLoad(String mixinPackage) {}
    @Override public String getRefMapperConfig() { return null; }
    @Override public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {}
    @Override public List<String> getMixins() { return null; }
    @Override public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {}
    @Override public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {}
}
