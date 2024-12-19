package bg.sofia.uni.fmi.mjt.sentimentanalyzer.document;

import bg.sofia.uni.fmi.mjt.sentimentanalyzer.AnalyzerInput;
import bg.sofia.uni.fmi.mjt.sentimentanalyzer.exceptions.SentimentAnalysisException;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class DocumentLoader implements Runnable {
    private AnalyzerInput docInput;
    private Queue<Document> documents;
    private AtomicInteger loadedCount;
    private AtomicInteger documentsCount;
    private AtomicBoolean areAllDocumentsLoaded;

    public DocumentLoader(AnalyzerInput docInput,
                          Queue<Document> documents,
                          AtomicInteger loadedCount,
                          AtomicInteger documentsCount,
                          AtomicBoolean areAllDocumentsLoaded) {
        this.docInput = docInput;
        this.documents = documents;
        this.loadedCount = loadedCount;
        this.documentsCount = documentsCount;
        this.areAllDocumentsLoaded = areAllDocumentsLoaded;
    }

    @Override
    public void run() {
        synchronized (documents) {
            try (BufferedReader reader = new BufferedReader(docInput.inputReader())) {
                String content = reader.lines()
                        .collect(Collectors.joining());

                Document newDocument = new Document(docInput.inputID(), content);
                documents.offer(newDocument);

                if (loadedCount.incrementAndGet() == documentsCount.get()) {
                    areAllDocumentsLoaded.set(true);
                }

                documents.notifyAll();
            } catch (IOException e) {
                throw new SentimentAnalysisException(e.getMessage(), e);
            }
        }
    }
}
