# rdf2pg
A java application to transform RDF databases into Property Graph (PG) databases.
rdf2pg implements three transformation methods described in the journal article "Mapping RDF Databases to Property Graph Databases" (IEEE Access, May 2020), which is available at https://ieeexplore.ieee.org/document/9088985

Usage:

## Simple instance mapping

$ java -jar rdf2pg -sdm <RDF_filename>

Output: instance.ypg (PG data)

## General database mapping (schema-independent)

$ java -jar rdf2pg -gdm <RDF_filename>

Output: instance.ypg (PG data) and schema.ypg (generic PG schema)

## Direct database mapping (schema-dependent)

$ java -jar rdf2pg -cdm <RDF_filename> <RDFS_filename>

Output: instance.ypg (PG data) and schema.ypg (PG schema)


## Direct database mapping (schema-dependent) --> Cypher Queries Output

$ java -jar rdf2pg -cdm <RDF_filename> <RDFS_filename> -neo4jQueries

Output:
[instance-queries-cdm.cypher](Output%2Fqueries%2Finstance-queries-cdm.cypher), [schema-queries-cdm.cypher](Output%2Fqueries%2Fschema-queries-cdm.cypher)

## Direct database mapping (schema-dependent) --> CSV and JSON Output (for Neo4j Admin Import) 

$ java -jar rdf2pg -cdm <RDF_filename> <RDFS_filename> -neo4jCsv

Output: 

[PG_NODES_PROPS_JSON.json](Output%2Fcsv%2FPG_NODES_PROPS_JSON.json), 
[PG_NODES_WD_LABELS.csv](Output%2Fcsv%2FPG_NODES_WD_LABELS.csv), 
[PG_PREFIX_MAP.csv](Output%2Fcsv%2FPG_PREFIX_MAP.csv),
[PG_RELATIONS.csv](Output%2Fcsv%2FPG_RELATIONS.csv)


## About the input and output files

rdf2pg uses Apache Jena to parse RDF files. Hence, rdf2pg allows the same RDF data formats supported by Jena.  

The output of rdf2pg is one or two files (depending of the mapping) containing property graph data encoded in the YARS-PG data format (https://github.com/lszeremeta/yarspg).

The directory "test" contains samples of input and output files. 

The rdf2pg API includes an interface named PGWriter which can be implemented to support other data formats. The use of PGWriter is very simple as it provides the methods WriteNode(PGNode node) and WriteEdge(PGEdge edge) which should be implemented with the corresponding instructions to write nodes and edges in the output file.
