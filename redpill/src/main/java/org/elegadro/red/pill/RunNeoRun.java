package org.elegadro.red.pill;

import org.elegadro.iota.legal.LawParticleEnum;
import org.elegadro.iota.legal.LegalMolecul;
import org.elegadro.iota.legal.LegalParticle;
import org.elegadro.iota.legal.impl.Seadus;
import org.elegadro.iota.legal.number.LegalNumber;
import org.elegadro.iota.rt.actronym.Actronym;
import org.neo4j.graphdb.*;
import org.neo4j.helpers.collection.Pair;
import org.neo4j.server.CommunityBootstrapper;
import org.neo4j.server.NeoServer;
import org.neo4j.server.ServerBootstrapper;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

import static org.elegadro.iota.legal.number.LegalNumber.*;

/**
 * @author Taimo Peelo
 */
public class RunNeoRun {
    private static final Path WRITABLE_PATH;

    private static final Path ELEGADRO_RT_XML_SAVE_PATH;
    private static final Path ELEGADRO_NEO_DATADIR;

    private enum RelTypes implements RelationshipType {
        HAS
    }

    static {
        WRITABLE_PATH = getWritablePath();
        if (WRITABLE_PATH == null) {
            System.err.println(
                "Tried\n " +
                " 1) user home directory\n" +
                " 2) system/user temp directory\n" +
                " 3) program working directory\n" +
                "but failed to find writable path."
            );
            System.err.println("No writable path detected. Exiting.");
            System.exit(1);
        }
        System.out.println("Detected writable path '" + WRITABLE_PATH + "'.");

        ELEGADRO_RT_XML_SAVE_PATH = WRITABLE_PATH.resolve("elegadro/rt-law-xml");
        ELEGADRO_NEO_DATADIR = WRITABLE_PATH.resolve("elegadro/neo-data");
    }

    public static void main(String[] args) throws IOException {
        // Whereever the Neo4J storage location is.
        File storeDir = ELEGADRO_NEO_DATADIR.toFile();

        ServerBootstrapper serverBootstrapper = new CommunityBootstrapper();
        String webAddr = "127.0.0.1:7474";
        serverBootstrapper.start(
            storeDir,
            Optional.empty(), // no configfile provided: few properties follow
            Pair.of("dbms.connector.http.address", webAddr),
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

        Set<Actronym> lawsNotPresent = detectMissingLaws(gdb);

        if (!lawsNotPresent.isEmpty()) {
            boolean initPush = lawsNotPresent.size() == Actronym.values().length;
            if (initPush)
                System.out.println(
                    "This appears to be first start of this database instance, " +
                    "attempting to bring the laws (~" + lawsNotPresent.size() + "), stand by."
                );
            else
                System.out.println("Not present in Neo4J graph db when starting up, attempting to retrieve\n" + lawsNotPresent);

            bringTheLaws(gdb, lawsNotPresent);

            if (initPush) {
                createIndexes(gdb);
            }
        }

        ensureElegadroUserExists(gdb);

        System.out.println(
            "* * *\n" +
            "Initialized Elegadro PoC database and started, run web-poc for simple demonstration client. " + "\n" +
            "This can be done be running following command in web-poc folder:\n" +
            "    mvn jetty:run\n" +
            "Also, Neo4J web console for graph examination is available at http://" + webAddr);

        System.out.println("\nPress ENTER to shut-down this Neo4J server instance.");
        System.in.read();

        System.exit(0);
    }

    private static final String ELE_USER = "elegadro";
    private static final String ELE_USER_PW = "ele";
    private static final String CREATE_ELE_USER = "CALL dbms.security.createUser('" + ELE_USER + "', '"+ ELE_USER_PW +"', false)";
    /**
      Create the Neo4J user who has a password that does NOT require immediate changing
      This is just for local demo setup to work simply, without end-user having to
      change password for default user (neo4j).
     */
    private static void ensureElegadroUserExists(GraphDatabaseService graphDB) {
        boolean hasUser = false;
        try (
            Transaction tx = graphDB.beginTx();
            Result qr = graphDB.execute("CALL dbms.security.listUsers();");
            ResourceIterator<Object> uit = qr.columnAs("username");
        ) {
            while (uit.hasNext()) {
                Object next = uit.next();
                if (ELE_USER.equals(next))
                    hasUser = true;
            }
            tx.success();
        }

        if (hasUser) {
            System.out.println("Elegadro database default user " + ELE_USER + "/" + ELE_USER_PW);
            return;
        }

        try(
            Transaction tx = graphDB.beginTx();
        ) {
            graphDB.execute(CREATE_ELE_USER);
            tx.success();
        }
        System.out.println("Created Elegardo database default user: " + ELE_USER + "/" + ELE_USER_PW);
    }

    private static void createIndexes(GraphDatabaseService graphDB) {
        System.out.print("Creating indexes ...");
        for (LawParticleEnum lpe: LawParticleEnum.values()) {
            try (Transaction tx = graphDB.beginTx()) {
                Result qr = graphDB.execute("CREATE INDEX ON :" + lpe.getLabel() + "(text)");
                qr.forEachRemaining(m -> {
                    System.out.println(m);
                });

                tx.success();
            }
            System.out.print(" " + lpe.getLabel());
        }
        System.out.println();
    }

    private static void persistTheLaw(GraphDatabaseService graphDB, Seadus seadus) {
        System.out.print("Persisting into graphdb ... ");
        long start = System.nanoTime();
        try (Transaction tx = graphDB.beginTx()) {
            legalMolecul2Node(graphDB, seadus);
            tx.success();
        }
        System.out.printf("done %3.3gs\n", (System.nanoTime()-start)/1000.0/1000.0/1000.0);
    }

    private static void bringTheLaws(GraphDatabaseService graphDB, Set<Actronym> lawsNotPresent) {
        if (!lawsNotPresent.isEmpty()) {
            ensureRtXmlSavePath(true);
        }

        for (Actronym actronym: lawsNotPresent) {
            Seadus seadus = bringTheLaw(actronym);
            if (seadus != null)
                System.out.println("Acquired " + actronym.getActronym() + " ('" + actronym.getExpanym() + "')");
            else
                System.err.println("!" + actronym.getActronym() + " ('" + actronym.getExpanym() + "') will not be inserted.");

            if (seadus != null)
                persistTheLaw(graphDB, seadus);
        }
    }

    private static Seadus bringTheLaw(Actronym actronym) {
        // fetch the xml for fun and profit...
        // ... UrlConnections should suffice.
        // then apply
        String localFileName = actronym.getActId() + ".xml";
        Path localFilePath = ELEGADRO_RT_XML_SAVE_PATH.resolve(localFileName);
        if (!Trinity.bringRemoteLaw(actronym, localFilePath))
            return null;
        return Trinity.bringLocalLaw(localFilePath);
    }

    /* Returns the list of laws that are not present, attempts to import them. */
    private static Set<Actronym> detectMissingLaws(GraphDatabaseService gdb) {
        Set<Actronym> actronyms = new LinkedHashSet<>(Arrays.asList(Actronym.values()));
        try(Transaction tx = gdb.beginTx()) {
            ResourceIterator<Node> presentLaws = gdb.findNodes(Label.label(LawParticleEnum.SEADUS.getLabel()));
            while (presentLaws.hasNext()) {
                Node lawNode = presentLaws.next();
                String lawName = (String) lawNode.getProperty("text", null);
                if (lawName != null) {
                    Actronym actronym = actronyms.stream().filter(a -> a.getExpanym().equals(lawName)).findFirst().get();
                    actronyms.remove(actronym);
                }
            }
            tx.success();
        }

        return actronyms;
    }

    //
    // INITIAL NODE POPULATION METHODS
    //

    private static Node legalMolecul2Node(GraphDatabaseService graphDB, LegalMolecul mol) {
        Node molNode = graphDB.createNode(Label.label(mol.getParticleName()));
        molNode.setProperty("text", mol.getLegalText());
        if (mol.getParticleNumber() != null) {
            appendLegalNumberToNode(mol.getParticleNumber(), molNode);
        }

        mol.getLegalParticles().forEach(p -> {
            Node child;
            if (p instanceof LegalMolecul) {
                child = legalMolecul2Node(graphDB, (LegalMolecul) p);
            } else {
                child = legalParticle2Node(graphDB, p);
            }

            molNode.createRelationshipTo(child, RelTypes.HAS);
        });

        return molNode;
    }

    private static Node legalParticle2Node(GraphDatabaseService graphDB, LegalParticle particle) {
        Node pNode = graphDB.createNode(Label.label(particle.getParticleName()));
        pNode.setProperty("text", particle.getLegalText());

        if (particle.getParticleNumber() != null) {
            appendLegalNumberToNode(particle.getParticleNumber(), pNode);
        }

        return pNode;
    }

    public static org.neo4j.graphdb.Node appendLegalNumberToNode(LegalNumber pn, org.neo4j.graphdb.Node node) {
        if (pn == null || (pn.getNum() == null && pn.getSup() == null))
            return node;

        if (pn.getNum() != null)
            node.setProperty(LEGAL_NUMBER_KEY, pn.getNum());

        if (pn.getSup() != null)
            node.setProperty(LEGAL_SUPER_KEY, pn.getSup());

        node.setProperty(LEGAL_ROMAN_KEY, pn.isRoman());

        return node;
    }

    //
    // FILE SYSTEM OPERATIONS
    //

    /**
     * Determines the path to use for storage and returns it, or returns {@code null}
     * when unable to find suitable writable storage path.
     * Tries in order:
     *   1) user home directory
     *   2) system/user temp directory
     *   3) working directory */
    private static final Path getWritablePath() {
        String userHome = System.getProperty("user.home");
        Path userHomePath = new File(userHome).toPath();
        if (Files.isWritable(userHomePath)) {
            return userHomePath;
        }

        String tmpDir = System.getProperty("java.io.tmpdir");
        Path tmpDirPath = new File(tmpDir).toPath();
        if (Files.isWritable(tmpDirPath)) {
            return tmpDirPath;
        }

        FileSystem defaultFs = FileSystems.getDefault();
        Path currentPath = defaultFs.getPath(".").toAbsolutePath();
        if (Files.isWritable(currentPath))
            return currentPath;

        return null;
    }

    /** Ensures that RT XML save path exists or is created -- exits otherwise. */
    private static void ensureRtXmlSavePath(boolean exitOnFailure) {
        try {
            Files.createDirectories(ELEGADRO_RT_XML_SAVE_PATH);
        }  catch (IOException e) {
            e.printStackTrace(System.err);
            System.err.println("Could not write '" + ELEGADRO_RT_XML_SAVE_PATH + "', exiting.");
            if (exitOnFailure)
                System.exit(3);
        }
    }
}
