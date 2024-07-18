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

    private final Option<java.lang.Boolean> displaySidebar = this.optionForKey(this.keys.displaySidebar);
    private final Option<java.lang.Boolean> displayDescription = this.optionForKey(this.keys.displayDescription);
    private final Option<java.lang.Boolean> alphabeticOrder = this.optionForKey(this.keys.alphabeticOrder);
    private final Option<java.lang.Boolean> blackBackground = this.optionForKey(this.keys.blackBackground);
    private final Option<java.lang.Integer> headerHeight = this.optionForKey(this.keys.headerHeight);
    private final Option<java.lang.Integer> footerHeight = this.optionForKey(this.keys.footerHeight);
    private final Option<java.lang.Integer> criteriasWidth = this.optionForKey(this.keys.criteriasWidth);
    private final Option<java.lang.Integer> aboveWidgetLimit = this.optionForKey(this.keys.aboveWidgetLimit);
    private final Option<java.lang.Integer> belowWidgetLimit = this.optionForKey(this.keys.belowWidgetLimit);

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

    public boolean displaySidebar() {
        return displaySidebar.value();
    }

    public void displaySidebar(boolean value) {
        displaySidebar.set(value);
    }

    public boolean displayDescription() {
        return displayDescription.value();
    }

    public void displayDescription(boolean value) {
        displayDescription.set(value);
    }

    public boolean alphabeticOrder() {
        return alphabeticOrder.value();
    }

    public void alphabeticOrder(boolean value) {
        alphabeticOrder.set(value);
    }

    public boolean blackBackground() {
        return blackBackground.value();
    }

    public void blackBackground(boolean value) {
        blackBackground.set(value);
    }

    public int headerHeight() {
        return headerHeight.value();
    }

    public void headerHeight(int value) {
        headerHeight.set(value);
    }

    public int footerHeight() {
        return footerHeight.value();
    }

    public void footerHeight(int value) {
        footerHeight.set(value);
    }

    public int criteriasWidth() {
        return criteriasWidth.value();
    }

    public void criteriasWidth(int value) {
        criteriasWidth.set(value);
    }

    public int aboveWidgetLimit() {
        return aboveWidgetLimit.value();
    }

    public void aboveWidgetLimit(int value) {
        aboveWidgetLimit.set(value);
    }

    public int belowWidgetLimit() {
        return belowWidgetLimit.value();
    }

    public void belowWidgetLimit(int value) {
        belowWidgetLimit.set(value);
    }


    public static class Keys {
        public final Option.Key displaySidebar = new Option.Key("displaySidebar");
        public final Option.Key displayDescription = new Option.Key("displayDescription");
        public final Option.Key alphabeticOrder = new Option.Key("alphabeticOrder");
        public final Option.Key blackBackground = new Option.Key("blackBackground");
        public final Option.Key headerHeight = new Option.Key("headerHeight");
        public final Option.Key footerHeight = new Option.Key("footerHeight");
        public final Option.Key criteriasWidth = new Option.Key("criteriasWidth");
        public final Option.Key aboveWidgetLimit = new Option.Key("aboveWidgetLimit");
        public final Option.Key belowWidgetLimit = new Option.Key("belowWidgetLimit");
    }
}

