package codes.atomys.advancementinforeloaded;

import blue.endless.jankson.Jankson;
import io.wispforest.owo.config.ConfigWrapper;
import io.wispforest.owo.config.Option;
import io.wispforest.owo.util.Observable;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class AdvancementInfoReloadedConfig extends ConfigWrapper<codes.atomys.advancementinforeloaded.AdvancementInfoReloadedConfigModel> {

    public final Keys keys = new Keys();

    private final Option<java.lang.Integer> marginX = this.optionForKey(this.keys.marginX);
    private final Option<java.lang.Integer> marginY = this.optionForKey(this.keys.marginY);

    private AdvancementInfoReloadedConfig() {
        super(codes.atomys.advancementinforeloaded.AdvancementInfoReloadedConfigModel.class);
    }

    private AdvancementInfoReloadedConfig(Consumer<Jankson.Builder> janksonBuilder) {
        super(codes.atomys.advancementinforeloaded.AdvancementInfoReloadedConfigModel.class, janksonBuilder);
    }

    public static AdvancementInfoReloadedConfig createAndLoad() {
        var wrapper = new AdvancementInfoReloadedConfig();
        wrapper.load();
        return wrapper;
    }

    public static AdvancementInfoReloadedConfig createAndLoad(Consumer<Jankson.Builder> janksonBuilder) {
        var wrapper = new AdvancementInfoReloadedConfig(janksonBuilder);
        wrapper.load();
        return wrapper;
    }

    public int marginX() {
        return marginX.value();
    }

    public void marginX(int value) {
        marginX.set(value);
    }

    public int marginY() {
        return marginY.value();
    }

    public void marginY(int value) {
        marginY.set(value);
    }


    public static class Keys {
        public final Option.Key marginX = new Option.Key("marginX");
        public final Option.Key marginY = new Option.Key("marginY");
    }
}

