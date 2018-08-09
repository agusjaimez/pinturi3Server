/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pinturi3server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Enumeration;
import java.util.Vector;

/**
 *
 * @author agustin
 */
public class Pinturi3Server implements Runnable {

    public final static int MAX_CONEXIONES = 40;

    private int puerto = 42066;
    private int numConexiones = 0;
    private Vector conexiones = null;

    public Pinturi3Server() {
    }

    public static void main(String[] args) {
        String puerto = null;

        //Se puede recibir el puerto a escuchar.
        if (args.length > 0) {
            puerto = args[0];
        }

        Pinturi3Server ps = new Pinturi3Server();

        ps.iniciar();
    }

    private void iniciar() {
        this.conexiones = new Vector(MAX_CONEXIONES);
        Thread t = new Thread(this);
        t.start();
    }

    @Override
    public void run() {
        ServerSocket socketServer = null;

        Socket socketCliente;

        //Se crea el socket que escucha el servidor
        try {
            System.out.println("Intentando inciar el servidor");
            socketServer = new ServerSocket(puerto, 5);
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(-1);
        }

        System.out.println("Servidor iniciado en puerto " + puerto);

        //Ejecutar el ciclo escuchar/aceptar por siempre
        while (true) {
            try {
                //Esperar hasta que se reciba una conexion.
                socketCliente = socketServer.accept();
                procesarConexion(socketCliente);
            } catch (Exception e) {
                System.out.println("Imposible crear socket cliente.");
                e.printStackTrace();
            }
        }
    }

    public void procesarConexion(Socket socketCliente) {
        synchronized (this) {
            //si se llego al máximo de conexiones, bloquear el
            //hilo de recepción hasta que haya un lugar.

            while (numConexiones == MAX_CONEXIONES) {
                try {
                    wait();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            numConexiones++;
        }
        ConexionCliente con = new ConexionCliente(socketCliente);
        Thread t = new Thread(con);
        t.start();
        conexiones.addElement(con);
    }

    public synchronized void conexionCerrada(ConexionCliente conexion) {
        conexiones.removeElement(conexion);
        numConexiones--;
        notify();
    }

    public void enviarMesajeClientes(String cliente, Object[] mensaje) {
        Enumeration cons = conexiones.elements();

        while (cons.hasMoreElements()) {
            ConexionCliente c = (ConexionCliente) cons.nextElement();
            c.enviarMensaje(mensaje);
        }
    }

    class ConexionCliente implements Runnable {

        private Socket socketCliente = null;
        private ObjectOutputStream flujoSalida = null;
        private ObjectInputStream flujoEntrada = null;

        public ConexionCliente(Socket s) {
            socketCliente = s;
        }

        public void run() {
            OutputStream socketSalida;
            InputStream socketEntrada;
            String nombreCliente;
            Object[] mensajeCliente;
            InetAddress direccion;

            try {
                socketSalida = socketCliente.getOutputStream();
                flujoSalida = new ObjectOutputStream(socketSalida);

                socketEntrada = socketCliente.getInputStream();
                flujoEntrada = new ObjectInputStream(socketEntrada);
                System.out.println(flujoEntrada);

                direccion = socketCliente.getInetAddress();
                nombreCliente = direccion.getHostName();
                
                mensajeCliente = (Object[])flujoEntrada.readObject();

                while ((mensajeCliente = (Object[])flujoEntrada.readObject()) != null) {
                    enviarMesajeClientes(nombreCliente, mensajeCliente);
                }
            } catch (Exception e) {
            } finally {
                try {
                    if (flujoEntrada != null) {
                        flujoEntrada.close();
                    }
                    if (flujoSalida != null) {
                        flujoSalida.close();
                    }
                    socketCliente.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                conexionCerrada(this);
            }
        }

        public void enviarMensaje(Object[] mensaje) {
            try {
                flujoSalida.writeObject(mensaje);
                flujoSalida.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

}
