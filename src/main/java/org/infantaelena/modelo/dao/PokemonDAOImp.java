package org.infantaelena.modelo.dao;

import org.infantaelena.excepciones.PokemonNotFoundException;
import org.infantaelena.excepciones.PokemonRepeatedException;
import org.infantaelena.modelo.entidades.Pokemon;
import org.infantaelena.modelo.entidades.TipoPokemon;

import java.io.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;


/**
 * Clase que implementa los métodos de acceso a datos de la clase Pokemon
 * Esta puede hacerse mediante un fichero CSV separado por puntos y coma o una base de datos
 *
 * @author Fernando
 * @author Pablo
 * @version 2.0
 * @since 13/05/2023
 */
public class PokemonDAOImp implements PokemonDAO {
    private final String RUTA ="jdbc:sqlite:main/resources/";
    private final String DEFAULT_DB = "pokedex.db";
    private final String POKEMON_TABLE = "CREATE TABLE IF NOT EXISTS pokemon (\n"
            + " nombre VARCHAR(50) NOT NULL UNIQUE,\n"
            + " tipoPrimario VARCHAR(15),\n"
            + " tipoSecundario VARCHAR(15),\n"
            + " puntosSalud INT,\n"
            + " ataque INT,\n"
            + " defensa INT,\n"
            + " ataqueEspecial INT,\n"
            + " defensaEspecial INT,\n"
            + " velocidad INT,\n"
            + ");\n";

    private final String POKEMON_UPDATE ="UPDATE pokemon SET tipoPrimario = ?, tipoSecundario = ?, puntoSalud = ?," +
            " ataque = ?, defensa = ?, ataqueEspecial = ?, defensaEspecial = ?, velocidad = ? ;";
    private final String POKEMON_INSERT= "INSERT INTO pokemon (nombre, tipoPrimario, tipoSecundario, puntosSalud," +
            " ataque, defensa, ataqueEspecial, defensaEspecial, velocidad) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?);";

    private final String POKEMON_DELETE = "DELETE FROM pokemon ";
    private final String POKEMON_SEARCH ="SELECT * FROM pokemon ";
    private final String BY_NAME = " WHERE nombre LIKE ";
    private final String END_SQL= ";";
    private Connection connection;
    private Statement statement;

    /**
     * Crea un nuevo objeto PokemonDAOImp utilizando la base de datos por defecto.
     */
   public PokemonDAOImp (){
        try {
            connection = DriverManager.getConnection(RUTA+DEFAULT_DB);
            statement = connection.createStatement();

            String createTableSQL = POKEMON_TABLE;
            statement.execute(createTableSQL);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Crea un nuevo objeto PokemonDAOImp utilizando la base de datos especificada.
     *
     * @param db el nombre de la base de datos
     */
  public  PokemonDAOImp (String db){
        try {
            connection = DriverManager.getConnection("jdbc:sqlite:resources/"+db);
            String createTableSQL = POKEMON_TABLE;
            statement.execute(createTableSQL);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Crea un nuevo registro de Pokemon en la base de datos.
     *
     * @param pokemon el Pokemon a crear
     * @throws PokemonRepeatedException si se intenta crear un Pokemon con un nombre repetido
     */
    @Override
    public void crear(Pokemon pokemon) throws PokemonRepeatedException {
        Pokemon encontrado = null;
        try {
            encontrado = leerPorNombre(pokemon.getNombre());
        } catch (PokemonNotFoundException e) {

        }
        if (encontrado != null) {
            throw new PokemonRepeatedException();
        } else {
            trabajoSQL(POKEMON_INSERT, pokemon);
        }
    }

    /**
     * Lee un Pokemon de la base de datos por su nombre.
     *
     * @param nombre el nombre del Pokemon a buscar
     * @return el Pokemon encontrado
     * @throws PokemonNotFoundException si no se encuentra un Pokemon con el nombre especificado
     */
    @Override
    public Pokemon leerPorNombre(String nombre) throws PokemonNotFoundException {
        Pokemon pokemonBuscado = null;
        try (ResultSet buscar = statement.executeQuery(POKEMON_SEARCH + BY_NAME + nombre + END_SQL)) {
            if (!buscar.next()) {
                throw new PokemonNotFoundException();
            } else {
                pokemonBuscado = leerPokemon(buscar);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return pokemonBuscado;
    }

    /**
     * Lee todos los Pokemon de la base de datos.
     *
     * @return una lista de Pokemon
     */
    @Override
    public List<Pokemon> leerTodos() {
        List<Pokemon> listaPokemon = new ArrayList<>();
        try (ResultSet allPokedex = statement.executeQuery(POKEMON_SEARCH + END_SQL)) {
            while (allPokedex.next()) {
                Pokemon pokemon = leerPokemon(allPokedex);
                listaPokemon.add(pokemon);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return listaPokemon;
    }

    /**
     * Actualiza un Pokemon existente en la base de datos.
     *
     * @param pokemon el Pokemon a actualizar
     * @throws PokemonNotFoundException si no se encuentra un Pokemon con el nombre especificado
     */
    @Override
    public void actualizar(Pokemon pokemon) throws PokemonNotFoundException {
        String nombreAbuscar = pokemon.getNombre();

        try (ResultSet pokemonEnPokedex = statement.executeQuery(POKEMON_SEARCH + BY_NAME + nombreAbuscar + END_SQL)) {
            if (!pokemonEnPokedex.next()) {
                throw new PokemonNotFoundException();
            } else {
                trabajoSQL(POKEMON_UPDATE+BY_NAME+nombreAbuscar+END_SQL, pokemon);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Elimina un Pokemon de la base de datos por su nombre.
     *
     * @param nombre el nombre del Pokemon a eliminar
     * @throws PokemonNotFoundException si no se encuentra un Pokemon con el nombre especificado
     */
    @Override
    public void eliminarPorNombre(String nombre) throws PokemonNotFoundException {
        Pokemon pokemonABorrar = null;

            pokemonABorrar = leerPorNombre(nombre);

            if(pokemonABorrar == null){
                throw new PokemonNotFoundException();
            } else{
                try (ResultSet borrar = statement.executeQuery(POKEMON_DELETE + BY_NAME + nombre + END_SQL)){
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            }


        }

    // FUNCIONES AUXILIARES
    /**
     * Lee los datos de un ResultSet y crea un objeto Pokemon.
     *
     * @param resultBusqueda el ResultSet que contiene los datos del Pokemon
     * @return el Pokemon creado
     */
    private Pokemon leerPokemon(ResultSet resultBusqueda) {
        Pokemon pokemonBuscado;
        try {
            String nombre = resultBusqueda.getNString("nombre");
            TipoPokemon tipPri = TipoPokemon.valueOf(resultBusqueda.getString("tipoPrimario"));
            TipoPokemon tipSec = TipoPokemon.valueOf(resultBusqueda.getString("tipoSecundario"));
            int puntosSalud = resultBusqueda.getInt("puntosSalud");
            int ataque = resultBusqueda.getInt("ataque");
            int defensa = resultBusqueda.getInt("defensa");
            int ataqueEspecial = resultBusqueda.getInt("ataqueEspecial");
            int defensaEspecial = resultBusqueda.getInt("defensaEspecial");
            int velocidad = resultBusqueda.getInt("velocidad");
            pokemonBuscado = new Pokemon(nombre, tipPri, tipSec,
                    puntosSalud, ataque, defensa, ataqueEspecial, defensaEspecial, velocidad);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            if (resultBusqueda != null) {
                try {
                    resultBusqueda.close();
                } catch (SQLException e) {

                }
            }
        }

        return pokemonBuscado;
    }
    /**
     * Ejecuta una consulta SQL para realizar operaciones en la base de datos.
     *
     * @param sqlQuery la consulta SQL a ejecutar
     * @param pokemon el Pokemon utilizado para completar los parámetros de la consulta
     */

    public void trabajoSQL(String sqlQuery, Pokemon pokemon) {
        try (PreparedStatement updateStatement = connection.prepareStatement(sqlQuery)) {
            updateStatement.setString(1, pokemon.getNombre());
            updateStatement.setString(2, pokemon.getTipoPrimario().toString());
            updateStatement.setString(3, pokemon.getTipoSecundario().toString());
            updateStatement.setInt(4, pokemon.getPuntosSalud());
            updateStatement.setInt(5, pokemon.getAtaque());
            updateStatement.setInt(6, pokemon.getDefensa());
            updateStatement.setInt(7, pokemon.getAtaqueEspecial());
            updateStatement.setInt(8, pokemon.getDefensaEspecial());
            updateStatement.setInt(9, pokemon.getVelocidad());
            updateStatement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
