package net.microfalx.bootstrap.web.search;

import net.microfalx.bootstrap.dataset.annotation.DataSet;
import net.microfalx.bootstrap.help.annotation.Help;
import net.microfalx.bootstrap.model.AbstractAttribute;
import net.microfalx.bootstrap.model.Field;
import net.microfalx.bootstrap.search.Attribute;
import net.microfalx.bootstrap.search.SearchService;
import net.microfalx.bootstrap.web.dataset.DataSetController;
import net.microfalx.lang.StringUtils;
import net.microfalx.resource.MimeType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import java.io.IOException;
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
@DataSet(rawQuery = true, model = SearchResult.class, viewTemplate = "search/view", detailTemplate = "search/detail",
        viewClasses = "modal-xl", filterOperator = DEFAULT_FILTER_OPERATOR, filterQuoteChar = DEFAULT_FILTER_QUOTE_CHAR,
queryHelp = "/help/dataset/search_engine.html")
@Help("search")
public final class SearchController extends DataSetController<SearchResult, String> {

    @Autowired
    private SearchService searchService;

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
        String text = StringUtils.defaultIfEmpty(dataSetModel.getDescription(), dataSetModel.getName());
        if (MimeType.get(dataSetModel.getMimeType()).isText()) {
            try {
                text = dataSetModel.getBody().loadAsString();
            } catch (IOException e) {
                text = "Resource '" + dataSetModel.getBody().toURI() + "' is unavailable";
            }
        }
        controllerModel.addAttribute("text", text);
        fieldAttributes.sort(Comparator.comparing(AbstractAttribute::getLabel));
        controllerModel.addAttribute("badges", badgeAttributes);
        controllerModel.addAttribute("fields", fieldAttributes);
    }
}
