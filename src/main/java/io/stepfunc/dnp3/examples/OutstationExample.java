/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Project/Maven2/JavaApp/src/main/java/${packagePath}/${mainClassName}.java to edit this template
 */
/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Project/Maven2/JavaApp/src/main/java/${packagePath}/${mainClassName}.java to edit this template
 */

package io.stepfunc.dnp3.examples;

import com.pi4j.io.gpio.digital.DigitalState;
import static org.joou.Unsigned.*;

import io.stepfunc.dnp3.*;
import io.stepfunc.dnp3.Runtime;
import static io.stepfunc.dnp3.examples.OutstationExample.now;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.time.Duration;
import org.joou.UByte;
import org.joou.ULong;
import org.joou.UShort;

class TestLogger implements Logger {
    

  @Override
  public void onMessage(LogLevel level, String message) {
      
      
   if(message.contains("TX")){        
         System.out.print(ColoresConsola.VERDE + "\n--------------------------------------------------------------------------------------------------------------------------------" );
         System.out.print("\n" + message);        
         System.out.print("--------------------------------------------------------------------------------------------------------------------------------" + ColoresConsola.RESET );
    }else{
   
    if(message.contains("RX")){
        System.out.print(ColoresConsola.AZUL + "\n--------------------------------------------------------------------------------------------------------------------------------" );
         System.out.print("\n" + message);        
         System.out.print("--------------------------------------------------------------------------------------------------------------------------------" + ColoresConsola.RESET );
              
    }else{System.out.print( message );}   
   
   }     
    
    System.out.print("\n>>" );
    
  }
}

class TestOutstationApplication implements OutstationApplication {

  @Override
  public UShort getProcessingDelayMs() {
    return ushort(0);
  }

  @Override
  public WriteTimeResult writeAbsoluteTime(ULong time) {
    return WriteTimeResult.OK;
  }

  @Override
  public ApplicationIin getApplicationIin() {
    return new ApplicationIin();
  }

  @Override
  public RestartDelay coldRestart() {
    return RestartDelay.notSupported();
  }

  @Override
  public RestartDelay warmRestart() {
    return RestartDelay.seconds(ushort(1));
  }

  @Override
  public FreezeResult freezeCountersAll(FreezeType freezeType, DatabaseHandle database) {
    return FreezeResult.NOT_SUPPORTED;
  }

  @Override
  public FreezeResult freezeCountersRange(
      UShort start, UShort stop, FreezeType freezeType, DatabaseHandle database) {
    return FreezeResult.NOT_SUPPORTED;
  }

  @Override
  public boolean writeStringAttr(UByte set, UByte variation, StringAttr attrType, String value) {
    // Allow writing any string attributes that have been defined as writable
    return true;
  }

}

class TestOutstationInformation implements OutstationInformation {

  @Override
  public void processRequestFromIdle(RequestHeader header) {}

  @Override
  public void broadcastReceived(FunctionCode functionCode, BroadcastAction action) {}

  @Override
  public void enterSolicitedConfirmWait(UByte ecsn) {}

  @Override
  public void solicitedConfirmTimeout(UByte ecsn) {}

  @Override
  public void solicitedConfirmReceived(UByte ecsn) {}

  @Override
  public void solicitedConfirmWaitNewRequest() {}

  @Override
  public void wrongSolicitedConfirmSeq(UByte ecsn, UByte seq) {}

  @Override
  public void unexpectedConfirm(boolean unsolicited, UByte seq) {}

  @Override
  public void enterUnsolicitedConfirmWait(UByte ecsn) {}

  @Override
  public void unsolicitedConfirmTimeout(UByte ecsn, boolean retry) {}

  @Override
  public void unsolicitedConfirmed(UByte ecsn) {}

  @Override
  public void clearRestartIin() {}
}

// ANCHOR: control_handler
class TestControlHandler implements ControlHandler {

    private final GpioControllerPi4j gpio;    
    
     public TestControlHandler(GpioControllerPi4j gpio){
        this.gpio = gpio;
    }   
    
    
    
    
  @Override
  public void beginFragment() {}

  @Override
  public void endFragment(DatabaseHandle database) {}

  @Override
  public CommandStatus selectG12v1(Group12Var1 control, UShort index, DatabaseHandle database) {
    if (index.compareTo(ushort(10)) < 0 && (control.code.opType == OpType.LATCH_ON || control.code.opType == OpType.LATCH_OFF||control.code.opType == OpType.PULSE_OFF)) {
      return CommandStatus.SUCCESS;
    } else {
      return CommandStatus.NOT_SUPPORTED;
    }
  }

  @Override
  public CommandStatus operateG12v1(Group12Var1 control, UShort index, OperateType opType, DatabaseHandle database) {
    if (index.compareTo(ushort(10)) < 0 && (control.code.opType == OpType.PULSE_ON && control.code.tcc == TripCloseCode.CLOSE)) {
      boolean status = true;
      
      try {
            gpio.pulseOutputOn(index.intValue(), 500);
      } catch (Exception e) {
            System.out.println(e.getMessage());
       }  


      database.transaction(db -> db.updateBinaryOutputStatus(new BinaryOutputStatus(index, status, new Flags(Flag.ONLINE), OutstationExample.now()), UpdateOptions.detectEvent()));
      
      
      return CommandStatus.SUCCESS;
    } else {        
        
           if (index.compareTo(ushort(10)) < 0 && (control.code.opType == OpType.PULSE_ON && control.code.tcc == TripCloseCode.TRIP)) {
                boolean status = false; 
                
                try {
                    gpio.pulseOutputOff(index.intValue(), 500);
                } catch (Exception e) {
                    System.out.println(e.getMessage());
                }               

                database.transaction(db -> db.updateBinaryOutputStatus(new BinaryOutputStatus(index, status, new Flags(Flag.ONLINE), OutstationExample.now()), UpdateOptions.detectEvent()));    
             
      
            return CommandStatus.SUCCESS;
           }       
      
    }
    
    return CommandStatus.NOT_SUPPORTED;
  }

  @Override
  public CommandStatus selectG41v1(int value, UShort index, DatabaseHandle database) {
    return selectAnalogOutput(index);
  }

  @Override
  public CommandStatus operateG41v1(
      int value, UShort index, OperateType opType, DatabaseHandle database) {
    return operateAnalogOutput(value, index, database);
  }

  @Override
  public CommandStatus selectG41v2(short value, UShort index, DatabaseHandle database) {
    return selectAnalogOutput(index);
  }

  @Override
  public CommandStatus operateG41v2(
      short value, UShort index, OperateType opType, DatabaseHandle database) {
    return operateAnalogOutput(value, index, database);
  }

  @Override
  public CommandStatus selectG41v3(float value, UShort index, DatabaseHandle database) {
    return selectAnalogOutput(index);
  }

  @Override
  public CommandStatus operateG41v3(
      float value, UShort index, OperateType opType, DatabaseHandle database) {
    return operateAnalogOutput(value, index, database);
  }

  @Override
  public CommandStatus selectG41v4(double value, UShort index, DatabaseHandle database) {
    return selectAnalogOutput(index);
  }

  @Override
  public CommandStatus operateG41v4(
      double value, UShort index, OperateType opType, DatabaseHandle database) {
    return operateAnalogOutput(value, index, database);
  }

  private CommandStatus selectAnalogOutput(UShort index) {
    return index.compareTo(ushort(10)) < 0 ? CommandStatus.SUCCESS : CommandStatus.NOT_SUPPORTED;
  }

  private CommandStatus operateAnalogOutput(double value, UShort index, DatabaseHandle database) {
    if (index.compareTo(ushort(10)) < 0) {
      database.transaction(db -> db.updateAnalogOutputStatus(new AnalogOutputStatus(index, value, new Flags(Flag.ONLINE), OutstationExample.now()), UpdateOptions.detectEvent()));
      return CommandStatus.SUCCESS;
    }
    else
    {
      return CommandStatus.NOT_SUPPORTED;
    }
  }
}
// ANCHOR_END: control_handler

class TestConnectionStateListener implements ConnectionStateListener {
  @Override
  public void onChange(ConnectionState state) {
    System.out.println("Connection state change: " + state);
  }
}






public class OutstationExample {
    
    // Variables globales (de clase)
  private static int binaryPoints;
  private static int analogPoints;
  private static int binaryOutputStatus;
  private static int counterPoints; 
  
  

  private static OutstationConfig getOutstationConfig(UShort outstationAddress, UShort masterAddress) {
    OutstationConfig config = new OutstationConfig(
        outstationAddress, // outstation address
        masterAddress, // master address
        new EventBufferConfig(
             ushort(10), // binary
             ushort(10), // double-bit binary
             ushort(10), // binary output status
             ushort(5), // counter
             ushort(5), // frozen counter
             ushort(5), // analog
             ushort(5), // analog output status
             ushort(3) // octet string
        )
    );
    
    OutstationFeatures outstationFeatures = new OutstationFeatures();
    outstationFeatures.withUnsolicited(false);
    return config.withDecodeLevel(new DecodeLevel().withApplication(AppDecodeLevel.OBJECT_HEADERS).withLink(LinkDecodeLevel.PAYLOAD)).withFeatures(outstationFeatures);
  }

  private static void runTcp(Runtime runtime, String ip, String port, UShort outstationAddress, UShort masterAddress, GpioControllerPi4j gpio) {
    OutstationServer server = OutstationServer.createTcpServer(runtime, LinkErrorMode.CLOSE, ip + ":" + port);
    try {
      runServer(server, outstationAddress, masterAddress, gpio);
    } finally {
      server.shutdown();
    }
  }

  private static void runSerial(Runtime runtime, String comPort, UShort outstationAddress, UShort masterAddress, GpioControllerPi4j gpio) {
    Outstation outstation = Outstation.createSerialSession2(
        runtime,
        comPort,
        new SerialSettings(), // default settings
        Duration.ofSeconds(5), // open port every 5 seconds
        getOutstationConfig( outstationAddress, masterAddress),
        new TestOutstationApplication(),
        new TestOutstationInformation(),
        new TestControlHandler(gpio),
        state -> System.out.println("Port state change: " + state)
    );   
     
    
    runOutstation(outstation, gpio);
  }

  public static void main(String[] args) {
      
    displayLogo();
    GpioControllerPi4j gpio = new GpioControllerPi4j();    
      
    try (BufferedReader reader = new BufferedReader(new InputStreamReader(System.in))) {
      System.out.println("Select communication type: ");
      System.out.println("1. TCP");
      System.out.println("2. Serial");
      String choice = reader.readLine();

      System.out.println("Enter Outstation Address: ");
      UShort outstationAddress = ushort(Integer.parseInt(reader.readLine()));

      System.out.println("Enter Master Address: ");
      UShort masterAddress = ushort(Integer.parseInt(reader.readLine()));

      System.out.println("Enter number of binary points: ");
      binaryPoints = Integer.parseInt(reader.readLine());
      System.out.println("Enter number of binary output status points: ");
      binaryOutputStatus = Integer.parseInt(reader.readLine());
      System.out.println("Enter number of analog points: ");
      analogPoints = Integer.parseInt(reader.readLine());
      System.out.println("Enter number of counter points: ");
      counterPoints = Integer.parseInt(reader.readLine());

      // Setup logging
      Logging.configure(new LoggingConfig(), new TestLogger());
      // Create the Tokio runtime
      Runtime runtime = new Runtime(new RuntimeConfig());

      if ("1".equals(choice)) {
        System.out.println("\nEnter IP address: ");
        String ip = reader.readLine();
        System.out.println("\nEnter Port: ");
        String port = reader.readLine();
        runTcp(runtime, ip, port, outstationAddress, masterAddress, gpio);
      } else if ("2".equals(choice)) {
        System.out.println("\nEnter COM port: ");
        String comPort = reader.readLine();
        runSerial(runtime, comPort, outstationAddress, masterAddress, gpio);
      } else {
        System.out.println("\nInvalid choice");
      }
    } catch (Exception ex) {
      System.out.println("\nError: " + ex.getMessage());
    }
  }

  private static void runServer(OutstationServer server, UShort outstationAddress, UShort masterAddress, GpioControllerPi4j gpio) {
    Outstation outstation = server.addOutstation(
        getOutstationConfig(outstationAddress, masterAddress),
        new TestOutstationApplication(),
        new TestOutstationInformation(),
        new TestControlHandler(gpio),
        new TestConnectionStateListener(),
        AddressFilter.any()
    );
    server.bind();
    runOutstation(outstation, gpio);
  }

  private static void runOutstation(Outstation outstation, GpioControllerPi4j gpio) {
    outstation.transaction((db) -> initializeDatabase(db));
   
            
    boolean binaryValue = false;    
    boolean binaryOutputStatusValue = false;
    long counterValue = 0;    
    double analogValue = 0.0;
    
    final Flags onlineFlags = new Flags(Flag.ONLINE);
    final UpdateOptions detectEvent = UpdateOptions.detectEvent();
    
    outstation.setDecodeLevel( new DecodeLevel().withApplication(AppDecodeLevel.OBJECT_HEADERS).withLink(LinkDecodeLevel.NOTHING).withPhysical(PhysDecodeLevel.NOTHING));      
   
  
    
    for (int i = 0; i < 8; i++) {                
            
            int finalI = i;
            
            gpio.getInputs().get(i).addListener(event -> {
                DigitalState state = event.state();
                boolean pointValue = (state == DigitalState.HIGH);

                System.out.println("Entrada " + finalI + " (GPIO " + gpio + ") cambió a: " + state);

                UShort index = ushort(finalI);

                outstation.transaction(
                    db -> {
                        BinaryInput value = new BinaryInput(
                            index,
                            pointValue,
                            onlineFlags,
                            OutstationExample.now());
                        db.updateBinaryInput(value, detectEvent);
                    });
            });

        }
    
    
    
    try {
      while (true) {  
          
         BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));   
         System.out.println("Enter a command or type 'x' to exit:");
            String command = reader.readLine();
            if (command.equals("x")) {
                gpio.shutdown();                
                System.out.println("Saliendo...");
                System.exit(0);
            }            
          
        
        
        String[] parts = command.split(" ");
        String cmd = parts[0].toLowerCase();
        
        /*
        for (int i = 0; i < 8; i++) {
            final boolean value = gpio.readInput(i);
            final int index = i;

            outstation.transaction(db -> {
            db.updateBinaryInput(new BinaryInput(ushort(index),
                    value,
                    new Flags(Flag.ONLINE),
                    OutstationExample.now()),
                    UpdateOptions.detectEvent());
            });           
        }*/

        
        
        try {        

          
        switch (cmd) {
              
           case "help":{
             System.out.println("\n=== Selecciona tipo de punto ===");
             System.out.println("bi [punto] - Binary Input (toggle)");
             System.out.println("bos [punto]- Binary Output Status (toggle)");
             System.out.println("co [punto]- Counter (+1)");
             System.out.println("ai [punto] - Analog Input (+1.0)");
             System.out.println("dl [0-3] - Decode Level");
             System.out.println("      0: App(NOTHING) + Link(NOTHING) + Physical Link(NOTHING)");
             System.out.println("      1: App(OBJECT_HEADERS) + Link(NOTHING) + Physical Link(NOTHING)");
             System.out.println("      2: App(OBJECT_HEADERS) + Link(HEADER) + Physical Link(NOTHING)");
             System.out.println("      3: App(OBJECT_HEADERS) + Link(HEADER) + Physical Link(DATA)"); 
             System.out.println("      4: App(OBJECT_HEADERS) + Link(PAYLOAD) + Physical Link(DATA)");
           }
    break;     
              
              
              
            case "bi":
            {
                
             UShort index = ushort(Integer.parseInt(parts[1]));
                
              binaryValue = !binaryValue;
              final boolean pointValue = binaryValue;
              outstation.transaction(
                  db -> {
                    BinaryInput value =
                        new BinaryInput(
                            index,
                            pointValue,
                            onlineFlags,
                            now());
                    db.updateBinaryInput(value, detectEvent);
                  });
              break;
            }
            case "bos":
              //toggleBinaryOutputStatus(outstation, index);
              {
              binaryOutputStatusValue = !binaryOutputStatusValue;
              final boolean pointValue = binaryOutputStatusValue;
              UShort index = ushort(Integer.parseInt(parts[1]));
              outstation.transaction(
                  db -> {
                    BinaryOutputStatus value =
                        new BinaryOutputStatus(
                            index,
                            pointValue,
                            onlineFlags,
                            now());
                    db.updateBinaryOutputStatus(value, detectEvent);
                  });
              break;
            }
              
            case "co":
               {
              counterValue += 1;
              final long pointValue = counterValue;
               UShort index = ushort(Integer.parseInt(parts[1]));
              outstation.transaction(
                  db -> {
                    Counter value =
                        new Counter(
                            index,
                            uint(pointValue),
                            onlineFlags,
                            now());
                    db.updateCounter(value, detectEvent);
                  });
              break;
            }
            case "dl":
            { 
              //UShort index = ushort(Integer.parseInt(parts[1]));             
              if(Integer.parseInt(parts[1])==0)
                  
                  outstation.setDecodeLevel( new DecodeLevel().withApplication(AppDecodeLevel.NOTHING).withLink(LinkDecodeLevel.NOTHING).withPhysical(PhysDecodeLevel.NOTHING));
              else{
                  if(Integer.parseInt(parts[1])==1)
                      
                  outstation.setDecodeLevel( new DecodeLevel().withApplication(AppDecodeLevel.OBJECT_HEADERS).withLink(LinkDecodeLevel.NOTHING).withPhysical(PhysDecodeLevel.NOTHING));
                  else{
                      if(Integer.parseInt(parts[1])==2)
                          
                      outstation.setDecodeLevel( new DecodeLevel().withApplication(AppDecodeLevel.OBJECT_HEADERS).withLink(LinkDecodeLevel.HEADER).withPhysical(PhysDecodeLevel.NOTHING));
                      else{
                         if(Integer.parseInt(parts[1])==3)
                            
                         outstation.setDecodeLevel( new DecodeLevel().withApplication(AppDecodeLevel.OBJECT_HEADERS).withLink(LinkDecodeLevel.HEADER).withPhysical(PhysDecodeLevel.DATA));
                         else{
                           if(Integer.parseInt(parts[1])==4)
                            
                            outstation.setDecodeLevel( new DecodeLevel().withApplication(AppDecodeLevel.OBJECT_HEADERS).withLink(LinkDecodeLevel.PAYLOAD).withPhysical(PhysDecodeLevel.DATA));
                         }
                     } 
                  } 
              }
               break;
              }                 
             
            
            case "ai":
               {
              analogValue += 1;
              final double pointValue = analogValue;
               UShort index = ushort(Integer.parseInt(parts[1]));
              outstation.transaction(
                  db -> {
                    AnalogInput value =
                        new AnalogInput(
                            index,
                            pointValue,
                            onlineFlags,
                            now());
                    db.updateAnalogInput(value, detectEvent);
                  });
              break;
            }   
               
               
            default:
              System.out.println("Unknown type: " + cmd);
          }
        } catch (NumberFormatException e) {
          System.out.println("Invalid point index.");
        }
      }
     
      
    } catch (Exception ex) {
      System.out.println(ex.getMessage());
    }
    
    gpio.shutdown();
  }

  
  
 
  
  
   private static void initializeDatabase(Database db) {
    // Inicializar puntos binarios
    for (int i = 0; i < binaryPoints; i++) {
      //System.out.println("Inicializando punto binario " + i);
      db.addBinaryInput(ushort(i), EventClass.CLASS1, new BinaryInputConfig(StaticBinaryInputVariation.GROUP1_VAR1, EventBinaryInputVariation.GROUP2_VAR2));
    }

    // Inicializar puntos de estado de salida binaria
    for (int i = 0; i < binaryOutputStatus; i++) {
      //System.out.println("Inicializando estado de salida binaria " + i);
      db.addBinaryOutputStatus(ushort(i), EventClass.CLASS1, new BinaryOutputStatusConfig());
    }

    // Inicializar puntos de contadores
    for (int i = 0; i < counterPoints; i++) {
      //System.out.println("Inicializando contador " + i);
      db.addCounter(ushort(i), EventClass.CLASS3, new CounterConfig());
    }

    // Inicializar puntos analógicos
    for (int i = 0; i < analogPoints; i++) {
      //System.out.println("Inicializando punto analógico " + i);
      db.addAnalogInput(ushort(i), EventClass.CLASS2, new AnalogInputConfig());
    }

    // Inicializar estado de salida analógico
    for (int i = 0; i < analogPoints; i++) {
      //System.out.println("Inicializando estado de salida analógico " + i);
      db.addAnalogOutputStatus(ushort(i), EventClass.CLASS2, new AnalogOutputStatusConfig());
    }
  }

  static Timestamp now() {
    return Timestamp.synchronizedTimestamp(ulong(System.currentTimeMillis()));
  }
  
  
  static void displayLogo() {
    String[] logo = {
        "╔════════════════════════════════════════════════════════════════════╗",
        "║ ██╗    ██╗██╗   ██╗██╗████████╗██╗███╗   ███╗ █████╗ ████████╗██╗  ║",
        "║ ██║    ██║██║   ██║██║╚══██╔══╝██║████╗ ████║██╔══██╗╚══██╔══╝██║  ║",
        "║ ██║ █╗ ██║██║   ██║██║   ██║   ██║██╔████╔██║███████║   ██║   ██║  ║",
        "║ ██║███╗██║██║   ██║██║   ██║   ██║██║╚██╔╝██║██╔══██║   ██║   ██║  ║",
        "║ ╚███╔███╔╝╚██████╔╝██║   ██║   ██║██║ ╚═╝ ██║██║  ██║   ██║   ██║  ║",
        "║  ╚══╝╚══╝  ╚═════╝ ╚═╝   ╚═╝   ╚═╝╚═╝     ╚═╝╚═╝  ╚═╝   ╚═╝   ╚═╝  ║",
        "╚════════════════════════════════════════════════════════════════════╝"
    };

    for (String line : logo) {
        System.out.println(line);
    }
}
  
}