package org.elegadro.rt.service.law;

import org.elegadro.rt.search.LawParagraphSearch;
import org.neo4j.driver.v1.types.Path;

import java.util.List;

/**
 * @author Taimo Peelo
 */
public interface RawIuraSearchService {
    List<Path> textSearch(String freeForm, String langDir);
    List<Path> lawParagraphSearch(List<LawParagraphSearch> lpSearch);
}
