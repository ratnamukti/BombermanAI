package Server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.Scanner;

public class PlayerProcess
{
  public ProcessBuilder processBuilder = null;
  public Process process = null;
  public BufferedReader in = null;
  public PrintWriter out = null;
  
  public PlayerProcess(String classPath, String className) {
    try {
      // Determine Java executable to call based on OS
      String osName = System.getProperty("os.name");
      String javaExecutableName = "";
      if (osName.contains("Windows")) {
        javaExecutableName = "java.exe";
      }
      else {
        javaExecutableName = "java";
      }
      // Start player process and get I/O stream.
      // If the startup is slow, ProcessBuilder could be the cause
      processBuilder = new ProcessBuilder(javaExecutableName, "-cp", classPath, className);
      process = processBuilder.start();
      in = new BufferedReader(new InputStreamReader(process.getInputStream(), "UTF-8"));
      out = new PrintWriter(process.getOutputStream());
    }
    catch (IOException e) {
      e.printStackTrace();
    }
  }
  
  public String getNextLine() {
    try {
      return in.readLine();
    }
    catch (IOException e) {
      return "TIMEOUT";
    }
  }
  
  public void sendLine(String line) {
    // Wait until output stream is ready
    while (out == null);
    out.println(line);
    out.flush();
  }
}
