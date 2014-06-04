package com.credera;

import org.neo4j.graphdb.*;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;

import java.util.HashMap;
import java.util.Map;

public class Neo4JSample {
  private final String DB_PATH = "/tmp/neo4j_db_sample";
  private final String PERSON = "person";
  private final String RECOGNITION = "recognition";
  private final String PROGRAM = "program";
  private final String NAME = "name";
  private GraphDatabaseService neo4jdb;
  private Map<String, Long> ids = new HashMap<>();

  private enum Relations implements RelationshipType {
    GIVES, RECEIVES, FRIENDS, FOR_PROGRAM
  }

  public void run() {
    createDB();
    try (Transaction tx = neo4jdb.beginTx()) {
    	//add people and programs to our model
      createNode(PERSON, "Kim");
      createNode(PERSON, "Bryan");
      createNode(PERSON, "Joe");
      createNode(PERSON, "Kristina");
      createNode(PROGRAM, "Celebrating You");
      createNode(PROGRAM, "Compass Kudos");
      createNode(PROGRAM, "Difference Makers");

      //make some friends
      friend("Kim", "Bryan");
      friend("Joe", "Kim");
      friend("Bryan", "Joe");
      friend("Kristina", "Bryan");
      
      //recognize folks for a job well done
      recognize("Kim", "Kristina", "Celebrating You");
      recognize("Bryan", "Joe", "Difference Makers");
      recognize("Joe", "Kristina", "Difference Makers");

      tx.success();
    }
    shutdownDB();
  }

  private void friend(String fromPerson, String toPerson) {
    Node from = neo4jdb.getNodeById(ids.get(fromPerson));
    Node to = neo4jdb.getNodeById(ids.get(toPerson));

    from.createRelationshipTo(to, Relations.FRIENDS);
  }

  private void recognize(String sender, String receiver, String programName) {
	    Node from = neo4jdb.getNodeById(ids.get(sender));
	    Node to = neo4jdb.getNodeById(ids.get(receiver));
	    
	    Node recognition = createNode(RECOGNITION, sender + "-" + receiver + "-" + programName);
	    Node program = neo4jdb.getNodeById(ids.get(programName));
	    recognition.createRelationshipTo(program, Relations.FOR_PROGRAM);

	    from.createRelationshipTo(recognition, Relations.GIVES);
	    to.createRelationshipTo(recognition, Relations.RECEIVES);
	  }

  public static void main(String[] args) {
    new Neo4JSample().run();
  }

  private Node createNode(String label, String name) {
    Node node = neo4jdb.createNode(DynamicLabel.label(label));
    node.setProperty(NAME, name);
    ids.put(name, node.getId());
    return node;
  }

  private void createDB() {
    neo4jdb = new GraphDatabaseFactory().newEmbeddedDatabase(DB_PATH);
  }

  private void shutdownDB() {
    neo4jdb.shutdown();
  }
}
