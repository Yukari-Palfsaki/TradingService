package net.froihofer.ejb.bank.common;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

public class Credentials {
  private Properties properties;

  /**
   * @brief  This class is to save the application information (client name and client password)
   */
  public Credentials(){
    this.properties = new Properties();
    File file = new File ("/Users/yukarisusaki/Desktop/ds-finance-bank.properties.txt");
    FileInputStream fileIO = null;
    try {
      fileIO = new FileInputStream(file);
      this.properties.load (fileIO);
      fileIO.close();
    } catch (FileNotFoundException e){
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public String clientUsername (){
    return properties.getProperty("ClientUsername");
  }

  public String clientPassword (){
    return properties.getProperty("ClientPassword");
  }

  public String employeeUsername (){
    return properties.getProperty("EmployeeUsername");
  }
  public String employeePassword (){
    return properties.getProperty("EmployeePassword");
  }

  public String serverUsername (){
    return properties.getProperty("ServerUsername");
  }
  public String serverPassword (){
    return properties.getProperty("ServerPassword");
  }
}

