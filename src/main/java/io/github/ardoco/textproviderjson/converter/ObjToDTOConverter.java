package io.github.ardoco.textproviderjson.converter;

import io.github.ardoco.textproviderjson.DependencyType;
import io.github.ardoco.textproviderjson.dto.*;
import io.github.ardoco.textproviderjson.textobject.DependencyImpl;
import io.github.ardoco.textproviderjson.textobject.text.Phrase;
import io.github.ardoco.textproviderjson.textobject.text.Sentence;
import io.github.ardoco.textproviderjson.textobject.text.Text;
import io.github.ardoco.textproviderjson.textobject.text.Word;
import org.eclipse.collections.api.list.ImmutableList;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ObjToDTOConverter {

    public TextDTO convertTextToDTO(Text text) {
        TextDTO textDTO = new TextDTO();
        List<SentenceDTO> sentences = generateSentenceDTOs(text.getSentences());
        textDTO.setSentences(sentences);
        return textDTO;
    }

    private List<SentenceDTO> generateSentenceDTOs(ImmutableList<Sentence> sentences) {
        return sentences.stream().map(this::convertToSentenceDTO).toList();
    }

    private SentenceDTO convertToSentenceDTO(Sentence sentence) {
        SentenceDTO sentenceDTO = new SentenceDTO();
        sentenceDTO.setSentenceNo(sentence.getSentenceNumber());
        sentenceDTO.setText(sentence.getText());
        List<WordDTO> words = generateWordDTOs(sentence.getWords());
        sentenceDTO.setWords(words);
        String tree = convertToConstituencyTree(sentence.getPhrases());
        sentenceDTO.setConstituencyTree(tree);
        return sentenceDTO;
    }

    private List<WordDTO> generateWordDTOs(ImmutableList<Word> words) {
        return words.stream().map(this::convertToWordDTO).toList();
    }

    private WordDTO convertToWordDTO(Word word) {
        WordDTO wordDTO = new WordDTO();
        wordDTO.setId(word.getPosition());
        wordDTO.setText(word.getText());
        wordDTO.setLemma(word.getLemma());
        wordDTO.setPosTag(word.getPosTag());
        wordDTO.setSentenceNo(word.getSentenceNo());
        List<DependencyImpl> inDep = new ArrayList<>();
        List<DependencyImpl> outDep = new ArrayList<>();
        for (DependencyType depType: DependencyType.values()) {
            ImmutableList<Word> inDepWords = word.getIncomingDependencyWordsWithType(depType);
            inDep.addAll(inDepWords.stream().map(x -> new DependencyImpl(depType, x.getPosition())).toList());
            ImmutableList<Word> outDepWords = word.getOutgoingDependencyWordsWithType(depType);
            outDep.addAll(outDepWords.stream().map(x -> new DependencyImpl(depType, x.getPosition())).toList());
        }
        List<IncomingDependencyDTO> inDepDTO = generateDepInDTOs(inDep);
        List<OutgoingDependencyDTO> outDepDTO = generateDepOutDTOs(outDep);
        wordDTO.setIncomingDependencies(inDepDTO);
        wordDTO.setOutgoingDependencies(outDepDTO);
        return wordDTO;
    }

    private String convertToConstituencyTree(ImmutableList<Phrase> phrases) {
        List<String> trees = phrases.stream().map(this::convertToSubtree).toList();
        StringBuilder constituencyTree = new StringBuilder("(ROOT");
        for(String tree: trees) {
            constituencyTree.append(" ").append(tree);
        }
        constituencyTree.append(")");
        return constituencyTree.toString();
    }

    private String convertToSubtree(Phrase phrase) {
        StringBuilder constituencyTree = new StringBuilder("(");
        constituencyTree.append(phrase.getPhraseType().toString());
        List<Phrase> subphrases = phrase.getSubPhrases().castToList();
        List<Word> words = phrase.getContainedWords().castToList();
        // since we don't know the order of words and subphrases we have to reconstruct the order by comparing the word index
        while(!subphrases.isEmpty() || !words.isEmpty()) {
            if (subphrases.isEmpty()) {
                // word next
                Word word = words.remove(0);
                constituencyTree.append(" ").append(convertWordToTree(word));
                continue;
            } else if (words.isEmpty()) {
                // phrase next
                Phrase subphrase = subphrases.remove(0);
                constituencyTree.append(" ").append(convertToSubtree(subphrase));
                continue;
            }
            int wordIndex = words.get(0).getPosition();
            List<Integer> phraseWordIndices = words.stream().map(Word::getPosition).toList();
            if (wordIndex < Collections.max(phraseWordIndices)) {
                // word next
                Word word = words.remove(0);
                constituencyTree.append(" ").append(convertWordToTree(word));
            } else {
                // phrase next
                Phrase subphrase = subphrases.remove(0);
                constituencyTree.append(" ").append(convertToSubtree(subphrase));
            }
        }
        constituencyTree.append(")");
        return constituencyTree.toString();
    }

    private String convertWordToTree(Word word) {
        return "(" + word.getPosTag().toString() + " " + word.getText() + ")";
    }

    private List<IncomingDependencyDTO> generateDepInDTOs(List<DependencyImpl> dependencies) {
        return dependencies.stream().map(this::convertToDepInDTO).toList();
    }

    private List<OutgoingDependencyDTO> generateDepOutDTOs(List<DependencyImpl> dependencies) {
        return dependencies.stream().map(this::convertToDepOutDTO).toList();
    }

    private IncomingDependencyDTO convertToDepInDTO(DependencyImpl dependency) {
        IncomingDependencyDTO dependencyDTO = new IncomingDependencyDTO();
        dependencyDTO.setDependencyType(dependency.getDependencyType());
        dependencyDTO.setSourceWordId(dependency.getWordId());
        return dependencyDTO;
    }

    private OutgoingDependencyDTO convertToDepOutDTO(DependencyImpl dependency) {
        OutgoingDependencyDTO dependencyDTO = new OutgoingDependencyDTO();
        dependencyDTO.setDependencyType(dependency.getDependencyType());
        dependencyDTO.setTargetWordId(dependency.getWordId());
        return dependencyDTO;
    }
}

