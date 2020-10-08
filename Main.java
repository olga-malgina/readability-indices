package readability;

import java.util.*;
import java.io.File;
import java.io.FileNotFoundException;

public class Main {

    public static void main(String[] args) {

        Text file = new Text(args[0]);
        file.getStats();
        file.printStats();
        System.out.println("Enter the score you want to calculate (ARI, FK, SMOG, CL, all): ");
        Scanner sc = new Scanner(System.in);
        String index = sc.next();

        IndexFactory factory = new IndexFactory();
        factory.chooseIndex(index, file.chars, file.words, file.sentences, file.syllables, file.polysyllables);
    }
}

class Text {
    String fileName;
    int syllables;
    int polysyllables;
    int chars;
    int words;
    int sentences;
    String text;

    public Text(String name) {
        fileName = name;
    }


    public void getText() {
        StringBuilder text = new StringBuilder();
        File file = new File(fileName);
        try (Scanner sc = new Scanner(file)) {
            while (sc.hasNext()) {
                text.append(sc.nextLine());
            }
        } catch (FileNotFoundException e) {
            System.out.println("Error - File not found: " + file);
        }
        this.text = String.valueOf(text);
    }

    public int getSentences() {
        return text.split("[.!?]+\\s").length;
    }

    // the field text of Text class has to be populated
    public String[] getWords() {
        //return text.split(" ");
        return this.text.split("[\\s]");
    }

    public int wordsCount() {
        return getWords().length;
    }

    public int charCount() {
        int count = 0;
        String[] words = getWords();
        for (String w : words) {
            count += w.length();
        }
        return count;
    }

    public int countSyllables(String word) {
        int count = 0;
        String[] chars = word.split("");
        for (int j = 0; j < chars.length; ++j) {
            if (chars[j].matches("[aeiouyAEIOU]")) {
                if (!(j == chars.length - 1 && chars[j].equals("e"))) {
                    ++count;
                    if (j != chars.length - 1 && chars[j + 1].matches("[aeiouyAEIOU]")) {
                        ++j;
                    }
                }
            }
        }
        return count;
    }

    public int getSyllables() {
        int count = 0;
        String[] words = text.split("[\\s.!?]+");
        for (String word : words) {
            if (word.charAt(word.length() - 1) == ',') {
                word.replace(',', ' ');
                word.trim();
            }
        }
        for (String word : words) {
            if (word.length() < 4 && word.length() > 0 || word.matches("[0-9]+[[,]{0,1}[0-9]]*")) {
                ++count;
                //System.out.println(word + " count: " + count);
            } else {
                count += countSyllables(word);
                //System.out.println(word + " count: " + count);
            }
        }

        return count;
    }

    public int getPolysyllables() {
        int count = 0;
        String[] words = text.split("[\\s.!?,]");
        for (String word : words) {
            if (word.length() > 3 && countSyllables(word) > 2) {
                ++count;
            }
        }
        return count;
    }

    public void getStats() {
        getText();
        this.sentences = getSentences();
        this.words = wordsCount();
        this.chars = charCount();
        this.syllables = getSyllables();
        this.polysyllables = getPolysyllables();

    }

    public void printStats() {
        System.out.println("Words: " + words);
        System.out.println("Sentences: " + sentences);
        System.out.println("Characters: " + chars);
        System.out.println("Syllables: " + syllables);
        System.out.println("Polysyllables: " + polysyllables);
    }
}

abstract class Index {
    float score;
    int group;
    String name;

    public int calcGroup() {
        int group;
        switch ((int) Math.round(score)) {
            case 1:
                group = 6;
                break;
            case 2:
                group = 7;
                break;
            case 3:
                group = 9;
                break;
            case 4:
                group = 10;
                break;
            case 5:
                group = 11;
                break;
            case 6:
                group = 12;
                break;
            case 7:
                group = 13;
                break;
            case 8:
                group = 14;
                break;
            case 9:
                group = 15;
                break;
            case 10:
                group = 16;
                break;
            case 11:
                group = 17;
                break;
            case 12:
                group = 18;
                break;
            case 13:
                group = 24;
                break;
            case 14:
                group = 25;
                break;
            default:
                group = 0;
        }
        return group;
    }

    public void printResult() {
        System.out.format("%s: %.2f (about %d year olds).%n", name, score, group);
    }

    abstract public void calcScore();
}

// not sure if I need this
class IndexFactory {
    public void chooseIndex(String index, int chars, int words, int sentences, int syllables, int polysyllables) {
        switch (index) {
            case "ARI":
                Index ari = new IndexARI(chars, words, sentences);
                ari.calcScore();
                ari.printResult();
                break;
            case "FK":
                Index fk = new IndexFK(words, sentences, syllables);
                fk.calcScore();
                fk.printResult();
                break;
            case "SMOG":
                Index smog = new IndexSMOG(polysyllables, sentences);
                smog.calcScore();
                smog.printResult();
                break;
            case "CL":
                Index cl = new IndexCL(chars, words, sentences);
                cl.calcScore();
                cl.printResult();
                break;
            case "all":
                Index ari2 = new IndexARI(chars, words, sentences);
                ari2.calcScore();
                ari2.printResult();
                Index fk2 = new IndexFK(words, sentences, syllables);
                fk2.calcScore();
                fk2.printResult();
                Index smog2 = new IndexSMOG(polysyllables, sentences);
                smog2.calcScore();
                smog2.printResult();
                Index cl2 = new IndexCL(chars, words, sentences);
                cl2.calcScore();
                cl2.printResult();
                float average = (float) (ari2.group + fk2.group + smog2.group + cl2.group) / 4;
                System.out.format("%nThis text should be understood in average by %.2f year olds.%n", average);
                break;
        }
    }
}

class IndexARI extends Index {

    int chars;
    int words;
    int sentences;

    public IndexARI(int chars, int words, int sentences) {
        this.chars = chars;
        this.words = words;
        this.sentences = sentences;
        this.name = "Automated Readability Index";
    }

    public void calcScore() {
        this.score = (float) (4.71 * ((float) this.chars / this.words)
                + 0.5 * ((float) this.words / this.sentences) - 21.43);
        this.group = calcGroup();
    }
}

class IndexFK extends Index {
    int words;
    int sentences;
    int syllables;

    public IndexFK(int words, int sentences, int syllables) {
        this.words = words;
        this.sentences = sentences;
        this.syllables = syllables;
        //StringBuilder name = new StringBuilder("Flesch");
        //name.append("\u8211");
        this.name = "Flesch–Kincaid readability tests";
    }

    public void calcScore() {
        this.score = (float) (0.39 * ((float) words / sentences) + 11.8 * ((float) syllables / words) - 15.59);
        this.group = calcGroup();
    }
}

class IndexSMOG extends Index {
    int polysyllables;
    int sentences;

    public IndexSMOG(int polysyllables, int sentences) {
        this.polysyllables = polysyllables;
        this.sentences = sentences;
        this.name = "Simple Measure of Gobbledygook";
    }

    public void calcScore() {
        this.score = (float) (1.043 * Math.sqrt((float) polysyllables * 30.0 / sentences) + 3.1291);
        this.group = calcGroup();
    }
}

class IndexCL extends Index {
    int chars;
    int sentences;
    int words;

    public IndexCL(int chars, int words, int sentences) {
        this.chars = chars;
        this.words = words;
        this.sentences = sentences;
        this.name = "Coleman–Liau index";
    }

    public void calcScore() {
        this.score = (float) (0.0588 * ((float) chars / words * 100) - 0.296 * ((float) sentences / words * 100) - 15.8);
        this.group = calcGroup();
    }
}