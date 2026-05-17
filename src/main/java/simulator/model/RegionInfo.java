package simulator.model;

import java.util.List;

// Interfaz que representa la información de una región, incluyendo la lista de animales que contiene. 
// Se extiende de JSONable para permitir convertir la información de la región a formato JSON, lo que es útil para guardar el estado del simulador o para mostrarlo en un visor gráfico.
public interface RegionInfo extends JSONable {
  public List<AnimalInfo> getAnimalsInfo(); //
}
