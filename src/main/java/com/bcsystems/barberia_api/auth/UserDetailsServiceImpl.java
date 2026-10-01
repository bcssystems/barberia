package com.bcsystems.barberia_api.auth;

import com.bcsystems.barberia_api.domain.Permiso;
import com.bcsystems.barberia_api.domain.Rol;
import com.bcsystems.barberia_api.domain.Usuario;
import com.bcsystems.barberia_api.repository.UsuarioRepository;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;

    public UserDetailsServiceImpl(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Usuario usuario = usuarioRepository.findByUsuarioIgnoreCase(username)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado"));

        if (!Boolean.TRUE.equals(usuario.getActivo())) {
            throw new UsernameNotFoundException("Usuario inactivo");
        }

        return new User(usuario.getUsuario(), usuario.getPassword(), authorities(usuario.getRol()));
    }

    public static List<GrantedAuthority> authorities(Rol rol) {
        List<GrantedAuthority> authorities = new ArrayList<>();
        if (rol != null) {
            authorities.add(new SimpleGrantedAuthority("ROL_" + rol.getNombre()));
            if (rol.getPermisos() != null) {
                rol.getPermisos().stream()
                        .filter(p -> Boolean.TRUE.equals(p.getActivo()))
                        .map(Permiso::getClave)
                        .map(SimpleGrantedAuthority::new)
                        .forEach(authorities::add);
            }
        }
        return authorities;
    }

}
