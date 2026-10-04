package io.github.rollylindenshnizzer.nexuscore.client.config;

import dev.architectury.platform.Platform;
import io.github.rollylindenshnizzer.nexuscore.api.config.NexusConfigHeading;
import io.github.rollylindenshnizzer.nexuscore.api.config.NexusConfigPage;
import io.github.rollylindenshnizzer.nexuscore.api.config.NexusConfigStatusLabel;
import io.github.rollylindenshnizzer.nexuscore.api.config.NexusConfigSubheading;
import io.github.rollylindenshnizzer.nexuscore.api.config.NexusConfigText;
import io.github.rollylindenshnizzer.nexuscore.api.config.NexusConfigValue;
import io.github.rollylindenshnizzer.nexuscore.api.config.NexusConfigs;
import io.github.rollylindenshnizzer.nexuscore.api.config.NexusJsonConfig;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

public final class NexusConfigScreen extends Screen {
    private static final int TAB_Y = 25;
    private static final int TAB_HEIGHT = 20;
    private static final int CONTENT_TOP = 54;
    private static final int FOOTER_HEIGHT = 34;
    private static final int ROW_HEIGHT = 30;
    private static final int INDEX_ROW_HEIGHT = 20;
    private static final int TEXT_LINE_HEIGHT = 10;
    private final Screen parent;
    private final String ownerModId;
    private final List<PageView> pages = new ArrayList<>();
    private final List<LayoutItem> layout = new ArrayList<>();
    private final List<ValueRow> rows = new ArrayList<>();
    private final List<HeadingAnchor> headingAnchors = new ArrayList<>();
    private final List<TabHit> tabHits = new ArrayList<>();
    private final List<HeadingHit> headingHits = new ArrayList<>();
    private int pageIndex;
    private int selectedRow = -1;
    private int contentHeight;
    private double scroll;
    private double targetScroll;
    private double indexScroll;
    private double targetIndexScroll;
    private double tabScroll;
    private double targetTabScroll;

    public NexusConfigScreen(Screen parent, String ownerModId) {
        super(Component.translatable("config.nexuscore.screen.title", Platform.getOptionalMod(ownerModId).map(mod -> mod.getName()).orElse(ownerModId)));
        this.parent = parent;
        this.ownerModId = ownerModId;
        buildPages();
    }

    private void buildPages() {
        pages.clear();
        for (NexusJsonConfig config : NexusConfigs.forMod(ownerModId)) {
            for (NexusConfigPage page : config.pages()) {
                pages.add(new PageView(config, page));
            }
        }
        pageIndex = pages.isEmpty() ? 0 : Math.min(pageIndex, pages.size() - 1);
    }

    @Override
    protected void init() {
        clearWidgets();
        layout.clear();
        rows.clear();
        headingAnchors.clear();
        tabHits.clear();
        headingHits.clear();
        if (!pages.isEmpty()) {
            buildLayout(pages.get(pageIndex));
        }
        selectedRow = rows.isEmpty() ? -1 : Math.max(0, Math.min(selectedRow, rows.size() - 1));
        scroll = clamp(scroll, 0.0D, maxContentScroll());
        targetScroll = clamp(targetScroll, 0.0D, maxContentScroll());
        indexScroll = clamp(indexScroll, 0.0D, maxIndexScroll());
        targetIndexScroll = clamp(targetIndexScroll, 0.0D, maxIndexScroll());
        addRenderableWidget(Button.builder(Component.translatable("config.nexuscore.button.save"), button -> {
            NexusConfigs.saveMod(ownerModId);
            onClose();
        }).bounds(width / 2 - 76, height - 28, 72, 20).build());
        Button reset = Button.builder(Component.translatable("config.nexuscore.button.reset_page"), button -> {
            if (!pages.isEmpty()) {
                PageView current = pages.get(pageIndex);
                current.config().entries(current.page().id()).forEach(NexusConfigValue::reset);
                rebuildWidgets();
            }
        }).bounds(width / 2 + 4, height - 28, 86, 20).build();
        reset.active = !pages.isEmpty();
        addRenderableWidget(reset);
        updateWidgetPositions();
    }

    private void buildLayout(PageView pageView) {
        int y = 4;
        int textWidth = Math.max(80, contentWidth() - 24);
        NexusConfigText pageDescription = pageView.page().description();
        if (!pageDescription.isEmpty()) {
            int lines = NexusConfigTextRenderer.wrap(font, pageDescription, textWidth).size();
            int height = Math.max(18, lines * TEXT_LINE_HEIGHT + 8);
            layout.add(LayoutItem.pageDescription(y, height, pageDescription));
            y += height;
        }
        for (NexusConfigHeading heading : pageView.config().headings(pageView.page().id())) {
            int descriptionLines = heading.description().isEmpty() ? 0 : NexusConfigTextRenderer.wrap(font, heading.description(), textWidth).size();
            int headingHeight = 20 + descriptionLines * TEXT_LINE_HEIGHT + (descriptionLines > 0 ? 6 : 0);
            headingAnchors.add(new HeadingAnchor(heading, y));
            layout.add(LayoutItem.heading(y, headingHeight, heading));
            y += headingHeight;
            y = addValues(pageView, heading, null, y);
            for (NexusConfigSubheading subheading : pageView.config().subheadings(heading.id())) {
                int subDescriptionLines = subheading.description().isEmpty() ? 0 : NexusConfigTextRenderer.wrap(font, subheading.description(), textWidth - 8).size();
                int subheadingHeight = 18 + subDescriptionLines * TEXT_LINE_HEIGHT + (subDescriptionLines > 0 ? 5 : 0);
                layout.add(LayoutItem.subheading(y, subheadingHeight, subheading));
                y += subheadingHeight;
                y = addValues(pageView, heading, subheading, y);
            }
            y += 6;
        }
        contentHeight = y;
    }

    private int addValues(PageView pageView, NexusConfigHeading heading, NexusConfigSubheading subheading, int y) {
        String subheadingId = subheading == null ? null : subheading.id();
        for (NexusConfigValue<?> value : pageView.config().entries(pageView.page().id(), heading.id(), subheadingId)) {
            AbstractWidget editor = createEditor(value);
            addRenderableWidget(editor);
            ValueRow row = new ValueRow(value, editor, y, heading.id());
            rows.add(row);
            layout.add(LayoutItem.value(y, ROW_HEIGHT, row));
            y += ROW_HEIGHT;
        }
        return y;
    }

    private AbstractWidget createEditor(NexusConfigValue<?> value) {
        if (value.kind() == NexusConfigValue.Kind.BOOLEAN) {
            return Button.builder(booleanMessage(value), pressed -> {
                value.setFromString(Boolean.toString(!Boolean.parseBoolean(value.displayValue())));
                pressed.setMessage(booleanMessage(value));
            }).bounds(0, 0, editorWidth(), 20).build();
        }
        if (value.usesDropdown()) {
            return new NexusDropdownWidget(font, 0, 0, editorWidth(), value);
        }
        EditBox box = value.autofillValues().isEmpty() ? new EditBox(font, 0, 0, editorWidth(), 20, value.label()) : new NexusAutofillEditBox(font, 0, 0, editorWidth(), 20, value.label(), value.autofillValues());
        box.setValue(value.displayValue());
        box.setMaxLength(128);
        box.setResponder(text -> box.setTextColor(value.setFromString(text) ? 0xE0E0E0 : 0xFF5555));
        return box;
    }

    private static Component booleanMessage(NexusConfigValue<?> value) {
        return Component.translatable(Boolean.parseBoolean(value.displayValue()) ? "config.nexuscore.value.enabled" : "config.nexuscore.value.disabled");
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics, mouseX, mouseY, partialTick);
        smoothScrolls();
        updateWidgetPositions();
        updateHoveredSelection(mouseX, mouseY);
        renderTitleAndTabs(graphics, mouseX, mouseY);
        renderPanels(graphics);
        renderContent(graphics, mouseX, mouseY);
        renderIndex(graphics, mouseX, mouseY);
        children().forEach(child -> {
            if (child instanceof net.minecraft.client.gui.components.Renderable renderable) {
                renderable.render(graphics, mouseX, mouseY, partialTick);
            }
        });
        renderDetails(graphics);
        renderVersion(graphics);
    }

    private void renderTitleAndTabs(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawCenteredString(font, title, width / 2, 8, 0xFFFFFF);
        tabHits.clear();
        if (pages.isEmpty()) {
            return;
        }
        int startX = 12;
        int endX = width - 12;
        int availableWidth = Math.max(1, endX - startX);
        int totalWidth = tabTotalWidth();
        double maxScroll = Math.max(0.0D, totalWidth - availableWidth);
        targetTabScroll = clamp(targetTabScroll, 0.0D, maxScroll);
        tabScroll = clamp(tabScroll, 0.0D, maxScroll);
        graphics.enableScissor(startX, TAB_Y, endX, TAB_Y + TAB_HEIGHT);
        int x = startX - (int) Math.round(tabScroll);
        for (int i = 0; i < pages.size(); i++) {
            PageView page = pages.get(i);
            int tabWidth = tabWidth(page.page());
            boolean selected = i == pageIndex;
            boolean hovered = mouseX >= x && mouseX < x + tabWidth && mouseY >= TAB_Y && mouseY < TAB_Y + TAB_HEIGHT;
            int background = selected ? 0xCC4A3A5A : hovered ? 0xAA3A3A3A : 0x88303030;
            graphics.fill(x, TAB_Y, x + tabWidth, TAB_Y + TAB_HEIGHT, background);
            if (selected) {
                graphics.fill(x, TAB_Y + TAB_HEIGHT - 2, x + tabWidth, TAB_Y + TAB_HEIGHT, 0xFFE0C0FF);
            }
            NexusConfigTextRenderer.drawCentered(graphics, font, page.page().title(), x + tabWidth / 2, TAB_Y + 6, 0xE8E8E8, false);
            tabHits.add(new TabHit(i, x, x + tabWidth));
            x += tabWidth + 3;
        }
        graphics.disableScissor();
        if (maxScroll > 0.0D) {
            int trackWidth = availableWidth;
            int thumbWidth = Math.max(18, (int) Math.round(trackWidth * (availableWidth / (double) totalWidth)));
            int thumbX = startX + (int) Math.round((trackWidth - thumbWidth) * (tabScroll / maxScroll));
            graphics.fill(startX, TAB_Y + TAB_HEIGHT + 1, endX, TAB_Y + TAB_HEIGHT + 2, 0x44303030);
            graphics.fill(thumbX, TAB_Y + TAB_HEIGHT + 1, thumbX + thumbWidth, TAB_Y + TAB_HEIGHT + 2, 0xAA9A9A9A);
        }
    }

    private void renderPanels(GuiGraphics graphics) {
        int bottom = contentBottom();
        graphics.fill(indexX(), CONTENT_TOP, indexX() + indexWidth(), bottom, 0x66181818);
        graphics.fill(detailX(), CONTENT_TOP, detailX() + detailWidth(), bottom, 0x66181818);
        graphics.fill(contentX(), CONTENT_TOP, contentRight(), bottom, 0x33101010);
        graphics.drawCenteredString(font, Component.translatable("config.nexuscore.index.title"), indexX() + indexWidth() / 2, CONTENT_TOP + 6, 0xB8B8B8);
    }

    private void renderContent(GuiGraphics graphics, int mouseX, int mouseY) {
        int top = CONTENT_TOP;
        int bottom = contentBottom();
        int left = contentX();
        int right = contentRight();
        graphics.enableScissor(left, top, right, bottom);
        int offset = (int) Math.round(scroll);
        for (LayoutItem item : layout) {
            int y = top + item.y() - offset;
            if (y + item.height() < top || y > bottom) {
                continue;
            }
            switch (item.kind()) {
                case PAGE_DESCRIPTION ->
                        NexusConfigTextRenderer.drawWrapped(graphics, font, item.text(), left + 8, y + 2, Math.max(40, right - left - 16), 0xA8A8A8, TEXT_LINE_HEIGHT);
                case HEADING -> renderHeading(graphics, item.heading(), left, right, y);
                case SUBHEADING -> renderSubheading(graphics, item.subheading(), left, right, y);
                case VALUE -> renderValueRow(graphics, item.row(), left, right, y, mouseX, mouseY);
            }
        }
        graphics.disableScissor();
        if (maxContentScroll() > 0.0D) {
            int viewportHeight = contentViewportHeight();
            int trackX = right - 3;
            int thumbHeight = Math.max(16, viewportHeight * viewportHeight / Math.max(viewportHeight, contentHeight));
            int thumbY = top + (int) Math.round((viewportHeight - thumbHeight) * (scroll / maxContentScroll()));
            graphics.fill(trackX, top, trackX + 2, bottom, 0x44202020);
            graphics.fill(trackX, thumbY, trackX + 2, thumbY + thumbHeight, 0xAA8A8A8A);
        }
    }

    private void renderHeading(GuiGraphics graphics, NexusConfigHeading heading, int left, int right, int y) {
        NexusConfigTextRenderer.draw(graphics, font, heading.title(), left + 8, y + 2, 0xF1D7FF, true);
        if (!heading.description().isEmpty()) {
            NexusConfigTextRenderer.drawWrapped(graphics, font, heading.description(), left + 8, y + 15, Math.max(40, right - left - 16), 0x999999, TEXT_LINE_HEIGHT);
        }
    }

    private void renderSubheading(GuiGraphics graphics, NexusConfigSubheading subheading, int left, int right, int y) {
        NexusConfigTextRenderer.draw(graphics, font, subheading.title(), left + 14, y + 1, 0xD8C1E8, false);
        if (!subheading.description().isEmpty()) {
            NexusConfigTextRenderer.drawWrapped(graphics, font, subheading.description(), left + 14, y + 13, Math.max(40, right - left - 22), 0x858585, TEXT_LINE_HEIGHT);
        }
    }

    private void renderValueRow(GuiGraphics graphics, ValueRow row, int left, int right, int y, int mouseX, int mouseY) {
        int rowIndex = rows.indexOf(row);
        boolean hovered = mouseX >= left && mouseX < right && mouseY >= y && mouseY < y + ROW_HEIGHT && mouseY >= CONTENT_TOP && mouseY < contentBottom();
        if (hovered) {
            graphics.fill(left + 2, y, right - 2, y + ROW_HEIGHT - 1, 0x28FFFFFF);
        } else if (rowIndex == selectedRow) {
            graphics.fill(left + 2, y, right - 2, y + ROW_HEIGHT - 1, 0x16FFFFFF);
        }
        int labelWidth = Math.max(40, editorX() - left - 18);
        graphics.enableScissor(left + 6, y, left + 6 + labelWidth, y + ROW_HEIGHT);
        NexusConfigTextRenderer.draw(graphics, font, row.value().labelText(), left + 8, y + 10, 0xE0E0E0, false);
        graphics.disableScissor();
    }

    private void renderIndex(GuiGraphics graphics, int mouseX, int mouseY) {
        headingHits.clear();
        int top = CONTENT_TOP + 20;
        int bottom = contentBottom() - 4;
        int left = indexX() + 4;
        int right = indexX() + indexWidth() - 4;
        graphics.enableScissor(left, top, right, bottom);
        int selectedHeading = selectedHeadingIndex();
        int y = top - (int) Math.round(indexScroll);
        for (int i = 0; i < headingAnchors.size(); i++) {
            HeadingAnchor anchor = headingAnchors.get(i);
            boolean selected = i == selectedHeading;
            boolean hovered = mouseX >= left && mouseX < right && mouseY >= y && mouseY < y + INDEX_ROW_HEIGHT;
            if (selected) {
                graphics.fill(left, y, right, y + INDEX_ROW_HEIGHT - 1, 0x444D3A5A);
            } else if (hovered) {
                graphics.fill(left, y, right, y + INDEX_ROW_HEIGHT - 1, 0x2CFFFFFF);
            }
            graphics.enableScissor(left + 3, y, right - 3, y + INDEX_ROW_HEIGHT);
            NexusConfigTextRenderer.draw(graphics, font, anchor.heading().title(), left + 4, y + 6, selected ? 0xF0D8FF : 0xB8B8B8, false);
            graphics.disableScissor();
            headingHits.add(new HeadingHit(i, left, right, y, y + INDEX_ROW_HEIGHT));
            y += INDEX_ROW_HEIGHT;
        }
        graphics.disableScissor();
        if (maxIndexScroll() > 0.0D) {
            int viewportHeight = Math.max(1, bottom - top);
            int content = headingAnchors.size() * INDEX_ROW_HEIGHT;
            int thumbHeight = Math.max(12, viewportHeight * viewportHeight / Math.max(viewportHeight, content));
            int thumbY = top + (int) Math.round((viewportHeight - thumbHeight) * (indexScroll / maxIndexScroll()));
            graphics.fill(right - 1, top, right + 1, bottom, 0x44202020);
            graphics.fill(right - 1, thumbY, right + 1, thumbY + thumbHeight, 0xAA8A8A8A);
        }
    }

    private void renderDetails(GuiGraphics graphics) {
        if (selectedRow < 0 || selectedRow >= rows.size()) {
            return;
        }
        NexusConfigValue<?> value = rows.get(selectedRow).value();
        int left = detailX() + 8;
        int right = detailX() + detailWidth() - 8;
        int textWidth = Math.max(40, right - left);
        int y = CONTENT_TOP + 8;
        y = NexusConfigTextRenderer.drawWrapped(graphics, font, value.labelText(), left, y, textWidth, 0xFFFFFF, TEXT_LINE_HEIGHT) + 5;
        if (!value.labels().isEmpty()) {
            y = renderStatusLabels(graphics, value.labels(), left, right, y) + 5;
        }
        if (!value.descriptionText().isEmpty()) {
            graphics.drawString(font, Component.translatable("config.nexuscore.detail.description"), left, y, 0xA0A0A0, false);
            y += 11;
            y = NexusConfigTextRenderer.drawWrapped(graphics, font, value.descriptionText(), left, y, textWidth, 0xD0D0D0, TEXT_LINE_HEIGHT) + 8;
        }
        graphics.drawString(font, Component.translatable("config.nexuscore.detail.current_value"), left, y, 0xA0A0A0, false);
        y += 11;
        y = NexusConfigTextRenderer.drawWrapped(graphics, font, value.currentValueText(), left, y, textWidth, 0xFFFFFF, TEXT_LINE_HEIGHT) + 8;
        NexusConfigText effect = value.currentEffectText();
        if (!effect.isEmpty()) {
            graphics.drawString(font, Component.translatable("config.nexuscore.detail.current_effect"), left, y, 0xA0A0A0, false);
            y += 11;
            NexusConfigTextRenderer.drawWrapped(graphics, font, effect, left, y, textWidth, 0xD8D8D8, TEXT_LINE_HEIGHT);
        }
    }

    private int renderStatusLabels(GuiGraphics graphics, List<NexusConfigStatusLabel> labels, int left, int right, int y) {
        int x = left;
        int lineY = y;
        for (NexusConfigStatusLabel label : labels) {
            int badgeWidth = font.width(label.text().component()) + 10;
            if (x > left && x + badgeWidth > right) {
                x = left;
                lineY += 16;
            }
            graphics.fill(x, lineY, Math.min(right, x + badgeWidth), lineY + 13, label.color());
            NexusConfigTextRenderer.draw(graphics, font, label.text(), x + 5, lineY + 3, 0xFFFFFF, false);
            x += badgeWidth + 4;
        }
        return lineY + 13;
    }

    private void renderVersion(GuiGraphics graphics) {
        if (pages.isEmpty()) {
            return;
        }
        Component version = Component.translatable("config.nexuscore.api_version", pages.get(pageIndex).config().configApiVersion());
        graphics.drawString(font, version, width - font.width(version) - 6, height - 12, 0x777777, false);
    }

    private void updateHoveredSelection(int mouseX, int mouseY) {
        int left = contentX();
        int right = contentRight();
        if (mouseX < left || mouseX >= right || mouseY < CONTENT_TOP || mouseY >= contentBottom()) {
            return;
        }
        int contentY = mouseY - CONTENT_TOP + (int) Math.round(scroll);
        for (int i = 0; i < rows.size(); i++) {
            ValueRow row = rows.get(i);
            if (contentY >= row.y() && contentY < row.y() + ROW_HEIGHT) {
                selectedRow = i;
                return;
            }
        }
    }

    private void updateWidgetPositions() {
        int top = CONTENT_TOP;
        int bottom = contentBottom();
        int offset = (int) Math.round(scroll);
        int x = editorX();
        int editorWidth = editorWidth();
        for (ValueRow row : rows) {
            int y = top + row.y() - offset + 5;
            row.editor().setX(x);
            row.editor().setY(y);
            row.editor().setWidth(editorWidth);
            row.editor().visible = y >= top && y + row.editor().getHeight() <= bottom;
        }
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_UP) {
            moveSelection(-1);
            return true;
        }
        if (keyCode == GLFW.GLFW_KEY_DOWN) {
            moveSelection(1);
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    private void moveSelection(int direction) {
        if (rows.isEmpty()) {
            return;
        }
        if (selectedRow < 0) {
            selectedRow = direction > 0 ? 0 : rows.size() - 1;
        } else {
            selectedRow = Math.max(0, Math.min(rows.size() - 1, selectedRow + direction));
        }
        ensureSelectedVisible();
        ensureSelectedHeadingVisible();
    }

    private void ensureSelectedVisible() {
        if (selectedRow < 0 || selectedRow >= rows.size()) {
            return;
        }
        ValueRow row = rows.get(selectedRow);
        double top = targetScroll;
        double bottom = top + contentViewportHeight();
        if (row.y() < top + 4) {
            targetScroll = row.y() - 4;
        } else if (row.y() + ROW_HEIGHT > bottom - 4) {
            targetScroll = row.y() + ROW_HEIGHT - contentViewportHeight() + 4;
        }
        targetScroll = clamp(targetScroll, 0.0D, maxContentScroll());
    }

    private void ensureSelectedHeadingVisible() {
        int selected = selectedHeadingIndex();
        if (selected < 0) {
            return;
        }
        int y = selected * INDEX_ROW_HEIGHT;
        int viewportHeight = indexViewportHeight();
        if (y < targetIndexScroll) {
            targetIndexScroll = y;
        } else if (y + INDEX_ROW_HEIGHT > targetIndexScroll + viewportHeight) {
            targetIndexScroll = y + INDEX_ROW_HEIGHT - viewportHeight;
        }
        targetIndexScroll = clamp(targetIndexScroll, 0.0D, maxIndexScroll());
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) {
            if (mouseY >= TAB_Y && mouseY < TAB_Y + TAB_HEIGHT) {
                for (TabHit hit : tabHits) {
                    if (mouseX >= hit.left() && mouseX < hit.right()) {
                        selectPage(hit.pageIndex());
                        return true;
                    }
                }
            }
            if (mouseX >= indexX() && mouseX < indexX() + indexWidth() && mouseY >= CONTENT_TOP && mouseY < contentBottom()) {
                for (HeadingHit hit : headingHits) {
                    if (mouseX >= hit.left() && mouseX < hit.right() && mouseY >= hit.top() && mouseY < hit.bottom()) {
                        scrollToHeading(hit.headingIndex());
                        return true;
                    }
                }
            }
            updateHoveredSelection((int) mouseX, (int) mouseY);
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        if (super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount)) {
            return true;
        }
        if (mouseY >= TAB_Y && mouseY <= TAB_Y + TAB_HEIGHT + 3 && maxTabScroll() > 0.0D) {
            targetTabScroll = clamp(targetTabScroll - verticalAmount * 48.0D, 0.0D, maxTabScroll());
            return true;
        }
        if (mouseX >= indexX() && mouseX < indexX() + indexWidth() && mouseY >= CONTENT_TOP && mouseY < contentBottom() && maxIndexScroll() > 0.0D) {
            targetIndexScroll = clamp(targetIndexScroll - verticalAmount * 36.0D, 0.0D, maxIndexScroll());
            return true;
        }
        if (mouseX >= contentX() && mouseX < contentRight() && mouseY >= CONTENT_TOP && mouseY < contentBottom() && maxContentScroll() > 0.0D) {
            targetScroll = clamp(targetScroll - verticalAmount * 46.0D, 0.0D, maxContentScroll());
            return true;
        }
        return false;
    }

    private void selectPage(int newPageIndex) {
        if (newPageIndex < 0 || newPageIndex >= pages.size() || newPageIndex == pageIndex) {
            return;
        }
        pageIndex = newPageIndex;
        selectedRow = -1;
        scroll = 0.0D;
        targetScroll = 0.0D;
        indexScroll = 0.0D;
        targetIndexScroll = 0.0D;
        rebuildWidgets();
    }

    private void scrollToHeading(int headingIndex) {
        if (headingIndex < 0 || headingIndex >= headingAnchors.size()) {
            return;
        }
        HeadingAnchor anchor = headingAnchors.get(headingIndex);
        targetScroll = clamp(anchor.y(), 0.0D, maxContentScroll());
        for (int i = 0; i < rows.size(); i++) {
            if (rows.get(i).headingId().equals(anchor.heading().id())) {
                selectedRow = i;
                break;
            }
        }
        ensureSelectedHeadingVisible();
    }

    private int selectedHeadingIndex() {
        if (selectedRow >= 0 && selectedRow < rows.size()) {
            String headingId = rows.get(selectedRow).headingId();
            for (int i = 0; i < headingAnchors.size(); i++) {
                if (headingAnchors.get(i).heading().id().equals(headingId)) {
                    return i;
                }
            }
        }
        double marker = scroll + 8.0D;
        int selected = headingAnchors.isEmpty() ? -1 : 0;
        for (int i = 0; i < headingAnchors.size(); i++) {
            if (headingAnchors.get(i).y() <= marker) {
                selected = i;
            } else {
                break;
            }
        }
        return selected;
    }

    private void smoothScrolls() {
        scroll = smooth(scroll, targetScroll);
        indexScroll = smooth(indexScroll, targetIndexScroll);
        tabScroll = smooth(tabScroll, targetTabScroll);
        if (Math.abs(scroll - targetScroll) < 0.1D) {
            scroll = targetScroll;
        }
        if (Math.abs(indexScroll - targetIndexScroll) < 0.1D) {
            indexScroll = targetIndexScroll;
        }
        if (Math.abs(tabScroll - targetTabScroll) < 0.1D) {
            tabScroll = targetTabScroll;
        }
    }

    private static double smooth(double current, double target) {
        return current + (target - current) * 0.28D;
    }

    private int contentBottom() {
        return Math.max(CONTENT_TOP + 20, height - FOOTER_HEIGHT);
    }

    private int contentViewportHeight() {
        return Math.max(1, contentBottom() - CONTENT_TOP);
    }

    private int indexViewportHeight() {
        return Math.max(1, contentBottom() - 4 - (CONTENT_TOP + 20));
    }

    private double maxContentScroll() {
        return Math.max(0.0D, contentHeight - contentViewportHeight());
    }

    private double maxIndexScroll() {
        return Math.max(0.0D, headingAnchors.size() * INDEX_ROW_HEIGHT - indexViewportHeight());
    }

    private double maxTabScroll() {
        int availableWidth = Math.max(1, width - 24);
        return Math.max(0.0D, tabTotalWidth() - availableWidth);
    }

    private int tabTotalWidth() {
        int total = 0;
        for (NexusConfigPage page : pages.stream().map(PageView::page).toList()) {
            total += tabWidth(page) + 3;
        }
        return Math.max(0, total - 3);
    }

    private int tabWidth(NexusConfigPage page) {
        return Math.max(54, font.width(page.title().component()) + 18);
    }

    private int indexX() {
        return 8;
    }

    private int indexWidth() {
        return width < 700 ? 96 : 132;
    }

    private int detailWidth() {
        return width < 700 ? 154 : 204;
    }

    private int detailX() {
        return width - detailWidth() - 8;
    }

    private int contentX() {
        return indexX() + indexWidth() + 8;
    }

    private int contentRight() {
        return detailX() - 8;
    }

    private int contentWidth() {
        return Math.max(80, contentRight() - contentX());
    }

    private int editorWidth() {
        return Math.max(72, Math.min(150, contentWidth() / 2));
    }

    private int editorX() {
        return contentRight() - editorWidth() - 7;
    }

    private static double clamp(double value, double minimum, double maximum) {
        return Math.max(minimum, Math.min(maximum, value));
    }

    @Override
    public void onClose() {
        if (minecraft != null) {
            minecraft.setScreen(parent);
        }
    }

    private record PageView(NexusJsonConfig config, NexusConfigPage page) {
    }

    private record ValueRow(NexusConfigValue<?> value, AbstractWidget editor, int y, String headingId) {
    }

    private record HeadingAnchor(NexusConfigHeading heading, int y) {
    }

    private record TabHit(int pageIndex, int left, int right) {
    }

    private record HeadingHit(int headingIndex, int left, int right, int top, int bottom) {
    }

    private enum LayoutKind {
        PAGE_DESCRIPTION, HEADING, SUBHEADING, VALUE
    }

    private record LayoutItem(LayoutKind kind, int y, int height, NexusConfigText text, NexusConfigHeading heading,
                              NexusConfigSubheading subheading, ValueRow row) {
        private static LayoutItem pageDescription(int y, int height, NexusConfigText text) {
            return new LayoutItem(LayoutKind.PAGE_DESCRIPTION, y, height, text, null, null, null);
        }

        private static LayoutItem heading(int y, int height, NexusConfigHeading heading) {
            return new LayoutItem(LayoutKind.HEADING, y, height, null, heading, null, null);
        }

        private static LayoutItem subheading(int y, int height, NexusConfigSubheading subheading) {
            return new LayoutItem(LayoutKind.SUBHEADING, y, height, null, null, subheading, null);
        }

        private static LayoutItem value(int y, int height, ValueRow row) {
            return new LayoutItem(LayoutKind.VALUE, y, height, null, null, null, row);
        }
    }
}
