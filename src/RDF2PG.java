/*
 * Copyright 2020 Renzo Angles (http://renzoangles.com/)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import maps.complete.CompleteMapping;
import maps.generic.GenericMapping;
import maps.simple.SimpleMapping;
import pgraph.PropertyGraph;

import java.io.*;
import java.nio.charset.StandardCharsets;

public class RDF2PG {

    public static void main(String[] args) {
        long itime;
        long etime;
        //BasicConfigurator.configure(); //to avoid log4j warning
        System.out.println("rdf2pg");
        System.out.println("Java app to transform an RDF database into a Property Graph database (i.e. schema and instance data).");
        if (args.length == 2) {
            String opt = String.valueOf(args[0]);
            String input_filename = String.valueOf(args[1]);
            itime = System.currentTimeMillis();
            if (opt.compareTo("-sdm") == 0) {
                System.out.println("Running simple database mapping");
                SimpleMapping smap = new SimpleMapping();
                smap.run(input_filename);
                System.out.println("Output: sudentExample-instance.ypg");
            } else if (opt.compareTo("-gdm") == 0) {
                System.out.println("Running generic database mapping");
                GenericMapping gdm = new GenericMapping();
                gdm.run(input_filename);
                PropertyGraph schema = gdm.getPGSchema();
                schema.exportAsYPG("sudentExample-schema.ypg");
                System.out.println("Output: sudentExample-instance.ypg and sudentExample-schema.ypg");
            } else {
                System.out.println("Invalid option");
            }
            etime = System.currentTimeMillis() - itime;
            System.out.println("Execution time: " + etime + " ms \n");

        } else if (args.length == 3) {
            itime = System.currentTimeMillis();
            String opt = String.valueOf(args[0]);
            String rdf_filename = String.valueOf(args[1]);
            String rdfs_filename = String.valueOf(args[2]);
            if (opt.compareTo("-cdm") == 0) {
                System.out.println("Running complete database mapping");
                CompleteMapping cdm = new CompleteMapping();
                cdm.run(rdf_filename, rdfs_filename);
                System.out.println("Output: sudentExample-instance.ypg and sudentExample-schema.ypg");
            } else {
                System.out.println("Invalid option");
            }
            etime = System.currentTimeMillis() - itime;
            System.out.println("Execution time: " + etime + " ms \n");

        } else if (args.length == 4) {
            itime = System.currentTimeMillis();
            String opt = String.valueOf(args[0]);
            String rdf_filename = String.valueOf(args[1]);
            String rdfs_filename = String.valueOf(args[2]);
            String neo4j_flag = String.valueOf(args[3]);
            if (opt.compareTo("-cdm") == 0 && neo4j_flag.compareTo("-neo4jCsv") == 0) {
                System.out.println("Hello, Running complete database mapping with Neo4j CSV output");
                Neo4jCsvWriter instance_pgWriter = new Neo4jCsvWriter();
                Neo4jCsvWriter schema_pgWriter = new Neo4jCsvWriter();
                CompleteMapping cdm = new CompleteMapping();
                cdm.run(rdf_filename, rdfs_filename, instance_pgWriter, schema_pgWriter);
                printPrefixes(cdm);
            } else if (opt.compareTo("-cdm") == 0 && neo4j_flag.compareTo("-neo4jQueries") == 0) {
                System.out.println("Hello, Running complete database mapping with Neo4j Query output");
                Neo4jWriter instance_pgWriter = new Neo4jWriter("instance-queries-cdm.cypher");
                Neo4jWriter schema_pgWriter = new Neo4jWriter("schema-queries-cdm.cypher");
                CompleteMapping cdm = new CompleteMapping();
                cdm.run(rdf_filename, rdfs_filename, instance_pgWriter, schema_pgWriter);
                printPrefixes(cdm);
            } else {
                System.out.println("Invalid option");
            }
            etime = System.currentTimeMillis() - itime;
            System.out.println("Execution time: " + etime + " ms \n");

        } else {
            System.out.println("Usage:");
            System.out.println("// Simple database mapping");
            System.out.println("$ java -jar rdf2pg -sdm <RDF_filename>");
            System.out.println("// General database mapping (schema-independent)");
            System.out.println("$ java -jar rdf2pg -gdm <RDF_filename>");
            System.out.println("// Complete database mapping (schema-dependent)");
            System.out.println("$ java -jar rdf2pg -cdm <RDF_filename> <RDFS_filename>");
            return;
        }
    }

    private static void printPrefixes(CompleteMapping cdm) {
        //write prefixes to a csv file
        try {
            Writer prefixWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("PG_PREFIX_MAP.csv"), StandardCharsets.UTF_8));
            prefixWriter.write("prefix,iri\n");
            for (String key : cdm.getPrefixes().keySet()) {
                prefixWriter.write(key + "," + cdm.getPrefixes().get(key) + "\n");
            }
            prefixWriter.close();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
