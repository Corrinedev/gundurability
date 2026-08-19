package mod.cdv.gdb.mixin;

import com.tacz.guns.api.client.gameplay.IClientPlayerGunOperator;
import com.tacz.guns.resource.manager.ScriptManager;
import mod.cdv.gdb.Util;
import mod.cdv.gdb.network.CancelReloadPacket;
import mod.cdv.gdb.network.NetworkHandler;
import mod.cdv.gdb.network.UnjamPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import org.luaj.vm2.LuaFunction;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.ThreeArgFunction;
import org.luaj.vm2.lib.TwoArgFunction;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;

@Mixin(value = ScriptManager.class, remap = false)
public abstract class ScriptManagerMixin {

    @Shadow
    public abstract LuaTable getScript(ResourceLocation id);

    @Shadow
    @Final
    private Map<String, LuaTable> scriptMap;

    @Inject(method = "apply(Ljava/util/List;Lnet/minecraft/server/packs/resources/ResourceManager;Lnet/minecraft/util/profiling/ProfilerFiller;)V", at = @At("TAIL"))
    void patchDefaultScript(CallbackInfo ci) {
        LuaTable defaultScript = getScript(ResourceLocation.fromNamespaceAndPath("tacz", "default_state_machine"));
        LuaTable mk23Script = getScript(ResourceLocation.fromNamespaceAndPath("tacz", "hk_mk23_state_machine"));
        patchScript(defaultScript);
        patchScript(mk23Script);
    }

    @Unique
    private static void patchScript(LuaTable script) {
        if (script != null) {
            LuaValue mainTrackStates = script.get("main_track_states");
            if(mainTrackStates == null) {
                mainTrackStates = script.get("default.main_track_states");
            }
            if (mainTrackStates != null && mainTrackStates.istable()) {
                LuaTable idle = mainTrackStates.get("idle").checktable();

                LuaFunction oldTransition = idle.get("transition").checkfunction();

                LuaTable unjam = new LuaTable();
                script.set("INPUT_UNJAM_FINISHED", LuaValue.valueOf("unjam_finished"));

                var st_b = script.get("static_track_top").checktable();
                st_b.set("value", st_b.get("value").checkinteger().add(1));
                LuaValue TRACK_JAMMED = st_b.get("value").sub(1);

                unjam.set("entry", new TwoArgFunction() {
                    @Override
                    public LuaValue call(LuaValue self, LuaValue context) {
                        IClientPlayerGunOperator.fromLocalPlayer(Minecraft.getInstance().player).getDataHolder().lockState((op) -> Util.getOrSetTag(Minecraft.getInstance().player.getMainHandItem(), "Jammed", false));
                        context.get("setShouldHideCrossHair").call(context, LuaValue.TRUE);

                        LuaValue track = context.get("getTrack").call(
                                context,
                                LuaValue.valueOf(script.get("STATIC_TRACK_LINE").toint()),
                                LuaValue.valueOf(script.get("MAIN_TRACK").toint())
                        );

                        context.get("stopAnimation").invoke(LuaValue.varargsOf(new LuaValue[]{
                                context,
                                TRACK_JAMMED
                        }));

                        context.get("runAnimation").invoke(LuaValue.varargsOf(new LuaValue[]{
                                context,
                                LuaValue.valueOf("unjam"),
                                track,
                                LuaValue.FALSE,
                                LuaValue.valueOf("PLAY_ONCE_STOP"),
                                LuaValue.valueOf(0.2)
                        }));


                        return LuaValue.NIL;
                    }
                });

                unjam.set("update", new TwoArgFunction() {
                    @Override
                    public LuaValue call(LuaValue self, LuaValue context) {
                        LuaValue track = context.get("getTrack").call(
                                context,
                                LuaValue.valueOf(script.get("STATIC_TRACK_LINE").toint()),
                                LuaValue.valueOf(script.get("MAIN_TRACK").toint())
                        );

                        if (context.get("isHolding").call(context, track).checkboolean()) {
                            context.get("trigger").call(
                                    context,
                                    script.get("INPUT_UNJAM_FINISHED")
                            );
                        }

                        NetworkHandler.sendToServer(new CancelReloadPacket());
                        return LuaValue.NIL;
                    }
                });

                unjam.set("exit", new TwoArgFunction() {
                    @Override
                    public LuaValue call(LuaValue self, LuaValue context) {
                        context.get("stopAnimation").call(
                                context,
                                context.get("getTrack").call(
                                        context,
                                        LuaValue.valueOf(script.get("STATIC_TRACK_LINE").toint()),
                                        LuaValue.valueOf(script.get("MAIN_TRACK").toint())
                                )
                        );

                        context.get("setShouldHideCrossHair").call(context, LuaValue.FALSE);

                        return LuaValue.NIL;
                    }
                });

                unjam.set("main_track_states", mainTrackStates);

                unjam.set("transition", new ThreeArgFunction() {
                    @Override
                    public LuaValue call(LuaValue self, LuaValue context, LuaValue input) {
                        String in = input.checkjstring();
                        if ("unjam".equals(in)) return LuaValue.NIL;
                        if ("put_away".equals(in)) {
                            self.get("main_track_states").get("idle").get("transition").call(self, context, input);
                            return LuaValue.NIL;
                        }
                        if (input.eq(script.get("INPUT_UNJAM_FINISHED")).checkboolean()) {

                            LuaValue track2 = context.get("getTrack").call(
                                    context,
                                    LuaValue.valueOf(script.get("STATIC_TRACK_LINE").toint()),
                                    LuaValue.valueOf(script.get("BASE_TRACK").toint())
                            );

                            context.get("stopAnimation").call(context, track2);

                            LuaValue track3 = context.get("getTrack").call(
                                    context,
                                    LuaValue.valueOf(script.get("STATIC_TRACK_LINE").toint()),
                                    LuaValue.valueOf(script.get("BOLT_CAUGHT_TRACK").toint())
                            );

                            context.get("runAnimation").invoke(LuaValue.varargsOf(new LuaValue[]{
                                    context,
                                    LuaValue.valueOf("static_idle"),
                                    track2,
                                    LuaValue.FALSE,
                                    LuaValue.valueOf("LOOP"),
                                    LuaValue.valueOf(0.0)
                            }));

                            context.get("stopAnimation").invoke(LuaValue.varargsOf(new LuaValue[]{
                                    context,
                                    track3
                            }));

                            NetworkHandler.sendToServer(new UnjamPacket());
                            return self.get("main_track_states").get("idle");
                        }
                        return LuaValue.NIL;
                    }
                });

                mainTrackStates.set("unjam", unjam);

                idle.set("transition", new ThreeArgFunction() {
                    @Override
                    public LuaValue call(LuaValue self, LuaValue context, LuaValue input) {
                        if (input.tojstring().equals("unjam")) {
                            context.get("stopAnimation").invoke(LuaValue.varargsOf(new LuaValue[]{
                                    context,
                                    TRACK_JAMMED
                            }));
                            return unjam;
                        }
                        var item = Minecraft.getInstance().player.getMainHandItem();
                        if(!Util.getOrSetTag(item, "Jammed", false)) {
                            context.get("stopAnimation").invoke(LuaValue.varargsOf(new LuaValue[]{
                                    context,
                                    TRACK_JAMMED
                            }));
                        } else if(input.tojstring().equals("unjam_idle")) {

                            context.get("runAnimation").invoke(LuaValue.varargsOf(new LuaValue[]{
                                    context,
                                    LuaValue.valueOf("unjam_idle"),
                                    TRACK_JAMMED,
                                    LuaValue.FALSE,
                                    LuaValue.valueOf("LOOP"),
                                    LuaValue.valueOf(0.0)
                            }));
                        }

                        return oldTransition.call(self, context, input);
                    }
                });
            }
        }
    }
}
