package simulator.factories;

import java.util.*;
import org.json.JSONObject;

public class BuilderBasedFactory<T> implements Factory<T> { //Dado un JSON con un campo "type", encuentra el builder adecuado y crea el objeto correspondiente.

    private Map<String, Builder<T>> builders; //Usar un HashMap permite encontrar el builder correcto en poco tiempo.
    private List<JSONObject> buildersInfo;    //Una lista con la información descriptiva de cada builder.

    //Constructor vacío.
    public BuilderBasedFactory() {
        
        builders = new HashMap<>();
        buildersInfo = new LinkedList<>(); //Se usa LinkedList para buildersInfo porque solo se necesita iterar sobre ella y añadir al final, no acceder por índice.
    }

    //Constructor con lista de builders.
    public BuilderBasedFactory(List<Builder<T>> builders) {
        this();

        if (builders == null)
            throw new IllegalArgumentException("Builders list cannot be null");

        //Llama a addBuilder(b) para cada constructir b en builders. Es el que se usa en Main.initFactories().
        for (Builder<T> b : builders) {
            addBuilder(b);
        }
    }

    public void addBuilder(Builder<T> b) {
        if (b == null)
            throw new IllegalArgumentException("Builder cannot be null");

        //Mete el builder en el HashMap con su tag como clave.
        builders.put(b.getTypeTag(), b);

        //Añade su información descriptiva a la lista buildersInfo.
        buildersInfo.add(b.getInfo());
    }

    @Override
    public T createInstance(JSONObject info) { //Recibe un JSON completo y devuelve el objeto construido.

        if (info == null)
            throw new IllegalArgumentException("'info' cannot be null");

        if (!info.has("type"))
            throw new IllegalArgumentException("Missing type field: " + info);

        String type = info.getString("type");
        Builder<T> b = builders.get(type); //Busca el builder.

        if (b != null) {
            JSONObject data = info.has("data")
                    ? info.getJSONObject("data")
                    : new JSONObject(); //Si el JSON tiene campo "data" lo usa, si no crea uno vacío.

            T result = b.createInstance(data); //b es el builder que encontró el HashMap. Se llama a su método createInstance pasándole únicamente el contenido de "data".

            if (result != null)
                return result;
        }

        // If no builder is found or the result is null ...
        throw new IllegalArgumentException("Unrecognized 'info': " + info.toString());
    }

    @Override
    public List<JSONObject> getInfo() { //Devuelve la lista de información de todos los builders registrados (NO modificable).
        return Collections.unmodifiableList(buildersInfo);
    }
}
