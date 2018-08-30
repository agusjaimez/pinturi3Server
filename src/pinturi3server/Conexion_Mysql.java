/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pinturi3server;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 *
 * @author agustin
 */
public class Conexion_Mysql {
    private static Connection cnx = null;

    public Conexion_Mysql(){
        
    }
    
    public static Connection obtener() throws SQLException, ClassNotFoundException {
        if (cnx == null) {
            try {
                Class.forName("com.mysql.jdbc.Driver");
                cnx = DriverManager.getConnection("jdbc:mysql://localhost/Pinturi3", "root", "43811871");
            } catch (SQLException ex) {
                throw new SQLException(ex);
            } catch (ClassNotFoundException ex) {
                throw new ClassCastException(ex.getMessage());
            }
        }
        return cnx;
    }
    
        public static void cerrar(Connection conexion, String direccion) throws SQLException {
        if (cnx != null) {
            PreparedStatement consulta;
            consulta=conexion.prepareStatement("delete from personas where direccion='"+direccion+"'");
            consulta.execute();
        }
    }
        public void resetear(Connection conexion) throws SQLException{
            PreparedStatement consulta;
            consulta=conexion.prepareStatement("drop table personas");
            consulta.execute();
            consulta=conexion.prepareStatement("create table personas (id INT not null AUTO_INCREMENT primary key, persona varchar(45), direccion varchar(45))");
            consulta.execute();
        }
}
