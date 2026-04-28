package simulator.factories;

import org.json.JSONObject;

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