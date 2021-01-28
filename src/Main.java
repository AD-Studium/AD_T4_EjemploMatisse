import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Scanner;

import com.matisse.MtDatabase;
import com.matisse.MtException;
import com.matisse.MtObjectIterator;
import com.matisse.MtPackageObjectFactory;

import biblioteca.Autor;
import biblioteca.Libro;
import biblioteca.Obra;

public class Main {
	static String hostname = "localhost";
	static String dbname = "biblio";
	static Scanner teclado= new Scanner(System.in);
	static int menu;

	public static void main(String[] args) {
		do {
		System.out.println("Elije que hacer:");
		System.out.println("1-Crear");
		System.out.println("2-Modificar");
		System.out.println("3-Consultar Autor");
		System.out.println("4-Borrar");
		menu=Integer.parseInt(teclado.nextLine());
		if(menu==1) {
			creaObjetos(hostname, dbname);
		}else if(menu==2) {
			modificaObjeto(hostname, dbname, "Haruki", 20);
		}else if(menu==3) {
			ejecutaOQL(hostname, dbname);
		}else if(menu==4) {
			borrarTodos(hostname, dbname);
		}else {
			System.out.println("Debe seleccionar una opcion valida");
		}
	}while(menu!=0);
		System.out.println("Saliendo del programa");
		teclado.close();
		
		
		
	} 
	//Crear
	public static void creaObjetos(String hostname, String dbname) {
		try {
			// Abre la base de datos con el Hostname (localhost), dbname (biblioteca1) y el namespace "biblioteca1".
			MtDatabase db = new MtDatabase(hostname, dbname, new MtPackageObjectFactory("", " biblioteca"));
			db.open();
			db.startTransaction();
			System.out.println("Conectado a la base de datos " +
					db.toString() + " de Matisse");
			// Crea un objeto Autor
			Autor a1 = new Autor(db);
			a1.setNombre("Haruki");
			a1.setApellidos("Murakami");
			a1.setEdad(53);
			System.out.println("Libro 'Baila Baila Baila' creado...");
			// Crea un objeto Libro
			Libro l1 = new Libro(db);
			l1.setTitulo("Baila Baila Baila");
			l1.setEditorial("TusQuests");
			l1.setPaginas(512);
			// Crea otro objeto libro
			Libro l2 = new Libro(db);
			l2.setTitulo("Tokio Blues");
			l2.setEditorial("TusQuests");
			l2.setPaginas(498);
			System.out.println("Libro 'Herejía' creado...");
			// Crea un array de Obras para guardar los libros y hacer las relaciones
			Obra o1[] = new Obra[2];
			o1[0] = l1;
			o1[1] = l2;
			// Guarda las relaciones del autor con los libros que ha escrito.
			a1.setEscribe(o1);
			// Ejecuta un commit para materializar las peticiones.
			db.commit();
			// Cierra la base de datos.
			db.close();
			System.out.println("\nHecho.");
		} catch (MtException mte) {
			System.out.println("MtException : " + mte.getMessage());
		}
	} 
	// Borrar todos los objetos de una clase
	public static void borrarTodos(String hostname, String dbname) {
		System.out.println("====================== Borrar Todos=====================\n");
		try {
			MtDatabase db = new MtDatabase(hostname, dbname, new
					MtPackageObjectFactory("", "biblioteca"));
			db.open();
			db.startTransaction();
			// Lista todos los objetos Obra que hay en la base de datos, con el método
			// getInstanceNumber
			System.out.println("\n" + Obra.getInstanceNumber(db) + "Obra(s) en la DB.");
			// Borra todas las instancias de Obra
			Obra.getClass(db).removeAllInstances();
			// materializa los cambios y cierra la BD
			db.commit();
			db.close();
			System.out.println("\nHecho.");
		} catch (MtException mte) {
			System.out.println("MtException : " + mte.getMessage());
		}
	}
	//Modificar
	public static void modificaObjeto(String hostname, String dbname, String
			nombre, Integer nuevaEdad) {
		System.out.println("=========== Modifica un objeto==========\n");
		int nAutores = 0;
		try {
			MtDatabase db = new MtDatabase(hostname, dbname, new
					MtPackageObjectFactory("", "biblioteca"));
			db.open();
			db.startTransaction();
			// Lista cuántos objetos Obra con el método getInstanceNumber
			System.out.println("\n" + Autor.getInstanceNumber(db) + "Autores en la DB.");
			nAutores = (int) Autor.getInstanceNumber(db);
			// Crea un Iterador (propio de Java)
			MtObjectIterator<Autor> iter =Autor.<Autor>instanceIterator(db);
			System.out.println("recorro el iterador de uno en uno y cambio cuando encuentro 'nombre'");
			while (iter.hasNext()) {
				Autor[] autores = iter.next(nAutores);
				for (int i = 0; i < autores.length; i++) {
					// Busca una autor con nombre 'nombre'
					if (autores[i].getNombre().compareTo(nombre)
							== 0) {
						autores[i].setEdad(nuevaEdad);
					} else {
					}
				}
			}
			iter.close();
			// materializa los cambios y cierra la BD
			db.commit();
			db.close();
			System.out.println("\nHecho.");
		} catch (MtException mte) {
			System.out.println("MtException : " + mte.getMessage());
		}
	} 
	//Mostrar
	public static void ejecutaOQL(String hostname, String dbname) {
		@SuppressWarnings("resource")
		MtDatabase dbcon = new MtDatabase(hostname, dbname);
		// Abre una conexión a la base de datos
		dbcon.open();
		Connection jdbcon = dbcon.getJDBCConnection();
		try {
			// Crea una instancia de Statement
			Statement stmt = dbcon.createStatement();
			// Asigna una consulta OQL. Esta consulta lo que hace es utilizar REF() para
			// obtener el objeto
			// directamente en vez de obtener valores concretos (que también podría ser).
			String commandText = "SELECT REF(a) from biblioteca.Autor a;";
			// Ejecuta la consulta y obtiene un ResultSet
			ResultSet rset = stmt.executeQuery(commandText);
			Autor a1;
			// Lee rset uno a uno.
			while (rset.next()) {
				// Obtiene los objetos Autor.
				a1 = (Autor) rset.getObject(1);
				// Imprime los atributos de cada objeto con un formato determinado. 
				System.out.println("Autor: " +
						String.format("%16s", a1.getNombre())
				+ String.format("%16s",
						a1.getApellidos()) + " Spouse: " + String.format("%16s", a1.getEdad()));
			}
			// Cierra las conexiones
			rset.close();
			stmt.close();
		} catch (SQLException e) {
			System.out.println("SQLException: " + e.getMessage());
		}
		//dbcon.close();
	}
}
