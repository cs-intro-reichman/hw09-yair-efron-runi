import java.util.HashMap;
import java.util.Random;

public class LanguageModel {

    // The map of this model.
    // Maps windows to lists of charachter data objects.
    HashMap<String, List> CharDataMap;
    
    // The window length used in this model.
    int windowLength;
    
    // The random number generator used by this model. 
	private Random randomGenerator;

    /** Constructs a language model with the given window length and a given
     *  seed value. Generating texts from this model multiple times with the 
     *  same seed value will produce the same random texts. Good for debugging. */
    public LanguageModel(int windowLength, int seed) {
        this.windowLength = windowLength;
        randomGenerator = new Random(seed);
        CharDataMap = new HashMap<String, List>();
    }

    /** Constructs a language model with he given window length.
     * Generating texts from this model multiple times will produce
     * different random texts. Good for production. */
    public LanguageModel(int windowLength) {
        this.windowLength = windowLength;
        randomGenerator = new Random();
        CharDataMap = new HashMap<String, List>();
    }

    /** Builds a language model from the text in the given file (the corpus). */
	public void train(String fileName) {
		String window = "";
        char c;
        In in = new In(fileName);
        for (int i = 0; i < windowLength; i++) {
            window = window + in.readChar();
        }
        while (!in.isEmpty()) {
            c = in.readChar();
            if (!CharDataMap.containsKey(window)) {
                CharDataMap.put (window, new List ());
            }
            List probs = CharDataMap.get(window);
            probs.update(c);
            window = window.substring(1) + c;
        }
        for (List list : CharDataMap.values()) {
            calculateProbabilities(list);
        }
	}

    // Computes and sets the probabilities (p and cp fields) of all the
	// characters in the given list. */
	void calculateProbabilities(List probs) {				
		int numOfLetters = 0;
        CharData[] arr = probs.toArray();
        for (int i = 0 ; i < arr.length; i ++) {
            numOfLetters = numOfLetters + arr[i].count;
        }
        for (int i = 0 ; i < arr.length; i ++) {
            arr[i].p = (double) arr[i].count / numOfLetters;
        }
        for (int i = 0 ; i < arr.length; i ++) {
            if (i == 0) {
                arr[i].cp = arr[i].p;
            } else {
                arr[i].cp = arr[i - 1].cp + arr[i].p;
            }
        }

	}

    // Returns a random character from the given probabilities list.
	char getRandomChar(List probs) {
		double rand = randomGenerator.nextDouble();
        CharData[] arr = probs.toArray();
        for (int i = 0; i < arr.length; i++) {
            if (arr[i].cp > rand) {
                return arr[i].chr;
            }
        }
		return ' ';
	}

    /**
	 * Generates a random text, based on the probabilities that were learned during training. 
	 * @param initialText - text to start with. If initialText's last substring of size numberOfLetters
	 * doesn't appear as a key in Map, we generate no text and return only the initial text. 
	 * @param numberOfLetters - the size of text to generate
	 * @return the generated text
	 */
	public String generate(String initialText, int textLength) {
		if (initialText.length() < windowLength) {
            return initialText;
        }
        String ret = initialText;
        String window = initialText.substring (initialText.length() - windowLength);
        while (ret.length() < textLength || ret.charAt (ret.length() -1) != ' ') {
            if (!CharDataMap.containsKey(window)) {
                return ret;
            }
            char c = getRandomChar(CharDataMap.get(window));
            ret = ret + c ;
            window = window.substring(1) + c;
        }
        return ret;
	}

    /** Returns a string representing the map of this language model. */
	public String toString() {
		StringBuilder str = new StringBuilder();
		for (String key : CharDataMap.keySet()) {
			List keyProbs = CharDataMap.get(key);
			str.append(key + " : " + keyProbs + "\n");
		}
		return str.toString();
	}

}
