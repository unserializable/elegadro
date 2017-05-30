package org.elegadro.rt.service.law.impl;

import org.elegadro.iota.legal.LawParticleEnum;
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
        "MATCH rada=(s:Seadus)-[:HAS*..]->(p:__ACT_SCOPE__)-[:HAS*..]->() " +
        "WHERE exists(p.__TEXTFIELD__) " +
        "AND p.__TEXTFIELD__ CONTAINS {ss} " +
        "RETURN rada;";

    private static final String UPPER_LEVEL_QS =
        "MATCH rada=(s:Seadus)-[:HAS*..]->() " +
        "WHERE exists(s.__TEXTFIELD__) " +
        "AND s.__TEXTFIELD__ CONTAINS {ss} " +
        "RETURN rada;";

    @Override
    public List<Path> textSearch(String needle, String langDir, LawParticleEnum actScope) {
        if (needle == null)
            return Collections.emptyList();

        needle = needle.trim();

        if (needle.isEmpty())
             return Collections.emptyList();

        String sharpNeedle = needle.toLowerCase();

        String sourceLang = langDir.split("_")[0];
        Map<String, Object> params = Collections.singletonMap("ss", sharpNeedle);

        List<Path> paths = new LinkedList<>();
        try (Session session = neo.session()) {
            String baseQuery = actScope.equals(LawParticleEnum.SEADUS) ? UPPER_LEVEL_QS : TEXT_SEARCH_QS;
            String sq = baseQuery.replaceAll("__TEXTFIELD__", "lc_tr_" + sourceLang);
            sq = sq.replaceAll("__ACT_SCOPE__", actScope.getLabel());
            StatementResult sr = session.run(sq, params);
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
