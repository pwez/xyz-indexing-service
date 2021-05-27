package com.xyz.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.*;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.store.FSDirectory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

import static java.nio.file.Files.*;
import static java.nio.file.Paths.get;

@Slf4j(topic = "xyz-indexing-service")
@Component
@RequiredArgsConstructor
public class StartupEvent implements ApplicationListener<ApplicationReadyEvent> {

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        log.info("Initializing indexing ...");
        //Input folder
        var docsPath = "inputFiles";

        //Output folder
        var indexPath = "indexedFiles";

        //Input Path Variable
        final var docDir = get(docsPath);

        try {
            var dir = FSDirectory.open(get(indexPath));
            var analyzer = new StandardAnalyzer();
            var iwc = new IndexWriterConfig(analyzer);
            iwc.setOpenMode(IndexWriterConfig.OpenMode.CREATE_OR_APPEND);

            var writer = new IndexWriter(dir, iwc);
            indexDocs(writer, docDir);
            writer.close();
            log.info("Completed indexing");
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void indexDocs(final IndexWriter writer, Path path) throws IOException {
        if (isDirectory(path)) {
            walkFileTree(path, new SimpleFileVisitor<>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                    try {
                        indexDoc(writer, file, attrs.lastModifiedTime().toMillis());
                    }
                    catch (IOException ioe) {
                        ioe.printStackTrace();
                    }
                    return FileVisitResult.CONTINUE;
                }
            });
        }
        else
            indexDoc(writer, path, getLastModifiedTime(path).toMillis());
    }

    private static void indexDoc(IndexWriter writer, Path file, long lastModified) throws IOException {
        try (var stream = newInputStream(file)) {
            var doc = new Document();
            doc.add(new StringField("path", file.toString(), Field.Store.YES));
            doc.add(new LongPoint("modified", lastModified));
            doc.add(new TextField("contents", new String(readAllBytes(file)), Field.Store.YES));
            writer.updateDocument(new Term("path", file.toString()), doc);
        }
    }

}
