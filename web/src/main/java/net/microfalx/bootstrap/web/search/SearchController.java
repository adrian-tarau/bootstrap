package net.microfalx.bootstrap.web.search;

import net.microfalx.bootstrap.content.Content;
import net.microfalx.bootstrap.content.ContentLocator;
import net.microfalx.bootstrap.content.ContentService;
import net.microfalx.bootstrap.dataset.annotation.DataSet;
import net.microfalx.bootstrap.help.annotation.Help;
import net.microfalx.bootstrap.model.AbstractAttribute;
import net.microfalx.bootstrap.model.Attributes;
import net.microfalx.bootstrap.model.Field;
import net.microfalx.bootstrap.search.Attribute;
import net.microfalx.bootstrap.search.SearchService;
import net.microfalx.bootstrap.web.dataset.DataSetController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;

import static net.microfalx.bootstrap.model.AttributeConstants.DEFAULT_MAXIMUM_ATTRIBUTES;
import static net.microfalx.bootstrap.model.AttributeUtils.shouldDisplayAsBadge;
import static net.microfalx.bootstrap.search.SearchUtils.DEFAULT_FILTER_OPERATOR;
import static net.microfalx.bootstrap.search.SearchUtils.DEFAULT_FILTER_QUOTE_CHAR;

@Controller
@RequestMapping(value = "/search")
@DataSet(rawQuery = true, trend = true, model = SearchResult.class, viewTemplate = "search/view", detailTemplate = "search/detail",
        viewClasses = "modal-xl", filterOperator = DEFAULT_FILTER_OPERATOR, filterQuoteChar = DEFAULT_FILTER_QUOTE_CHAR,
        queryHelp = "/help/dataset/search_engine.html", tags = {"ai"})
@Help("search")
public final class SearchController extends DataSetController<SearchResult, String> {

    @Autowired
    private SearchService searchService;

    @Autowired
    private ContentService contentService;

    @Override
    protected void beforeView(net.microfalx.bootstrap.dataset.DataSet<SearchResult, Field<SearchResult>, String> dataSet,
                              Model controllerModel, SearchResult dataSetModel) {
        Collection<Attribute> attributes = dataSetModel.getAttributes();
        List<Attribute> badgeAttributes = new ArrayList<>();
        List<Attribute> fieldAttributes = new ArrayList<>();
        int maxBadges = 2 * DEFAULT_MAXIMUM_ATTRIBUTES;
        for (Attribute attribute : attributes) {
            if (shouldDisplayAsBadge(attribute, true) && maxBadges-- > 0) {
                badgeAttributes.add(attribute);
            }
            fieldAttributes.add(attribute);
        }
        Content content = contentService.resolve(ContentLocator.create(getContentAttributes(dataSetModel)));
        controllerModel.addAttribute("body", content);
        fieldAttributes.sort(Comparator.comparing(AbstractAttribute::getLabel));
        List<Attribute> finalFields = new ArrayList<>();
        finalFields.addAll(dataSetModel.getCoreAttributes());
        finalFields.addAll(fieldAttributes);
        controllerModel.addAttribute("badges", badgeAttributes);
        controllerModel.addAttribute("fields", finalFields);
    }

    private Attributes<?> getContentAttributes(SearchResult dataSetModel) {
        Attributes<net.microfalx.bootstrap.model.Attribute> contentAttributes = Attributes.create();
        contentAttributes.add(Content.ID_ATTR, dataSetModel.getId());
        contentAttributes.add(Content.NAME_ATTR, dataSetModel.getName());
        contentAttributes.add(Content.DESCRIPTION_ATTR, dataSetModel.getDescription());
        contentAttributes.add(Content.MIME_TYPE_ATTR, dataSetModel.getMimeType());
        contentAttributes.add(Content.TYPE_ATTR, dataSetModel.getType());
        contentAttributes.add(Content.URI_ATTR, dataSetModel.getBodyUri());
        return contentAttributes;
    }
}
