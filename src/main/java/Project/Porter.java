package Project;

public class Porter {
    class NewString {
        public String str;

        NewString() {
            str = "";
        }
    }
    public Porter() {
    }

    private String Clean(String str) {
        int last = str.length();
        Character ch = str.charAt(0);
        String temp = "";

        for(int i = 0; i < last; ++i) {
            if (Character.isLetterOrDigit(str.charAt(i))) {
                temp = temp + str.charAt(i);
            }
        }

        return temp;
    }

    private boolean hasSuffix(String word, String suffix, NewString stem) {
        String tmp = "";
        if (word.length() <= suffix.length()) {
            return false;
        } else if (suffix.length() > 1 && word.charAt(word.length() - 2) != suffix.charAt(suffix.length() - 2)) {
            return false;
        } else {
            stem.str = "";

            int i;
            for(i = 0; i < word.length() - suffix.length(); ++i) {
                String var10001 = stem.str;
                stem.str = var10001 + word.charAt(i);
            }

            tmp = stem.str;

            for(i = 0; i < suffix.length(); ++i) {
                tmp = tmp + suffix.charAt(i);
            }

            return tmp.compareTo(word) == 0;
        }
    }

    private boolean vowel(char ch, char prev) {
        switch (ch) {
            case 'a':
            case 'e':
            case 'i':
            case 'o':
            case 'u':
                return true;
            case 'y':
                switch (prev) {
                    case 'a':
                    case 'e':
                    case 'i':
                    case 'o':
                    case 'u':
                        return false;
                    default:
                        return true;
                }
            default:
                return false;
        }
    }

    private int measure(String stem) {
        int i = 0;
        int count = 0;
        int length = stem.length();

        while(i < length) {
            for(; i < length; ++i) {
                if (i > 0) {
                    if (this.vowel(stem.charAt(i), stem.charAt(i - 1))) {
                        break;
                    }
                } else if (this.vowel(stem.charAt(i), 'a')) {
                    break;
                }
            }

            ++i;

            for(; i < length; ++i) {
                if (i > 0) {
                    if (!this.vowel(stem.charAt(i), stem.charAt(i - 1))) {
                        break;
                    }
                } else if (!this.vowel(stem.charAt(i), '?')) {
                    break;
                }
            }

            if (i < length) {
                ++count;
                ++i;
            }
        }

        return count;
    }

    private boolean containsVowel(String word) {
        for(int i = 0; i < word.length(); ++i) {
            if (i > 0) {
                if (this.vowel(word.charAt(i), word.charAt(i - 1))) {
                    return true;
                }
            } else if (this.vowel(word.charAt(0), 'a')) {
                return true;
            }
        }

        return false;
    }

    private boolean cvc(String str) {
        int length = str.length();
        if (length < 3) {
            return false;
        } else if (!this.vowel(str.charAt(length - 1), str.charAt(length - 2)) && str.charAt(length - 1) != 'w' && str.charAt(length - 1) != 'x' && str.charAt(length - 1) != 'y' && this.vowel(str.charAt(length - 2), str.charAt(length - 3))) {
            if (length == 3) {
                return !this.vowel(str.charAt(0), '?');
            } else {
                return !this.vowel(str.charAt(length - 3), str.charAt(length - 4));
            }
        } else {
            return false;
        }
    }

    private String step1(String str) {
        NewString stem = new NewString();
        String tmp;
        int length;
        if (str.charAt(str.length() - 1) == 's') {
            if (!this.hasSuffix(str, "sses", stem) && !this.hasSuffix(str, "ies", stem)) {
                if (str.length() == 1 && str.charAt(str.length() - 1) == 's') {
                    str = "";
                    return str;
                }

                if (str.charAt(str.length() - 2) != 's') {
                    tmp = "";

                    for(length = 0; length < str.length() - 1; ++length) {
                        tmp = tmp + str.charAt(length);
                    }

                    str = tmp;
                }
            } else {
                tmp = "";

                for(length = 0; length < str.length() - 2; ++length) {
                    tmp = tmp + str.charAt(length);
                }

                str = tmp;
            }
        }

        if (this.hasSuffix(str, "eed", stem)) {
            if (this.measure(stem.str) > 0) {
                tmp = "";

                for(length = 0; length < str.length() - 1; ++length) {
                    tmp = tmp + str.charAt(length);
                }

                str = tmp;
            }
        } else if ((this.hasSuffix(str, "ed", stem) || this.hasSuffix(str, "ing", stem)) && this.containsVowel(stem.str)) {
            tmp = "";

            for(length = 0; length < stem.str.length(); ++length) {
                tmp = tmp + str.charAt(length);
            }

            str = tmp;
            if (tmp.length() == 1) {
                return tmp;
            }

            if (!this.hasSuffix(tmp, "at", stem) && !this.hasSuffix(tmp, "bl", stem) && !this.hasSuffix(tmp, "iz", stem)) {
                length = tmp.length();
                if (tmp.charAt(length - 1) == tmp.charAt(length - 2) && tmp.charAt(length - 1) != 'l' && tmp.charAt(length - 1) != 's' && tmp.charAt(length - 1) != 'z') {
                    tmp = "";

                    for(int i = 0; i < str.length() - 1; ++i) {
                        tmp = tmp + str.charAt(i);
                    }

                    str = tmp;
                } else if (this.measure(tmp) == 1 && this.cvc(tmp)) {
                    str = tmp + "e";
                }
            } else {
                str = tmp + "e";
            }
        }

        if (this.hasSuffix(str, "y", stem) && this.containsVowel(stem.str)) {
            tmp = "";

            for(length = 0; length < str.length() - 1; ++length) {
                tmp = tmp + str.charAt(length);
            }

            str = tmp + "i";
        }

        return str;
    }

    private String step2(String str) {
        String[][] suffixes = new String[][]{{"ational", "ate"}, {"tional", "tion"}, {"enci", "ence"}, {"anci", "ance"}, {"izer", "ize"}, {"iser", "ize"}, {"abli", "able"}, {"alli", "al"}, {"entli", "ent"}, {"eli", "e"}, {"ousli", "ous"}, {"ization", "ize"}, {"isation", "ize"}, {"ation", "ate"}, {"ator", "ate"}, {"alism", "al"}, {"iveness", "ive"}, {"fulness", "ful"}, {"ousness", "ous"}, {"aliti", "al"}, {"iviti", "ive"}, {"biliti", "ble"}};
        NewString stem = new NewString();

        for(int index = 0; index < suffixes.length; ++index) {
            if (this.hasSuffix(str, suffixes[index][0], stem) && this.measure(stem.str) > 0) {
                str = stem.str + suffixes[index][1];
                return str;
            }
        }

        return str;
    }

    private String step3(String str) {
        String[][] suffixes = new String[][]{{"icate", "ic"}, {"ative", ""}, {"alize", "al"}, {"alise", "al"}, {"iciti", "ic"}, {"ical", "ic"}, {"ful", ""}, {"ness", ""}};
        NewString stem = new NewString();

        for(int index = 0; index < suffixes.length; ++index) {
            if (this.hasSuffix(str, suffixes[index][0], stem) && this.measure(stem.str) > 0) {
                str = stem.str + suffixes[index][1];
                return str;
            }
        }

        return str;
    }

    private String step4(String str) {
        String[] suffixes = new String[]{"al", "ance", "ence", "er", "ic", "able", "ible", "ant", "ement", "ment", "ent", "sion", "tion", "ou", "ism", "ate", "iti", "ous", "ive", "ize", "ise"};
        NewString stem = new NewString();

        for(int index = 0; index < suffixes.length; ++index) {
            if (this.hasSuffix(str, suffixes[index], stem) && this.measure(stem.str) > 1) {
                str = stem.str;
                return str;
            }
        }

        return str;
    }

    private String step5(String str) {
        String stem;
        int i;
        if (str.charAt(str.length() - 1) == 'e') {
            if (this.measure(str) > 1) {
                stem = "";

                for(i = 0; i < str.length() - 1; ++i) {
                    stem = stem + str.charAt(i);
                }

                str = stem;
            } else if (this.measure(str) == 1) {
                stem = "";

                for(i = 0; i < str.length() - 1; ++i) {
                    stem = stem + str.charAt(i);
                }

                if (!this.cvc(stem)) {
                    str = stem;
                }
            }
        }

        if (str.length() == 1) {
            return str;
        } else {
            if (str.charAt(str.length() - 1) == 'l' && str.charAt(str.length() - 2) == 'l' && this.measure(str) > 1 && this.measure(str) > 1) {
                stem = "";

                for(i = 0; i < str.length() - 1; ++i) {
                    stem = stem + str.charAt(i);
                }

                str = stem;
            }

            return str;
        }
    }

    private String stripPrefixes(String str) {
        String[] prefixes = new String[]{"kilo", "micro", "milli", "intra", "ultra", "mega", "nano", "pico", "pseudo"};
        int last = prefixes.length;

        for(int i = 0; i < last; ++i) {
            if (str.startsWith(prefixes[i])) {
                String temp = "";

                for(int j = 0; j < str.length() - prefixes[i].length(); ++j) {
                    temp = temp + str.charAt(j + prefixes[i].length());
                }

                return temp;
            }
        }

        return str;
    }

    private String stripSuffixes(String str) {
        str = this.step1(str);
        if (str.length() >= 1) {
            str = this.step2(str);
        }

        if (str.length() >= 1) {
            str = this.step3(str);
        }

        if (str.length() >= 1) {
            str = this.step4(str);
        }

        if (str.length() >= 1) {
            str = this.step5(str);
        }

        return str;
    }

    public String stripAffixes(String str) {
        str = str.toLowerCase();
        str = this.Clean(str);
        if (str != "" && str.length() > 2) {
            str = this.stripPrefixes(str);
            if (str != "") {
                str = this.stripSuffixes(str);
            }
        }

        return str;
    }
}
