package org.elegadro.red.pill;

import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
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

import java.io.*;
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

    private static final Path ELEGADRO_DATA_PATH;
    private static final Path ELEGADRO_RT_XML_SAVE_PATH;
    private static final Path ELEGADRO_RT_XML_EN_SAVE_PATH;
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
        ELEGADRO_DATA_PATH = WRITABLE_PATH.resolve("elegadro_" + getSelfVersion());
        System.out.println("Detected writable path '" + WRITABLE_PATH + "', storing Elegadro data in '" +  ELEGADRO_DATA_PATH + "'.");

        ELEGADRO_RT_XML_SAVE_PATH = ELEGADRO_DATA_PATH.resolve("rt-law-xml");
        ELEGADRO_RT_XML_EN_SAVE_PATH = ELEGADRO_RT_XML_SAVE_PATH.resolve("tr-en");
        ELEGADRO_NEO_DATADIR = ELEGADRO_DATA_PATH.resolve("neo-data");
    }

    public static void main(String[] args) throws IOException {
        // Whereever the Neo4J storage location is.
        File storeDir = ELEGADRO_NEO_DATADIR.toFile();

        ServerBootstrapper serverBootstrapper = new CommunityBootstrapper();
        String bindHost = System.getProperty("redpill.bindHost", "127.0.0.1");
        String httpPort = System.getProperty("redpill.httpPort", "7474");
        String boltPort = System.getProperty("redpill.boltPort", "7687");
        String shellPort = System.getProperty("redpill.shellPort", "1337");

        String webAddr = bindHost + ":" + httpPort;
        String boltAddr = bindHost + ":" + boltPort;
        serverBootstrapper.start(
            storeDir,
            Optional.empty(), // no configfile provided: few properties follow
            Pair.of("dbms.connector.http.address", webAddr),
            Pair.of("dbms.connector.bolt.address", boltAddr),
            Pair.of("dbms.connector.bolt.enabled", "true"),
            Pair.of("dbms.connector.http.enabled", "true"),

            // allow the shell connections via port 1337 (default)
            Pair.of("dbms.shell.enabled", "true"),
            Pair.of("dbms.shell.host", bindHost),
            Pair.of("dbms.shell.port", shellPort)

            /*
                # Enable a remote shell server which Neo4j Shell clients can log in to.
                #dbms.shell.enabled=true
                # The network interface IP the shell will listen on (use 0.0.0.0 for all interfaces).
                #dbms.shell.host=127.0.0.1
                # The port the shell will listen on, default is 1337.
                #dbms.shell.port=1337
            */
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
        System.out.println("Created Elegadro database default user: " + ELE_USER + "/" + ELE_USER_PW);
    }

    private static void createIndexes(GraphDatabaseService graphDB) {
        System.out.print("Creating indexes ...");
        for (LawParticleEnum lpe: LawParticleEnum.values()) {
            try (Transaction tx = graphDB.beginTx()) {
                graphDB.execute("CREATE INDEX ON :" + lpe.getLabel() + "(tr_et)");
                graphDB.execute("CREATE INDEX ON :" + lpe.getLabel() + "(tr_en)");
                graphDB.execute("CREATE INDEX ON :" + lpe.getLabel() + "(lc_tr_et)");
                graphDB.execute("CREATE INDEX ON :" + lpe.getLabel() + "(lc_tr_en)");
                tx.success();
            }
            System.out.print(" " + lpe.getLabel());
        }
        System.out.println();
    }

    private static void persistTheLaw(GraphDatabaseService graphDB, Seadus et, Seadus en) {
        System.out.print("Persisting into graphdb  " + ((en != null) ? "(ET+EN)" : "(ET)") + " ... ");
        long start = System.nanoTime();
        try (Transaction tx = graphDB.beginTx()) {
            legalMolecul2Node(graphDB, et, en);
            tx.success();
        }
        System.out.printf("done %3.3gs\n", (System.nanoTime()-start)/1000.0/1000.0/1000.0);
    }

    private static void bringTheLaws(GraphDatabaseService graphDB, Set<Actronym> lawsNotPresent) {
        if (!lawsNotPresent.isEmpty()) {
            ensureRtXmlSavePath(true);
        }

        int total = lawsNotPresent.size(), etc = 0, enc = 0;

        for (Actronym actronym: lawsNotPresent) {
            Seadus seadusET = bringTheLawET(actronym);
            if (seadusET == null) {
                System.err.println("!" + actronym.getActronym() + " ('" + actronym.getExpanym() + "') will not be inserted.");
                continue;
            }
            System.out.println("Acquired " + actronym.getActronym() + " ('" + actronym.getExpanym() + "')");

            if (null == actronym.getTrnId()) {
                System.out.println("!" + actronym.getActronym() + " does not have EN translation available at RT.");
                persistTheLaw(graphDB, seadusET, null);
                etc++;
                continue;
            }

            Seadus seadusEN = bringTheLawEN(actronym);
            if (seadusEN == null) {
                System.out.println("!" + actronym.getActronym() + " could not fetch or parse EN translation available at RT.");
                persistTheLaw(graphDB, seadusET, null);
                etc++;
                continue;
            }

            System.out.println("Acquired EN translation for " + actronym.getActronym());
            boolean et_en_match = haveStructuralMatch(new LinkedList<>(), seadusET, seadusEN);
            if (!et_en_match) {
                System.err.println("! Due to structural mismatch(es), EN translations for " + actronym.getActronym() + " will NOT be inserted.");
                persistTheLaw(graphDB, seadusET, null);
                etc++;
                continue;
            }

            persistTheLaw(graphDB, seadusET, seadusEN);
            enc++;
        }

        if (!lawsNotPresent.isEmpty())
            System.out.println("Inserted " + etc + " acts (ET) and " + enc + " acts (ET+EN), "+ (etc+enc) +"/" + total +" looked for.");
    }

    private static Seadus bringTheLawET(Actronym actronym) {
        String localFileName = actronym.getActId() + ".xml";
        Path localFilePath = ELEGADRO_RT_XML_SAVE_PATH.resolve(localFileName);
        if (!Trinity.bringRemoteLawET(actronym, localFilePath))
            return null;
        return Trinity.bringLocalLaw(localFilePath);
    }

    private static Seadus bringTheLawEN(Actronym actronym) {
        String localFileName = actronym.getTrnId() + ".xml";
        Path localFilePath = ELEGADRO_RT_XML_EN_SAVE_PATH.resolve(localFileName);
        if (!Trinity.bringRemoteLawEN(actronym, localFilePath))
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
                String lawName = (String) lawNode.getProperty("tr_et", null);
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

    private static String toParentDbgString(Deque<LegalMolecul> parents) {
        StringBuilder sb = new StringBuilder();
        Iterator<LegalMolecul> it = parents.descendingIterator();
        it.forEachRemaining(p -> {
            if (LawParticleEnum.SEADUS.getLabel().equals(p.getParticleName()))
                sb.append(p.getLegalText()).append(' ');
            else
                sb.append(p.getParticleNumber()).append(' ').append(p.getParticleName()).append(' ');
        });
        return sb.toString();
    }

    private static boolean haveSameTypeAndLegalNumber(Deque<LegalMolecul> parents, LegalParticle p1, LegalParticle p2) {
        boolean result = true;
        String n1 = p1.getParticleName(), n2 = p2.getParticleName();
        if (!n1.equals(n2)) {
            System.err.println("Particle type mismatch: " + toParentDbgString(parents) + " '" + n1 + "' != '" + n2 + "'");
            result = false;
        }

        LegalNumber p1n = p1.getParticleNumber(), p2n = p2.getParticleNumber();
        // Need to cut slack for the cases where just roman/arabic representation of the numeral is different,
        // as happens to be somewhat of a convention between Estonian/English acts...
        // e.g. LegalNumber{isRoman=T, num=2, sup=null} & LegalNumber{isRoman=F, num=2, sup=null}
        if (!((p1n == p2n) || (p1n != null && 0 == p1n.compareTo(p2n)))) {
            // Additionally cut slack when both particles are correctly marked expired (rare...)
            // while their numbers have been left unsynced
            if (p1n.isUnexpired() || p2n.isUnexpired()) {
                String v1 = (p1n != null) ? p1n.toDebugString() : null;
                String v2 = (p2n != null) ? p2n.toDebugString() : null;
                System.err.println("Particle number mismatch " + toParentDbgString(parents) + " at " + n1 + ": '" + v1 + "' != '" + v2 + "'");
                result = false;
            }
        }

        return result;
    }

    private static boolean haveStructuralMatch(Deque<LegalMolecul> parents, LegalMolecul et, LegalMolecul en) {
        boolean result = true;
        if (!haveSameTypeAndLegalNumber(parents, et, en))
            result = false;

        Iterator<LegalParticle> iET = et.getLegalParticles().iterator();
        Iterator<LegalParticle> iEN = en.getLegalParticles().iterator();

        parents.push(et);
        while (iET.hasNext()) {
            if (!iEN.hasNext())
                return false;

            LegalParticle pET = iET.next(), pEN = iEN.next();
            if (pET instanceof LegalMolecul && !haveStructuralMatch(parents, (LegalMolecul) pET, (LegalMolecul) pEN))
                result = false;
            else if (!haveStructuralMatch(parents, pET, pEN))
                result = false;
        }
        parents.pop();

        return result;
    }

    private static boolean haveStructuralMatch(Deque<LegalMolecul> parents, LegalParticle et, LegalParticle en) {
        return haveSameTypeAndLegalNumber(parents, et, en);
    }

    private static Node legalMolecul2Node(GraphDatabaseService graphDB, LegalMolecul molET, LegalMolecul molEN) {
        Node molNode = graphDB.createNode(Label.label(molET.getParticleName()));
        molNode.setProperty("tr_et", molET.getLegalText());
        molNode.setProperty("lc_tr_et", molET.getLegalText().toLowerCase());
        if (molEN != null) {
            molNode.setProperty("tr_en", molEN.getLegalText());
            molNode.setProperty("lc_tr_en", molEN.getLegalText().toLowerCase());
        }

        if (molET.getParticleNumber() != null) {
            appendLegalNumberToNode(molET.getParticleNumber(), molNode);
        }

        Iterator<LegalParticle> iET = molET.getLegalParticles().iterator();
        Iterator<LegalParticle> iEN = (molEN != null) ? molEN.getLegalParticles().iterator() : null;
        while (iET.hasNext()) {
            Node child;
            LegalParticle pET = iET.next(), pEN = (molEN != null) ? iEN.next() : null;
            if (pET instanceof LegalMolecul) {
                child = legalMolecul2Node(graphDB, (LegalMolecul) pET, (LegalMolecul) pEN);
            } else {
                child = legalParticle2Node(graphDB, pET, pEN);
            }

            molNode.createRelationshipTo(child, RelTypes.HAS);
        }

        return molNode;
    }

    private static Node legalParticle2Node(GraphDatabaseService graphDB, LegalParticle particle, LegalParticle enTrParticle) {
        Node pNode = graphDB.createNode(Label.label(particle.getParticleName()));
        pNode.setProperty("tr_et", particle.getLegalText());
        pNode.setProperty("lc_tr_et", particle.getLegalText().toLowerCase());
        if (enTrParticle != null) {
            pNode.setProperty("tr_en", enTrParticle.getLegalText());
            pNode.setProperty("lc_tr_en", enTrParticle.getLegalText().toLowerCase());
        }

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
            Files.createDirectories(ELEGADRO_RT_XML_EN_SAVE_PATH);
        }  catch (IOException e) {
            e.printStackTrace(System.err);
            System.err.println("Could not write '" + ELEGADRO_RT_XML_SAVE_PATH + "', exiting.");
            if (exitOnFailure)
                System.exit(3);
        }
    }

    //
    // JUGGLE SOME TO KNOW THE VERSION OF SELF
    //

    /* Inspired from: https://stackoverflow.com/questions/3697449/retrieve-version-from-maven-pom-xml-in-code */
    private static String getSelfVersion() {
        Package selfPackage = RunNeoRun.class.getPackage();
        if (selfPackage != null) {
            String implementationVersion = selfPackage.getImplementationVersion();
            if (null != implementationVersion)
                return implementationVersion;
        }

        InputStream pomStream ;
        try {
            pomStream = new FileInputStream("redpill/pom.xml");
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }

        MavenXpp3Reader xpp3Reader = new MavenXpp3Reader();
        Model model = null;

        try {
            model = xpp3Reader.read(pomStream);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return model.getVersion();
    }
}
