package io.github.rollylindenshnizzer.nexuscore.client.config;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;

import java.util.List;

final class NexusAutofillEditBox extends EditBox {
    private final List<String> suggestions;
    private String completion;

    NexusAutofillEditBox(Font font, int x, int y, int width, int height, Component message, List<String> suggestions) {
        super(font, x, y, width, height, message);
        this.suggestions = List.copyOf(suggestions);
        updateSuggestion();
    }

    @Override
    public void setValue(String value) {
        super.setValue(value);
        updateSuggestion();
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == 258 && isFocused() && completion != null) {
            setValue(completion);
            return true;
        }
        boolean handled = super.keyPressed(keyCode, scanCode, modifiers);
        updateSuggestion();
        return handled;
    }

    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        boolean handled = super.charTyped(codePoint, modifiers);
        updateSuggestion();
        return handled;
    }

    private void updateSuggestion() {
        if (suggestions == null) {
            return;
        }
        String current = getValue();
        completion = suggestions.stream().filter(candidate -> candidate.length() > current.length()).filter(candidate -> candidate.regionMatches(true, 0, current, 0, current.length())).findFirst().orElse(null);
        setSuggestion(completion == null ? null : completion.substring(current.length()));
    }
}
