package mod.cdv.gdb.mixin;

import com.tacz.guns.client.resource.ClientAssetsManager;
import com.tacz.guns.resource.manager.ScriptManager;
import mod.cdv.gdb.ClientEvents;
import mod.cdv.gdb.Util;
import mod.cdv.gdb.network.NetworkHandler;
import mod.cdv.gdb.network.UnjamPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import org.luaj.vm2.LuaFunction;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.ThreeArgFunction;
import org.luaj.vm2.lib.TwoArgFunction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = ScriptManager.class, remap = false)
public class ScriptManagerMixin {

    @Inject(method = "apply(Ljava/util/List;Lnet/minecraft/server/packs/resources/ResourceManager;Lnet/minecraft/util/profiling/ProfilerFiller;)V", at = @At("TAIL"))
    void patchDefaultScript(CallbackInfo ci) {
        var scriptManager = ClientAssetsManager.INSTANCE;
        LuaTable script = scriptManager.getScript(ResourceLocation.parse("tacz:default_state_machine"));

        if(script != null) {
            LuaTable mainTrackStates = script.get("main_track_states").checktable();
            LuaTable idle = mainTrackStates.get("idle").checktable();

            LuaFunction oldTransition = idle.get("transition").checkfunction();

            LuaTable unjam = new LuaTable();
            script.set("INPUT_UNJAM_FINISHED", LuaValue.valueOf("unjam_finished"));

            unjam.set("entry", new TwoArgFunction() {
                @Override
                public LuaValue call(LuaValue self, LuaValue context) {
                    context.get("setShouldHideCrossHair").call(context, LuaValue.TRUE);

                    LuaValue track = context.get("getTrack").call(
                            context,
                            LuaValue.valueOf(script.get("STATIC_TRACK_LINE").toint()),
                            LuaValue.valueOf(script.get("MAIN_TRACK").toint())
                    );

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

                        var item = Minecraft.getInstance().player.getMainHandItem();
                        Util.getOrSetTag(item, "Jammed", false);

                        NetworkHandler.sendToServer(new UnjamPacket());
                        return self.get("main_track_states").get("idle");
                    } else if ("reload".equals(in) || "reload_empty".equals(in) || "put_away".equals(in)) {
                        self.get("main_track_states").get("idle").get("transition").call(self, context, input);
                        return self.get("main_track_states").get("idle");
                    }

                    return LuaValue.NIL;
                }
            });

            mainTrackStates.set("unjam", unjam);

            LuaTable unjam_idle = new LuaTable();
            unjam_idle.set("entry", new TwoArgFunction() {
                @Override
                public LuaValue call(LuaValue self, LuaValue context) {
                    LuaValue track = context.get("getTrack").call(
                            context,
                            LuaValue.valueOf(script.get("STATIC_TRACK_LINE").toint()),
                            LuaValue.valueOf(script.get("BASE_TRACK").toint())
                    );

                    LuaValue track2 = context.get("getTrack").call(
                            context,
                            LuaValue.valueOf(script.get("STATIC_TRACK_LINE").toint()),
                            LuaValue.valueOf(script.get("BOLT_CAUGHT_TRACK").toint())
                    );

                    context.get("runAnimation").invoke(LuaValue.varargsOf(new LuaValue[]{
                            context,
                            LuaValue.valueOf("unjam_idle"),
                            track,
                            LuaValue.FALSE,
                            LuaValue.valueOf("LOOP"),
                            LuaValue.valueOf(0.0)
                    }));

                    context.get("stopAnimation").invoke(LuaValue.varargsOf(new LuaValue[]{
                            context,
                            track2
                    }));

                    return LuaValue.NIL;
                }
            });
            unjam_idle.set("update", new TwoArgFunction() {
                @Override
                public LuaValue call(LuaValue self, LuaValue context) {
                    return LuaValue.NIL;
                }
            });

            unjam_idle.set("exit", new TwoArgFunction() {
                @Override
                public LuaValue call(LuaValue self, LuaValue context) {

                    return LuaValue.NIL;
                }
            });

            unjam_idle.set("main_track_states", mainTrackStates);

            unjam_idle.set("transition", new ThreeArgFunction() {
                @Override
                public LuaValue call(LuaValue self, LuaValue context, LuaValue input) {
                    String in = input.checkjstring();
                    if ("unjam".equals(in)) {
                        return unjam;
                    } else if (!in.equals("walk") && !in.equals("run") && !in.equals("idle")) {
                        self.get("main_track_states").get("idle").get("transition").call(self, context, input);
                        return unjam_idle;
                    }

                    return unjam_idle;
                }
            });

            mainTrackStates.set("unjam_idle", unjam_idle);

            idle.set("transition", new ThreeArgFunction() {
                @Override
                public LuaValue call(LuaValue self, LuaValue context, LuaValue input) {
                    if (input.tojstring().equals("unjam")) {
                        return unjam;
                    }
                    if(input.tojstring().equals("unjam_idle")) {
                        return unjam_idle;
                    }

                    return oldTransition.call(self, context, input);
                }
            });
        }
    }
}
