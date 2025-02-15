package io.hyperfoil.tools.qdup;

import io.hyperfoil.tools.yaup.AsciiArt;
import org.junit.Test;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.security.Permission;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

public class QDupTest extends SshTestBase{

    public static String makeFile(String...lines){
        String rtrn = "";
        try {
            File f = File.createTempFile("qdup-test","yaml");
            rtrn = f.getPath();
            f.deleteOnExit();
            if(lines!=null){
                BufferedWriter writer = new BufferedWriter(new FileWriter(f));
                Arrays.stream(lines).forEach(line->{
                    try {
                        writer.write(line);
                        writer.newLine();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
                writer.flush();
                writer.close();
            }
        } catch (IOException e) {
            //e.printStackTrace();
            rtrn = "";
        }
        return rtrn;
    }
    public String[] runMain(String...args){
        String result = "0";
        SecurityManager securityManager = System.getSecurityManager();
        System.setSecurityManager(new TempSecurityManager());
        PrintStream originalOut = System.out;
        PrintStream originalErr = System.err;

        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        final String utf8 = StandardCharsets.UTF_8.name();

        try (PrintStream tempPrintStream = new PrintStream(baos, true, utf8)) {
//            System.setOut(tempPrintStream);
//            System.setErr(tempPrintStream);
            System.setProperty("disableRestApi", "true");
            QDup.main(args);
        } catch (SecurityException e) {
            result = e.getMessage();
        } catch (UnsupportedEncodingException e) {
            result = e.getMessage();
        } finally {
            System.setErr(originalErr);
            System.setOut(originalOut);
            System.setSecurityManager(securityManager);
        }
        return new String[]{result,baos.toString()};
    }

    class TempSecurityManager extends SecurityManager {
        @Override
        public void checkExit(int status) {
            throw new SecurityException(String.valueOf(status)); //do not exit JVM if System.exit() is called
        }

        @Override
        public void checkPermission(Permission perm) {
            // Allow other activities by default
        }
    }
    @Test
    public void main_exit_sh() {
        String output[] = runMain(
                "-i",
                getIdentity(),
                makeFile(
                    "hosts:",
                    "  local: "+getHost(),
                    "scripts:",
                    "  doit:",
                    "  - sh: whoami; (exit 42);",
                    "  - set-state: RUN.foo true",
                    "",
                    "roles:",
                    "  run:",
                    "    hosts: [local]",
                    "    run-scripts:",
                    "    - doit"
                )
        );
        assertNotNull(output);
        assertEquals("Qdup.main should exit with 1","1",output[0]);
    }

    @Test
    public void main_exit_invalid_args(){
        String output[] = runMain("");
        assertNotNull(output);
        assertEquals("incorrect exit code for invalid args to QDup.main"+output[1],"1",output[0]);
    }
    @Test
    public void main_exit_bad_yaml(){
        String output[] = runMain(
                "-i",
                getIdentity(),
                makeFile(
                    "hosts:",
                    "  local: "+getHost(),
                    "scripts:",
                    "  doit:",
                    "  - sh: whoami",
                    "    - set-state: RUN.foo true",
                    "",
                    "roles:",
                    "  run:",
                    "    hosts: [local]",
                    "    run-scripts:",
                    "    - doit"
                )
        );
        assertNotNull(output);
        assertEquals("incorrect exit code for invalid args to QDup.main"+output[1],"1",output[0]);
    }
    @Test
    public void main_exit_sh_ignore() {
        String output[] = runMain(
                "-ix",
                "-i",
                getIdentity(),
                makeFile(
                    "hosts:",
                    "  local: "+getHost(),
                    "scripts:",
                    "  doit:",
                    "  - sh: whoami; (exit 42);",
                    "  - set-state: RUN.foo true",
                    "",
                    "roles:",
                    "  run:",
                    "    hosts: [local]",
                    "    run-scripts:",
                    "    - doit"
                )
        );
        assertNotNull(output);
        assertEquals("Qdup.main should exit with 0","0",output[0]);
    }
    @Test
    public void exit_code_checking_by_default(){
        QDup qdup = new QDup("-T","fake.yaml");
        assertTrue("exit checking by default",qdup.checkExitCode());
    }
    @Test
    public void disable_exit_checking(){
        QDup qdup = new QDup("-ix","-T","fake.yaml");
        assertFalse("exit checking by default",qdup.checkExitCode());
    }
    @Test
    public void skip_stages_valid_lowercase(){
        QDup qdup = new QDup("--skip-stages","setup,cleanup","-T","fake.yaml");
        assertTrue("expect targetStages",qdup.hasSkipStages());
        assertEquals("expect 2 targetStages: "+qdup.getSkipStages(),2,qdup.getSkipStages().size());
        List<Stage> targetStages = qdup.getSkipStages();
        assertTrue("should contain setup "+targetStages,targetStages.contains(Stage.Setup));
        assertTrue("should contain cleanup "+targetStages,targetStages.contains(Stage.Cleanup));
    }
    @Test
    public void skip_stages_valid_mixed_case(){
        QDup qdup = new QDup("--skip-stages","Setup,Cleanup","-T","fake.yaml");
        assertTrue("expect targetStages",qdup.hasSkipStages());
        assertEquals("expect 2 targetStages: "+qdup.getSkipStages(),2,qdup.getSkipStages().size());
        List<Stage> targetStages = qdup.getSkipStages();
        assertTrue("should contain setup "+targetStages,targetStages.contains(Stage.Setup));
        assertTrue("should contain cleanup "+targetStages,targetStages.contains(Stage.Cleanup));
    }
}
