#!/usr/bin/env python
import sys from nltk.tokenize
import wordpunct_tokenize
def read_input(file):
    for line in file:
        # split the line into tokens
        yield wordpunct_tokenize(line)

def main(separator='\t'):
    # input comes from STDIN (standard input) 
    data = read_input(sys.stdin) 
    for tokens in data: 
        # write the results to STDOUT (standard output); 
        # what we output here will be the input for the 
        # Reduce step, i.e. the input for reducer.py 
        # 
        # tab-delimited; the trivial token count is 1 for token in tokens: 
        print '%s%s%d' % (word, separator, 1) 

if __name__ == "__main__":
    main()

