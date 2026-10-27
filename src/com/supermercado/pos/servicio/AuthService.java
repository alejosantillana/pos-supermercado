package com.supermercado.pos.servicio;

import com.supermercado.pos.dao.UsuarioDAO;
import com.supermercado.pos.dominio.Usuario;
import com.supermercado.pos.excepcion.AutorizacionException;
import com.supermercado.pos.util.Conexion;
import com.supermercado.pos.util.Seguridad;

import java.sql.Connection;
import java.sql.SQLException;

public class AuthService {
    private final UsuarioDAO usuarioDao = new UsuarioDAO();

    public Usuario autenticar(String username, char[] clave) {
        try (Connection con = Conexion.obtener()) {
            return usuarioDao.buscarPorUsername(con, username)
                    .filter(Usuario::isActivo)
                    .filter(u -> Seguridad.verificar(clave, u.getPasswordHash()))
                    .orElseThrow(() -> new AutorizacionException("Usuario o contraseña inválidos"));
        } catch (SQLException e) {
            throw new IllegalStateException("Error de base de datos", e);
        }
    }
}