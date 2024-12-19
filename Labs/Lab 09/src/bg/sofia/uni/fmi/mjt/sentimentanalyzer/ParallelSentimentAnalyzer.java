package bg.sofia.uni.fmi.mjt.sentimentanalyzer;

import bg.sofia.uni.fmi.mjt.sentimentanalyzer.document.Document;
import bg.sofia.uni.fmi.mjt.sentimentanalyzer.document.DocumentAnalyzer;
import bg.sofia.uni.fmi.mjt.sentimentanalyzer.document.DocumentLoader;
import bg.sofia.uni.fmi.mjt.sentimentanalyzer.exceptions.SentimentAnalysisException;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class ParallelSentimentAnalyzer implements SentimentAnalyzerAPI {
    private final int workersCount;
    private final Set<String> stopwords;
    private final Map<String, SentimentScore> sentimentLexicon;
    private Queue<Document> documents;
    /**
     * @param workersCount number of consumer workers
     * @param stopWords set containing stop words
     * @param sentimentLexicon map containing the sentiment lexicon,
     *                         where the key is the word and the value is the sentiment score
     */
    public ParallelSentimentAnalyzer(int workersCount,
                                     Set<String> stopWords,
                                     Map<String, SentimentScore> sentimentLexicon) {
        this.workersCount = workersCount;
        this.stopwords = stopWords;
        this.sentimentLexicon = sentimentLexicon;
        this.documents = new LinkedList<>();
    }

    @Override
    public Map<String, SentimentScore> analyze(AnalyzerInput... input) {
        Map<String, SentimentScore> result = new LinkedHashMap<>();

        AtomicInteger loadedCount = new AtomicInteger(0);
        AtomicInteger documentsCount = new AtomicInteger(input.length);
        AtomicBoolean areAllDocumentsLoaded = new AtomicBoolean(false);

        Thread[] workerThreads = new Thread[workersCount];
        for (int i = 0; i < workersCount; i++) {
            workerThreads[i] = createDocumentAnalyzerThread(result, areAllDocumentsLoaded);
            workerThreads[i].start();
        }

        for (AnalyzerInput docInput : input) {
            startDocumentLoaderThread(docInput, loadedCount, documentsCount, areAllDocumentsLoaded);
        }

        for (Thread thread : workerThreads) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                throw new SentimentAnalysisException(e.getMessage(), e);
            }
        }

        return result;
    }

    private Thread createDocumentAnalyzerThread(Map<String, SentimentScore> result,
                                                AtomicBoolean areAllDocumentsLoaded) {
        DocumentAnalyzer analyzer =
                new DocumentAnalyzer(result, documents, stopwords, sentimentLexicon, areAllDocumentsLoaded);

        return Thread.ofPlatform().unstarted(analyzer);
    }

    private void startDocumentLoaderThread(AnalyzerInput docInput,
                                         AtomicInteger loadedCount,
                                         AtomicInteger documentsCount,
                                         AtomicBoolean areAllDocumentsLoaded) {
        DocumentLoader loader =
                new DocumentLoader(docInput, documents, loadedCount, documentsCount, areAllDocumentsLoaded);
        Thread.ofVirtual().start(loader);
    }
}