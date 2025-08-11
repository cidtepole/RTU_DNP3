/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package io.stepfunc.dnp3.examples;

/**
 *
 * @author DELLCID
 */
import com.pi4j.Pi4J;
import com.pi4j.context.Context;
import com.pi4j.io.gpio.digital.*;
import com.pi4j.util.Console;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class GpioControllerPi4j {

    private final Context pi4j;
    
   private final  Map<Integer, DigitalOutput> outputs_On = new HashMap<>();
   private final  Map<Integer, DigitalOutput> outputs_Off = new HashMap<>();
   private final  Map<Integer, DigitalInput> inputs = new HashMap<>();    

    // GPIOs específicos: cambia estos a tus necesidades
    private final int[] inputGpios = {4, 17, 27, 22, 5, 6, 13, 26};    // GPIOs para entradas
    
    private final int[] gpioPins_On = {23, 12};
    
    private final int[] gpioPins_Off = {18, 25};  

    
    public GpioControllerPi4j() {
        
        pi4j = Pi4J.newAutoContext();
        
        var console = new Console();
        
        System.out.println("Proveedores registrados:");
        pi4j.providers().all().forEach((id, provider) -> {
        System.out.println(" - " + id + " => " + provider.getClass().getName());
        });
        
        
        // ------------------------------------------------------------
        // Output Pi4J Context information
        // ------------------------------------------------------------
        // The created Pi4J Context initializes platforms, providers
        // and the I/O registry. To help you to better understand this
        // approach, we print out the info of these. This can be removed
        // from your own application.
        // OPTIONAL
        PrintInfo.printLoadedPlatforms(console, pi4j);
        PrintInfo.printDefaultPlatform(console, pi4j);
        PrintInfo.printProviders(console, pi4j);
        
        
        initGpios();
        /*
        initGpios();
      */// create a digital output instance using the default digital output provider
      /*
        var output = pi4j.dout().create(23);
        output.config().shutdownState(DigitalState.HIGH);

        // setup a digital output listener to listen for any state changes on the digital output
        output.addListener(System.out::println);

        // lets invoke some changes on the digital output
        System.out.println("Encender LED");
        output.state(DigitalState.HIGH);*/

    }
    
    
    
    
    private void initGpios(){
    
            // Inicializar entradas digitales
        for (int i = 0; i < inputGpios.length; i++) {
            int gpio = inputGpios[i];

            var inputConfig = DigitalInput.newConfigBuilder(pi4j)
                    .id("entrada-" + i)
                    .name("Entrada " + i + " (GPIO " + gpio + ")")
                    .address(gpio)
                    .pull(PullResistance.PULL_DOWN)                    
                    .debounce(5000L);     
       
            var input = pi4j.create(inputConfig);           
            
            inputs.put(i, input);
        }

        // Inicializar salidas digitales
        for (int i = 0; i < gpioPins_On.length; i++) {
            
            int gpio = gpioPins_On[i];       

            var output = pi4j.digitalOutput().create(gpio);
            output.config().shutdownState(DigitalState.LOW);
            outputs_On.put(i, output); // Usa el índice lógico 0–7
        }        
        
        for (int i = 0; i < gpioPins_Off.length; i++) {
            
            int gpio = gpioPins_Off[i];       

            var output = pi4j.digitalOutput().create(gpio);
            output.config().shutdownState(DigitalState.LOW);
            outputs_Off.put(i, output); // Usa el índice lógico 0–7
        }    
    
    }    
    

   public boolean readInput(int index) {
        DigitalInput input = inputs.get(index);
        if (input != null) {
            return input.state().isHigh();
        }
        throw new IllegalArgumentException("Índice de entrada fuera de rango: " + index);
    }
   

    public void setOutputOn(int index, boolean value) {
    DigitalOutput output = outputs_On.get(index);
    if (output != null) {
        output.state(value ? DigitalState.HIGH : DigitalState.LOW);
    } else {
        throw new IllegalArgumentException("Índice de salida ON no definido: " + index);
    }
}

public void setOutputOff(int index, boolean value) {
    DigitalOutput output = outputs_Off.get(index);
    if (output != null) {
        output.state(value ? DigitalState.HIGH : DigitalState.LOW);
    } else {
        throw new IllegalArgumentException("Índice de salida OFF no definido: " + index);
    }
}


public void pulseOutputOn(int index, int durationMs) {
    DigitalOutput output = outputs_On.get(index);
    if (output != null) {
        //System.out.println("PULSE_ON_CLOSE ejecutado");
       output.pulse(durationMs, TimeUnit.MILLISECONDS);
       System.out.println("PULSE_ON_CLOSE ejecutado");
    } else {
        throw new IllegalArgumentException("Índice de salida ON no definido: " + index);
    }
}

public void pulseOutputOff(int index, int durationMs) {
    DigitalOutput output = outputs_Off.get(index);
    if (output != null) {
        //System.out.println("PULSE_ON_CLOSE ejecutado");
        output.pulse(durationMs, TimeUnit.MILLISECONDS);
        System.out.println("PULSE_ON_TRIP ejecutado");
    } else {
        throw new IllegalArgumentException("Índice de salida OFF no definido: " + index);
    }
}


public Map<Integer, DigitalInput> getInputs() {
    return inputs;
}

public Map<Integer, DigitalOutput> getOutputsOn() {
    return outputs_On;
}

public Map<Integer, DigitalOutput> getOutputsOff() {
    return outputs_Off;
}



    public void shutdown() {
        pi4j.shutdown();
    }
}












 