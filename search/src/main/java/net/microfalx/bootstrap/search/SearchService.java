package net.microfalx.bootstrap.search;

import net.microfalx.bootstrap.resource.ResourceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * A service used to execute full text searches.
 */
@Service
public class SearchService {

    @Autowired
    private SearchSettings configuration;

    @Autowired
    private ResourceService resourceService;

    /**
     * Search the index for all items matching the query.
     *
     * @param query the query to the index
     * @param start the start of first item
     * @param limit the highest amount of items returned
     * @return matching items
     * @throws SearchException if the query cannot be executed
     */
    public SearchResult search(String query, int start, int limit) {
        return null;
    }

    /**
     * Search the index for all items matching the query.
     *
     * @param query the query information
     * @return matching items
     * @throws SearchException if the query cannot be executed
     */
    public SearchResult search(SearchQuery query) {
        return null;
    }

    /**
     * Returns the description of the item with searched terms highlighted.
     *
     * @param query  the query
     * @param itemId the item id to highlight
     * @return the text
     */
    public String getHighlightedText(SearchQuery query, String itemId) {
        return null;
    }
}
