package com.xyz.service;


import com.xyz.model.SearchResult;
import lombok.RequiredArgsConstructor;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.FuzzyQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.FSDirectory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static java.util.Comparator.comparing;

@RequiredArgsConstructor
@Component
public class SearchService {

    public List<SearchResult> search(String term) {
        var searchResults = new ArrayList<SearchResult>();
        try {
            var searcher = createSearcher();
            var foundDocs = searchInContent(term, searcher);
            for (var sd : foundDocs.scoreDocs) {
                var d = searcher.doc(sd.doc);
                System.out.println("Path : "+ d.get("path") + ", Score : " + sd.score);
                searchResults.add(new SearchResult(d.get("path"), sd.score));
            }
        }
        catch (Exception ignored) {}
        searchResults.sort(comparing(SearchResult::getScore).reversed());
        return searchResults;
    }

    public List<SearchResult> searchFuzzy(String fuzzyTerm) {
        var searchFuzzyResults = new ArrayList<SearchResult>();
        var term = new Term("contents", fuzzyTerm);
        var fuzzyQuery = new FuzzyQuery(term);
        try {
            var searcher = createSearcher();
            var docs = searcher.search(fuzzyQuery, 10);
            for (var doc : docs.scoreDocs) {
                var d = searcher.doc(doc.doc);
                System.out.println("Path : "+ d.get("path") + ", Score : " + doc.score);
                searchFuzzyResults.add(new SearchResult(d.get("path"), doc.score));
            }
        }
        catch (Exception ignored) {}
        searchFuzzyResults.sort(comparing(SearchResult::getScore).reversed());
        return searchFuzzyResults;
    }

    private TopDocs searchInContent(String textToFind, IndexSearcher searcher) throws Exception {
        var qp = new QueryParser("contents", new StandardAnalyzer());
        var query = qp.parse(textToFind);
        return searcher.search(query, 10);
    }

    private IndexSearcher createSearcher() throws IOException {
        var dir = FSDirectory.open(Paths.get("indexedFiles"));
        var reader = DirectoryReader.open(dir);
        return new IndexSearcher(reader);
    }

}
