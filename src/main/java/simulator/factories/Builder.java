package simulator.factories;

import org.json.JSONObject;

/* 
Clase abstracta que representa un constructor genérico para crear objetos a partir de un JSON. 
Cada subclase de Builder se especializa en construir un tipo específico de objeto (por ejemplo, animales, estrategias de apareamiento, etc.) y proporciona la lógica necesaria para interpretar el JSON y crear la instancia correspondiente. 
El método createInstance es abstracto y debe ser implementado por cada subclase para definir cómo se construye el objeto concreto a partir del JSON proporcionado. 
Además, el método getInfo devuelve un JSON con información sobre el tipo de objeto que construye el builder, su descripción y cualquier dato adicional que pueda ser relevante para entender cómo usarlo.
*/

/*
Esta es la clase abstracta de la que heredan todos los constructores.
El objetivo de un Builder es separar la lectura del JSON de la creación del objeto.
@param <T> El tipo de objeto que construye (Animal, Region, o SelectionStrategy).
*/

public abstract class Builder<T> {
    private String typeTag; //Identificador del builder (Sheep, wolf, selectClosest...) que usará BuliderBasedFactory para encontrar el builder correcto.
    private String desc; //Descripción breve.

    public Builder(String typeTag, String desc) {
        if (typeTag == null || desc == null || typeTag.trim().isEmpty() || desc.trim().isEmpty())
            throw new IllegalArgumentException("Invalid type/desc");
        this.typeTag = typeTag;
        this.desc = desc;
    }

    public String getTypeTag() {
        return typeTag;
    }

    public JSONObject getInfo() { //Construye un JSON que describe este builder.
        JSONObject info = new JSONObject();
        info.put("type", typeTag);
        info.put("desc", desc);
        JSONObject data = new JSONObject();
        fillInData(data);
        info.put("data", data);
        return info;
    }

    protected void fillInData(JSONObject o) { //Las subclases lo sobreescriben si necesitan añadir información extra al JSON.
    }

    @Override
    public String toString() {
        return desc;
    }

    protected abstract T createInstance(JSONObject data); //Cada subclase obligatoriamente debe implementarlo para construir su objeto concreto.
}