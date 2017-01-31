package org.elegadro.red.pill;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.helpers.collection.Pair;
import org.neo4j.server.CommunityBootstrapper;
import org.neo4j.server.NeoServer;
import org.neo4j.server.ServerBootstrapper;

import java.io.File;
import java.io.IOException;
import java.util.Optional;

/**
 * @author Taimo Peelo
 */
public class RunNeoRun {
    public static void main(String[] args) throws IOException {
//        String storagePath = System.getProperty("elegadro.neo4j.storage.path");
//        if (storagePath == null)

        // Whereever the Neo4J storage location is.
        File storeDir = new File("/tmp/tmp4j");

        ServerBootstrapper serverBootstrapper = new CommunityBootstrapper();
        serverBootstrapper.start(
            storeDir,
            Optional.empty(), // no configfile provided: few properties follow
            Pair.of("dbms.connector.http.address","127.0.0.1:7474"),
            Pair.of("dbms.connector.http.enabled", "true"),
            Pair.of("dbms.connector.bolt.enabled", "true"),

            // allow the shell connections via port 1337 (default)
            Pair.of("dbms.shell.enabled", "true"),
            Pair.of("dbms.shell.host", "127.0.0.1"),
            Pair.of("dbms.shell.port", "1337")

            /*
            # Enable a remote shell server which Neo4j Shell clients can log in to.
            #dbms.shell.enabled=true
            # The network interface IP the shell will listen on (use 0.0.0.0 for all interfaces).
            #dbms.shell.host=127.0.0.1
            # The port the shell will listen on, default is 1337.
            #dbms.shell.port=1337
             */

            //
        );
        // ^^ serverBootstrapper.start() also registered shutdown hook!

        NeoServer neoServer = serverBootstrapper.getServer();
        GraphDatabaseService gdb = neoServer.getDatabase().getGraph();

        /*
        try(Transaction tx = gdb.beginTx()) {
            gdb.getAllNodes().forEach(
                n -> System.out.println(n)
            );
            tx.success();
        } */

        System.out.println("Press ENTER to quit.");
        System.in.read();

        System.exit(0);
    }
}
