package bg.sofia.uni.fmi.mjt.sentimentanalyzer.document;

import bg.sofia.uni.fmi.mjt.sentimentanalyzer.SentimentScore;
import bg.sofia.uni.fmi.mjt.sentimentanalyzer.exceptions.SentimentAnalysisException;
import bg.sofia.uni.fmi.mjt.sentimentanalyzer.tokenizer.TextTokenizer;

import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;
import java.util.stream.Collectors;

public class DocumentAnalyzer implements Runnable {
    private Map<String, SentimentScore> result;
    private Queue<Document> documents;
    private final Set<String> stopwords;
    private final Map<String, SentimentScore> sentimentLexicon;
    private AtomicBoolean areAllDocumentsLoaded;

    public DocumentAnalyzer(Map<String, SentimentScore> result,
                            Queue<Document> documents,
                            Set<String> stopwords,
                            Map<String, SentimentScore> sentimentLexicon,
                            AtomicBoolean areAllDocumentsLoaded) {
        this.result = result;
        this.documents = documents;
        this.stopwords = stopwords;
        this.sentimentLexicon = sentimentLexicon;
        this.areAllDocumentsLoaded = areAllDocumentsLoaded;
    }

    @Override
    public void run() {
        while (true) {
            Document document = getDocumentFromQueue();

            if (document == null) {
                break;
            }

            String id = document.id();
            SentimentScore score = calculateSentimentScore(document);

            synchronized (result) {
                result.put(id, score);
            }
        }
    }

    private Document getDocumentFromQueue() {
        synchronized (documents) {
            while (documents.isEmpty()) {
                if (documents.isEmpty() && areAllDocumentsLoaded.get()) {
                    return null;
                }

                try {
                    documents.wait();
                } catch (InterruptedException e) {
                    throw new SentimentAnalysisException(e.getMessage(), e);
                }
            }
            return documents.poll();
        }
    }

    private SentimentScore calculateSentimentScore(Document document) {
        TextTokenizer tokenizer = new TextTokenizer(stopwords);
        int sumOfScores = tokenizer
                .tokenize(document.content())
                .stream()
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()))
                .entrySet()
                .stream()
                .filter(entry -> sentimentLexicon.containsKey(entry.getKey()))
                .collect(Collectors.toMap(entry -> entry.getKey(),
                        entry -> entry.getValue() * sentimentLexicon.get(entry.getKey()).getScore()))
                .values()
                .stream()
                .mapToInt(e -> e.intValue())
                .sum();

        return SentimentScore.fromScore(sumOfScores);
    }
}
