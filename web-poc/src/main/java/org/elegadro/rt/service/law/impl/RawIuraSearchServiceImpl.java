package org.elegadro.rt.service.law.impl;

import org.elegadro.rt.search.LawParagraphSearch;
import org.elegadro.rt.search.SearchUtil;
import org.elegadro.rt.service.law.RawIuraSearchService;
import lombok.extern.slf4j.Slf4j;
import org.neo4j.driver.v1.Driver;
import org.neo4j.driver.v1.Record;
import org.neo4j.driver.v1.Session;
import org.neo4j.driver.v1.StatementResult;
import org.neo4j.driver.v1.types.Path;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * @author Taimo Peelo
 */
@Slf4j
@Repository
public class RawIuraSearchServiceImpl implements RawIuraSearchService {
    @Autowired
    private Driver neo;

    private static final String TEXT_SEARCH_QS =
        "MATCH rada=(s:Seadus)-[:HAS*..]->(p:Paragrahv)-[:HAS*..]->() " +
        "WHERE exists (p.text) " +
        "AND p.text CONTAINS {ss} " +
        "RETURN rada;";

    @Override
    public List<Path> textSearch(String freeForm) {
        if (freeForm == null)
            return Collections.emptyList();

        freeForm = freeForm.trim();

         if (freeForm.isEmpty())
             return Collections.emptyList();

        Map<String, Object> params = Collections.singletonMap("ss", freeForm);

        List<Path> paths = new LinkedList<>();
        try (Session session = neo.session()) {
            StatementResult sr = session.run(TEXT_SEARCH_QS, params);
            while (sr.hasNext()) {
                Record next = sr.next();
                Path path = next.get("rada").asPath();
                paths.add(path);
            }
        }

        return paths;
    }

    @Override
    public List<Path> lawParagraphSearch(List<LawParagraphSearch> lpSearch) {
        if (lpSearch == null || lpSearch.isEmpty())
            return Collections.emptyList();

        String qs = SearchUtil.toLawParagraphQueryString(lpSearch);
        if (log.isTraceEnabled()) {
            log.trace("Executing query " + qs);
        }

        List<Path> paths = new LinkedList<>();
        try (Session session = neo.session()) {
            StatementResult sr = session.run(qs);
            while (sr.hasNext()) {
                Record next = sr.next();
                Path path = next.get("rada").asPath();
                paths.add(path);
            }
        }

        return paths;
    }
}
