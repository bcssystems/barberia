package com.bcsystems.barberia_api.config;

import com.bcsystems.barberia_api.domain.Permiso;
import com.bcsystems.barberia_api.domain.Rol;
import com.bcsystems.barberia_api.domain.Usuario;
import com.bcsystems.barberia_api.repository.PermisoRepository;
import com.bcsystems.barberia_api.repository.RolRepository;
import com.bcsystems.barberia_api.repository.UsuarioRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Configuration
public class DataInitializer {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private static final Map<String, String[]> PERMISOS = new LinkedHashMap<>();

    static {
        PERMISOS.put("DASHBOARD_VER", new String[]{"DASHBOARD", "Ver dashboard", "Acceso al panel principal"});

        PERMISOS.put("CLIENTES_VER", new String[]{"CLIENTES", "Ver clientes", "Consultar el listado de clientes"});
        PERMISOS.put("CLIENTES_CREAR", new String[]{"CLIENTES", "Crear clientes", "Registrar nuevos clientes"});
        PERMISOS.put("CLIENTES_EDITAR", new String[]{"CLIENTES", "Editar clientes", "Modificar datos de clientes"});
        PERMISOS.put("CLIENTES_ELIMINAR", new String[]{"CLIENTES", "Eliminar clientes", "Dar de baja clientes"});

        PERMISOS.put("EMPLEADOS_VER", new String[]{"EMPLEADOS", "Ver empleados", "Consultar el listado de empleados"});
        PERMISOS.put("EMPLEADOS_CREAR", new String[]{"EMPLEADOS", "Crear empleados", "Registrar nuevos empleados"});
        PERMISOS.put("EMPLEADOS_EDITAR", new String[]{"EMPLEADOS", "Editar empleados", "Modificar datos de empleados"});
        PERMISOS.put("EMPLEADOS_ELIMINAR", new String[]{"EMPLEADOS", "Eliminar empleados", "Dar de baja empleados"});

        PERMISOS.put("SERVICIOS_VER", new String[]{"SERVICIOS", "Ver servicios", "Consultar el catálogo de servicios"});
        PERMISOS.put("SERVICIOS_CREAR", new String[]{"SERVICIOS", "Crear servicios", "Agregar servicios al catálogo"});
        PERMISOS.put("SERVICIOS_EDITAR", new String[]{"SERVICIOS", "Editar servicios", "Modificar servicios"});
        PERMISOS.put("SERVICIOS_ELIMINAR", new String[]{"SERVICIOS", "Eliminar servicios", "Quitar servicios del catálogo"});

        PERMISOS.put("PRODUCTOS_VER", new String[]{"PRODUCTOS", "Ver productos", "Consultar el catálogo de productos"});
        PERMISOS.put("PRODUCTOS_CREAR", new String[]{"PRODUCTOS", "Crear productos", "Agregar productos al catálogo"});
        PERMISOS.put("PRODUCTOS_EDITAR", new String[]{"PRODUCTOS", "Editar productos", "Modificar productos"});
        PERMISOS.put("PRODUCTOS_ELIMINAR", new String[]{"PRODUCTOS", "Eliminar productos", "Quitar productos del catálogo"});

        PERMISOS.put("INVENTARIO_VER", new String[]{"INVENTARIO", "Ver inventario", "Consultar movimientos de inventario"});
        PERMISOS.put("INVENTARIO_CREAR", new String[]{"INVENTARIO", "Registrar inventario", "Registrar entradas y salidas"});
        PERMISOS.put("INVENTARIO_ELIMINAR", new String[]{"INVENTARIO", "Eliminar movimientos", "Eliminar movimientos de inventario"});

        PERMISOS.put("CITAS_VER", new String[]{"CITAS", "Ver citas", "Consultar la agenda de citas"});
        PERMISOS.put("CITAS_CREAR", new String[]{"CITAS", "Crear citas", "Agendar nuevas citas"});
        PERMISOS.put("CITAS_EDITAR", new String[]{"CITAS", "Editar citas", "Modificar y reprogramar citas"});
        PERMISOS.put("CITAS_ELIMINAR", new String[]{"CITAS", "Eliminar citas", "Cancelar y eliminar citas"});

        PERMISOS.put("VENTAS_VER", new String[]{"VENTAS", "Ver ventas", "Consultar ventas y su detalle"});
        PERMISOS.put("VENTAS_CREAR", new String[]{"VENTAS", "Crear ventas", "Cobrar en el punto de venta"});
        PERMISOS.put("VENTAS_CANCELAR", new String[]{"VENTAS", "Cancelar ventas", "Anular ventas cobradas"});

        PERMISOS.put("CAJA_VER", new String[]{"CAJA", "Ver caja", "Consultar cajas y saldos"});
        PERMISOS.put("CAJA_APERTURA", new String[]{"CAJA", "Abrir caja", "Abrir caja con fondo inicial"});
        PERMISOS.put("CAJA_INGRESOS", new String[]{"CAJA", "Ingresos de caja", "Registrar ingresos de efectivo"});
        PERMISOS.put("CAJA_EGRESOS", new String[]{"CAJA", "Egresos de caja", "Registrar retiros de efectivo"});
        PERMISOS.put("CAJA_CIERRE", new String[]{"CAJA", "Cerrar caja", "Cerrar la caja del turno"});
        PERMISOS.put("CAJA_CORTE", new String[]{"CAJA", "Corte de caja", "Ver y generar el corte del turno"});

        PERMISOS.put("COMISIONES_VER", new String[]{"COMISIONES", "Ver comisiones", "Consultar comisiones e historial de cortes"});
        PERMISOS.put("COMISIONES_PAGAR", new String[]{"COMISIONES", "Pagar comisiones", "Registrar pagos de comisiones"});
        PERMISOS.put("COMISIONES_EDITAR", new String[]{"COMISIONES", "Editar comisiones", "Modificar comisiones e historial de cortes"});
        PERMISOS.put("COMISIONES_ELIMINAR", new String[]{"COMISIONES", "Eliminar comisiones", "Eliminar registros de comisiones"});

        PERMISOS.put("CONFIGURACION_VER", new String[]{"CONFIGURACION", "Ver configuración", "Consultar los datos del negocio"});
        PERMISOS.put("CONFIGURACION_EDITAR", new String[]{"CONFIGURACION", "Editar configuración", "Modificar los datos del negocio"});

        PERMISOS.put("CARRITO_VER", new String[]{"POS", "Ver carrito", "Sincronizar el carrito del punto de venta"});
        PERMISOS.put("CARRITO_EDITAR", new String[]{"POS", "Editar carrito", "Agregar y modificar líneas del carrito"});

        PERMISOS.put("USUARIOS_VER", new String[]{"SEGURIDAD", "Ver usuarios", "Consultar los usuarios del sistema"});
        PERMISOS.put("USUARIOS_CREAR", new String[]{"SEGURIDAD", "Crear usuarios", "Registrar nuevos usuarios"});
        PERMISOS.put("USUARIOS_EDITAR", new String[]{"SEGURIDAD", "Editar usuarios", "Modificar usuarios, roles y contraseñas"});
        PERMISOS.put("USUARIOS_ELIMINAR", new String[]{"SEGURIDAD", "Eliminar usuarios", "Desactivar o eliminar usuarios"});

        PERMISOS.put("ROLES_VER", new String[]{"SEGURIDAD", "Ver roles", "Consultar los roles y sus permisos"});
        PERMISOS.put("ROLES_CREAR", new String[]{"SEGURIDAD", "Crear roles", "Crear roles con permisos granulares"});
        PERMISOS.put("ROLES_EDITAR", new String[]{"SEGURIDAD", "Editar roles", "Modificar permisos de los roles"});
        PERMISOS.put("ROLES_ELIMINAR", new String[]{"SEGURIDAD", "Eliminar roles", "Eliminar roles del sistema"});

        PERMISOS.put("PERMISOS_VER", new String[]{"SEGURIDAD", "Ver permisos", "Consultar el catálogo de permisos"});
        PERMISOS.put("PERMISOS_EDITAR", new String[]{"SEGURIDAD", "Editar permisos", "Activar o desactivar permisos"});
    }

    private static final String[] TODOS = PERMISOS.keySet().toArray(new String[0]);

    private static final String[] PERMISOS_CAJERO = {
            "DASHBOARD_VER",
            "CARRITO_VER", "CARRITO_EDITAR",
            "VENTAS_VER", "VENTAS_CREAR",
            "CAJA_VER", "CAJA_APERTURA", "CAJA_INGRESOS", "CAJA_EGRESOS", "CAJA_CIERRE", "CAJA_CORTE",
            "CLIENTES_VER", "CLIENTES_CREAR", "CLIENTES_EDITAR",
            "CITAS_VER",
            "SERVICIOS_VER", "PRODUCTOS_VER",
            "COMISIONES_VER"
    };

    private static final String[] PERMISOS_CITAS = {
            "DASHBOARD_VER",
            "CITAS_VER", "CITAS_CREAR", "CITAS_EDITAR", "CITAS_ELIMINAR",
            "CLIENTES_VER", "CLIENTES_CREAR", "CLIENTES_EDITAR",
            "EMPLEADOS_VER",
            "SERVICIOS_VER"
    };

    private static final String[] PERMISOS_BARBERO = {
            "DASHBOARD_VER",
            "CITAS_VER", "CITAS_CREAR", "CITAS_EDITAR",
            "CLIENTES_VER",
            "SERVICIOS_VER", "PRODUCTOS_VER",
            "VENTAS_VER",
            "COMISIONES_VER"
    };

    @Value("${application.security.admin.usuario:admin}")
    private String adminUsuario;

    @Value("${application.security.admin.password:admin123}")
    private String adminPassword;

    @Value("${application.security.admin.nombre:Administrador}")
    private String adminNombre;

    @Bean
    CommandLineRunner seedSeguridad(PermisoRepository permisoRepository,
                                    RolRepository rolRepository,
                                    UsuarioRepository usuarioRepository,
                                    PasswordEncoder passwordEncoder) {
        return args -> inicializar(permisoRepository, rolRepository, usuarioRepository, passwordEncoder);
    }

    @Transactional
    void inicializar(PermisoRepository permisoRepository,
                     RolRepository rolRepository,
                     UsuarioRepository usuarioRepository,
                     PasswordEncoder passwordEncoder) {

        Map<String, Permiso> permisos = new LinkedHashMap<>();
        for (Map.Entry<String, String[]> entry : PERMISOS.entrySet()) {
            String clave = entry.getKey();
            String[] meta = entry.getValue();
            Permiso permiso = permisoRepository.findByClave(clave).orElseGet(Permiso::new);
            permiso.setClave(clave);
            permiso.setModulo(meta[0]);
            permiso.setNombre(meta[1]);
            permiso.setDescripcion(meta[2]);
            permiso.setActivo(true);
            permisos.put(clave, permisoRepository.save(permiso));
        }

        crearRol(rolRepository, permisos, "ADMINISTRADOR", "Acceso total al sistema", TODOS);
        crearRol(rolRepository, permisos, "CAJERO", "Cobra ventas y administra la caja del turno", PERMISOS_CAJERO);
        crearRol(rolRepository, permisos, "CITAS", "Administra la agenda de citas y clientes", PERMISOS_CITAS);
        crearRol(rolRepository, permisos, "BARBERO", "Consulta su agenda, sus comisiones y el catálogo", PERMISOS_BARBERO);

        if (usuarioRepository.count() == 0) {
            Rol admin = rolRepository.findByNombreIgnoreCase("ADMINISTRADOR").orElseThrow();
            Usuario usuario = new Usuario();
            usuario.setNombre(adminNombre);
            usuario.setUsuario(adminUsuario);
            usuario.setPassword(passwordEncoder.encode(adminPassword));
            usuario.setRol(admin);
            usuario.setActivo(true);
            usuarioRepository.save(usuario);
            log.warn("Usuario administrador creado. Usuario: '{}' Contraseña: '{}' — cámbiala desde Usuarios.",
                    adminUsuario, adminPassword);
        } else if (!usuarioRepository.existsByUsuarioIgnoreCase(adminUsuario)) {
            log.info("Ya existen usuarios en el sistema. No se creó el administrador '{}'.", adminUsuario);
        }
    }

    private void crearRol(RolRepository rolRepository,
                          Map<String, Permiso> permisos,
                          String nombre,
                          String descripcion,
                          String[] claves) {
        Rol rol = rolRepository.findByNombreIgnoreCase(nombre).orElseGet(Rol::new);
        rol.setNombre(nombre);
        rol.setDescripcion(descripcion);
        rol.setEsSistema(true);
        rol.setActivo(true);

        List<Permiso> asignados = new ArrayList<>();
        for (String clave : claves) {
            Permiso permiso = permisos.get(clave);
            if (permiso != null) {
                asignados.add(permiso);
            }
        }
        rol.setPermisos(asignados);
        rolRepository.save(rol);
    }

}
