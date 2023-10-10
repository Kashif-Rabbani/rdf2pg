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


import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import maps.complete.CompleteMapping;
import pgraph.PGEdge;
import pgraph.PGNode;
import pgraph.PGProperty;
import writers.PGWriter;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Iterator;

/**
 *
 */
public class Neo4jCsvWriter implements PGWriter {

    Writer pgRelWriter;
    Writer pgNodesWdLabelsWriter;
    Writer pgNodesPropWriter;
    String filename = "output.ypg";
    HashMap<Integer, Integer> oidmap = new HashMap();
    int oid = 1;
    private HashMap<String, String> prefixes;

    public Neo4jCsvWriter(String _filename) {
        this.filename = _filename;
    }

    public Neo4jCsvWriter() {
    }

    @Override
    public void begin() {
        try {
            pgNodesWdLabelsWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("PG_NODES_WD_LABELS.csv"), StandardCharsets.UTF_8));
            pgNodesWdLabelsWriter.write("id:ID|iri|:LABEL\n");
            pgNodesPropWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("PG_NODES_PROPS_JSON.json"), StandardCharsets.UTF_8));
            pgNodesPropWriter.write("[");
            pgRelWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("PG_RELATIONS.csv"), StandardCharsets.UTF_8));
            pgRelWriter.write(":START_ID|property|:END_ID|:TYPE\n");
        } catch (Exception ex) {
            System.out.println("Error1: " + ex.getMessage());
        }
    }


    @Override
    public void writeNode(PGNode node) {
        Integer node_id = oidmap.get(node.getId());
        if (node_id == null) {
            node_id = oid;
            oidmap.put(node.getId(), oid);
            oid++;
        }

        StringBuilder labels = new StringBuilder();
        Iterator<String> it1 = node.getLabels();
        while (it1.hasNext()) {
            String label = it1.next();
            labels.append(label);
            if (it1.hasNext()) {
                labels.append(";");
            } else {
                labels.append(";Node");
            }
        }

        int cnt = 0;
        ObjectNode propsNode = null;
        String nodeIriProp = null;
        Iterator<PGProperty> it2 = node.getProperties();
        while (it2.hasNext()) {
            PGProperty prop = it2.next();
            if (prop.getLabel().equals("iri")) {
                nodeIriProp = prop.getValue();
                continue;
            }
            cnt++;
            if (propsNode == null) {
                propsNode = new ObjectMapper().createObjectNode();
            }
            propsNode.put(prop.getLabel(), prop.getValue());
        }

        if (propsNode != null) {
            ObjectNode jsonObject = new ObjectMapper().createObjectNode();
            jsonObject.put("iri", nodeIriProp);
            jsonObject.set("properties", propsNode);
            if (isValid(jsonObject.toString(), new ObjectMapper())) {
                // Check if this is the first JSON object
                if (oid == 2) {
                    // If it's the first object, don't write a comma before it
                    writePgNodePropJson(jsonObject + "\n");
                } else {
                    // If it's not the first object, write a comma before it
                    writePgNodePropJson("," + jsonObject + "\n");
                }
            } else {
                System.out.println("Invalid JSON: " + jsonObject.toString());
            }
        }
        String lineCsv = node.getId() + "|" + nodeIriProp + "|" + labels + "\n";
        this.writePgNodeWdLabel(lineCsv);
    }

    @Override
    public void writeEdge(PGEdge edge) {
        Integer edge_id = oidmap.get(edge.getId());
        if (edge_id == null) {
            edge_id = oid;
            oidmap.put(edge.getId(), oid);
            oid++;
        }

        String labels = "";
        Iterator<String> it1 = edge.getLabels();
        while (it1.hasNext()) {
            String label = it1.next();
            if (!label.isEmpty()) {
                labels = label;
            } else {
                labels = labels + ";" + label;
            }

        }

        int cnt = 0;
        String props = "";
        Iterator<PGProperty> it2 = edge.getProperties();
        while (it2.hasNext()) {
            PGProperty prop = it2.next();
            cnt++;
            if (cnt < edge.propertiesCounter()) {
                props = props + prop.getLabel() + ":\"" + prop.getValue() + "\"" + ",";
            } else {
                props = props + prop.getLabel() + ":\"" + prop.getValue() + "\"";
            }
        }

        Integer snode_oid = oidmap.get(edge.getSourceNode());
        if (snode_oid == null) {
            snode_oid = oid;
            oidmap.put(edge.getSourceNode(), oid);
            oid++;
        }
        Integer tnode_oid = oidmap.get(edge.getTargetNode());
        if (tnode_oid == null) {
            tnode_oid = oid;
            oidmap.put(edge.getTargetNode(), oid);
            oid++;
        }

        snode_oid = edge.getSourceNode();
        tnode_oid = edge.getTargetNode();


        // CREATE (a)-[r:RELTYPE]->(b)
        //String line;
        //id|http://dbpedia.org/ontology/wrong|0|ns0_wrong
        String lineCsv;
        if (edge.propertiesCounter() == 0) {
            //:START_ID|property|:END_ID|:TYPE
            lineCsv = snode_oid + "|" + labels + "|" + tnode_oid + "|" + labels + "\n";
        } else {
            lineCsv = snode_oid + "|" + labels + "|" + tnode_oid + "|" + labels + "\n";
        }
        this.writePgRelation(lineCsv);
    }

    @Override
    public void end() {
        try {
            pgRelWriter.close();
            pgNodesWdLabelsWriter.close();
            pgNodesPropWriter.write("]");
            pgNodesPropWriter.close();
        } catch (Exception ex) {
            System.out.println("Error: " + ex.getMessage());
        }
    }


    private void writePgRelation(String line) {
        try {
            pgRelWriter.write(line);
        } catch (Exception ex) {
            System.out.println("Error2: " + ex.getMessage());
        }
    }

    private void writePgNodeWdLabel(String line) {
        try {
            pgNodesWdLabelsWriter.write(line);
        } catch (Exception ex) {
            System.out.println("Error2: " + ex.getMessage());
        }
    }

    private void writePgNodePropJson(String line) {
        try {
            pgNodesPropWriter.write(line);
        } catch (Exception ex) {
            System.out.println("Error2: " + ex.getMessage());
        }
    }

    public void setPrefixes(HashMap<String, String> prefixes) {
        this.prefixes = prefixes;
    }

    public boolean isValid(String json, ObjectMapper mapper) {
        try {
            mapper.readTree(json);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return true;
    }
}
