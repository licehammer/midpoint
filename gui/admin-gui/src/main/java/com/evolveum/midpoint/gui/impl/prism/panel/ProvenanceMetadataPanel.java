/*
 * Copyright (c) 2020 Evolveum and contributors
 *
 * This work is dual-licensed under the Apache License 2.0
 * and European Union Public License. See LICENSE file for details.
 */
package com.evolveum.midpoint.gui.impl.prism.panel;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.PropertyModel;

import com.evolveum.midpoint.gui.api.GuiStyleConstants;
import com.evolveum.midpoint.gui.api.component.togglebutton.ToggleIconButton;
import com.evolveum.midpoint.gui.api.prism.wrapper.PrismContainerValueWrapper;
import com.evolveum.midpoint.gui.api.prism.wrapper.PrismContainerWrapper;
import com.evolveum.midpoint.gui.impl.factory.panel.ItemRealValueModel;
import com.evolveum.midpoint.web.component.prism.ItemVisibility;
import com.evolveum.midpoint.web.component.util.VisibleBehaviour;
import com.evolveum.midpoint.web.model.PrismContainerWrapperModel;
import com.evolveum.midpoint.xml.ns._public.common.common_3.ProvenanceAcquisitionType;
import com.evolveum.midpoint.xml.ns._public.common.common_3.ProvenanceMetadataType;
import com.evolveum.midpoint.xml.ns._public.common.common_3.ProvenanceYieldType;

public class ProvenanceMetadataPanel extends PrismContainerPanel<ProvenanceMetadataType> {

    private static final String ID_YIELD_HEADER = "yieldHeader";
    private static final String ID_YIELD = "yield";
    private static final String ID_ACQUISITION_HEADER = "acquisitionHeader";
    private static final String ID_ACQUISITIONS = "acquisitions";
    private static final String ID_ACQUISITION = "acquisition";
    private static final String ID_SHOW_MORE = "showMore";
    private static final String ID_DEFAULT_PANEL = "defaultPanel";

    /**
     * @param id
     * @param model
     * @param settings
     */
    public ProvenanceMetadataPanel(String id, IModel<PrismContainerWrapper<ProvenanceMetadataType>> model, ItemPanelSettings settings) {
        super(id, model, settings);
    }

    @Override
    protected boolean getHeaderVisibility() {
        return false;
    }

    @Override
    protected Component createValuePanel(ListItem<PrismContainerValueWrapper<ProvenanceMetadataType>> item) {
        WebMarkupContainer customPanel = createHeader(item.getModel());
        item.add(customPanel);
        item.setOutputMarkupId(true);
        return customPanel;
    }

    private WebMarkupContainer createHeader(IModel<PrismContainerValueWrapper<ProvenanceMetadataType>> model) {
        WebMarkupContainer container = new WebMarkupContainer(ID_YIELD_HEADER);
        container.setOutputMarkupId(true);
        container.setOutputMarkupPlaceholderTag(true);

        PrismContainerWrapperModel yieldModel = PrismContainerWrapperModel.fromContainerValueWrapper(model, ProvenanceMetadataType.F_YIELD);
        ListView<PrismContainerValueWrapper<ProvenanceYieldType>> yield =
                new ListView<PrismContainerValueWrapper<ProvenanceYieldType>>(ID_YIELD, new PropertyModel<>(yieldModel, "values")) {

            @Override
            protected void populateItem(ListItem<PrismContainerValueWrapper<ProvenanceYieldType>> listItem) {
                WebMarkupContainer panel = createAcquisitionPanel(PrismContainerWrapperModel.fromContainerValueWrapper(listItem.getModel(), ProvenanceYieldType.F_ACQUISITION));
                listItem.add(panel);

                ToggleIconButton<Void> showMore = new ToggleIconButton<Void>(ID_SHOW_MORE,
                        GuiStyleConstants.CLASS_ICON_EXPAND_CONTAINER, GuiStyleConstants.CLASS_ICON_COLLAPSE_CONTAINER) {

                    @Override
                    public boolean isOn() {
                        return listItem.getModelObject().isShowEmpty();
                    }

                    @Override
                    public void onClick(AjaxRequestTarget ajaxRequestTarget) {
                        PrismContainerValueWrapper<ProvenanceYieldType> modelObject = listItem.getModelObject();
                        modelObject.setShowEmpty(!modelObject.isShowEmpty());
                        ajaxRequestTarget.add(ProvenanceMetadataPanel.this);
                    }
                };

                showMore.setEnabled(true);
                showMore.setOutputMarkupId(true);
                showMore.setOutputMarkupPlaceholderTag(true);
                listItem.add(showMore);

                ItemPanelSettings settings = getSettings().copy();
                settings.setVisibilityHandler(w -> ItemVisibility.AUTO);
                Component defaultPanel = new MetadataContainerValuePanel<>(ID_DEFAULT_PANEL, listItem.getModel(), settings);
                defaultPanel.setOutputMarkupPlaceholderTag(true);
                defaultPanel.setOutputMarkupId(true);
                defaultPanel.add(new VisibleBehaviour(() -> listItem.getModelObject().isShowEmpty()));
                listItem.add(defaultPanel);
            }
        };

        yield.setOutputMarkupId(true);
        container.add(yield);

        return container;
    }

    private WebMarkupContainer createAcquisitionPanel(IModel<PrismContainerWrapper<ProvenanceAcquisitionType>> listPropertyModel) {
        WebMarkupContainer container = new WebMarkupContainer(ID_ACQUISITION_HEADER);

        ListView<PrismContainerValueWrapper<ProvenanceAcquisitionType>> acquisition =
                new ListView<PrismContainerValueWrapper<ProvenanceAcquisitionType>>(ID_ACQUISITIONS, new PropertyModel<>(listPropertyModel, "values")) {

            @Override
            protected void populateItem(ListItem<PrismContainerValueWrapper<ProvenanceAcquisitionType>> listItem) {
                ProvenanceAcquisitionHeaderPanel panel = new ProvenanceAcquisitionHeaderPanel(ID_ACQUISITION, new ItemRealValueModel<>(listItem.getModel()));
                panel.setOutputMarkupId(true);
                listItem.add(panel);
            }
        };
        container.add(acquisition);
        return container;

    }
}
