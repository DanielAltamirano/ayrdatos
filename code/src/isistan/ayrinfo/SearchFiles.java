package isistan.ayrinfo;

/**
 * Buscador simple
 * Análisis y recuperación de información
 * @author Marcelo Armentano
 */

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

public class SearchFiles {

    MovieSearch app;

    public SearchFiles(MovieSearch app) {
        this.app = app;
    }

    public void search(String indexPath, String queryString, String field) {

        boolean raw = true;
        int hitsPerPage = 10;

        IndexReader reader;
        try {
            reader = DirectoryReader
                    .open(FSDirectory.open(new File(indexPath)));

            IndexSearcher searcher = new IndexSearcher(reader);
            Analyzer analyzer = new StandardAnalyzer(Version.LUCENE_40);

            BufferedReader in = null;
            in = new BufferedReader(new InputStreamReader(System.in, "UTF-8"));

            QueryParser parser = new QueryParser(Version.LUCENE_40, field,
                    analyzer);

            Query query = parser.parse(queryString);
            System.out.println("Searching for: " + query.toString(field));

            doSearch(in, searcher, query, hitsPerPage, raw);

            reader.close();

        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (ParseException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

   
    public void doSearch(BufferedReader in,
            IndexSearcher searcher, Query query, int hitsPerPage, boolean raw)
            throws IOException {

        app.clearResults();
        // Collect enough docs to show 5 pages
        TopDocs results = searcher.search(query, hitsPerPage);

        int numTotalHits = results.totalHits;
        if (numTotalHits==0)
            return;
        results = searcher.search(query, numTotalHits);
        ScoreDoc[] hits = results.scoreDocs;
        app.setStatus("Total de documentos encontrados: "+numTotalHits);

        int start = 0;
        int end = numTotalHits;// Math.min(numTotalHits, hitsPerPage);

        for (int i = start; i < end; i++) {
            
            double score = hits[i].score;

            Document doc = searcher.doc(hits[i].doc);
            String path = doc.get("path");
            if (path==null)
                path="";
             app.newResult(score, path);
        }

    }
}
