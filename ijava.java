import java.io.*;
import java.lang.reflect.Method;
import static java.lang.System.*;

// The code is ugly because I wanted to have only one class file.
// That's my excuse, anyway...

public class ijava extends ClassLoader {
    @Override
    protected Class findClass(String path) throws ClassNotFoundException {
        File f = new File(path);
        byte[] code = new byte[(int) f.length()];

        try {
            FileInputStream fis = new FileInputStream(f);
            fis.read(code); fis.close();
        } catch (IOException e) {
            throw new ClassNotFoundException();
        }

        String className = f.getName().replace(".class", "");
        return defineClass(className, code, 0, code.length);
    }
    
    @SuppressWarnings("unchecked")
    public static void main(String[] args) throws IOException {
        BufferedReader r = new BufferedReader(new InputStreamReader(in));
        ijava loader = new ijava();

        for (;;) {
            if (!r.ready())
                out.print(">>> ");
            String line = r.readLine();
            if (line == null) {
                out.println();
                break;
            }
            if (line.trim().isEmpty())
                continue;
            
            File fJava = File.createTempFile("jrepl", ".java");
            File dir = fJava.getParentFile();
            String className = fJava.getName().replace(".java", "");
            String pathClass = fJava.getAbsolutePath().replace(".java", ".class");
            
            String code = String.format(
                "public class %s {public static Object eval() {return %s\n;}}",
                className, line);
            
            FileWriter fw = new FileWriter(fJava);
            fw.write(code); fw.close();

            String[] cmd = {"javac", fJava.getName()};
            Process javac = Runtime.getRuntime().exec(cmd, null, dir);
            int exitValue = 0;
            try {
                exitValue = javac.waitFor();
            } catch (InterruptedException e) {
                e.printStackTrace();
                exit(1);
            } finally {
                fJava.delete();
            }

            if (exitValue == 0)
                try {
                    Class cls = loader.loadClass(pathClass);
                    Method eval = cls.getMethod("eval");
                    Object result = eval.invoke(null);
                    out.println(result);
                } catch (Exception e) {
                    e.printStackTrace();
                    exit(1);
                } finally {
                    new File(pathClass).delete();
                }
            else
                out.println("expression failed to compile");
        }
    }
}
