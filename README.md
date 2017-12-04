# Elegadro

## Quick start

For instructions about running Elegadro PoC locally, skip to
[Local execution](#local-execution) section.

For online tryout, visit Elegadro website at
[http://elegadro.com](http://elegadro.com).

## Concepts
Elegadro is e-legal PoC with legal documents (currently Estonian Laws) stored
in a [graph](https://en.wikipedia.org/wiki/Graph_%28discrete_mathematics%29)
database [Neo4J](https://neo4j.com/). Compared to relational databases, graphs
are still neatly structured, but relationships between vertices can be defined
more flexibly. This is beneficial, for lawyers reference over many different
sources and documents, producing 'ad-hoc' relationships that are tiresome to
model in the relational databases and result in overabundance of [relational
joins](https://en.wikipedia.org/wiki/Relational_database#Relational_operations)
which are bound to become both computationally expensive and unwieldy for
representing as legible SQL query expressions.

Since local edges are straightforward to trace from the vertices, hope is that
with the quality data, localized legal context can be narrowed down effectively.
E.g. in [Cypher](https://en.wikipedia.org/wiki/Cypher_Query_Language) graph
query language, an expression for finding adjacent vertices (types unconsidered)
from a particular vertice _s_ representing a law called 'Võlaõigusseadus' can be
represented as simply as:

```
MATCH (s:Seadus)-[]->(n)
WHERE s.text='Võlaõigusseadus'
RETURN n;
```
The highlight here is the fact that _all_ kinds of relationships from _s_ to
adjacent vertices are found that way, without JOIN explosions (which first
have to be known about, to represent them in the query and later return them).

In this PoC, graph structure is formed of Estonian Laws that are referenced
often enough to have [acronyms](https://www.riigiteataja.ee/lyhendid.html)
that are often semi-familiar to laymen (and maybe official to legalists?).
Vertices are the elements of processed legislature, edges represent the element
relations, with just one type of edge relation (`[:HAS]`) present in the PoC.
That one relation type provides simple way to return _structural_ results from
_simple text searches_.

## Legal Content Sources

At the beginning of the project,  idea was entertained of scraping legal
content from [Riigi Teataja (RT)](https://www.riigiteataja.ee) website HTML
representations of legal acts, considered this advantageous as RT content
processor appears to have some workarounds in place to display erroneous quirks
of legal source XML files that are available both from Riigi Teataja ('Laadi
alla' -> 'XML failina') and from
[avaandmed.rik.ee/andmed/ERT](http://avaandmed.rik.ee/andmed/ERT/). Examples of
the XML errors that RT is working around include numbering fixes and e.g. empty
part 6 in VÕS that gets attached paragraphs 578 and 579, even though it appears
fully empty in the VÕS XML document, as in the snippet below:

```
<osa id="osa6">
  <osaNr>6</osaNr>
  <kuvatavNr><![CDATA[6. osa]]></kuvatavNr>
  <osaPealkiri>KOMPROMISSILEPING</osaPealkiri>
</osa>

```

However, due to weak semantics in the RT website HTML, parsing XML sources
proved more fruitful, which means that some numbering quirks and content
placement errors that are present in legal XML files, but worked
around in RT website HTML representation, do remain present in this PoC.

XML sources for Estonian Laws were parsed with legal XML Schema (_tvviseadus_
Schema available from [RIHA](https://riha.eesti.ee) published
[XML vara: Õigusakt](https://riha.eesti.ee/riha/main/xml/oigusakt) sections.

## Features

In its present form, PoC includes two runnable and/or deployable artifacts,
which provide following features:
1. `redpill`
  * runnable artifact (`jar`)
  * (re)-creates Neo4J GDB, if not present or data missing
  * fetches RT XML sources for legal acts with acronyms
  * validates legal acts according to _tyviseadus_ XML Schema
  * parses legal act XML into Java object representation (see `iota` package)
  * converts object representations into graph nodes and relation(s)
  * stores the legal content nodes and relations into Neo4J GDB
  * brings up Neo4J instance to be used by `web-poc` or other local clients
2. `web-poc`
  * deployable artifact (`war`)
  * web front-end for performing simple queries against running database
  * returns structural results
  * two types of queries:
    1. text search, returning structured results from the legal acts in order
       of relevance, e.g.:
       * tähtaeg (matches 81 legal acts, most relevant VÕS, collapsed)
       * andmevahetus (matches 5 legal acts, most relevant MaaKatS, collapsed)
       * koduloom (match from 1 legal act, expanded)
    2. "actronym" based search for paragraphs from one or more laws, e.g.:
       * KrMS 179-180 KrMS 237-238 KarS 68-69 (returns particular paragraphs)

## Functional artifacts

Notable non-runnable and non-deployable artifacts included in the PoC are:

* `tyvi-law` -- XML Schema compiled to visitable Java types
* `iota` -- Java type definitions for legal act representations
* `iota-parser` -- tyviakt XML to iota representation conversion tools

At the moment of writing, these three artifacts are, to the best of my
knowledge, only open source Java libraries available for processing Estonian
legal acts.

## Extension and expansion
* With extensions, the legal iotas can be made to fully support  _määrus_,
  _muutmismäärus_, _muutmisseadus_, _riigikogu otsus_ and possibly other
  _structured_ legal act types.
* Less structured legal acts can be parsed after establishing the notions about
  supported structured legal act types, to extract the connections with
  structured acts.
* With additional _relation_ type definitions, these less structured legal
  documents, like court rulings, can then be connected with the legal acts.
* With additional relations established and stored in the graph database,
  particular knowledge extraction, together with the use of natural language
  processing toolkits, should provide possibilities for much more powerful
  searches and knowledge extraction.


## Local execution

### Prerequisites

Local code fetch, compilation and execution requires installations of:

  * [Git](https://git-scm.com/)
  * Java 8+ JDK e.g. [Oracle JDK](http://www.oracle.com/technetwork/java/javase/downloads/index.html)
  * Maven 3.3.3+ [Apache Maven](https://maven.apache.org/)

### Code check-out

Code should be checked out with `git` to `elegadro` folder

* via HTTPS
    ```
    git clone https://github.com/unserializable/elegadro.git
    ```
* via SSH
    ```
    git clone git@github.com:unserializable/elegadro.git
    ```

### Compiling the PoC components locally

In checked out `elegadro` folder run
```
mvn clean install
```

### Initializing and running Elegadro PoC graph database locally

Note that initialization of Elegadro graph database requires internet connection to be
present. Open separate console in `elegadro/redpill` folder and execute:
```
java -jar target/redpill-0.4.0-SNAPSHOT.jar
```

Wait until program gives a message about database having been started. On the
first run, console output should look similar to following:

```
Detected writable path '/somewere/somefolder'.
2017-02-05 18:52:25.778+0000 INFO  Starting...
2017-02-05 18:52:27.019+0000 INFO  Bolt enabled on localhost:7687.
2017-02-05 18:52:30.662+0000 INFO  Started.
2017-02-05 18:52:31.888+0000 INFO  Remote interface available at http://localhost:7474/
This appears to be first start of this database instance, attempting to bring the laws (~365), stand by.
https://www.riigiteataja.ee/akt/105122014039.xml (AVRS) downloaded
Acquired AVRS ('Abieluvararegistri seadus')
Persisting into graphdb ... done 0.555s
https://www.riigiteataja.ee/akt/117122015033.xml (APolS) downloaded
Acquired APolS ('Abipolitseiniku seadus')
Persisting into graphdb ... done 0.198s
https://www.riigiteataja.ee/akt/122062016023.xml (AdvS) downloaded
Acquired AdvS ('Advokatuuriseadus')
... (omitted output until startup message) ...
Initialized Elegadro PoC database and started, run web-poc for simple demonstration client.
```

After database is started, it can be browsed with locally running web front-end
that allows executing and visualizing Cypher query results and also accessed
with Elegadro web front-end PoC.

### Running Elegadro web fronted PoC locally

After starting Elegadro database, open separate console in `elegadro/web-poc`
folder and execute:
```
    mvn jetty:run
```

After the log message:
```
[INFO] Started Jetty Server
```
appears, `web-poc` running against the local database is accessible from browser
via: [http://localhost:8080/ele](http://localhost:8080/ele)

## License

Elegadro code is available for use under
[GPLv3](https://www.gnu.org/licenses/gpl.txt) license, (C) 2017 Taimo Peelo
