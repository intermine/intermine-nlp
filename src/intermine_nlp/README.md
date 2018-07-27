# core.clj
Core project namespace. Contains top-level parser pipeline functions (parser-pipeline).

Key functions: `parser-pipeline`, `-main`

# parse.clj
Functions for generating parses, based on the combination of a grammar file (resources/grammar.bnf)
and a model-specific set of productions generated on-the-fly. Also, transformation functions of
produced parse trees to facilitate full query translation.

Key function: `gen-parser`

# nlp.clj
NLP toolbox: an interface to tools from the OpenNLP and Stanford CoreNLP projects. Currently
only the tokenize and lemmatize functions are being used, although treebank parsring and chunking
may come into use soon for handling difficult queries.

Key function: `rand-query`

# model.clj
For fetching model data from a remote intermine database, as well as for loading and storing said
data locally.

Key function: `fetch-model`

# randquery.clj
For generating random PathQuery queries, used internally in order to automate the collection of
query translations from the development team. These query translations are being used to test and
design grammar parsing rules.

Key function: `rand-query`

# util.clj
Contains helper functions, mostly for fetching data from an intermine remote database.

Key functions: `possible-values`, `summaries`

# template.clj
For fetching template data from a remote intermine database, plus loading and storing (same as model.clj).

Key function: `fetch-templates`
